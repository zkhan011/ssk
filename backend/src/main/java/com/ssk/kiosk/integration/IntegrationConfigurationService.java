package com.ssk.kiosk.integration;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class IntegrationConfigurationService {
  private static final Set<String> METHODS=Set.of("GET","POST","PUT","PATCH","DELETE");
  private final IntegrationConfigurationRepository repository; private final ObjectMapper mapper;
  public IntegrationConfigurationService(IntegrationConfigurationRepository repository,ObjectMapper mapper){this.repository=repository;this.mapper=mapper;}
  public List<IntegrationConfiguration> all(){return repository.findAll();}
  public IntegrationConfiguration required(String key){return repository.findById(key).orElseThrow(()->new IllegalStateException(key+" integration is not configured"));}
  @Transactional public IntegrationConfiguration update(String key,Map<String,Object> values,String actor){IntegrationConfiguration c=repository.findById(key.toUpperCase(Locale.ROOT)).orElseThrow();Object revision=values.get("rowVersion");if(revision instanceof Number n&&!Objects.equals(c.getRowVersion(),n.longValue()))throw new ObjectOptimisticLockingFailureException(IntegrationConfiguration.class,key);c.setEnabled(Boolean.TRUE.equals(values.get("enabled")));c.setBaseUrl(url(values,"baseUrl"));c.setVerificationPath(endpoint(values,"verificationPath"));c.setApprovalField(fieldPath(values,"approvalField"));c.setApprovalValue(String.valueOf(values.getOrDefault("approvalValue","true")));String mode=String.valueOf(values.getOrDefault("executionMode","SEQUENTIAL"));if(!Set.of("SEQUENTIAL").contains(mode))throw new IllegalArgumentException("Invalid execution mode");c.setExecutionMode(mode);Object workflow=values.get("workflow");if(workflow==null&&values.get("workflowSnapshot") instanceof String raw){workflow=parse(raw);}if(workflow!=null){validateWorkflow(workflow);c.setWorkflowSnapshot(write(workflow));}c.setConnectTimeoutMs(range(values,"connectTimeoutMs",500,60000));c.setReadTimeoutMs(range(values,"readTimeoutMs",500,120000));c.setRetryCount(range(values,"retryCount",0,5));c.setUpdatedAt(Instant.now());c.setUpdatedBy(actor);return repository.save(c);}
  private void validateWorkflow(Object workflow){JsonNode root=mapper.valueToTree(workflow);JsonNode steps=root.isArray()?root:root.path("steps");if(!steps.isArray()||steps.isEmpty())throw new IllegalArgumentException("Workflow requires at least one step");Set<String> ids=new java.util.HashSet<>();for(JsonNode step:steps){String id=step.path("id").asText();String method=step.path("method").asText();String path=step.path("path").asText();if(!id.matches("[A-Za-z][A-Za-z0-9_-]{0,49}")||!ids.add(id))throw new IllegalArgumentException("Workflow step IDs must be unique and safe");if(!METHODS.contains(method))throw new IllegalArgumentException("Unsupported workflow HTTP method");if(!path.matches("/[A-Za-z0-9_./{}-]*")||path.contains(".."))throw new IllegalArgumentException("Unsafe workflow path");scanTemplates(step);}}
  private void scanTemplates(JsonNode node){if(node.isTextual()){String text=node.asText();if(text.contains("${")||text.contains("#{")||text.contains("<%")||text.contains("javascript:")||text.contains("file:")||text.contains("classpath:"))throw new IllegalArgumentException("Unsafe workflow template");java.util.regex.Matcher m=java.util.regex.Pattern.compile("\\{\\{([^}]+)}}").matcher(text);while(m.find())if(!m.group(1).matches("((input|context|steps)(\\.[A-Za-z0-9_-]+|\\[\\d+])*(\\.outputs(\\.[A-Za-z0-9_-]+|\\[\\d+])*)?|secrets\\.[A-Z][A-Z0-9_]*)"))throw new IllegalArgumentException("Unsupported workflow variable: "+m.group(1));}else node.forEach(this::scanTemplates);}
  @SuppressWarnings("unchecked") public List<Map<String,Object>> workflow(IntegrationConfiguration c){if(c.getWorkflowSnapshot()==null||c.getWorkflowSnapshot().isBlank())return List.of(Map.of("id","verify","method","POST","path",c.getVerificationPath(),"body",Map.of("gatePassId","{{input.gatePassId}}"),"outputs",Map.of()));try{JsonNode root=mapper.readTree(c.getWorkflowSnapshot());JsonNode steps=root.isArray()?root:root.path("steps");return mapper.convertValue(steps,List.class);}catch(JsonProcessingException e){throw new IllegalStateException("Stored integration workflow is invalid",e);}}
  private Object parse(String value){try{return mapper.readValue(value,Object.class);}catch(JsonProcessingException e){throw new IllegalArgumentException("Workflow JSON is invalid");}}
  private String write(Object value){try{return mapper.writeValueAsString(value);}catch(JsonProcessingException e){throw new IllegalArgumentException("Workflow JSON is invalid");}}
  private String url(Map<String,Object>v,String key){String value=String.valueOf(v.getOrDefault(key,"")).trim();if(!value.isEmpty()&&!value.matches("https://[^\\s]+"))throw new IllegalArgumentException(key+" must be an HTTPS URL");return value;}private String endpoint(Map<String,Object>v,String key){String value=String.valueOf(v.getOrDefault(key,"/verify")).trim();if(!value.matches("/[A-Za-z0-9_./-]*")||value.contains(".."))throw new IllegalArgumentException(key+" is invalid");return value;}private String fieldPath(Map<String,Object>v,String key){String value=String.valueOf(v.getOrDefault(key,"verified")).trim();if(!value.equals("$httpStatus")&&!value.matches("[A-Za-z0-9_.\\[\\]-]+"))throw new IllegalArgumentException(key+" is invalid");return value;}private int range(Map<String,Object>v,String key,int min,int max){int n=((Number)v.getOrDefault(key,min)).intValue();if(n<min||n>max)throw new IllegalArgumentException(key+" outside permitted range");return n;}
}

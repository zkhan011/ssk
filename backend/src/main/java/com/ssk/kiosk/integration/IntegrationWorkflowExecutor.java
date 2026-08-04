package com.ssk.kiosk.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.core.env.Environment;
import org.springframework.web.client.RestClient;

@Component
public class IntegrationWorkflowExecutor {
  private static final Pattern VARIABLE=Pattern.compile("\\{\\{([^}]+)}}");
  private final IntegrationConfigurationService configurations; private final ObjectMapper mapper; private final Environment environment;
  public IntegrationWorkflowExecutor(IntegrationConfigurationService configurations,ObjectMapper mapper,Environment environment){this.configurations=configurations;this.mapper=mapper;this.environment=environment;}
  public Execution execute(IntegrationConfiguration config,String gatePassId,String kioskId){var requestFactory=new org.springframework.http.client.JdkClientHttpRequestFactory(java.net.http.HttpClient.newBuilder().connectTimeout(Duration.ofMillis(config.getConnectTimeoutMs())).build());requestFactory.setReadTimeout(Duration.ofMillis(config.getReadTimeoutMs()));RestClient client=RestClient.builder().baseUrl(config.getBaseUrl()).requestFactory(requestFactory).build();Map<String,Object> root=new LinkedHashMap<>();root.put("input",Map.of("gatePassId",gatePassId));root.put("context",Map.of("kioskId",kioskId==null?"":kioskId));Map<String,Object> completed=new LinkedHashMap<>();root.put("steps",completed);JsonNode last=null;int status=0;for(Map<String,Object> step:configurations.workflow(config)){String id=String.valueOf(step.get("id"));HttpMethod method=HttpMethod.valueOf(String.valueOf(step.get("method")));String path=render(String.valueOf(step.get("path")),root);var request=client.method(method).uri(path).accept(MediaType.APPLICATION_JSON);Object headers=step.get("headers");if(headers instanceof Map<?,?> map)map.forEach((key,value)->request.header(String.valueOf(key),render(String.valueOf(value),root)));Object body=step.get("body");if(body!=null&&method!=HttpMethod.GET&&method!=HttpMethod.DELETE)request.contentType(MediaType.APPLICATION_JSON).body(renderObject(body,root));RuntimeException failure=null;org.springframework.http.ResponseEntity<JsonNode> response=null;for(int attempt=0;attempt<=config.getRetryCount();attempt++){try{response=request.retrieve().toEntity(JsonNode.class);failure=null;break;}catch(RuntimeException e){failure=e;}}if(failure!=null)throw failure;last=response.getBody()==null?com.fasterxml.jackson.databind.node.NullNode.getInstance():response.getBody();status=response.getStatusCode().value();List<Integer> successCodes=step.get("successStatusCodes") instanceof List<?> codes?codes.stream().map(v->((Number)v).intValue()).toList():List.of(200);if(!successCodes.contains(status))throw new IllegalStateException("Integration step failed");Map<String,Object> outputs=new LinkedHashMap<>();JsonNode responseNode=last;if(step.get("outputs") instanceof Map<?,?> mappings)mappings.forEach((name,responsePath)->{JsonNode value=GatePassVerificationService.resolve(responseNode,String.valueOf(responsePath));outputs.put(String.valueOf(name),value==null?null:mapper.convertValue(value,Object.class));});completed.put(id,Map.of("outputs",outputs,"status",status));}return new Execution(last,status);}
  private Object renderObject(Object value,Map<String,Object> root){JsonNode node=mapper.valueToTree(value);return mapper.convertValue(renderNode(node,root),Object.class);}
  private JsonNode renderNode(JsonNode node,Map<String,Object> root){if(node.isTextual())return mapper.getNodeFactory().textNode(render(node.asText(),root));if(node.isObject()){var result=mapper.createObjectNode();node.fields().forEachRemaining(e->result.set(e.getKey(),renderNode(e.getValue(),root)));return result;}if(node.isArray()){var result=mapper.createArrayNode();node.forEach(v->result.add(renderNode(v,root)));return result;}return node;}
  private String render(String template,Map<String,Object> root){Matcher matcher=VARIABLE.matcher(template);StringBuffer result=new StringBuffer();while(matcher.find()){Object value=resolve(root,matcher.group(1));matcher.appendReplacement(result,Matcher.quoteReplacement(value==null?"":String.valueOf(value)));}matcher.appendTail(result);return result.toString();}
  @SuppressWarnings("unchecked") private Object resolve(Object current,String path){if(path.startsWith("secrets.")){String name=path.substring(8);return environment.getProperty(name,"");}for(String part:path.replaceAll("\\[(\\d+)]",".$1").split("\\.")){if(current instanceof Map<?,?> map)current=map.get(part);else if(current instanceof List<?> list&&part.matches("\\d+"))current=list.get(Integer.parseInt(part));else return null;}return current;}
  public record Execution(JsonNode response,int httpStatus){}
}

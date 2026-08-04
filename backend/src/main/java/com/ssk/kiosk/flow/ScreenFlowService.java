package com.ssk.kiosk.flow;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ScreenFlowService {
  private static final Set<String> ROUTES=Set.of("/kiosk","/kiosk/language","/kiosk/validate","/kiosk/tesreeh-check","/kiosk/face-capture","/kiosk/access-granted");
  private final ScreenFlowConfigurationRepository repository; private final ObjectMapper mapper;
  public ScreenFlowService(ScreenFlowConfigurationRepository repository,ObjectMapper mapper){this.repository=repository;this.mapper=mapper;}
  public Map<String,Object> defaults(){return Map.of("startRoute","/kiosk","successRoute","/kiosk/access-granted","failureRoute","/kiosk/validate","steps",List.of(step("/kiosk",true),step("/kiosk/language",false),step("/kiosk/validate",true),step("/kiosk/tesreeh-check",true),step("/kiosk/face-capture",false),step("/kiosk/access-granted",true)));}
  public Map<String,Object> published(){return repository.findFirstByStatusOrderByVersionNumberDesc(ScreenFlowConfiguration.Status.PUBLISHED).map(this::read).orElse(defaults());}
  public Map<String,Object> draft(){return repository.findFirstByStatusOrderByVersionNumberDesc(ScreenFlowConfiguration.Status.DRAFT).map(this::read).orElse(published());}
  @Transactional public Map<String,Object> saveDraft(Map<String,Object> value,String actor){validate(value);ScreenFlowConfiguration c=repository.findFirstByStatusOrderByVersionNumberDesc(ScreenFlowConfiguration.Status.DRAFT).orElseGet(ScreenFlowConfiguration::new);c.setStatus(ScreenFlowConfiguration.Status.DRAFT);c.setSnapshot(write(value));c.setChangedBy(actor);repository.save(c);return read(c);}
  @Transactional public Map<String,Object> publish(String actor){ScreenFlowConfiguration draft=repository.findFirstByStatusOrderByVersionNumberDesc(ScreenFlowConfiguration.Status.DRAFT).orElseThrow(()->new IllegalStateException("No screen-flow draft exists"));validate(read(draft));repository.findFirstByStatusOrderByVersionNumberDesc(ScreenFlowConfiguration.Status.PUBLISHED).ifPresent(current->{current.setStatus(ScreenFlowConfiguration.Status.ARCHIVED);repository.save(current);});draft.setStatus(ScreenFlowConfiguration.Status.PUBLISHED);draft.setVersionNumber(repository.findAll().stream().mapToLong(ScreenFlowConfiguration::getVersionNumber).max().orElse(0)+1);draft.setPublishedAt(Instant.now());draft.setChangedBy(actor);return read(repository.save(draft));}
  @SuppressWarnings("unchecked") private void validate(Map<String,Object> value){Object raw=value.get("steps");if(!(raw instanceof List<?> steps)||steps.isEmpty())throw new IllegalArgumentException("Screen flow cannot be empty");Set<String> enabled=new HashSet<>();for(Object item:steps){if(!(item instanceof Map<?,?> step))throw new IllegalArgumentException("Invalid screen step");String route=String.valueOf(step.get("route"));if(!ROUTES.contains(route))throw new IllegalArgumentException("Unsupported kiosk route: "+route);if(Boolean.TRUE.equals(step.get("enabled"))&&!enabled.add(route))throw new IllegalArgumentException("Duplicate enabled route: "+route);}for(String key:List.of("startRoute","successRoute","failureRoute")){String route=String.valueOf(value.get(key));if(!enabled.contains(route))throw new IllegalArgumentException(key+" must reference an enabled route");}if(!enabled.contains("/kiosk/validate")||!enabled.contains("/kiosk/tesreeh-check"))throw new IllegalArgumentException("Gate Pass entry and verification are required");}
  private Map<String,Object> step(String route,boolean required){return Map.of("route",route,"enabled",required,"required",required);}
  @SuppressWarnings("unchecked") private Map<String,Object> read(ScreenFlowConfiguration c){try{return mapper.readValue(c.getSnapshot(),Map.class);}catch(JsonProcessingException e){throw new IllegalStateException("Stored screen flow is invalid",e);}}
  private String write(Map<String,Object> value){try{return mapper.writeValueAsString(value);}catch(JsonProcessingException e){throw new IllegalStateException("Cannot serialize screen flow",e);}}
}

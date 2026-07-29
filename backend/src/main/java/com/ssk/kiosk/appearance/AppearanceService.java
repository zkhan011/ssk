package com.ssk.kiosk.appearance;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
@Service
public class AppearanceService {
 private final AppearanceConfigurationRepository repository; private final ObjectMapper objectMapper;
 public AppearanceService(AppearanceConfigurationRepository repository,ObjectMapper objectMapper){this.repository=repository;this.objectMapper=objectMapper;}
 public Map<String,Object> defaults(){return Map.of("logoText","DP WORLD","logoWidth",116,"headerHeight",242,"background","#f7f7f8","contentWidth",540,"fontScale",1.0,"mobileBreakpoint",768,"tabletBreakpoint",1024);}
 public Map<String,Object> published(){return repository.findFirstByStatusOrderByVersionNumberDesc(AppearanceConfiguration.Status.PUBLISHED).map(this::map).orElse(defaults());}
 public Map<String,Object> draft(){return repository.findFirstByStatusOrderByVersionNumberDesc(AppearanceConfiguration.Status.DRAFT).map(this::map).orElse(published());}
 @Transactional public Map<String,Object> saveDraft(Map<String,Object> snapshot,String actor){validate(snapshot);AppearanceConfiguration c=repository.findFirstByStatusOrderByVersionNumberDesc(AppearanceConfiguration.Status.DRAFT).orElseGet(AppearanceConfiguration::new);c.setStatus(AppearanceConfiguration.Status.DRAFT);c.setChangedBy(actor);c.setSnapshot(write(snapshot));repository.save(c);return map(c);}
 @Transactional public Map<String,Object> publish(String actor){AppearanceConfiguration draft=repository.findFirstByStatusOrderByVersionNumberDesc(AppearanceConfiguration.Status.DRAFT).orElseThrow(()->new IllegalStateException("No appearance draft exists"));repository.findFirstByStatusOrderByVersionNumberDesc(AppearanceConfiguration.Status.PUBLISHED).ifPresent(current->{current.setStatus(AppearanceConfiguration.Status.ARCHIVED);repository.save(current);});draft.setStatus(AppearanceConfiguration.Status.PUBLISHED);draft.setVersionNumber(repository.findAll().stream().mapToLong(AppearanceConfiguration::getVersionNumber).max().orElse(0)+1);draft.setPublishedAt(Instant.now());draft.setChangedBy(actor);repository.save(draft);return map(draft);}
 public List<Map<String,Object>> history(){return repository.findAllByOrderByVersionNumberDesc().stream().map(this::map).toList();}
 private void validate(Map<String,Object> s){number(s,"logoWidth",40,400);number(s,"headerHeight",120,600);number(s,"contentWidth",320,1920);number(s,"fontScale",0.8,1.5);number(s,"mobileBreakpoint",320,1023);number(s,"tabletBreakpoint",768,1600);}
 private void number(Map<String,Object>s,String key,double min,double max){Object v=s.get(key);if(v instanceof Number n&&(n.doubleValue()<min||n.doubleValue()>max))throw new IllegalArgumentException(key+" is outside the permitted range");}
 @SuppressWarnings("unchecked") private Map<String,Object> map(AppearanceConfiguration c){try{return objectMapper.readValue(c.getSnapshot(),Map.class);}catch(JsonProcessingException e){throw new IllegalStateException("Stored appearance configuration is invalid",e);}}
 private String write(Map<String,Object>v){try{return objectMapper.writeValueAsString(v);}catch(JsonProcessingException e){throw new IllegalStateException("Cannot serialize appearance configuration",e);}}
}

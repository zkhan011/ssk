package com.ssk.kiosk.flow;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class ScreenFlowServiceTest {
  @Test void rejectsDisablingRequiredVerificationRoute(){ScreenFlowService service=new ScreenFlowService(mock(ScreenFlowConfigurationRepository.class),new ObjectMapper());Map<String,Object> invalid=Map.of("startRoute","/kiosk","successRoute","/kiosk/access-granted","failureRoute","/kiosk","steps",List.of(Map.of("route","/kiosk","enabled",true),Map.of("route","/kiosk/access-granted","enabled",true)));assertThrows(IllegalArgumentException.class,()->service.saveDraft(invalid,"admin"));}
}

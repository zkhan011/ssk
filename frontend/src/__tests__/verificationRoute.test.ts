import{describe,expect,it}from'vitest';
import{routeAfterVerification}from'../config/verificationRoute';

describe('verification routing',()=>{
  it('captures a new image when Tasreeh succeeds but Pangu image registration fails',()=>expect(routeAfterVerification({outcome:'REJECTED',integrations:{TASREEH:{verified:true},PANGU:{verified:false,status:'REJECTED'}}},'/success','/failure')).toBe('/kiosk/face-capture'));
  it('uses the configured success route when both providers approve',()=>expect(routeAfterVerification({outcome:'APPROVED',integrations:{TASREEH:{verified:true},PANGU:{verified:true}}},'/success','/failure')).toBe('/success'));
  it('uses the configured failure route when Tasreeh fails',()=>expect(routeAfterVerification({outcome:'REJECTED',integrations:{TASREEH:{verified:false},PANGU:{verified:false}}},'/success','/failure')).toBe('/failure'));
  it('does not open the camera for a Pangu outage',()=>expect(routeAfterVerification({outcome:'PARTIAL_FAILURE',integrations:{TASREEH:{verified:true},PANGU:{verified:false,status:'SYSTEM_ERROR'}}},'/success','/failure')).toBe('/failure'));
});

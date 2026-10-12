import{describe,expect,it}from'vitest';
import{resolveKioskIdentity}from'../config/kioskIdentity';

const resolve=(overrides:Partial<Parameters<typeof resolveKioskIdentity>[0]>={})=>resolveKioskIdentity({
  search:'',storedIdentity:null,configuredIdentity:undefined,randomUUID:()=> 'generated-id',...overrides,
});

describe('kiosk identity',()=>{
  it('uses and trims a provisioned URL identity',()=>expect(resolve({search:'?kioskId=%20GATE-01%20'})).toBe('GATE-01'));
  it('keeps a persisted device identity ahead of a build default',()=>expect(resolve({storedIdentity:'GATE-02',configuredIdentity:'DEFAULT'})).toBe('GATE-02'));
  it('uses the build default when the device is not provisioned',()=>expect(resolve({configuredIdentity:'LOBBY'})).toBe('LOBBY'));
  it('generates an identity when no valid provision exists',()=>expect(resolve({search:'?kioskId=not%20valid'})).toBe('WEB-generated-id'));
});

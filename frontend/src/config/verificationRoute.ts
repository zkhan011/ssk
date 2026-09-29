type IntegrationResult={verified?:boolean;status?:string};
type Verification={outcome?:string;integrations?:{TASREEH?:IntegrationResult;PANGU?:IntegrationResult}};

export function routeAfterVerification(result:Verification,successRoute:string):string{
  if(result.integrations?.TASREEH?.verified&&result.integrations?.PANGU?.status==='REJECTED')return'/kiosk/face-capture';
  if(result.outcome==='APPROVED')return successRoute;
  if(result.outcome==='PARTIAL_FAILURE'||result.outcome==='VERIFICATION_UNAVAILABLE')return'/kiosk/something-went-wrong';
  return'/kiosk/access-denied';
}

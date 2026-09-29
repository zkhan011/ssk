type IntegrationResult={verified?:boolean;status?:string};
type Verification={outcome?:string;integrations?:{TASREEH?:IntegrationResult;PANGU?:IntegrationResult}};

export function routeAfterVerification(result:Verification,successRoute:string,failureRoute:string):string{
  if(result.integrations?.TASREEH?.verified&&result.integrations?.PANGU?.status==='REJECTED')return'/kiosk/face-capture';
  return result.outcome==='APPROVED'?successRoute:failureRoute;
}

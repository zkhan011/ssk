export type Appearance={logoText:string;logoWidth:number;headerHeight:number;background:string;contentWidth:number;fontScale:number};
export const defaultAppearance:Appearance={logoText:'DP WORLD',logoWidth:116,headerHeight:242,background:'#f7f7f8',contentWidth:540,fontScale:1};
export const appearanceStyle=(a:Appearance)=>({'--appearance-content-width':`${a.contentWidth}px`,'--appearance-header-height':`${a.headerHeight}px`,'--appearance-background':a.background,'--appearance-font-scale':String(a.fontScale)} as Record<string,string>);

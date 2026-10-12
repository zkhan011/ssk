import{useEffect,useRef,useState}from'react';

type Props={onCaptured:(image:string)=>Promise<void>;alignLabel:string;capturedLabel:string};

export function FaceCamera({onCaptured,alignLabel,capturedLabel}:Props){
  const videoRef=useRef(null as HTMLVideoElement|null);const callbackRef=useRef(onCaptured as (image:string)=>Promise<void>);const[status,setStatus]=useState('Starting camera…');const[error,setError]=useState('');const[attempt,setAttempt]=useState(0);callbackRef.current=onCaptured;
  useEffect(()=>{let stream:MediaStream|undefined;let timer:number|undefined;let active=true;
    async function start(){try{setError('');setStatus('Position your face in the frame');stream=await navigator.mediaDevices.getUserMedia({video:{facingMode:'user',width:{ideal:1280},height:{ideal:720}},audio:false});if(!active){stream.getTracks().forEach(track=>track.stop());return}const video=videoRef.current;if(!video)return;video.srcObject=stream;await video.play();timer=window.setTimeout(async()=>{if(!active||!video.videoWidth)return;const width=Math.min(video.videoWidth,640);const height=Math.round(video.videoHeight*(width/video.videoWidth));const canvas=document.createElement('canvas');canvas.width=width;canvas.height=height;canvas.getContext('2d')?.drawImage(video,0,0,width,height);const image=canvas.toDataURL('image/jpeg',.88);setStatus('Registering captured image…');try{await callbackRef.current(image);if(active)setStatus(capturedLabel)}catch{if(active){setError('Face registration failed. Please try again.');setStatus('')}}},1200)}catch{if(active){setError('Camera access is unavailable. Ask an administrator to enable the kiosk camera.');setStatus('')}}}
    start();return()=>{active=false;if(timer)clearTimeout(timer);stream?.getTracks().forEach(track=>track.stop())};
  },[attempt,capturedLabel]);
  return <div className="face-camera" aria-live="polite"><video ref={videoRef} autoPlay muted playsInline aria-label={alignLabel}/><span>{status||alignLabel}</span>{error&&<><p className="validation-error" role="alert">{error}</p><button type="button" className="secondary" onClick={()=>setAttempt(value=>value+1)}>Retry camera</button></>}</div>;
}

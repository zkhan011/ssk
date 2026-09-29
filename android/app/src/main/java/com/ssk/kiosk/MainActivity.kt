package com.ssk.kiosk
import android.annotation.SuppressLint
import android.app.Activity
import android.os.Bundle
import android.net.Uri
import android.view.WindowManager
import android.webkit.WebResourceRequest
import android.webkit.PermissionRequest
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
@SuppressLint("SetJavaScriptEnabled")
class MainActivity : Activity() {
 override fun onCreate(savedInstanceState: Bundle?) { super.onCreate(savedInstanceState); window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON); window.decorView.systemUiVisibility = 5894
  setContentView(WebView(this).apply { settings.javaScriptEnabled=true; settings.domStorageEnabled=true; settings.mediaPlaybackRequiresUserGesture=false; webViewClient=object:WebViewClient(){override fun shouldOverrideUrlLoading(v:WebView,r:WebResourceRequest)=false}; webChromeClient=object:WebChromeClient(){override fun onPermissionRequest(request:PermissionRequest){runOnUiThread{val configured=Uri.parse(BuildConfig.KIOSK_URL);val trusted=request.origin.scheme==configured.scheme&&request.origin.authority==configured.authority;val video=request.resources.contains(PermissionRequest.RESOURCE_VIDEO_CAPTURE);if(trusted&&video)request.grant(arrayOf(PermissionRequest.RESOURCE_VIDEO_CAPTURE))else request.deny()}}}; loadUrl(Uri.parse(BuildConfig.KIOSK_URL).buildUpon().appendQueryParameter("kioskId",BuildConfig.KIOSK_ID).build().toString()) })
 }
}

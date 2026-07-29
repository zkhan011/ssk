package com.ssk.kiosk
import android.annotation.SuppressLint
import android.app.Activity
import android.os.Bundle
import android.view.WindowManager
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
@SuppressLint("SetJavaScriptEnabled")
class MainActivity : Activity() {
 override fun onCreate(savedInstanceState: Bundle?) { super.onCreate(savedInstanceState); window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON); window.decorView.systemUiVisibility = 5894
  setContentView(WebView(this).apply { settings.javaScriptEnabled=true; settings.domStorageEnabled=true; settings.mediaPlaybackRequiresUserGesture=false; webViewClient=object:WebViewClient(){override fun shouldOverrideUrlLoading(v:WebView,r:WebResourceRequest)=false}; loadUrl(BuildConfig.KIOSK_URL) })
 }
}

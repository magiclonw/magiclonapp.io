package com.magiclon.assessment

import android.app.ProgressDialog
import android.graphics.Bitmap
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.ViewGroup
import com.tencent.smtt.export.external.interfaces.JsPromptResult
import com.tencent.smtt.export.external.interfaces.JsResult
import com.tencent.smtt.export.external.interfaces.WebResourceError
import com.tencent.smtt.export.external.interfaces.WebResourceRequest
import com.tencent.smtt.sdk.WebChromeClient
import com.tencent.smtt.sdk.WebView
import com.tencent.smtt.sdk.WebViewClient


class MainActivity : AppCompatActivity() {
    private var webview: WebView? = null
    var mProgress: ProgressDialog? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        webview = WebView(applicationContext)
        setContentView(webview)
        var settings = webview?.settings
        settings?.setAppCacheEnabled(true)     // 默认值 false
        settings?.setAppCachePath(applicationContext.cacheDir.absolutePath)
        // 存储(storage)
        settings?.domStorageEnabled = true   // 默认值 false
        settings?.databaseEnabled = true      // 默认值 false
        settings?.setSupportZoom(true)
        settings?.builtInZoomControls = true
        settings?.displayZoomControls = false
        // 是否支持viewport属性，默认值 false
        // 页面通过`<meta name="viewport" ... />`自适应手机屏幕
        settings?.useWideViewPort = true
        // 是否使用overview mode加载页面，默认值 false
        // 当页面宽度大于WebView宽度时，缩小使页面宽度等于WebView宽度
        settings?.loadWithOverviewMode = true
        // 是否支持Javascript，默认值false
        settings?.javaScriptEnabled = true

        webview?.webChromeClient = object : WebChromeClient() {
            override fun onJsAlert(view: WebView?, url: String?, message: String?, result: JsResult?): Boolean {
                return super.onJsAlert(view, url, message, result)
            }

            override fun onJsConfirm(view: WebView?, url: String?, message: String?, result: JsResult?): Boolean {
                return super.onJsConfirm(view, url, message, result)
            }

            override fun onJsPrompt(view: WebView?, url: String?, message: String?, defaultValue: String?, result: JsPromptResult?): Boolean {
                return super.onJsPrompt(view, url, message, defaultValue, result)
            }
        }
        webview?.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                view.loadUrl(url)
                Log.e("webview", url)
                return true
            }

            override fun onLoadResource(p0: WebView?, p1: String?) {
                super.onLoadResource(p0, p1)
                Log.e("webview", p1)
            }

            override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {
                super.onReceivedError(view, request, error)
                Log.e("webview", error?.description?.toString())
            }

            override fun onPageFinished(p0: WebView?, p1: String?) {
                super.onPageFinished(p0, p1)
                mProgress?.dismiss()
            }

            override fun onPageStarted(p0: WebView?, p1: String?, p2: Bitmap?) {
                super.onPageStarted(p0, p1, p2)
                showDialog("正在处理...")
            }
        }
        settings?.setAllowUniversalAccessFromFileURLs(true)
        webview?.loadUrl("https://magiclonw.github.io/")
    }

    override fun onBackPressed() {
        if (webview?.canGoBack()!!) {
            webview?.goBack()
        } else {
            super.onBackPressed()
        }
    }

    fun showDialog(msg: String) {
        if (null == mProgress) {
            mProgress = ProgressDialog(this)
            mProgress?.setCanceledOnTouchOutside(false)
        }
        mProgress?.setMessage(msg)
        mProgress?.show()
    }

    override fun onDestroy() {
        if (webview != null) {
            webview?.loadDataWithBaseURL(null, "", "text/html", "utf-8", null)
            webview?.clearHistory()
            (webview?.parent as ViewGroup).removeView(webview)
            webview?.destroy()
            webview = null
        }
        super.onDestroy()
    }
}

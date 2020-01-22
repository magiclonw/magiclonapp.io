package com.magiclon.assessment

import android.annotation.TargetApi
import android.app.Activity
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import com.bigkoo.pickerview.builder.OptionsPickerBuilder
import com.bigkoo.pickerview.listener.OnOptionsSelectListener
import com.bigkoo.pickerview.view.OptionsPickerView
import com.tencent.smtt.export.external.interfaces.JsPromptResult
import com.tencent.smtt.export.external.interfaces.JsResult
import com.tencent.smtt.export.external.interfaces.WebResourceError
import com.tencent.smtt.export.external.interfaces.WebResourceRequest
import com.tencent.smtt.sdk.ValueCallback
import com.tencent.smtt.sdk.WebChromeClient
import com.tencent.smtt.sdk.WebView
import com.tencent.smtt.sdk.WebViewClient
import java.util.*


class WebActivity : AppCompatActivity() {
    private var webview: WebView? = null
    var mProgress: ProgressDialog? = null
    private var writepaddialog: WritePadDialog? = null
    private var uploadMessage: ValueCallback<Uri>? = null
    private var uploadMessageAboveL: ValueCallback<Array<Uri>>? = null
    private var FILE_CHOOSER_RESULT_CODE = 10000
    private var picker: OptionsPickerView<String>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        webview = WebView(applicationContext)
//        com.magiclon.logutilslibrary.Log.init("------", true)
//        writepaddialog = WritePadDialog(this@WebActivity) {
//            //                    com.magiclon.logutilslibrary.Log.e("$it")
//            webview?.loadUrl("javascript:signGiveVal('$it')")
//
//        }
//        writepaddialog?.show()
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
//         页面通过`<meta name="viewport" ... />`自适应手机屏幕
        settings?.useWideViewPort = true
        // 是否使用overview mode加载页面，默认值 false
        // 当页面宽度大于WebView宽度时，缩小使页面宽度等于WebView宽度
        settings?.loadWithOverviewMode = true
        // 是否支持Javascript，默认值false
        settings?.javaScriptEnabled = true

        webview?.webChromeClient = object : WebChromeClient() {
            override fun onJsAlert(view: WebView?, url: String?, message: String?, result: JsResult?): Boolean {
                Log.e("onJsAlert", message + "--------" + url);
                var b = AlertDialog.Builder(this@WebActivity)
                b.setTitle("Alert")
                b.setMessage(message)
                b.setPositiveButton(android.R.string.ok) { p0, p1 -> result?.confirm(); }
                b.setCancelable(true)
                b.create().show()
                return true
            }

            override fun onJsConfirm(view: WebView?, url: String?, message: String?, result: JsResult?): Boolean {
                return super.onJsConfirm(view, url, message, result)
            }

            override fun onJsPrompt(view: WebView?, url: String?, message: String?, defaultValue: String?, result: JsPromptResult?): Boolean {
                return super.onJsPrompt(view, url, message, defaultValue, result)
            }

            // For Android < 3.0
            fun openFileChooser(valueCallback: ValueCallback<Uri>) {
                uploadMessage = valueCallback;
                openImageChooserActivity();
            }

            // For Android  >= 3.0
            fun openFileChooser(valueCallback1: ValueCallback<Uri>, acceptType: String) {
                uploadMessage = valueCallback1;
                openImageChooserActivity();
            }

            //For Android  >= 4.1
            override fun openFileChooser(valueCallback: ValueCallback<Uri>, acceptType: String, capture: String) {
                uploadMessage = valueCallback;
                openImageChooserActivity();
            }

            // For Android >= 5.0
            override fun onShowFileChooser(webView: WebView, filePathCallback: ValueCallback<Array<Uri>>, fileChooserParams: WebChromeClient.FileChooserParams): Boolean {
                uploadMessageAboveL = filePathCallback;
                openImageChooserActivity();
                return true;
            }

        }
        webview?.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                view.loadUrl(url)
                Log.e("shouldOverrideUrl", url)
                return true
            }

            override fun onLoadResource(p0: WebView?, p1: String?) {
                super.onLoadResource(p0, p1)
                Log.e("onLoadResource", p1)
            }

            override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {
                super.onReceivedError(view, request, error)
                Log.e("onReceivedError", error?.description?.toString())
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
        webview?.loadUrl("http://baseeasy110.eicp.net/gx_wechat/wechat/service/init.do")
        webview?.addJavascriptInterface(object : Any() {
            @JavascriptInterface
            fun tosign() {
                runOnUiThread {
                    if (writepaddialog == null) {
                        writepaddialog = WritePadDialog(this@WebActivity) {
                            //                    com.magiclon.logutilslibrary.Log.e("$it")
                            webview?.loadUrl("javascript:signGiveVal('$it')")

                        }
                    }
                    writepaddialog!!.show()
                }
            }

            @JavascriptInterface
            fun tofinish() {
                finish()
            }
        }, "sign")

    }

    override fun onBackPressed() {
        showPicker()
//        if (webview?.canGoBack()!!) {
//            webview?.goBack()
//        } else {
//            super.onBackPressed()
//        }
    }

    fun showDialog(msg: String) {
        if (null == mProgress) {
            mProgress = ProgressDialog(this)
            mProgress?.setCanceledOnTouchOutside(false)
        }
        mProgress?.setMessage(msg)
        mProgress?.show()
    }

    private fun openImageChooserActivity() {
        var i = Intent(Intent.ACTION_GET_CONTENT);
        i.addCategory(Intent.CATEGORY_OPENABLE);
//        i.setType("image/*");//图片上传
//        i.setType("file/*");//文件上传
        i.setType("*/*");//文件上传
        startActivityForResult(Intent.createChooser(i, "Image Chooser"), FILE_CHOOSER_RESULT_CODE);
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

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private fun onActivityResultAboveL(requestCode: Int, resultCode: Int, intent: Intent?) {
        if (requestCode != FILE_CHOOSER_RESULT_CODE || uploadMessageAboveL == null)
            return
        var results: Array<Uri?>? = null
        if (resultCode == Activity.RESULT_OK) {
            if (intent != null) {
                val dataString = intent.dataString
                val clipData = intent.clipData
                if (clipData != null) {
                    results = arrayOfNulls(clipData.itemCount)
                    for (i in 0 until clipData.itemCount) {
                        val item = clipData.getItemAt(i)
                        results[i] = item.uri
                    }
                }
                if (dataString != null)
                    results = arrayOf(Uri.parse(dataString))
            }
        }
        uploadMessageAboveL!!.onReceiveValue(results as Array<Uri>)
        uploadMessageAboveL = null
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == FILE_CHOOSER_RESULT_CODE) {
            if (null == uploadMessage && null == uploadMessageAboveL) return
            val result = if (data == null || resultCode != RESULT_OK) null else data!!.getData()
            // Uri result = (((data == null) || (resultCode != RESULT_OK)) ? null : data.getData());
            if (uploadMessageAboveL != null) {
                onActivityResultAboveL(requestCode, resultCode, data)
            } else if (uploadMessage != null) {
                uploadMessage!!.onReceiveValue(result)
                uploadMessage = null
            }
        } else {
            //这里uploadMessage跟uploadMessageAboveL在不同系统版本下分别持有了
            //WebView对象，在用户取消文件选择器的情况下，需给onReceiveValue传null返回值
            //否则WebView在未收到返回值的情况下，无法进行任何操作，文件选择器会失效
            if (uploadMessage != null) {
                uploadMessage!!.onReceiveValue(null)
                uploadMessage = null
            } else if (uploadMessageAboveL != null) {
                uploadMessageAboveL!!.onReceiveValue(null)
                uploadMessageAboveL = null
            }
        }
    }

    fun showPicker() {
        picker=OptionsPickerBuilder(this,object : OnOptionsSelectListener {
            override fun onOptionsSelect(options1: Int, options2: Int, options3: Int, v: View?) {
                Log.e("---","--${options1}-22-")
            }

        }).build()
        var c= arrayListOf("a","b","c","d","e","f")
        picker?.setPicker(c)
        picker?.show()
    }

}

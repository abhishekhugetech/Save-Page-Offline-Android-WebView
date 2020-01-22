package com.abhishek.savepageoffline

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File

class MainActivity : AppCompatActivity() {

    lateinit var webChromeClient:VideoEnabledWebChromeClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setClickListeners()
        setUpWebView()
        webView.loadUrl(START_PAGE)
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setUpWebView() {
        webView.settings.javaScriptEnabled = true
        webView.webViewClient = object : WebViewClient(){
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                // Hide Page Loading Progress.
                browserProgress.visibility = View.GONE
            }

            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                browserProgress.progress = 0
                browserProgress.visibility = View.VISIBLE
            }

            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
            ): Boolean {
                return false
            }
        }
        // Initialize the VideoEnabledWebChromeClient and set event handlers
        val loadingView = layoutInflater.inflate(R.layout.view_loading_video, null); // Your own view, read class comments
        webChromeClient = object : VideoEnabledWebChromeClient(nonVideoLayout, videoLayout, loadingView, webView) // See all available constructors...
        {
            override fun onReceivedTitle(view: WebView?, title: String?) {
                super.onReceivedTitle(view, title)
                // Update Page title.
                pageTitle.text = title.toString()
            }

            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                super.onProgressChanged(view, newProgress)
                // Update Page progress
                browserProgress.progress = newProgress
            }
        }
        webChromeClient.setOnToggledFullscreen(object : VideoEnabledWebChromeClient.ToggledFullscreenCallback
        {
            override fun toggledFullscreen(fullscreen: Boolean) {

                // Your code to handle the full-screen change, for example showing and hiding the title bar. Example:
                if (fullscreen)
                {
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE)
                    appBar.visibility = View.GONE
                    val attrs = getWindow().getAttributes()
                    attrs.flags = WindowManager.LayoutParams.FLAG_FULLSCREEN or WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
                    getWindow().setAttributes(attrs)
                    //noinspection all
                    window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LOW_PROFILE;
                }
                else
                {
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
                    appBar.visibility = View.VISIBLE
                    val attrs = getWindow().getAttributes()
                    attrs.flags = WindowManager.LayoutParams.FLAG_FULLSCREEN and WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
                    getWindow().setAttributes(attrs)
                    //noinspection all
                    getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
                }
            }
        });
        webView.webChromeClient = webChromeClient

        // Navigate everywhere you want, this classes have only been tested on YouTube's mobile site
        webView.loadUrl("http://m.youtube.com");
    }

    private fun setClickListeners() {
        saveWebPage.setOnClickListener { view ->
            webView.saveWebArchive( filesDir.absolutePath  + File.separator + webView.title+ ".mhtml" , false ) {
                val message = if (it!=null) "Page saved" else "Failed to save page"
                Snackbar.make( view , message, Snackbar.LENGTH_SHORT ).show()
            }
        }
        goToSavedPage.setOnClickListener {
            startActivityForResult(Intent(this,SavedPages::class.java), FILE_OPEN_REQUEST)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode== FILE_OPEN_REQUEST && data!=null && data.hasExtra(FILE_DATA)){
            val filePath = data.getStringExtra(FILE_DATA) ?: return
            webView.loadUrl("file://"+File(filePath).path)
        }
    }

    override fun onBackPressed() {
        if (webView.canGoBack()){
            webView.goBack()
        }else{
            super.onBackPressed()
        }
    }

    companion object{
        val TAG = "SavePageExample"
        const val START_PAGE = "https://google.com"
        const val SAVED_PAGES = "SavedPages"
        const val FILE_OPEN_REQUEST = 23422
        const val FILE_DATA = "file_open_path"
    }
}

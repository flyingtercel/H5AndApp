package com.mifeng.us.androidandh5;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.Toast;

import java.util.Iterator;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    private WebView mWebView;
    private Button button;
    private Button button2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();
        setOnClick();
        jsToAndroid();
    }

    private void setOnClick() {
        button = (Button) findViewById(R.id.button);
        button2 = findViewById(R.id.button2);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 通过Handler发送消息
                //toHtml1();
                toHtml2();
            }
        });
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this,CameraToH5Activity.class);
                startActivity(intent);
            }
        });
    }

    private void initView() {
        mWebView = findViewById(R.id.mWebView);
        button = findViewById(R.id.button);
        WebSettings webSettings = mWebView.getSettings();
        // 设置与Js交互的权限
        webSettings.setJavaScriptEnabled(true);
        // 设置允许JS弹窗
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
        mWebView.loadUrl("file:///android_asset/web.html");
        //mWebView.addJavascriptInterface(new AndroidToJs(), "android");
        mWebView.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onJsAlert(WebView view, String url, String message, final JsResult result) {
                AlertDialog.Builder b = new AlertDialog.Builder(MainActivity.this);
                b.setTitle("Alert");
                b.setMessage(message);
                b.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        result.confirm();
                    }
                });
                b.setCancelable(false);
                b.create().show();
                return true;
            }

            @Override
            public boolean onJsPrompt(WebView view, String url, String message, String defaultValue, JsPromptResult result) {

                Uri uri = Uri.parse(url);
                if (uri.getScheme().equals("js") && uri.getAuthority().equals("www.webview.com")){
                    Toast.makeText(MainActivity.this,"Js调用了Android中的代码",Toast.LENGTH_SHORT).show();
                    result.confirm("Js调用Android的方法成功了");
                    return true;
                }

                return super.onJsPrompt(view, url, message, defaultValue, result);
            }
        });
    }

    public void toHtml1() {
        /*不需要有返回值，针对效率高*/
        mWebView.post(new Runnable() {
            @Override
            public void run() {
                mWebView.loadUrl("javascript:callJS()");
            }
        });
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public void toHtml2() {
        mWebView.evaluateJavascript("javascript:callJS()", new ValueCallback<String>() {
            @Override
            public void onReceiveValue(String value) {
                Toast.makeText(MainActivity.this, value, Toast.LENGTH_SHORT).show();
                button.setText(value);
            }
        });

    }

    /*Js调用Android交互方式二*/
    public void jsToAndroid() {
        mWebView.setWebViewClient(new WebViewClient() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {

                Uri uri = request.getUrl();
                if (uri.getScheme().equals("js")){
                    if (uri.getAuthority().equals("www.webview.com")){
                        Set<String> set = uri.getQueryParameterNames();
                        Iterator<String> iterator = set.iterator();
                        while (iterator.hasNext()){
                            Log.i("tag","============="+iterator.next());
                        }
                        Toast.makeText(MainActivity.this,"Js调用了Android中的代码",Toast.LENGTH_SHORT).show();
                        return true;
                    }
                }
                return super.shouldOverrideUrlLoading(view, request);
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                Uri uri = Uri.parse(url);
                if (uri.getScheme().equals("js")){
                    if (uri.getAuthority().equals("www.webview.com")){
                        Set<String> set = uri.getQueryParameterNames();
                        Iterator<String> iterator = set.iterator();
                        while (iterator.hasNext()){
                            Log.i("tag","============="+iterator.next());
                        }
                        Toast.makeText(MainActivity.this,"Js调用了Android中的代码",Toast.LENGTH_SHORT).show();
                        return true;
                    }
                }
                return super.shouldOverrideUrlLoading(view, url);
            }
        });

    }


    /*Js调用Android交互方式一*/
    public class AndroidToJs extends Object {

        @JavascriptInterface
        public void toAndroid(String message) {
            button.setText(message);
        }
    }
}

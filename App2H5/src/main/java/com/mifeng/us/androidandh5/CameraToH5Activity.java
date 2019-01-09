package com.mifeng.us.androidandh5;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.TextView;

import com.luck.picture.lib.PictureSelector;
import com.luck.picture.lib.config.PictureConfig;
import com.luck.picture.lib.config.PictureMimeType;
import com.luck.picture.lib.entity.LocalMedia;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public class CameraToH5Activity extends AppCompatActivity {

    private WebView mWebView;
    private ValueCallback<Uri[]> mFilePathCallback;
    private ValueCallback<Uri> mFilePathCallback1;
    private ValueCallback<Uri> mFilePathCallback2;
    private ValueCallback<Uri> mFilePathCallback3;
    private int type;
    private RewritePopwindow bottomPopupWindow;
    private TextView mAlbum;
    private TextView mCamera;
    private TextView mCancel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_to_h5);

        initView();

    }

    private void initView() {

        mWebView = findViewById(R.id.mWebView);
        initWebView();
    }

    private void initWebView() {
        WebSettings webSettings = mWebView.getSettings();
        //设置WebView属性，能够执行Javascript脚本
        webSettings.setJavaScriptEnabled(true);
        //设置可以访问文件
        webSettings.setAllowFileAccess(true);
        //设置支持缩放
        webSettings.setBuiltInZoomControls(true);
        mWebView.loadUrl("file:///android_asset/cameratoh5.html");
        mWebView.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {//5.0+
                initPopWindow();
                mFilePathCallback = filePathCallback;
                return true;
            }

            //openFileChooser 方法是隐藏方法
            /** For Android > 4.1 */
            public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType, String capture) {
                initPopWindow();
                mFilePathCallback1 = uploadMsg;
            }
            /** android 系统版本<3.0 */
            public void openFileChooser(ValueCallback<Uri> uploadMsg) {
                initPopWindow();
                mFilePathCallback2 = uploadMsg;
            }
            /**a ndroid 系统版本3.0+ */
            public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType) {
                initPopWindow();
                mFilePathCallback3 = uploadMsg;
            }

        });
    }

    public void initPopWindow() {
        if (bottomPopupWindow == null) {
            View bottomView = View.inflate(this, R.layout.layout_bottom_dialog, null);
            mAlbum = bottomView.findViewById(R.id.tv_album);
            mCamera = bottomView.findViewById(R.id.tv_camera);
            mCancel = bottomView.findViewById(R.id.tv_cancel);
            bottomPopupWindow = new RewritePopwindow(this, bottomView);
        }


        View.OnClickListener clickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (view.getId()) {
                    case R.id.tv_album:
                        //相册
                        PictureSelector.create(CameraToH5Activity.this)
                                .openGallery(PictureMimeType.ofImage())
                                .maxSelectNum(1)
                                .minSelectNum(1)
                                .imageSpanCount(4)
                                .selectionMode(PictureConfig.MULTIPLE)
                                .forResult(PictureConfig.CHOOSE_REQUEST);
                        break;
                    case R.id.tv_camera:
                        //拍照
                        PictureSelector.create(CameraToH5Activity.this)
                                .openCamera(PictureMimeType.ofImage())
                                .forResult(PictureConfig.CHOOSE_REQUEST);
                        break;
                    case R.id.tv_cancel:

                        break;
                }
                bottomPopupWindow.dismiss();
            }
        };

        mAlbum.setOnClickListener(clickListener);
        mCamera.setOnClickListener(clickListener);
        mCancel.setOnClickListener(clickListener);

        if (bottomPopupWindow != null && !bottomPopupWindow.isShowing()) {
            bottomPopupWindow.showAtLocation(getWindow().getDecorView(), Gravity.BOTTOM, 0, 0);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == PictureConfig.CHOOSE_REQUEST) {
                // 图片选择结果回调
                List<LocalMedia> images = PictureSelector.obtainMultipleResult(data);
                getbitmap(type,images.get(0).getPath(),null);
            }
        }
    }

    /*考虑4.3之上的版本*/
    protected void getbitmap(int type, String imagePath, Bitmap bitmap) {
        Uri uri = Uri.fromFile(new File(imagePath));
        if (Build.VERSION.SDK_INT > 18) {
            mFilePathCallback.onReceiveValue(new Uri[]{uri});
        } else{
            mFilePathCallback1.onReceiveValue(uri);
        }
    }
}

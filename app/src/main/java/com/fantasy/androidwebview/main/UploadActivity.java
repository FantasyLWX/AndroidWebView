package com.fantasy.androidwebview.main;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.fantasy.androidwebview.Constant;
import com.fantasy.androidwebview.R;
import com.fantasy.androidwebview.base.BaseActivity;
import com.fantasy.androidwebview.base.PermissionListener;
import com.fantasy.androidwebview.utils.DirUtils;
import com.fantasy.androidwebview.utils.PreferencesUtils;
import com.fantasy.androidwebview.utils.WebViewUtils;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * WebView拍照和上传文件
 * <pre>
 *     author  : Fantasy
 *     version : 1.0, 2020-02-22
 *     since   : 1.0, 2020-02-22
 * </pre>
 */
public class UploadActivity extends BaseActivity implements View.OnClickListener {
    private LinearLayout mLlWeb;
    private ImageView mIvClose;
    private TextView mTvTitle;
    private FrameLayout mFlWeb;
    private WebView mWebView;

    private String mAppName;
    private String mAcceptType;
    private String mCameraPhotoPath;
    private ValueCallback<Uri> mUploadMessage;
    private ValueCallback<Uri[]> mUploadMessageAnother;
    private static final int REQUEST_CODE_FILE_CHOOSER = 0;
    private static final int REQUEST_CODE_FILE_CHOOSER_ANOTHER = 1;
    /**
     * 拍照上传的请求码
     */
    private static final int REQUEST_CODE_PHOTO_UPLOAD = 3;

    /**
     * 启动WebActivity
     *
     * @param context 上下文
     * @param url     网址
     */
    public static void actionStart(Context context, String url) {
        Intent intent = new Intent(context, UploadActivity.class);
        intent.putExtra("url", url);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web);
        bindEvent();
        initWebView();
        initData();
    }

    @Override
    protected void onResume() {
        if (mWebView != null) {
            // 激活WebView为活跃状态，能正常执行网页的响应
            mWebView.onResume();
        }
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mWebView != null) {
            // 当页面被失去焦点被切换到后台不可见状态，需要执行onPause
            // 通过onPause动作通知内核暂停所有的动作，比如DOM的解析、plugin的执行、JavaScript执行
            mWebView.onPause();
        }
    }

    @Override
    protected void onDestroy() {
        if (mWebView != null) {
            mWebView.loadDataWithBaseURL(null, "", "text/html", "utf-8", null);
            mFlWeb.removeView(mWebView); // 在退出时，移除WebView，可以防止其在后台消耗资源
            // clearCache()和destroy()这两个方法，要在移除WebView后调用，不然APP会崩溃
            mWebView.clearHistory();
            //mWebView.clearCache(true); // 屏蔽的原因：加载网页会保存缓存，如果清掉缓存了，则每次打开网页会重新下载资源
            mWebView.destroy();
            mWebView = null;
        }
        super.onDestroy();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            goBack();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void bindEvent() {
        mLlWeb = findViewById(R.id.ll_web);
        mIvClose = findViewById(R.id.iv_title_bar_close);
        mTvTitle = findViewById(R.id.tv_title_bar_title);
        mFlWeb = findViewById(R.id.fl_web);
        mWebView = findViewById(R.id.wv_web);

        mIvClose.setOnClickListener(this);
        findViewById(R.id.iv_title_bar_back).setOnClickListener(this);
    }

    private void initData() {
        mAppName = getString(R.string.app_name);
        mWebView.loadUrl(getIntent().getStringExtra("url"));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_title_bar_back:
                goBack();
                break;
            case R.id.iv_title_bar_close:
                finish();
                break;
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void initWebView() {
        WebSettings settings = mWebView.getSettings();
        settings.setAppCacheEnabled(true); // 开启 Application Caches 功能
        settings.setDomStorageEnabled(true); // 开启 DOM storage API 功能
        settings.setAllowFileAccess(true); // 设置可以访问文件，默认启用
        settings.setJavaScriptEnabled(true); // 如果访问的页面中要与Javascript交互，则webview必须设置支持Javascript
        settings.setJavaScriptCanOpenWindowsAutomatically(true); // 支持通过JS打开新窗口

        settings.setCacheMode(WebSettings.LOAD_DEFAULT); // 设置缓存模式
        // 缓存模式如下：
        // LOAD_CACHE_ONLY: 不使用网络，只读取本地缓存数据
        // LOAD_DEFAULT: （默认）根据cache-control决定是否从网络上取数据
        // LOAD_NO_CACHE: 不使用缓存，只从网络获取数据
        // LOAD_CACHE_ELSE_NETWORK，只要本地有，无论是否过期，或者no-cache，都使用缓存中的数据

        settings.setSavePassword(false); // 禁止自动保存密码
        settings.setSupportZoom(true); // 支持缩放
        settings.setBuiltInZoomControls(true); // 启动缩放机制
        settings.setDisplayZoomControls(false); // 隐藏原生缩放控件，即缩小放大两个按钮
        settings.setUseWideViewPort(true); // 将图片调整到适合WebView的大小
        settings.setLoadWithOverviewMode(true); // 缩放内容以适应屏幕大小

        // 设置请求头的User-Agent，前端可以通过这来判断页面是不是运行在自家的APP中（这里用 Fantasy 来作为自家的APP标识）
        settings.setUserAgentString(settings.getUserAgentString() + " Fantasy");

        settings.setGeolocationEnabled(true); // 启用地理定位，默认为true
        String dir = getApplicationContext().getDir("database", Context.MODE_PRIVATE).getPath();
        settings.setGeolocationDatabasePath(dir); // 设置定位的数据库路径

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW); // 支持HTTP和HTTPS混合
        }

        WebViewUtils.removeJavascriptInterfaces(mWebView);

        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                // 页面加载结束
                mTvTitle.setText(view.getTitle()); // 获取网页的标题并设置在自定义的标题栏上面
                if (view.canGoBack()) {
                    mIvClose.setVisibility(View.VISIBLE);
                } else {
                    mIvClose.setVisibility(View.GONE);
                }
            }
        });

        mWebView.setWebChromeClient(new WebChromeClient() {
            // 扩展浏览器上传文件
            // For Android < 3.0
            public void openFileChooser(ValueCallback<Uri> uploadMsg) {
                mAcceptType = "*/*";
                toOpenFileChooser(uploadMsg);
            }

            // For Android >= 3.0
            public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType) {
                if (TextUtils.isEmpty(acceptType)) {
                    mAcceptType = "*/*";
                }
                mAcceptType = acceptType;
                toOpenFileChooser(uploadMsg);
            }

            // For Android >= 4.1.1
            public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType, String capture) {
                if (TextUtils.isEmpty(acceptType)) {
                    mAcceptType = "*/*";
                }
                mAcceptType = acceptType;
                toOpenFileChooser(uploadMsg);
            }

            // For Android >= 5.0
            @TargetApi(Build.VERSION_CODES.LOLLIPOP)
            @Override
            public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback,
                                             FileChooserParams fileChooserParams) {
                String[] acceptTypes = fileChooserParams.getAcceptTypes();
                if (acceptTypes.length > 0) {
                    if (TextUtils.isEmpty(acceptTypes[0])) {
                        mAcceptType = "*/*";
                    } else {
                        mAcceptType = acceptTypes[0];
                    }
                } else {
                    mAcceptType = "*/*";
                }
                toOpenFileChooserAnother(filePathCallback);
                return true;
            }
        });
    }

    /**
     * 返回
     */
    private void goBack() {
        finish();
    }

    /**
     * 申请相机权限
     */
    private void toRequestCameraPermission() {
        String message = mAppName + getString(R.string.main_permission_photo_upload);

        showAlertDialog(message, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();

                String[] permissions = new String[]{
                        Manifest.permission.CAMERA
                };

                requestPermissions(permissions, new PermissionListener() {
                    @Override
                    public void onGranted() {
                        toOpenFileChooserAnother();
                    }

                    @Override
                    public void onGranted(List<String> grantedList) {
                    }

                    @Override
                    public void onDenied(List<String> deniedList) {
                        if (ActivityCompat.shouldShowRequestPermissionRationale(UploadActivity.this, Manifest.permission.CAMERA)) {
                            toRequestCameraPermission();
                        } else {
                            showAlertDialog(String.format(getString(R.string.main_permission_photo_upload_1), mAppName), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                    Intent intent = new Intent();
                                    intent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
                                    intent.addCategory("android.intent.category.DEFAULT");
                                    intent.setData(Uri.parse("package:" + UploadActivity.this.getPackageName()));
                                    startActivityForResult(intent, REQUEST_CODE_PHOTO_UPLOAD);
                                }
                            }, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                    toOpenFileChooserAnother();
                                }
                            });
                        }
                    }
                });
            }
        }, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                toOpenFileChooserAnother();
            }
        });
    }

    /**
     * 打开文件选择器，适用于Android 5.0 以下
     *
     * @param uploadMsg 加载文件的信息
     */
    private void toOpenFileChooser(ValueCallback<Uri> uploadMsg) {
        mUploadMessage = uploadMsg;
        Intent i = new Intent(Intent.ACTION_GET_CONTENT);
        i.addCategory(Intent.CATEGORY_OPENABLE);
        i.setType(mAcceptType);
        startActivityForResult(Intent.createChooser(i, getString(R.string.choose_app)), REQUEST_CODE_FILE_CHOOSER);
    }

    /**
     * 打开文件选择器，适用于Android 5.0 及以上
     *
     * @param uploadMsg 加载文件的信息
     */
    private void toOpenFileChooserAnother(ValueCallback<Uri[]> uploadMsg) {
        mUploadMessageAnother = uploadMsg;
        if ((mAcceptType.equals("*/*") || mAcceptType.contains("image"))
                && ContextCompat.checkSelfPermission(mContext, Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED) {
            long oldTime = PreferencesUtils.getLong(mContext, Constant.PREF_REQUEST_CAMERA_TIME);
            long newTime = System.currentTimeMillis();

            if (oldTime == -1) { // 首次要提示申请权限
                PreferencesUtils.putLong(mContext, Constant.PREF_REQUEST_CAMERA_TIME, newTime);
                toRequestCameraPermission();
                return;
            } else {
                if (newTime - oldTime >= 7 * 24 * 60 * 60 * 1000) { // 7天后再提示申请权限
                    PreferencesUtils.putLong(mContext, Constant.PREF_REQUEST_CAMERA_TIME, newTime);
                    toRequestCameraPermission();
                    return;
                }
            }
        }
        toOpenFileChooserAnother();
    }

    /**
     * 打开文件选择器，适用于Android 5.0 及以上
     */
    private void toOpenFileChooserAnother() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if ((mAcceptType.equals("*/*") || mAcceptType.contains("image"))
                && takePictureIntent.resolveActivity(mContext.getPackageManager()) != null) {
            File photoFile = null;
            try {
                if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                    String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.CHINA).format(new Date());
                    String imageFileName = "PIC_" + timeStamp + "_";
                    File storageDir = new File(DirUtils.getPictures());
                    if (!storageDir.exists()) {
                        storageDir.mkdirs();
                    }
                    photoFile = File.createTempFile(imageFileName, ".jpg", storageDir);
                }
            } catch (Exception e) {
                Log.d(Constant.TAG, mClassName + " Unable to create Image File", e);
            }

            if (photoFile != null) {
                mCameraPhotoPath = photoFile.getAbsolutePath();
                Log.d(Constant.TAG, "图片路径 : " + mCameraPhotoPath);
                takePictureIntent.putExtra("PhotoPath", "file:" + mCameraPhotoPath);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
            } else {
                takePictureIntent = null;
            }
        } else {
            takePictureIntent = null;
        }

        Intent[] intentArray;
        if (takePictureIntent != null) {
            intentArray = new Intent[]{takePictureIntent};
        } else {
            intentArray = new Intent[0];
        }

        Intent contentSelectionIntent = new Intent(Intent.ACTION_GET_CONTENT);
        contentSelectionIntent.addCategory(Intent.CATEGORY_OPENABLE);
        contentSelectionIntent.setType(mAcceptType);

        Intent chooserIntent = new Intent(Intent.ACTION_CHOOSER);
        chooserIntent.putExtra(Intent.EXTRA_INTENT, contentSelectionIntent);
        chooserIntent.putExtra(Intent.EXTRA_TITLE, getString(R.string.choose_app));
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, intentArray);

        startActivityForResult(chooserIntent, REQUEST_CODE_FILE_CHOOSER_ANOTHER);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        switch (requestCode) {
            case REQUEST_CODE_FILE_CHOOSER:
                if (mUploadMessage == null) {
                    return;
                }
                Uri result = (intent == null || resultCode != RESULT_OK) ? null : intent.getData();
                mUploadMessage.onReceiveValue(result);
                mUploadMessage = null;
                break;
            case REQUEST_CODE_FILE_CHOOSER_ANOTHER: // Android 5.0 及以上
                if (mUploadMessageAnother == null) {
                    return;
                }

                Uri[] results = null;
                if (resultCode == RESULT_OK) {
                    if (intent != null) {
                        results = new Uri[]{intent.getData()};
                    } else {
                        if (mCameraPhotoPath != null) {
                            results = new Uri[]{Uri.parse("file:" + mCameraPhotoPath)};
                        }
                    }
                } else {
                    if (!TextUtils.isEmpty(mCameraPhotoPath)) {
                        File file = new File(mCameraPhotoPath);
                        file.delete();
                    }
                }

                mUploadMessageAnother.onReceiveValue(results);
                mUploadMessageAnother = null;
                break;
            case REQUEST_CODE_PHOTO_UPLOAD:
                toOpenFileChooserAnother();
                break;
            default:
                super.onActivityResult(requestCode, resultCode, intent);
                break;
        }
    }

}

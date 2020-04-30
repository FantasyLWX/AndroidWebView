package com.fantasy.androidwebview.main;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.webkit.ConsoleMessage;
import android.webkit.DownloadListener;
import android.webkit.GeolocationPermissions;
import android.webkit.JavascriptInterface;
import android.webkit.JsResult;
import android.webkit.SslErrorHandler;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.fantasy.androidwebview.Constant;
import com.fantasy.androidwebview.R;
import com.fantasy.androidwebview.base.BaseActivity;
import com.fantasy.androidwebview.base.PermissionListener;
import com.fantasy.androidwebview.scan.ScanActivity;
import com.fantasy.androidwebview.utils.DirUtils;
import com.fantasy.androidwebview.utils.DownloadAsyncTask;
import com.fantasy.androidwebview.utils.IntentHelper;
import com.fantasy.androidwebview.utils.PreferencesUtils;
import com.fantasy.androidwebview.utils.TextHelper;
import com.fantasy.androidwebview.utils.WebViewUtils;
import com.fantasy.androidwebview.utils.file.OpenFileHelper;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * 包含所有功能的WebView
 * <pre>
 *     author  : Fantasy
 *     version : 1.3, 2020-04-30
 *     since   : 1.0, 2020-01-07
 * </pre>
 */
public class WebActivity extends BaseActivity implements View.OnClickListener {
    private LinearLayout mLlWeb;
    private ImageView mIvClose;
    private TextView mTvTitle;
    private FrameLayout mFlWeb;
    private WebView mWebView;
    private ProgressBar mProgressBar;
    private RelativeLayout mRlWarning;
    private FrameLayout mFlVideoContainer;

    private String mUrl;
    private String mAppName;
    private DownloadAsyncTask mDownloadAsyncTask;

    private boolean mIsShowVideo;
    private String mGeolocationPermissionsOrigin;
    private GeolocationPermissions.Callback mGeolocationPermissionsCallback;
    private String mAcceptType;
    private String mCameraPhotoPath;
    private ValueCallback<Uri> mUploadMessage;
    private ValueCallback<Uri[]> mUploadMessageAnother;
    private static final int REQUEST_CODE_FILE_CHOOSER = 0;
    private static final int REQUEST_CODE_FILE_CHOOSER_ANOTHER = 1;
    private static final int REQUEST_CODE_INSTALL_APK = 2;
    /**
     * 扫一扫的请求码
     */
    private static final int REQUEST_CODE_SCAN = 3;
    private static final int REQUEST_CODE_SCAN_RESULT = 4;
    /**
     * 拍照上传的请求码
     */
    private static final int REQUEST_CODE_PHOTO_UPLOAD = 5;
    private static final int REQUEST_CODE_LOCATION = 6;

    /**
     * 启动WebActivity
     *
     * @param context 上下文
     * @param url     网址
     */
    public static void actionStart(Context context, String url) {
        Intent intent = new Intent(context, WebActivity.class);
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
        mProgressBar = findViewById(R.id.pb_web_progress);
        mRlWarning = findViewById(R.id.rl_warning_network);
        mFlVideoContainer = findViewById(R.id.fl_web_video_container);

        mIvClose.setOnClickListener(this);
        mRlWarning.setOnClickListener(this);
        findViewById(R.id.iv_title_bar_back).setOnClickListener(this);
    }

    private void initData() {
        mUrl = getIntent().getStringExtra("url");

        // 同步Cookie
        // Map<String, String> map = new HashMap<>();
        // map.put("Fantasy-Token", "123456789");
        // WebViewUtils.syncCookie(mContext, mUrl, map);

        // 设置请求头
        // Map<String, String> map = new HashMap<>();
        // map.put("Fantasy-Token", "123456789");
        // mWebView.loadUrl(mUrl, map);

        mAppName = getString(R.string.app_name);
        mIsShowVideo = false;
        mWebView.loadUrl(mUrl);
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
            case R.id.rl_warning_network:
                mWebView.loadUrl(mUrl);
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

        mWebView.addJavascriptInterface(new JavascriptObject(), "android");

        mWebView.setDownloadListener(new DownloadListener() {
            @Override
            public void onDownloadStart(final String url, String userAgent, String contentDisposition,
                                        String mimetype, long contentLength) {
                try {
                    Log.d(Constant.TAG, mClassName + " onDownloadStart :\nurl : " + url
                            + "\nuserAgent : " + userAgent + "\ncontentDisposition : " + contentDisposition
                            + "\nmimetype : " + mimetype + "\ncontentLength : " + contentLength);
                    String fileName = contentDisposition;
                    if (TextUtils.isEmpty(fileName)) {
                        fileName = url.substring(url.lastIndexOf("/") + 1);
                    } else {
                        fileName = URLDecoder.decode(fileName.substring(
                                fileName.indexOf("filename=") + 9), "UTF-8");
                    }
                    showAlertDialog(String.format(getString(R.string.main_download_file), fileName), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            toDownloadFile(url);
                        }
                    });
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }

            }
        });

        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
                // 捕获网页的请求：接口请求、图片请求、JS文件请求、CSS文件请求
                Log.d(Constant.TAG, mClassName + " request url : " + url);
                return super.shouldInterceptRequest(view, url);
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                // 作用：打开网页时不调用系统浏览器，而是在本WebView中显示；在网页上的所有加载都经过这个方法
                Log.d(Constant.TAG, mClassName + " loading url : " + url);

                if (!url.startsWith("http") && !url.startsWith("file")) {
                    try {
                        IntentHelper.openBrowser(mContext, url);
                    } catch (Exception e) {
                        Log.d(Constant.TAG, mClassName + " WebViewClient Exception", e);
                        if (url.startsWith("tel:")) {
                            showToast(R.string.main_no_tel);
                        } else if (url.startsWith("sms:")) {
                            showToast(R.string.main_no_sms);
                        } else if (url.startsWith("mailto:")) {
                            showToast(R.string.main_no_mail);
                        } else {
                            showToast(R.string.load_failed);
                        }
                    }
                    return true;
                }
                return super.shouldOverrideUrlLoading(view, url);
            }

            @Override
            public void onReceivedHttpError(WebView view, WebResourceRequest request, WebResourceResponse errorResponse) {
                super.onReceivedHttpError(view, request, errorResponse);
                // 网页的请求失败信息
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    Log.d(Constant.TAG, mClassName + " url : " + request.getUrl() + "\ncode : " + errorResponse.getStatusCode()
                            + "\ndescription : " + errorResponse.getReasonPhrase());
                }
            }

            @Override
            public void onReceivedSslError(WebView view, final SslErrorHandler handler, SslError error) {
                Log.d(Constant.TAG, mClassName + " onReceivedSslError url : " + view.getUrl());
                showAlertDialog(R.string.error_ssl_cert_invalid, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        handler.proceed();
                    }
                }, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        handler.cancel();
                    }
                });
            }

            // 旧版本，会在新版本中也可能被调用，所以加上一个判断，防止重复显示
            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                super.onReceivedError(view, errorCode, description, failingUrl);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    return;
                }
                // 加载网页出错，例如：404、没有网络
                Log.d(Constant.TAG, mClassName + " onReceivedError errorCode : " + errorCode
                        + "\ndescription : " + description + "\nfailingUrl : " + failingUrl);
                mUrl = failingUrl;
                mRlWarning.setVisibility(View.VISIBLE);
                mWebView.setVisibility(View.GONE);
            }

            // 新版本，只会在 Android 6.0 及以上调用
            @TargetApi(Build.VERSION_CODES.M)
            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                super.onReceivedError(view, request, error);
                // 加载网页出错，例如：404、没有网络
                mUrl = request.getUrl().toString();
                Log.d(Constant.TAG, mClassName + " onReceivedError errorCode : " + error.getErrorCode()
                        + "\ndescription : " + error.getDescription() + "\nurl : " + mUrl);
                mRlWarning.setVisibility(View.VISIBLE);
                mWebView.setVisibility(View.GONE);
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                // 开始加载页面
                mRlWarning.setVisibility(View.GONE);
                mWebView.setVisibility(View.VISIBLE);
            }

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
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                if (newProgress == 100) {
                    mProgressBar.setVisibility(View.GONE); // 加载完网页进度条消失
                } else {
                    mProgressBar.setVisibility(View.VISIBLE); // 开始加载网页时显示进度条
                    mProgressBar.setProgress(newProgress);
                }
                super.onProgressChanged(view, newProgress);
            }

            @Override
            public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
                // 捕获网页的日志
                Log.d(Constant.TAG, mClassName + " WebView console message : " + consoleMessage.message()
                        + "\nsource : " + consoleMessage.sourceId()
                        + " (" + consoleMessage.lineNumber() + ")");
                return true;
            }

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

            @Override
            public boolean onJsAlert(WebView view, String url, String message, final JsResult result) {
                showConfirmDialog(message, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        result.confirm();
                    }
                });
                return true;
            }

            @Override
            public boolean onJsConfirm(WebView view, String url, String message, final JsResult result) {
                showAlertDialog(message, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        result.confirm();
                    }
                }, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        result.cancel();
                    }
                });
                return true;
            }

            @Override
            public void onShowCustomView(View view, CustomViewCallback callback) {
                // 进入视频全屏
                Log.d(Constant.TAG, mClassName + " onShowCustomView");
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE); // 横屏
                getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
                mLlWeb.setVisibility(View.GONE);
                mFlVideoContainer.setVisibility(View.VISIBLE);
                mFlVideoContainer.addView(view);
                mIsShowVideo = true;
                super.onShowCustomView(view, callback);
            }

            @Override
            public void onHideCustomView() {
                // 退出视频全屏
                Log.d(Constant.TAG, mClassName + " onHideCustomView");
                //setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT); // 竖屏
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED); // 默认值，系统根据方向感应自动选择屏幕方向
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
                mLlWeb.setVisibility(View.VISIBLE);
                mFlVideoContainer.setVisibility(View.GONE);
                mFlVideoContainer.removeAllViews();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        // 点击手机返回键的话，会同时调用onKeyDown和onHideCustomView
                        // 为了避免同时执行退出全屏和返回上一个网页这两个操作的情况出现，要延迟修改mIsShowVideo的值
                        mIsShowVideo = false;
                    }
                }, 800);
                super.onHideCustomView();
            }

            @Override
            public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback callback) {
                Log.d(Constant.TAG, mClassName + " onGeolocationPermissionsShowPrompt origin : " + origin);
                mGeolocationPermissionsOrigin = origin;
                mGeolocationPermissionsCallback = callback;
                toRequestLocationPermission();
            }
        });
    }

    /**
     * 返回
     */
    private void goBack() {
        mWebView.evaluateJavascript("javascript:phoneBackButtonListener()", new ValueCallback<String>() {
            @Override
            public void onReceiveValue(String value) {
                // value的值为"true"时，H5页面屏蔽手机返回键
                // value的值为"false"或"null"时，H5页面不屏蔽手机返回键
                // phoneBackButtonListener()未定义或没有返回任何数据，则value的值为"null"
                if ("false".equals(value) || "null".equals(value)) {
                    if (mWebView.canGoBack()) {
                        if (!mIsShowVideo) {
                            mWebView.goBack();
                        }
                        mIsShowVideo = false;
                    } else {
                        finish();
                    }
                }
            }
        });
    }

    /**
     * 下载文件
     *
     * @param url 下载地址
     */
    private void toDownloadFile(String url) {
        mDownloadAsyncTask = DownloadAsyncTask.execute(mContext, url, DirUtils.getDownload(), new DownloadAsyncTask.CallbackListener() {
            @Override
            public void onProgress(int progress) {
                showDownloadDialog(R.string.dialog_title_download_file, progress, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mDownloadAsyncTask.cancel();
                        mDownloadAsyncTask = null;
                        hideDownloadDialog();
                    }
                });
            }

            @Override
            public void onSuccess(String filePath) {
                mDownloadAsyncTask = null;
                hideDownloadDialog();
                try {
                    String end = filePath.substring(filePath.lastIndexOf(".") + 1).toLowerCase(Locale.ENGLISH);
                    if ("apk".equals(end)) {
                        PreferencesUtils.putString(mContext, Constant.PREF_DOWNLOAD_APK_PATH, filePath);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && !getPackageManager().canRequestPackageInstalls()) {
                            showConfirmDialog(R.string.request_install_apk, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                    Uri packageURI = Uri.parse("package:" + getPackageName());
                                    Intent intent = new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, packageURI);
                                    startActivityForResult(intent, REQUEST_CODE_INSTALL_APK);
                                }
                            });
                            return;
                        }
                        startActivity(OpenFileHelper.getIntent(mContext, filePath));
                    } else {
                        startActivity(OpenFileHelper.getIntent(mContext, filePath));
                    }
                } catch (Exception e) {
                    Log.d(Constant.TAG, mClassName + " toDownloadFile Exception", e);
                    showAlertDialog(R.string.can_not_open_file);
                }
            }

            @Override
            public void onError(String message) {
                mDownloadAsyncTask = null;
                hideDownloadDialog();
                showAlertDialog(message);
            }
        });
    }

    /**
     * 扫码
     */
    private void toScan() {
        if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED) {
            toRequestCameraPermission(REQUEST_CODE_SCAN);
            return;
        }

        ScanActivity.actionStart(WebActivity.this, REQUEST_CODE_SCAN_RESULT);
    }

    /**
     * 申请相机权限
     *
     * @param code 请求码
     */
    private void toRequestCameraPermission(final int code) {
        String message = mAppName;
        switch (code) {
            case REQUEST_CODE_SCAN:
                message += getString(R.string.main_permission_scan);
                break;
            case REQUEST_CODE_PHOTO_UPLOAD:
                message += getString(R.string.main_permission_photo_upload);
                break;
        }

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
                        switch (code) {
                            case REQUEST_CODE_SCAN:
                                toScan();
                                break;
                            case REQUEST_CODE_PHOTO_UPLOAD:
                                toOpenFileChooserAnother();
                                break;
                        }
                    }

                    @Override
                    public void onGranted(List<String> grantedList) {
                    }

                    @Override
                    public void onDenied(List<String> deniedList) {
                        if (ActivityCompat.shouldShowRequestPermissionRationale(WebActivity.this, Manifest.permission.CAMERA)) {
                            toRequestCameraPermission(code);
                        } else {
                            switch (code) {
                                case REQUEST_CODE_SCAN:
                                    showAlertDialog(String.format(getString(R.string.main_permission_scan_1), mAppName), new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                            Intent intent = new Intent();
                                            intent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
                                            intent.addCategory("android.intent.category.DEFAULT");
                                            intent.setData(Uri.parse("package:" + WebActivity.this.getPackageName()));
                                            startActivityForResult(intent, REQUEST_CODE_SCAN);
                                        }
                                    });
                                    break;
                                case REQUEST_CODE_PHOTO_UPLOAD:
                                    showAlertDialog(String.format(getString(R.string.main_permission_photo_upload_1), mAppName), new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                            Intent intent = new Intent();
                                            intent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
                                            intent.addCategory("android.intent.category.DEFAULT");
                                            intent.setData(Uri.parse("package:" + WebActivity.this.getPackageName()));
                                            startActivityForResult(intent, REQUEST_CODE_PHOTO_UPLOAD);
                                        }
                                    }, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                            toOpenFileChooserAnother();
                                        }
                                    });
                                    break;
                            }
                        }
                    }
                });
            }
        }, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                switch (code) {
                    case REQUEST_CODE_PHOTO_UPLOAD:
                        toOpenFileChooserAnother();
                        break;
                }
            }
        });
    }

    /**
     * 申请定位权限
     */
    private void toRequestLocationPermission() {
        if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_DENIED
                && ContextCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_DENIED) {
            showAlertDialog(mAppName + getString(R.string.main_permission_location), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();

                    String[] permissions = new String[]{
                            Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.ACCESS_FINE_LOCATION
                    };

                    requestPermissions(permissions, new PermissionListener() {
                        @Override
                        public void onGranted() {
                            mGeolocationPermissionsCallback.invoke(mGeolocationPermissionsOrigin, true, false);
                        }

                        @Override
                        public void onGranted(List<String> grantedList) {
                        }

                        @Override
                        public void onDenied(List<String> deniedList) {
                            if (ActivityCompat.shouldShowRequestPermissionRationale(WebActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION)
                                    && ActivityCompat.shouldShowRequestPermissionRationale(WebActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                                toRequestLocationPermission();
                            } else {
                                showAlertDialog(String.format(getString(R.string.main_permission_location_1), mAppName), new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                        Intent intent = new Intent();
                                        intent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
                                        intent.addCategory("android.intent.category.DEFAULT");
                                        intent.setData(Uri.parse("package:" + WebActivity.this.getPackageName()));
                                        startActivityForResult(intent, REQUEST_CODE_LOCATION);
                                    }
                                }, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                        mGeolocationPermissionsCallback.invoke(mGeolocationPermissionsOrigin, false, false);
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
                    mGeolocationPermissionsCallback.invoke(mGeolocationPermissionsOrigin, false, false);
                }
            });
        } else {
            mGeolocationPermissionsCallback.invoke(mGeolocationPermissionsOrigin, true, false);
        }
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
                toRequestCameraPermission(REQUEST_CODE_PHOTO_UPLOAD);
                return;
            } else {
                if (newTime - oldTime >= 7 * 24 * 60 * 60 * 1000) { // 7天后再提示申请权限
                    PreferencesUtils.putLong(mContext, Constant.PREF_REQUEST_CAMERA_TIME, newTime);
                    toRequestCameraPermission(REQUEST_CODE_PHOTO_UPLOAD);
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
            case REQUEST_CODE_INSTALL_APK:
                if (resultCode == RESULT_OK) {
                    startActivity(OpenFileHelper.getIntent(mContext,
                            PreferencesUtils.getString(mContext, Constant.PREF_DOWNLOAD_APK_PATH)));
                } else {
                    showAlertDialog(R.string.request_install_apk_fail);
                }
                break;
            case REQUEST_CODE_SCAN:
                toScan();
                break;
            case REQUEST_CODE_SCAN_RESULT:
                if (resultCode == RESULT_OK) {
                    String data = intent.getStringExtra(Constant.EXTRA_DATA);
                    Log.d(Constant.TAG, mClassName + " scan result : " + data);
                    data = TextHelper.handleJSFunctionParams(data);
                    Log.d(Constant.TAG, mClassName + " scan handle result : " + data);
                    mWebView.evaluateJavascript("javascript:scanResult('" + data + "')", null);
                    //mWebView.evaluateJavascript("javascript:scanResult(\"" + data + "\")", null);
                }
                break;
            case REQUEST_CODE_PHOTO_UPLOAD:
                toOpenFileChooserAnother();
                break;
            case REQUEST_CODE_LOCATION:
                toRequestLocationPermission();
                break;
            default:
                super.onActivityResult(requestCode, resultCode, intent);
                break;
        }
    }

    /**
     * HTML页面的JS调用Android本地的方法
     */
    private class JavascriptObject {

        /**
         * 退出
         */
        @JavascriptInterface
        public void exit() {
            finish();
        }

        /**
         * 打开新的WebView来加载网页
         *
         * @param url 网址
         */
        @JavascriptInterface
        public void toNewWebView(String url) {
            actionStart(mContext, url);
        }

        /**
         * 调用手机浏览器加载网页
         *
         * @param url 网址
         */
        @JavascriptInterface
        public void openAppBrowser(String url) {
            IntentHelper.openBrowser(mContext, url);
        }

        /**
         * 扫码
         */
        @JavascriptInterface
        public void scanCode() {
            toScan();
        }

    }

}

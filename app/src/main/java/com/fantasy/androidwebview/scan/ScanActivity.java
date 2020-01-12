package com.fantasy.androidwebview.scan;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.fantasy.androidwebview.Constant;
import com.fantasy.androidwebview.R;
import com.fantasy.androidwebview.base.BaseActivity;
import com.fantasy.androidwebview.utils.ConvertUtils;

import cn.bingoogolapple.qrcode.core.QRCodeView;
import cn.bingoogolapple.qrcode.zxing.ZXingView;

/**
 * 扫码
 * <pre>
 *     author  : Fantasy
 *     version : 1.0, 2020-01-08
 *     since   : 1.0, 2020-01-08
 * </pre>
 */
public class ScanActivity extends BaseActivity implements QRCodeView.Delegate {
    private ZXingView mZXingView;
    private ImageView mIvBack;
    /**
     * 是否返回结果
     */
    private boolean mIsReturnResult;

    /**
     * 打开扫码，扫码结果在“扫码结果页”显示
     *
     * @param context 上下文
     */
    public static void actionStart(Context context) {
        Intent intent = new Intent(context, ScanActivity.class);
        intent.putExtra("isReturnResult", false);
        context.startActivity(intent);
    }

    /**
     * 打开扫码，并且返回扫码结果
     *
     * @param activity    活动
     * @param requestCode 请求码
     */
    public static void actionStart(Activity activity, int requestCode) {
        Intent intent = new Intent(activity, ScanActivity.class);
        intent.putExtra("isReturnResult", true);
        activity.startActivityForResult(intent, requestCode);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);

        // 透明状态栏
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            View decorView = getWindow().getDecorView();
            int option = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
            decorView.setSystemUiVisibility(option);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }

        bindEvent();
        initData();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mZXingView.startCamera();
        mZXingView.startSpotAndShowRect();
    }

    @Override
    protected void onStop() {
        mZXingView.stopCamera();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        mZXingView.onDestroy();
        super.onDestroy();
    }

    private void bindEvent() {
        mZXingView = findViewById(R.id.zxingview);
        mIvBack = findViewById(R.id.iv_scan_back);

        mIvBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        ((CheckBox) findViewById(R.id.cb_scan_light)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    mZXingView.openFlashlight();
                } else {
                    mZXingView.closeFlashlight();
                }
            }
        });
    }

    private void initData() {
        mIsReturnResult = getIntent().getBooleanExtra("isReturnResult", false);

        mZXingView.setDelegate(this);
        //mZXingView.getScanBoxView().setOnlyDecodeScanBoxArea(true); // 仅识别扫描框中的码

        // 让返回按钮不被状态栏遮住
        int statusBarHeight = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            statusBarHeight = getResources().getDimensionPixelSize(resourceId);
        }
        if (statusBarHeight <= 0) {
            statusBarHeight = 60;
        }
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) mIvBack.getLayoutParams();
        layoutParams.topMargin = statusBarHeight + ConvertUtils.dpToPx(15);
        mIvBack.setLayoutParams(layoutParams);
    }

    @Override
    public void onScanQRCodeSuccess(String result) {
        Log.d(Constant.TAG, mClassName + " result : " + result);

        Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        if (vibrator != null) {
            vibrator.vibrate(200);
        }

        if (mIsReturnResult) {
            setResult(RESULT_OK, new Intent().putExtra(Constant.EXTRA_DATA, result));
            finish();
        }
    }

    @Override
    public void onCameraAmbientBrightnessChanged(boolean isDark) {
    }

    @Override
    public void onScanQRCodeOpenCameraError() {
        showConfirmDialog(R.string.scan_error, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                finish();
            }
        });
    }

}
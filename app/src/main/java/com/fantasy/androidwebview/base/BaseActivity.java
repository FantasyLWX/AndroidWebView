package com.fantasy.androidwebview.base;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.fantasy.androidwebview.Constant;
import com.fantasy.androidwebview.R;

import java.util.ArrayList;
import java.util.List;

/**
 * 活动基础类
 * <pre>
 *     author  : Fantasy
 *     version : 1.0, 2020-01-08
 *     since   : 1.0, 2020-01-07
 * </pre>
 */
public class BaseActivity extends AppCompatActivity {
    private ProgressDialog mLoadDialog;
    private ProgressDialog mDownloadDialog;
    private PermissionListener mPermissionListener;
    private static final int REQUEST_CODE_PERMISSION = 100;
    protected Context mContext;
    /**
     * 当前类名
     */
    protected String mClassName;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        mClassName = getClass().getSimpleName();
        Log.d(Constant.TAG, mClassName + " onCreate");
    }

    @Override
    protected void onDestroy() {
        Log.d(Constant.TAG, mClassName + " onDestroy");
        super.onDestroy();
    }

    /**
     * 重写getResources()方法，让APP的字体不受系统设置字体大小影响
     */
    @Override
    public Resources getResources() {
        Resources res = super.getResources();
        Configuration config = new Configuration();
        config.setToDefaults();
        res.updateConfiguration(config, res.getDisplayMetrics());
        return res;
    }

    /**
     * 显示加载对话框
     *
     * @param message 提示内容
     */
    protected void showLoadDialog(String message) {
        if (mLoadDialog == null) {
            mLoadDialog = new ProgressDialog(this);
            mLoadDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            mLoadDialog.setCancelable(false);
        }
        mLoadDialog.setMessage(message);
        if (mLoadDialog != null && !mLoadDialog.isShowing()) {
            mLoadDialog.show();
        }
    }

    /**
     * 关闭加载对话框
     */
    protected void hideLoadDialog() {
        if (mLoadDialog != null && mLoadDialog.isShowing()) {
            mLoadDialog.dismiss();
        }
    }

    /**
     * 显示下载对话框
     *
     * @param title    标题
     * @param progress 进度
     */
    protected void showDownloadDialog(int title, int progress) {
        if (mDownloadDialog == null) {
            mDownloadDialog = new ProgressDialog(this);
            mDownloadDialog.setMax(100);
            mDownloadDialog.setTitle(title);
            mDownloadDialog.setCancelable(false);
            mDownloadDialog.setProgressPercentFormat(null); // 不显示百分比
            mDownloadDialog.setProgressStyle(android.app.ProgressDialog.STYLE_HORIZONTAL);
        }
        mDownloadDialog.setProgress(progress);
        if (mDownloadDialog != null && !mDownloadDialog.isShowing()) {
            mDownloadDialog.show();
        }
    }

    /**
     * 显示下载对话框
     *
     * @param title    标题
     * @param progress 进度
     * @param listener “取消下载”的监听器
     */
    protected void showDownloadDialog(int title, int progress, DialogInterface.OnClickListener listener) {
        if (mDownloadDialog == null) {
            mDownloadDialog = new ProgressDialog(this);
            mDownloadDialog.setMax(100);
            mDownloadDialog.setTitle(title);
            mDownloadDialog.setCancelable(false);
            mDownloadDialog.setProgressPercentFormat(null); // 不显示百分比
            mDownloadDialog.setProgressStyle(android.app.ProgressDialog.STYLE_HORIZONTAL);
            mDownloadDialog.setButton(DialogInterface.BUTTON_POSITIVE,
                    getString(R.string.btn_cancel_download), listener);
        }
        mDownloadDialog.setProgress(progress);
        if (mDownloadDialog != null && !mDownloadDialog.isShowing()) {
            mDownloadDialog.show();
        }
    }

    /**
     * 关闭下载对话框
     */
    protected void hideDownloadDialog() {
        if (!this.isFinishing() && mDownloadDialog != null && mDownloadDialog.isShowing()) {
            mDownloadDialog.setProgress(0);
            mDownloadDialog.cancel();
        }
    }

    /**
     * 短时间的Toast
     *
     * @param message 提示内容
     */
    protected void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    /**
     * 短时间的Toast
     *
     * @param message 提示内容
     */
    protected void showToast(int message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    /**
     * 长时间的Toast
     *
     * @param message 提示内容
     */
    protected void showLongToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    /**
     * 长时间的Toast
     *
     * @param message 提示内容
     */
    protected void showLongToast(int message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    /**
     * 提示对话框，带有“确定”按钮，而且setCancelable(false)
     *
     * @param message  提示内容
     * @param listener “确定”按钮的点击监听器
     */
    protected void showConfirmDialog(String message, DialogInterface.OnClickListener listener) {
        AlertDialog dialog = new AlertDialog.Builder(this).setCancelable(false)
                .setMessage(message).setPositiveButton(R.string.btn_confirm, listener).create();
        dialog.show();
    }

    /**
     * 提示对话框，带有“确定”按钮，而且setCancelable(false)
     *
     * @param message  提示内容
     * @param listener “确定”按钮的点击监听器
     */
    protected void showConfirmDialog(int message, DialogInterface.OnClickListener listener) {
        AlertDialog dialog = new AlertDialog.Builder(this).setCancelable(false)
                .setMessage(message).setPositiveButton(R.string.btn_confirm, listener).create();
        dialog.show();
    }

    /**
     * 提示对话框，带有“确定”按钮
     *
     * @param message 提示内容
     */
    protected void showAlertDialog(String message) {
        AlertDialog dialog = new AlertDialog.Builder(this).setMessage(message)
                .setPositiveButton(R.string.btn_confirm, null).create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }

    /**
     * 提示对话框，带有“确定”按钮
     *
     * @param message 提示内容
     */
    protected void showAlertDialog(int message) {
        AlertDialog dialog = new AlertDialog.Builder(this).setMessage(message)
                .setPositiveButton(R.string.btn_confirm, null).create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }

    /**
     * 提示对话框，带有“确定”按钮
     *
     * @param title   标题
     * @param message 提示内容
     */
    protected void showAlertDialog(String title, String message) {
        AlertDialog dialog = new AlertDialog.Builder(this).setTitle(title).setMessage(message)
                .setPositiveButton(R.string.btn_confirm, null).create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }

    /**
     * 提示对话框，带有“确定”和“取消”两个按钮
     *
     * @param message  提示内容
     * @param listener “确定”按钮的点击监听器
     */
    protected void showAlertDialog(String message, DialogInterface.OnClickListener listener) {
        AlertDialog dialog = new AlertDialog.Builder(this).setMessage(message)
                .setPositiveButton(R.string.btn_confirm, listener)
                .setNegativeButton(R.string.btn_cancel, null).create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }

    /**
     * 提示对话框，带有“确定”和“取消”两个按钮
     *
     * @param message  提示内容
     * @param listener “确定”按钮的点击监听器
     */
    protected void showAlertDialog(int message, DialogInterface.OnClickListener listener) {
        AlertDialog dialog = new AlertDialog.Builder(this).setMessage(message)
                .setPositiveButton(R.string.btn_confirm, listener)
                .setNegativeButton(R.string.btn_cancel, null).create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }

    /**
     * 提示对话框，带有“确定”和“取消”两个按钮
     *
     * @param title    标题
     * @param message  提示内容
     * @param listener “确定”按钮的点击监听器
     */
    protected void showAlertDialog(String title, String message, DialogInterface.OnClickListener listener) {
        AlertDialog dialog = new AlertDialog.Builder(this).setTitle(title).setMessage(message)
                .setPositiveButton(R.string.btn_confirm, listener)
                .setNegativeButton(R.string.btn_cancel, null).create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }

    /**
     * 提示对话框，带有“确定”和“取消”两个按钮，而且setCancelable(false)
     *
     * @param message          提示内容
     * @param positiveListener “确定”按钮的点击监听器
     * @param negativeListener “取消”按钮的点击监听器
     */
    protected void showAlertDialog(String message, DialogInterface.OnClickListener positiveListener,
                                   DialogInterface.OnClickListener negativeListener) {
        AlertDialog dialog = new AlertDialog.Builder(this).setMessage(message)
                .setPositiveButton(R.string.btn_confirm, positiveListener)
                .setNegativeButton(R.string.btn_cancel, negativeListener).create();
        dialog.setCancelable(false);
        dialog.show();
    }

    /**
     * 提示对话框，带有“确定”和“取消”两个按钮，而且setCancelable(false)
     *
     * @param message          提示内容
     * @param positiveListener “确定”按钮的点击监听器
     * @param negativeListener “取消”按钮的点击监听器
     */
    protected void showAlertDialog(int message, DialogInterface.OnClickListener positiveListener,
                                   DialogInterface.OnClickListener negativeListener) {
        AlertDialog dialog = new AlertDialog.Builder(this).setMessage(message)
                .setPositiveButton(R.string.btn_confirm, positiveListener)
                .setNegativeButton(R.string.btn_cancel, negativeListener).create();
        dialog.setCancelable(false);
        dialog.show();
    }

    /**
     * 申请运行时权限
     *
     * @param permissions 待申请的权限组
     * @param listener    回调监听器
     */
    protected void requestPermissions(String[] permissions, PermissionListener listener) {
        mPermissionListener = listener;
        List<String> deniedList = new ArrayList<>();

        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_DENIED) {
                deniedList.add(permission);
            }
        }

        if (deniedList.isEmpty()) {
            listener.onGranted();
        } else {
            ActivityCompat.requestPermissions(this,
                    deniedList.toArray(new String[deniedList.size()]), REQUEST_CODE_PERMISSION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_PERMISSION) {
            if (grantResults.length > 0) {
                List<String> deniedList = new ArrayList<>();
                List<String> grantedList = new ArrayList<>();

                for (int i = 0; i < grantResults.length; i++) {
                    int grantResult = grantResults[i];
                    if (grantResult != PackageManager.PERMISSION_GRANTED) {
                        deniedList.add(permissions[i]);
                    } else {
                        grantedList.add(permissions[i]);
                    }
                }

                if (deniedList.isEmpty()) {
                    mPermissionListener.onGranted();
                } else {
                    mPermissionListener.onDenied(deniedList);
                    mPermissionListener.onGranted(grantedList);
                }
            }
        }
    }

}

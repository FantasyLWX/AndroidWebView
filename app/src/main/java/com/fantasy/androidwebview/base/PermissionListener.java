package com.fantasy.androidwebview.base;

import java.util.List;

/**
 * 申请运行时权限的回调监听器
 * <pre>
 *     author  : Fantasy
 *     version : 1.0, 2020-01-07
 *     since   : 1.0, 2020-01-07
 * </pre>
 */
public interface PermissionListener {
    /**
     * 全部授权成功
     */
    void onGranted();

    /**
     * 部分授权成功
     *
     * @param grantedList 已授权成功的权限列表
     */
    void onGranted(List<String> grantedList);

    /**
     * 拒绝授权
     *
     * @param deniedList 未授权成功的权限列表
     */
    void onDenied(List<String> deniedList);
}

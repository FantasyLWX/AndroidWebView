package com.fantasy.androidwebview.utils;

import android.content.Context;

import java.io.File;

/**
 * APP文件目录的工具类，用于存放安装包、下载文件、图片等等
 * <pre>
 *     author  : Fantasy
 *     version : 1.0, 2020-01-07
 *     since   : 1.0, 2020-01-07
 * </pre>
 */
public class DirUtils {
    /**
     * 根目录
     */
    private static String mPathRoot;
    /**
     * 缓存目录
     */
    private static String mPathCache;
    /**
     * 下载文件目录
     */
    private static String mPathDownload;
    /**
     * 日志目录
     */
    private static String mPathLogs;
    /**
     * 图片目录
     */
    private static String mPathPictures;
    /**
     * 更新文件目录
     */
    private static String mPathUpdate;

    /**
     * 初始化文件目录
     *
     * @param context 上下文
     */
    public static void init(Context context) {
        File fileRoot = context.getExternalFilesDir("");
        File fileCache = context.getExternalFilesDir("cache");
        File fileDownload = context.getExternalFilesDir("download");
        File fileLogs = context.getExternalFilesDir("logs");
        File filePictures = context.getExternalFilesDir("pictures");
        File fileUpdate = context.getExternalFilesDir("update");

        if (fileRoot != null) {
            mPathRoot = fileRoot.getPath();
        }
        if (fileCache != null) {
            mPathCache = fileCache.getPath();
        }
        if (fileDownload != null) {
            mPathDownload = fileDownload.getPath();
        }
        if (fileLogs != null) {
            mPathLogs = fileLogs.getPath();
        }
        if (filePictures != null) {
            mPathPictures = filePictures.getPath();
        }
        if (fileUpdate != null) {
            mPathUpdate = fileUpdate.getPath();
        }
    }

    /**
     * 获取根目录
     *
     * @return /storage/emulated/0/Android/data/package name/files
     */
    public static String getRoot() {
        return mPathRoot;
    }

    /**
     * 获取缓存目录
     *
     * @return /storage/emulated/0/Android/data/package name/files/cache
     */
    public static String getCache() {
        return mPathCache;
    }

    /**
     * 获取下载文件目录
     *
     * @return /storage/emulated/0/Android/data/package name/files/download
     */
    public static String getDownload() {
        return mPathDownload;
    }

    /**
     * 获取日志目录
     *
     * @return /storage/emulated/0/Android/data/package name/files/logs
     */
    public static String getLogs() {
        return mPathLogs;
    }

    /**
     * 获取图片目录
     *
     * @return /storage/emulated/0/Android/data/package name/files/pictures
     */
    public static String getPictures() {
        return mPathPictures;
    }

    /**
     * 获取更新文件目录
     *
     * @return /storage/emulated/0/Android/data/package name/files/update
     */
    public static String getUpdate() {
        return mPathUpdate;
    }

}

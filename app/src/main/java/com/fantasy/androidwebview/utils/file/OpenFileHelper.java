package com.fantasy.androidwebview.utils.file;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.v4.content.FileProvider;

import java.io.File;
import java.util.Locale;

/**
 * 打开文件帮助类
 * <pre>
 *     author  : Fantasy
 *     version : 1.0, 2020-01-08
 *     since   : 1.0, 2020-01-08
 * </pre>
 */
public class OpenFileHelper {

    /**
     * 获取打开文件的intent
     *
     * @param filePath 文件的路径
     * @return intent
     */
    public static Intent getIntent(Context context, String filePath) {
        File file = new File(filePath);
        if (!file.exists()) {
            return null;
        }

        // 取得扩展名
        String fileName = file.getName();
        String end = fileName.substring(fileName.lastIndexOf(".") + 1, fileName.length());
        end = end.toLowerCase(Locale.ENGLISH);

        // 依扩展名的类型决定MimeType
        switch (end) {
            case "apk":
                return getApkFileIntent(context, filePath);
            case "ppt":
                return getPptFileIntent(context, filePath);
            case "pptx":
                return getPptxFileIntent(context, filePath);
            case "xls":
                return getExcelFileIntent(context, filePath);
            case "xlsx":
                return getXlsxFileIntent(context, filePath);
            case "doc":
                return getWordFileIntent(context, filePath);
            case "docx":
                return getDocxFileIntent(context, filePath);
            case "pdf":
                return getPdfFileIntent(context, filePath);
            case "chm":
                return getChmFileIntent(context, filePath);
            case "txt":
                return getTextFileIntent(context, filePath, true);
            case "html":
                return getHtmlFileIntent(context, filePath);
            case "m4a":
            case "mp3":
            case "mid":
            case "xmf":
            case "ogg":
            case "wav":
                return getAudioFileIntent(context, filePath);
            case "jpg":
            case "gif":
            case "png":
            case "jpeg":
            case "bmp":
                return getImageFileIntent(context, filePath);
            case "3gp":
            case "rmvb":
            case "mkv":
            case "mp4":
                return getVideoFileIntent(context, filePath);
            default:
                return getAllIntent(context, filePath);
        }
    }

    /**
     * 获取一个用于打开文件的intent
     *
     * @param context  上下文
     * @param filePath 文件的路径
     */
    public static Intent getAllIntent(Context context, String filePath) {
        Intent intent = new Intent();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            // 添加这一句表示对目标应用临时授权该Uri所代表的文件
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setAction(Intent.ACTION_VIEW);
        Uri uri = getUriForFile(context, new File(filePath));
        intent.setDataAndType(uri, "*/*");
        return intent;
    }

    /**
     * 获取一个用于打开APK文件的intent
     *
     * @param context  上下文
     * @param filePath 文件的路径
     */
    public static Intent getApkFileIntent(Context context, String filePath) {
        Intent intent = new Intent();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setAction(Intent.ACTION_VIEW);
        Uri uri = getUriForFile(context, new File(filePath));
        intent.setDataAndType(uri, "application/vnd.android.package-archive");
        return intent;
    }

    /**
     * 获取一个用于打开AUDIO文件的intent
     *
     * @param context  上下文
     * @param filePath 文件的路径
     */
    public static Intent getAudioFileIntent(Context context, String filePath) {
        Intent intent = new Intent("android.intent.action.VIEW");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("oneshot", 0);
        intent.putExtra("configchange", 0);
        Uri uri = getUriForFile(context, new File(filePath));
        intent.setDataAndType(uri, "audio/*");
        return intent;
    }

    /**
     * 获取一个用于打开VIDEO文件的intent
     *
     * @param context  上下文
     * @param filePath 文件的路径
     */
    public static Intent getVideoFileIntent(Context context, String filePath) {
        Intent intent = new Intent("android.intent.action.VIEW");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("oneshot", 0);
        intent.putExtra("configchange", 0);
        Uri uri = getUriForFile(context, new File(filePath));
        intent.setDataAndType(uri, "video/*");
        return intent;
    }

    /**
     * 获取一个用于打开HTML文件的intent
     *
     * @param context  上下文
     * @param filePath 文件的路径
     */
    public static Intent getHtmlFileIntent(Context context, String filePath) {
        Intent intent = new Intent("android.intent.action.VIEW");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        }
        Uri uri = getUriForFile(context, new File(filePath)).buildUpon()
                .encodedAuthority("com.android.htmlfileprovider")
                .scheme("content").encodedPath(filePath).build();
        intent.setDataAndType(uri, "text/html");
        return intent;
    }

    /**
     * 获取一个用于打开图片文件的intent
     *
     * @param context  上下文
     * @param filePath 文件的路径
     */
    public static Intent getImageFileIntent(Context context, String filePath) {
        Intent intent = new Intent("android.intent.action.VIEW");
        intent.addCategory("android.intent.category.DEFAULT");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        }
        Uri uri = getUriForFile(context, new File(filePath));
        intent.setDataAndType(uri, "image/*");
        return intent;
    }

    /**
     * 获取一个用于打开PPT文件的intent
     *
     * @param context  上下文
     * @param filePath 文件的路径
     */
    public static Intent getPptFileIntent(Context context, String filePath) {
        Intent intent = new Intent("android.intent.action.VIEW");
        intent.addCategory("android.intent.category.DEFAULT");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        }
        Uri uri = getUriForFile(context, new File(filePath));
        intent.setDataAndType(uri, "application/vnd.ms-powerpoint");
        return intent;
    }

    /**
     * 获取一个用于打开PPTX文件的intent
     *
     * @param context  上下文
     * @param filePath 文件的路径
     */
    public static Intent getPptxFileIntent(Context context, String filePath) {
        Intent intent = new Intent("android.intent.action.VIEW");
        intent.addCategory("android.intent.category.DEFAULT");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        }
        Uri uri = getUriForFile(context, new File(filePath));
        intent.setDataAndType(uri, "application/vnd.openxmlformats-officedocument.presentationml.presentation");
        return intent;
    }

    /**
     * 获取一个用于打开Excel文件的intent
     *
     * @param context  上下文
     * @param filePath 文件的路径
     */
    public static Intent getExcelFileIntent(Context context, String filePath) {
        Intent intent = new Intent("android.intent.action.VIEW");
        intent.addCategory("android.intent.category.DEFAULT");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        }
        Uri uri = getUriForFile(context, new File(filePath));
        intent.setDataAndType(uri, "application/vnd.ms-excel");
        return intent;
    }

    /**
     * 获取一个用于打开XLSX文件的intent
     *
     * @param context  上下文
     * @param filePath 文件的路径
     */
    public static Intent getXlsxFileIntent(Context context, String filePath) {
        Intent intent = new Intent("android.intent.action.VIEW");
        intent.addCategory("android.intent.category.DEFAULT");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        }
        Uri uri = getUriForFile(context, new File(filePath));
        intent.setDataAndType(uri, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        return intent;
    }

    /**
     * 获取一个用于打开Word文件的intent
     *
     * @param context  上下文
     * @param filePath 文件的路径
     */
    public static Intent getWordFileIntent(Context context, String filePath) {
        Intent intent = new Intent("android.intent.action.VIEW");
        intent.addCategory("android.intent.category.DEFAULT");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        }
        Uri uri = getUriForFile(context, new File(filePath));
        intent.setDataAndType(uri, "application/msword");
        return intent;
    }

    /**
     * 获取一个用于打开DOCX文件的intent
     *
     * @param context  上下文
     * @param filePath 文件的路径
     */
    public static Intent getDocxFileIntent(Context context, String filePath) {
        Intent intent = new Intent("android.intent.action.VIEW");
        intent.addCategory("android.intent.category.DEFAULT");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        }
        Uri uri = getUriForFile(context, new File(filePath));
        intent.setDataAndType(uri, "application/vnd.openxmlformats-officedocument.wordprocessingml.document");
        return intent;
    }

    /**
     * 获取一个用于打开CHM文件的intent
     *
     * @param context  上下文
     * @param filePath 文件的路径
     */
    public static Intent getChmFileIntent(Context context, String filePath) {
        Intent intent = new Intent("android.intent.action.VIEW");
        intent.addCategory("android.intent.category.DEFAULT");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        }
        Uri uri = getUriForFile(context, new File(filePath));
        intent.setDataAndType(uri, "application/x-chm");
        return intent;
    }

    /**
     * 获取一个用于打开PDF文件的intent
     *
     * @param context  上下文
     * @param filePath 文件的路径
     */
    public static Intent getPdfFileIntent(Context context, String filePath) {
        Intent intent = new Intent("android.intent.action.VIEW");
        intent.addCategory("android.intent.category.DEFAULT");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        }
        Uri uri = getUriForFile(context, new File(filePath));
        intent.setDataAndType(uri, "application/pdf");
        return intent;
    }

    /**
     * 获取一个用于打开文本文件的intent
     *
     * @param context      上下文
     * @param filePath     文件的路径
     * @param paramBoolean 是否打开本地文件
     */
    public static Intent getTextFileIntent(Context context, String filePath, boolean paramBoolean) {
        Intent intent = new Intent("android.intent.action.VIEW");
        intent.addCategory("android.intent.category.DEFAULT");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        }
        if (paramBoolean) {
            Uri uri1 = getUriForFile(context, new File(filePath));
            intent.setDataAndType(uri1, "text/plain");
        } else {
            Uri uri2 = Uri.parse(filePath);
            intent.setDataAndType(uri2, "text/plain");
        }
        return intent;
    }

    private static Uri getUriForFile(Context context, File file) {
        if (context == null || file == null) {
            throw new NullPointerException();
        }
        Uri uri;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            uri = FileProvider.getUriForFile(context.getApplicationContext(),
                    context.getApplicationContext().getPackageName() + ".provider", file);
        } else {
            uri = Uri.fromFile(file);
        }
        return uri;
    }

}

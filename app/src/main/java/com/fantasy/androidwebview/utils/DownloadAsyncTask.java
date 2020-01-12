package com.fantasy.androidwebview.utils;

import android.app.ActivityManager;
import android.content.Context;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;

import com.fantasy.androidwebview.Constant;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLDecoder;

/**
 * 下载工具类
 * <pre>
 *     author  : Fantasy
 *     version : 1.0, 2020-01-07
 *     since   : 1.0, 2020-01-07
 * </pre>
 */
public class DownloadAsyncTask extends AsyncTask<String, Integer, Integer> {
    /**
     * 下载成功
     */
    private static final int SUCCESS = 0;
    /**
     * 网络连接异常
     */
    private static final int ERROR_NETWORK = 1;
    /**
     * 网络连接超时
     */
    private static final int ERROR_TIME_OUT = 2;
    /**
     * 服务响应失败
     */
    private static final int ERROR_RESPONSE = 3;
    /**
     * 文件处理异常
     */
    private static final int ERROR_IO = 4;
    /**
     * 存储文件失败
     */
    private static final int ERROR_FILE_NOT_FOUND = 5;
    /**
     * 存储空间不足
     */
    private static final int ERROR_SPACE = 6;
    /**
     * 无效的下载地址
     */
    private static final int ERROR_URL = 7;
    /**
     * 下载的文件不存在
     */
    private static final int ERROR_SOURCE = 8;

    private Context mContext = null;
    /**
     * 回调监听器
     */
    private CallbackListener mListener = null;
    /**
     * 下载的文件大小
     */
    private long mFileSize = 0;
    /**
     * 已下载成功的文件的绝对路径
     */
    private String mFilePath = "";
    /**
     * 是否取消下载
     */
    private boolean sIsCancel = false;

    private DownloadAsyncTask(Context context, CallbackListener listener) {
        super();
        mContext = context;
        mListener = listener;
    }

    @Override
    protected void onPreExecute() {
        mListener.onProgress(0); // 进度条初始化
        Log.d(Constant.TAG, "DownloadAsyncTask onPreExecute");
    }

    @Override
    protected Integer doInBackground(String... params) {
        Log.d(Constant.TAG, "DownloadAsyncTask doInBackground");
        HttpURLConnection connection = null;
        int code;
        InputStream is = null;
        FileOutputStream fos = null;

        try {
            String downloadUrl = params[0];
            String path = params[1];
            String fileName = params[2];

            Log.d(Constant.TAG, "DownloadAsyncTask url : " + downloadUrl);

            connection = (HttpURLConnection) new URL(downloadUrl).openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(8 * 1000);
            connection.setReadTimeout(8 * 1000);
            // 要取得长度则，要求HTTP请求不要gzip压缩，具体设置如下，要不然出现length=-1的情况
            connection.setRequestProperty("Accept-Encoding", "identity");
            connection.setRequestProperty("Connection", "Keep-Alive");
            connection.connect();
            code = connection.getResponseCode();

            Log.d(Constant.TAG, "DownloadAsyncTask code : " + code);

            if (code == 200) {
                if (TextUtils.isEmpty(fileName)) { // 未设置文件名，则动态获取
                    // 通过Content-Disposition获取文件名，这点跟服务器有关，需要灵活变通
                    fileName = connection.getHeaderField("Content-Disposition");
                    if (TextUtils.isEmpty(fileName)) {
                        // 通过截取URL来获取文件名
                        fileName = downloadUrl.substring(downloadUrl.lastIndexOf("/") + 1);
                    } else {
                        fileName = URLDecoder.decode(fileName.substring(
                                fileName.indexOf("filename=") + 9), "UTF-8");
                        // 有些文件名会被包含在""里面，所以要去掉，不然无法读取文件后缀
                        fileName = fileName.replaceAll("\"", "");
                    }
                }
                Log.d(Constant.TAG, "DownloadAsyncTask fileName : " + fileName);

                mFileSize = connection.getContentLength();
                Log.d(Constant.TAG, "DownloadAsyncTask fileSize : " + mFileSize);
                if (isMemoryEnough(mFileSize)) {
                    return ERROR_SPACE;
                }
                if (mFileSize <= 0) {
                    return ERROR_SOURCE;
                }

                mFilePath = path + File.separator + fileName;
                Log.d(Constant.TAG, "DownloadAsyncTask filePath : " + mFilePath);

                File file = new File(mFilePath);
                File parent = file.getParentFile();
                if (!parent.exists()) {
                    parent.mkdirs();
                }
                if (file.exists()) { // 删除已存在的文件
                    file.delete();
                }

                fos = new FileOutputStream(file);

                is = connection.getInputStream();
                byte[] bytes = new byte[1024];
                int temp = -1;
                int alreadyDownloadSize = 0; // 已经下载的大小

                while (true) {
                    if (sIsCancel) {
                        return null;
                    }

                    temp = is.read(bytes);
                    fos.write(bytes, 0, temp);
                    alreadyDownloadSize = alreadyDownloadSize + temp;
                    publishProgress(alreadyDownloadSize);
                    if (alreadyDownloadSize >= mFileSize) {
                        return SUCCESS;
                    }
                }
            } else {
                return ERROR_RESPONSE;
            }
        } catch (MalformedURLException e) {
            Log.d(Constant.TAG, "DownloadAsyncTask MalformedURLException", e);
            return ERROR_URL;
        } catch (SocketTimeoutException e) {
            Log.d(Constant.TAG, "DownloadAsyncTask SocketTimeoutException", e);
            return ERROR_TIME_OUT;
        } catch (ArrayIndexOutOfBoundsException e) {
            Log.d(Constant.TAG, "DownloadAsyncTask ArrayIndexOutOfBoundsException", e);
            return ERROR_IO;
        } catch (FileNotFoundException e) {
            Log.d(Constant.TAG, "DownloadAsyncTask FileNotFoundException", e);
            return ERROR_FILE_NOT_FOUND;
        } catch (Exception e) {
            Log.d(Constant.TAG, "DownloadAsyncTask Exception", e);
            return ERROR_NETWORK;
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
                if (fos != null) {
                    fos.close();
                }
            } catch (IOException e) {
                Log.d(Constant.TAG, "DownloadAsyncTask IOException", e);
            }
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        if (!sIsCancel) {
            double temp = (double) values[0] / mFileSize;
            int progress = (int) (temp * 100);
            mListener.onProgress(progress);
        }
    }

    @Override
    protected void onCancelled(Integer integer) {
        super.onCancelled(integer);
        Log.d(Constant.TAG, "DownloadAsyncTask onCancelled");
    }

    @Override
    protected void onPostExecute(Integer integer) {
        Log.d(Constant.TAG, "DownloadAsyncTask onPostExecute");
        switch (integer) {
            case SUCCESS:
                mListener.onSuccess(mFilePath);
                Log.d(Constant.TAG, "DownloadAsyncTask SUCCESS");
                break;
            case ERROR_NETWORK:
                mListener.onError("网络连接异常，请稍后重试。");
                Log.d(Constant.TAG, "DownloadAsyncTask ERROR_NETWORK");
                break;
            case ERROR_TIME_OUT:
                mListener.onError("网络连接超时，请稍后重试。");
                Log.d(Constant.TAG, "DownloadAsyncTask ERROR_TIME_OUT");
                break;
            case ERROR_RESPONSE:
                mListener.onError("服务响应失败，请稍后重试。");
                Log.d(Constant.TAG, "DownloadAsyncTask ERROR_RESPONSE");
                break;
            case ERROR_IO:
                mListener.onError("文件处理异常，请稍后重试。");
                Log.d(Constant.TAG, "DownloadAsyncTask ERROR_IO");
                break;
            case ERROR_FILE_NOT_FOUND:
                mListener.onError("存储文件失败，请允许应用读写手机储存。");
                Log.d(Constant.TAG, "DownloadAsyncTask ERROR_FILE_NOT_FOUND");
                break;
            case ERROR_SPACE:
                mListener.onError("存储空间不足。");
                Log.d(Constant.TAG, "DownloadAsyncTask ERROR_SPACE");
                break;
            case ERROR_URL:
                mListener.onError("无效的下载地址。");
                Log.d(Constant.TAG, "DownloadAsyncTask ERROR_URL");
                break;
            case ERROR_SOURCE:
                mListener.onError("下载的文件不存在。");
                Log.d(Constant.TAG, "DownloadAsyncTask ERROR_SOURCE");
                break;
            default:
                break;
        }
    }

    /**
     * 执行下载任务
     *
     * @param context  上下文
     * @param url      下载地址
     * @param path     下载的文件所要存放的文件夹路径，以文件夹的名称结尾，例如：/storage/sdcard0/Download
     * @param fileName 下载的文件的名称，包含后缀，例如：demo.doc
     * @param listener 回调监听器
     * @return DownloadAsyncTask的实例
     */
    public static DownloadAsyncTask execute(Context context, String url, String path, String fileName,
                                            CallbackListener listener) {
        DownloadAsyncTask asyncTask = new DownloadAsyncTask(context, listener);
        asyncTask.execute(url, path, fileName);
        return asyncTask;
    }

    /**
     * 执行下载任务，文件名动态获取
     *
     * @param context  上下文
     * @param url      下载地址
     * @param path     下载的文件所要存放的文件夹路径，以文件夹的名称结尾，例如：/storage/sdcard0/Download
     * @param listener 回调监听器
     * @return DownloadAsyncTask的实例
     */
    public static DownloadAsyncTask execute(Context context, String url, String path, CallbackListener listener) {
        DownloadAsyncTask asyncTask = new DownloadAsyncTask(context, listener);
        asyncTask.execute(url, path, null);
        return asyncTask;
    }

    /**
     * 取消下载
     */
    public void cancel() {
        sIsCancel = true;
        cancel(true);
    }

    /**
     * 判断内存是否充足
     *
     * @param fileSize 文件大小
     * @return 充足就返回true，反之返回false
     */
    private boolean isMemoryEnough(long fileSize) {
        ActivityManager am = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
        am.getMemoryInfo(mi);
        return fileSize >= mi.availMem;
    }

    /**
     * 回调监听器
     */
    public interface CallbackListener {
        /**
         * 更新进度
         *
         * @param progress 进度值
         */
        void onProgress(int progress);

        /**
         * 下载完成
         *
         * @param filePath 已下载的文件的绝对路径
         */
        void onSuccess(String filePath);

        /**
         * 下载失败
         *
         * @param message 错误信息
         */
        void onError(String message);
    }

}

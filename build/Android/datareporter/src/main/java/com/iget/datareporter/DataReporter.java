package com.iget.datareporter;

import android.util.Log;

public class DataReporter {

    private static ISoload mSoloadImp;
    private static boolean sIsLoad = false;

    private long mNativeReporter = 0;

    private static void loadLibary() {
        synchronized (DataReporter.class) {
            if (!sIsLoad) {
                boolean isLoadSuccess = false;
                if (mSoloadImp != null) {
                    try {
                        System.loadLibrary("c++_shared");
                        isLoadSuccess = mSoloadImp.loadlibrary("data-reporter");
                        if (!isLoadSuccess) {
                            Log.d("soload:", "net-dispatcher load error!");
                        }
                    } catch (UnsatisfiedLinkError e) {
                        Log.d("soload:", e.getMessage());
                    }
                }

                if (isLoadSuccess) {
                    sIsLoad = true;
                    return;
                }

                try {
                    System.loadLibrary("c++_shared");
                    System.loadLibrary("data-reporter");
                } catch (UnsatisfiedLinkError e) {
                    Log.d("soload:", e.getMessage());
                }
                sIsLoad = true;
            }
        }
    }

    public static void setSoLoadImp(ISoload soLoadImp) {
        mSoloadImp = soLoadImp;
    }

    public synchronized static DataReporter makeDataReporter(String uuid, String cachePath, String encryptKey, IReport reportImp) {
        return new DataReporter(uuid, cachePath, encryptKey, reportImp);
    }

    public static void releaseDataReporter(DataReporter dataReporter) {
        dataReporter.release();
    }

    public void setReportCount(int count) {
        setReportCount(mNativeReporter, count);
    }

    public void setFileMaxSize(int fileMaxSize) {
        setFileMaxSize(mNativeReporter, fileMaxSize);
    }

    public void setExpiredTime(long expiredTime) {
        setExpiredTime(mNativeReporter, expiredTime);
    }

    public void setReportingInterval(long reportingInterval) {
        setReportingInterval(mNativeReporter, reportingInterval);
    }

    public void start() {
        start(mNativeReporter);
    }

    public void reaWaken() {
        reaWaken(mNativeReporter);
    }

    public void push(byte[] data) {
        push(mNativeReporter, data);
    }

    public void uploadSucess(long key) {
        uploadSucess(mNativeReporter, key);
    }

    public void uploadFailed(long key) {
        uploadFailed(mNativeReporter, key);
    }

    private DataReporter(String uuid, String cachePath, String encryptKey, IReport reportImp) {
        loadLibary();
        mNativeReporter = makeReporter(uuid, cachePath, encryptKey, reportImp);
    }

    private void release() {
        releaseReporter(mNativeReporter);
    }

    private static void upload(long key, byte[][] data, IReport report) {
        if (report != null) {
            report.upload(key, data);
        }
    }

    public static int getVersionCode() {
        loadLibary();
        return getVersion();
    }

    /**
     * 创建Reporter实例，实例是native的，保存在返回的int值中
     *
     * @param uuid       业务唯一id,由使用者定义一个唯一id
     * @param cachePath  缓存路径，必须全局唯一，不同业务不同路径
     * @param reportImp  上报实现接口
     * @param encryptKey 缓存加密key
     * @return 返回实例指针
     */
    private static native long makeReporter(String uuid, String cachePath, String encryptKey, IReport reportImp);

    /**
     * 设置单次上报的数据条数，在start之前调用
     *
     * @param nativeReporter 由makeReporter返回的值
     * @param count          一次上报的数据条数
     */
    private static native void setReportCount(long nativeReporter, int count);

    /**
     * 设置缓存文件单文件最大大小限制，默认值为10k
     *
     * @param nativeReporter 由makeReporter返回的值
     * @param fileMaxSize    缓存文件最大大小
     */
    private static native void setFileMaxSize(long nativeReporter, int fileMaxSize);

    /**
     * 设置缓存文件有效期
     *
     * @param nativeReporter 由makeReporter返回的值
     * @param expiredTime    数据有效期
     */
    private static native void setExpiredTime(long nativeReporter, long expiredTime);

    /**
     * 设置上报间隔
     *
     * @param nativeReporter    由makeReporter返回的值
     * @param reportingInterval 上报间隔
     */
    private static native void setReportingInterval(long nativeReporter, long reportingInterval);

    /**
     * 开始上报线程 设置完相关参数之后，执行一次。
     *
     * @param nativeReporter 由makeReporter返回的值
     */
    private static native void start(long nativeReporter);

    /**
     * 唤醒上报，在网络状态转好时调用或者其他想唤醒场景调用
     *
     * @param nativeReporter 由makeReporter返回的值
     */
    private static native void reaWaken(long nativeReporter);


    /**
     * 数据上报调用接口
     *
     * @param nativeReporter 由makeReporter返回的值
     * @param data           需要上报的数据
     */
    private static native void push(long nativeReporter, byte[] data);


    /**
     * 上报成功后调用该方法
     *
     * @param nativeReporter 由makeReporter返回的值
     * @param key            由上报实现接口返回的值
     */
    private static native void uploadSucess(long nativeReporter, long key);

    /**
     * 上报失败后调用该方法
     *
     * @param nativeReporter 由makeReporter返回的值
     * @param key            由上报实现接口返回的值
     */
    private static native void uploadFailed(long nativeReporter, long key);


    /**
     * 释放上报实例，释放后其他方法都不可以调用
     *
     * @param nativeReporter 由makeReporter返回的值
     */
    private static native void releaseReporter(long nativeReporter);

    public static native int getVersion();
}

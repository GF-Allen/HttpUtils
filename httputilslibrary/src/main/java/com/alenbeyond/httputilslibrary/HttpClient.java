package com.alenbeyond.httputilslibrary;

import android.os.Handler;
import android.os.Message;

import com.alenbeyond.httputilslibrary.interfaces.ICallbackWith3MObject;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by alen on 17/1/12.
 */

public class HttpClient {

    private static final int CORE_POOL_SIZE = 4;
    private static final int CORE_POOL_MAX_SIZE = 8;
    private static final int KEEP_ALIVE_TIME = 10; // 10s

    private static HttpClient mHttpClient;
    private ThreadPoolExecutor mExecutor;
    private Handler mHandler;

    private HttpClient() {
        init();
    }

    private void init() {
        mExecutor = new ThreadPoolExecutor(CORE_POOL_SIZE,
                CORE_POOL_MAX_SIZE,
                KEEP_ALIVE_TIME,
                TimeUnit.SECONDS,
                new LinkedBlockingDeque<Runnable>());
        mHandler = new Handler(){
            @Override
            public void handleMessage(Message msg) {

            }
        };
    }

    public static HttpClient getInstance() {
        if (mHttpClient == null) {
            mHttpClient = new HttpClient();
        }
        return mHttpClient;
    }

    public void doGet(Map<String, String> params, ICallbackWith3MObject callback) {

    }

}

package com.alenbeyond.httputilslibrary.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Created by alen on 17/1/12.
 */

public class Utils {


    /**
     * 是否有网络
     * @param context
     * @return
     */
    public static boolean isOnline(Context context) {
        ConnectivityManager manager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = manager.getActiveNetworkInfo();
        return (info != null && info.isConnectedOrConnecting());
    }
}

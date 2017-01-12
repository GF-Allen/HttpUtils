package com.alenbeyond.httputilslibrary.interfaces;

import com.alenbeyond.httputilslibrary.http.HttpResponse;

/**
 * Created by alen on 17/1/12.
 */

public interface ICallbackWith3MObject {
    void onSuccess(Object result, HttpResponse response);

    void onFail(String reason,HttpResponse response);

    void onNetUnAvailable();
}

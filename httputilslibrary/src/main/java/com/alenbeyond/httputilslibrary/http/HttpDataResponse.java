package com.alenbeyond.httputilslibrary.http;

import java.util.List;
import java.util.Map;

public class HttpDataResponse extends HttpResponse {
    private Object data;

    public HttpDataResponse(Object data, int code, Map<String, List<String>> headers) {
        super(code, headers);
        this.data = data;
    }

    public Object getData() {
        return data;
    }
}

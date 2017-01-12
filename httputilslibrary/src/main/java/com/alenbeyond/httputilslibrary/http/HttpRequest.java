package com.alenbeyond.httputilslibrary.http;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.alenbeyond.httputilslibrary.interfaces.ICallbackWith3MObject;
import com.alenbeyond.httputilslibrary.utils.Utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.Proxy;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class HttpRequest {
    private static final String TAG = "HttpUtils";
    private static final int DEFAULT_TIMEOUT = 60000;
    private Proxy proxy = Proxy.NO_PROXY;
    private int timeout = DEFAULT_TIMEOUT;
    private String contentType;

    private Map<String, String> headers = new HashMap<String, String>();
    private Object data;

    private Context context;
    private URL url;
    private String method;

    private ICallbackWith3MObject callback;

    public HttpRequest(Context context, URL url, String method, ICallbackWith3MObject callback) {
        this.context = context;
        this.url = url;
        this.method = method;
        this.callback = callback;
    }

    public HttpRequest data(Object data) {
        this.data = data;
        return this;
    }

    public HttpRequest header(String key, String value) {
        headers.put(key, value);
        return this;
    }

    public HttpRequest contentType(String value) {
        contentType = value;
        return this;
    }

    public HttpRequest timeout(int timeout) {
        this.timeout = timeout;
        return this;
    }

    public HttpRequest proxy(Proxy proxy) {
        this.proxy = proxy;
        return this;
    }

    public void send() {
        if (!Utils.isOnline(context)) {
            callback.onNetUnAvailable();
        }
        new AsyncTask<Void, Void, Action>() {
            @Override
            protected Action doInBackground(Void... params) {
                HttpURLConnection connection = null;
                try {
                    connection = (HttpURLConnection) url.openConnection(proxy);
                    init(connection);
                    sendData(connection);
                    final HttpDataResponse response = readData(connection);
                    return new Action() {
                        @Override
                        public void call() {
                            if (response.getCode() < 400)
                                callback.onSuccess(response.getData(), response);
                            else {
                                callback.onFail((String) response.getData(), response);
                            }
                        }
                    };
                } catch (SocketTimeoutException e) {
                    Log.e(TAG, e.getMessage());
                    callback.onFail(e.getMessage(), null);
                } catch (ProtocolException e) {
                    Log.wtf(TAG, e.getMessage());
                    callback.onFail(e.getMessage(), null);
                } catch (Throwable e) {
                    Log.wtf(TAG, e);
                    callback.onFail(e.getMessage(), null);
                } finally {
                    if (connection != null)
                        connection.disconnect();
                }
                return null;
            }

            @Override
            protected void onPostExecute(Action action) {
                if (action != null) {
                    action.call();
                }
            }

        }.execute();
    }

    private HttpDataResponse readData(HttpURLConnection connection) throws IOException {
        int responseCode = getResponseCode(connection);
        if (responseCode >= 500) {
            String response = getString(connection.getErrorStream());
            Log.e(TAG, response);
            return new HttpDataResponse(response, responseCode, connection.getHeaderFields());
        }

        if (responseCode >= 400) {
            return new HttpDataResponse(getString(connection.getErrorStream()), responseCode, connection.getHeaderFields());
        }

        InputStream input = new BufferedInputStream(connection.getInputStream());

        String value = getString(input);
        Log.d(TAG, "RECEIVED: " + value);
        return new HttpDataResponse(value, responseCode, connection.getHeaderFields());
    }

    private int getResponseCode(HttpURLConnection connection) throws IOException {
        try {
            return connection.getResponseCode();
        } catch (IOException e) {
            if (e.getMessage().equals("Received authentication challenge is null"))
                return 401;
            throw e;
        }
    }

    private String getString(InputStream input) throws IOException {
        if (input == null)
            return null;

        StringBuilder builder = new StringBuilder();
        InputStreamReader reader = new InputStreamReader(input, "UTF-8");
        int bytes;
        char[] buffer = new char[64 * 1024];
        while ((bytes = reader.read(buffer)) != -1) {
            builder.append(buffer, 0, bytes);
        }
        return builder.toString();
    }

    private void copyStream(InputStream input, OutputStream output) throws IOException {
        byte[] buffer = new byte[64 * 1024];
        int bytes;
        while ((bytes = input.read(buffer)) != -1) {
            output.write(buffer, 0, bytes);
        }
    }

    private void sendData(HttpURLConnection connection) throws IOException {
        if (data == null)
            return;

        connection.setDoOutput(true);
        OutputStream outputStream = new BufferedOutputStream(connection.getOutputStream());
        try {
            if (data instanceof InputStream) {
                copyStream((InputStream) data, outputStream);
            } else if (data instanceof String) {
                OutputStreamWriter writer = new OutputStreamWriter(outputStream, "UTF-8");
                Log.d(TAG, "SENT: " + data);
                writer.write((String) data);
                writer.flush();
            }
        } finally {
            outputStream.flush();
            outputStream.close();
        }
    }

    private void setContentType(Object data, HttpURLConnection connection) {
        if (headers.containsKey("Content-Type"))
            return;
        if (contentType != null) {
            connection.setRequestProperty("Content-Type", contentType);
            return;
        }

        if (data instanceof InputStream)
            connection.setRequestProperty("Content-Type", "application/octet-stream");
        else
            connection.setRequestProperty("Content-Type", "application/application/json");
    }

    private void init(HttpURLConnection connection) throws ProtocolException {
        connection.setRequestMethod(method);
        connection.setConnectTimeout(timeout);
        connection.setReadTimeout(timeout);
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            connection.setRequestProperty(entry.getKey(), entry.getValue());
        }
        setContentType(data, connection);
    }
}
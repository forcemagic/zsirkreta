package com.speedyblur.shared;

import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.res.TypedArrayUtils;
import android.support.v4.util.ArrayMap;

import com.speedyblur.kretaremastered.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedHashMap;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class HttpHandler {

    private static OkHttpClient httpClient = new OkHttpClient();

    /**
     * Issues a GET request against url. Parses JSON for the callback.
     * @param url the target URL
     * @param callback the callback to return to when the request has been executed
     */
    public static void getJson(String url, JsonRequestCallback callback) {
        getJson(url, new ArrayMap<String, String>(), callback);
    }

    /**
     * Issues a GET request against url with added headers. Parses JSON for the callback.
     * @param url the target URL
     * @param headers ArrayMap of headers
     * @param callback the callback to return to when the request has been executed
     */
    public static void getJson(String url, ArrayMap<String, String> headers, final JsonRequestCallback callback) {
        Request req = buildReq("GET", url, null, headers);
        httpClient.newCall(req).enqueue(new MainCallbackHandler(callback));
    }

    /**
     * Issues a POST request against url. Parses JSON for the callback.
     * @param url the target URL
     * @param payload the payload (request body) to send
     * @param callback the callback to return to when the request has been executed
     */
    public static void postJson(String url, JSONObject payload, JsonRequestCallback callback) {
        postJson(url, payload, new ArrayMap<String, String>(), callback);
    }

    /**
     * Issues a POST request against url with added headers. Parses JSON for the callback.
     * @param url the target URL
     * @param payload the payload (request body) to send
     * @param headers ArrayMap of headers
     * @param callback the callback to return to when the request has been executed
     */
    public static void postJson(String url, JSONObject payload, ArrayMap<String, String> headers, final JsonRequestCallback callback) {
        Request req = buildReq("POST", url, RequestBody.create(MediaType.parse("application/json"), payload.toString()), headers);
        httpClient.newCall(req).enqueue(new MainCallbackHandler(callback));
    }

    private static Request buildReq(String method, String url, @Nullable RequestBody payload, ArrayMap<String, String> headers) {
        Request.Builder reqBuild = new Request.Builder().url(url);
        for (int i=0; i<headers.size(); i++) {
            reqBuild.header(headers.keyAt(i), headers.valueAt(i));
        }

        if (method.equals("GET")) {
            return reqBuild.get().build();
        } else if (method.equals("DELETE")) {
            // DELETE can have a body
            if (payload != null) {
                return reqBuild.delete(payload).build();
            } else {
                return reqBuild.delete().build();
            }
        } else if (payload != null) {
            // POST, PUT and PATCH (should) all have bodies
            return reqBuild.method(method, payload).build();
        } else {
            throw new UnsupportedOperationException("You have specified a request (either POST, PUT or PATCH), which needs a body. (Other request types are not supported.)");
        }
    }

    public interface JsonRequestCallback {
        void onComplete(JSONObject resp);
        void onFailure(String localizedError);
    }

    private static class MainCallbackHandler implements Callback {

        private JsonRequestCallback jsCallback;

        public MainCallbackHandler(JsonRequestCallback callback) {
            this.jsCallback = callback;
        }

        @Override
        public void onFailure(@NonNull Call call, @NonNull IOException e) {
            if (e.getMessage().equals("timeout")) {
                jsCallback.onFailure(Resources.getSystem().getString(R.string.http_timeout));
            } else {
                jsCallback.onFailure(Resources.getSystem().getString(R.string.http_unknown));
            }
        }

        @Override
        public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
            if (response.isSuccessful()) {
                try {
                    jsCallback.onComplete(new JSONObject(response.body().string()));
                } catch (JSONException e) {
                    jsCallback.onFailure(Resources.getSystem().getString(R.string.http_server_error));
                }
            } else {
                if (response.code() == 403) {
                    jsCallback.onFailure(Resources.getSystem().getString(R.string.http_unauthorized));
                } else if (response.code() == 502) {
                    jsCallback.onFailure(Resources.getSystem().getString(R.string.http_bad_gateway));
                } else {
                    jsCallback.onFailure(Resources.getSystem().getString(R.string.http_server_error));
                }
            }
        }
    }
}

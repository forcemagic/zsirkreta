package com.speedyblur.shared;

import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.ArrayMap;
import android.util.Log;

import com.speedyblur.kretaremastered.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

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
        Log.d("HttpHandler", "Dispatching GET "+url);
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
        Log.d("HttpHandler", "Dispatching POST "+url);
        httpClient.newCall(req).enqueue(new MainCallbackHandler(callback));
    }

    private static Request buildReq(String method, String url, @Nullable RequestBody payload, ArrayMap<String, String> headers) {
        Log.d("HttpHandler", String.format("Building request: %s %s", method, url));
        Request.Builder reqBuild = new Request.Builder().url(url);
        for (int i=0; i<headers.size(); i++) {
            reqBuild.header(headers.keyAt(i), headers.valueAt(i));
        }

        if (method.equals("GET")) {
            Log.d("HttpHandler", "This is a GET request. Ignoring payload (if present).");
            return reqBuild.get().build();
        } else if (method.equals("DELETE")) {
            // DELETE can have a body
            if (payload != null) {
                Log.d("HttpHandler", "Request built with payload.");
                return reqBuild.delete(payload).build();
            } else {
                Log.d("HttpHandler", "Request DELETE built without payload.");
                return reqBuild.delete().build();
            }
        } else if (payload != null) {
            Log.d("HttpHandler", "Request built with payload.");
            // POST, PUT and PATCH (should) all have bodies
            return reqBuild.method(method, payload).build();
        } else {
            throw new UnsupportedOperationException("You have specified a request (either POST, PUT or PATCH), which needs a body. (Other request types are not supported.)");
        }
    }

    /**
     * Master callback for a JSON request.
     */
    public interface JsonRequestCallback {
        /**
         * Function is called when a request completes successfully.
         * (The status code must be 200 OK)
         * @param resp the response body (parsed JSON)
         */
        void onComplete(JSONObject resp) throws JSONException;

        /**
         * Function is called when a request fails.
         * @param localizedError the error message (localized)
         */
        void onFailure(final String localizedError);
    }

    private static class MainCallbackHandler implements Callback {

        private JsonRequestCallback jsCallback;

        public MainCallbackHandler(JsonRequestCallback callback) {
            this.jsCallback = callback;
        }

        @Override
        public void onFailure(@NonNull Call call, @NonNull IOException e) {
            Log.e("HttpHandler", "Failed to complete request.");
            if (e.getMessage().equals("timeout")) {
                Log.e("HttpHandler", "Timeout error.");
                jsCallback.onFailure(Resources.getSystem().getString(R.string.http_timeout));
            } else {
                Log.e("HttpHandler", "Unknown error: "+e.getMessage());
                jsCallback.onFailure(Resources.getSystem().getString(R.string.http_unknown));
            }
        }

        @Override
        public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
            Log.d("HttpHandler", "Got response. Parsing...");
            if (response.isSuccessful()) {
                try {
                    Log.d("HttpHandler", "200 OK. Parsing JSON...");
                    jsCallback.onComplete(new JSONObject(response.body().string()));
                    Log.d("HttpHandler", "Successfully parsed JSON.");
                } catch (JSONException e) {
                    Log.e("HttpHandler", "Unable to parse JSON (or tried to get a nonexistent object). Dumping request...");
                    jsCallback.onFailure(Resources.getSystem().getString(R.string.http_server_error));
                    e.printStackTrace();
                }
            } else {
                if (response.code() == 403) {
                    Log.e("HttpHandler", "Got 403 (Forbidden) from server.");
                    jsCallback.onFailure(Resources.getSystem().getString(R.string.http_unauthorized));
                } else if (response.code() == 502) {
                    Log.e("HttpHandler", "Got 502 (Bad Gateway) from server.");
                    jsCallback.onFailure(Resources.getSystem().getString(R.string.http_bad_gateway));
                } else {
                    Log.e("HttpHandler", "Got unknown error code from server: "+response.code());
                    jsCallback.onFailure(Resources.getSystem().getString(R.string.http_server_error));
                }
            }
        }
    }
}

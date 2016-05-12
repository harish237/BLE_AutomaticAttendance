package hanumesh.sjsu.attendancematters;

import android.content.Context;
import android.util.Log;

import com.loopj.android.http.*;

import cz.msebera.android.httpclient.HttpEntity;

public class RestClient {
    private static final String BASE_URL = "http://52.26.47.116:5000/";

    private static SyncHttpClient client = new SyncHttpClient();

    private static AsyncHttpClient asyncClient = new AsyncHttpClient();


    public static void get(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        Log.d("url", getAbsoluteUrl(url));
        client.get(getAbsoluteUrl(url), params, responseHandler);
    }

    public static void post(Context applicationContext, String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        //client.addHeader("Content-Type", "application/json");
        client.post(getAbsoluteUrl(url), params, responseHandler);
    }

    public static void post(Context context, String url, HttpEntity entity, String contentType, ResponseHandlerInterface responseHandler) {
        client.post(context, getAbsoluteUrl(url), entity, contentType, responseHandler);
    }

    public static void put(Context context, String url, cz.msebera.android.httpclient.Header[] headers, cz.msebera.android.httpclient.HttpEntity entity, java.lang.String contentType, ResponseHandlerInterface responseHandler) {
        Log.d("url", getAbsoluteUrl(url));
        client.put(context, getAbsoluteUrl(url), headers, entity, contentType, responseHandler);
    }

    private static String getAbsoluteUrl(String relativeUrl) {
        return BASE_URL + relativeUrl;
    }
}

package com.uleman.apextracker;

import android.content.Context;
import android.view.View;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestHandle;
import com.loopj.android.http.ResponseHandlerInterface;
import com.loopj.android.http.SyncHttpClient;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.HttpEntity;
import cz.msebera.android.httpclient.entity.StringEntity;
import cz.msebera.android.httpclient.entity.mime.MultipartEntityBuilder;


public class HttpClient {

    private Context mCtx;
    private AsyncHttpClient client;
    private JSONObject requestParams;

    private String serverUrl = Connections.addr;
    private String user = Connections.user;
    private String password = Connections.password;

    private MultipartEntityBuilder builder;
    private String pathToFile;



    public HttpClient(Context Ctx) {

        Init(Ctx, false);

    }

    public HttpClient(Context Ctx, String addr, String usr, String pwd) {

        serverUrl = addr;
        user = usr;
        password = pwd;

        Init(Ctx, false);

    }

    public void addHeader(String header, String value){

        client.addHeader(header, value);

    }

    private void Init(Context Ctx, Boolean sync){

        client = sync ? new SyncHttpClient() : new AsyncHttpClient();

        mCtx = Ctx;

        requestParams = new JSONObject();

        String appId = getDbConstant("appId");

//        client.addHeader("Authorization", "Basic " + Base64.getEncoder().encodeToString((user + ":" + password).getBytes(StandardCharsets.UTF_8)));
        client.addHeader("appId", appId);

    }

    public String getDbConstant(String constName) {
        DB db = new DB(mCtx);

        db.open();

        String appId = db.getConstant(constName);

        db.close();
        return appId;
    }


    public RequestHandle request_get(final String url, final HttpRequestInterface httpRequestInterface) {

        return client.get(mCtx, serverUrl + url, null, "application/json", new AsyncHttpResponseHandler(){

            @Override
            public void onStart() {
                super.onStart();

                httpRequestInterface.setProgressVisibility(View.VISIBLE);

            }

            @Override
            public void onFinish() {
                super.onFinish();

                httpRequestInterface.setProgressVisibility(View.GONE);

            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {

                httpRequestInterface.processResponse(getResponseString(responseBody));

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                showMessageOnFailure(statusCode, headers, responseBody, error);
            }
        });

    }

    public RequestHandle request_get(final String url, String methodName, final HttpRequestInterface httpRequestInterface) {

        JSONArray params = new JSONArray();

        JSONObject request = new JSONObject();
        try {
            request.put("request", methodName);
            request.put("parameters", requestParams);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        params.put(request);

        StringEntity entity = new StringEntity(params.toString(), "UTF-8");

        return client.get(mCtx, serverUrl + url, entity, "application/json", new AsyncHttpResponseHandler(){

            @Override
            public void onStart() {
                super.onStart();

                httpRequestInterface.setProgressVisibility(View.VISIBLE);

            }

            @Override
            public void onFinish() {
                super.onFinish();

                httpRequestInterface.setProgressVisibility(View.GONE);

            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {

                httpRequestInterface.processResponse(getResponseString(responseBody));

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                showMessageOnFailure(statusCode, headers, responseBody, error);
            }
        });

    }

    public RequestHandle postBinary(Context context, String url, HttpEntity entity, ResponseHandlerInterface responseHandler) {

        return client.post(context, serverUrl + url, entity, entity.getContentType().toString(), responseHandler);

    }

    public RequestHandle postBinary(String url, HttpEntity entity, HttpRequestInterface httpRequestInterface) {

        return client.post(mCtx, serverUrl + url, entity, entity.getContentType().toString(), new AsyncHttpResponseHandler() {
            @Override
            public void onStart() {
                super.onStart();

                httpRequestInterface.setProgressVisibility(View.VISIBLE);

            }

            @Override
            public void onFinish() {
                super.onFinish();

                httpRequestInterface.setProgressVisibility(View.GONE);

            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {

                httpRequestInterface.processResponse(getResponseString(responseBody));

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                showMessageOnFailure(statusCode, headers, responseBody, error);
            }
        });

    }

    public void showMessageOnFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {

        String message = error.getLocalizedMessage() + ", status code " + String.valueOf(statusCode) + ": " + getResponseString(responseBody);

//                if (debug){

        try {

            Toast toast = Toast.makeText(mCtx, message, Toast.LENGTH_LONG);
            toast.show();
        } catch (Exception e){};


//            }
//                Log.v("HTTPLOG", message);

    }

    public String getResponseString(byte[] responseBody){

        String[] str = new String[1];

        str[0] = null;
        try {
            str[0] = responseBody == null ? "" : new String(responseBody, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return str[0];
    }

    public void addParam(String name, String value){

        try {
            requestParams.put(name, value);
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    public void addParam(String name, int value){

        try {
            requestParams.put(name, value);
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }



}

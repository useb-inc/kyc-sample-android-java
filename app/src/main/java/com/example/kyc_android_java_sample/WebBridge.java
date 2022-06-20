package com.example.kyc_android_java_sample;

import android.os.Handler;
import android.util.Base64;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

public class WebBridge {

    private String TAG = "AdroidBridge";
    private WebView mAppView = null;
    private MainActivity mContext = null;
    private Handler handler = new Handler();

    public WebBridge() {
    }

    @JavascriptInterface
    public void receive(String data) throws UnsupportedEncodingException {

//        Log.d("result data:", data);

        String success = "{\"result\": \"success\"}";
        String failed = "{\"result\": \"failed\"}";
        String complete = "{\"result\": \"complete\"}";
        String close = "{\"result\": \"close\"}";

        String decodedData = decodedReceiveData(data);
        if(decodedData == success)
            Log.d("success", "KYC 작업이 성공했습니다.");
        else if(decodedData == failed)
            Log.d("failed", "KYC 작업이 실패했습니다.");
        else if(decodedData == complete)
            Log.d("complete", "KYC가 완료되었습니다.");
        else if(decodedData == close)
            Log.d("close", "KYC가 완료되지 않았습니다.");
        else
            Log.d("decoding failed", "KYC 응답 메세지 분석에 실패했습니다.");
    }

    private String decodedReceiveData(String data) throws UnsupportedEncodingException {

        String decoded = getBase64decode(data);
        return URLDecoder.decode(decoded, "UTF-8");
    }

    private String getBase64decode(String content){

        return Base64.encodeToString(content.getBytes(), 0);
    }
}

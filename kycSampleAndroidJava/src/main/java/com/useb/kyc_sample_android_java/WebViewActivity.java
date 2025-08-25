package com.useb.kyc_sample_android_java;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Base64;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.PermissionRequest;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;

public class WebViewActivity extends AppCompatActivity {
    WebView webview = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_view);

        WebView.setWebContentsDebuggingEnabled(true);

        webview = findViewById(R.id.webview);
        webview.getSettings().setJavaScriptEnabled(true);
        webview.setWebViewClient(new WebViewClient());
//        webview.setWebChromeClient(new WebChromeClient());
        webview.addJavascriptInterface(this, "alcherakyc");

        // 카메라 프리뷰 성능
        webview.clearCache(true);
        webview.clearHistory();

        String url = "https://kyc.useb.co.kr/auth";
        String param = createParam();

        Handler handler = new Handler();
        handler.post(() -> {
            cameraAuthRequest();
            webview.loadUrl(url);
            webview.setWebViewClient(new WebViewClient() {
                @Override
                public void onPageFinished(WebView view, String url) {
                    webview.loadUrl("javascript:alcherakycreceive('" + param + "')");
                }
            });
        });
    }

    private String createParam() {
        JSONObject paramObj = new JSONObject();
        try {
            paramObj.put("customer_id", "2");
            paramObj.put("id", "demoUser");
            paramObj.put("key", "demoUser0000!");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return encodeJson(paramObj);
    }

    private String encodeJson(JSONObject data) {
        String uriEncodedData = encodeURIComponent(data.toString());
        return Base64.encodeToString(uriEncodedData.getBytes(), 0);
    }

    private String encodeURIComponent(String encoded) {
        String encodedURI = null;
        try {
            encodedURI = URLEncoder.encode(encoded, "UTF-8")
                    .replaceAll("\\+", "%20")
                    .replaceAll("\\%21", "!")
                    .replaceAll("\\%27", "'")
                    .replaceAll("\\%28", "(")
                    .replaceAll("\\%29", ")")
                    .replaceAll("\\%7E", "~");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return encodedURI;
    }

    private void cameraAuthRequest() {
        webview = findViewById(R.id.webview);
        WebSettings ws = webview.getSettings();
        ws.setMediaPlaybackRequiresUserGesture(false);

        webview.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onPermissionRequest(final PermissionRequest request) {
                //API레벨이 21이상인 경우
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    final String[] requestedResources = request.getResources();
                    for (String r : requestedResources) {
                        if (r.equals(PermissionRequest.RESOURCE_VIDEO_CAPTURE)) {
                            request.grant(new String[]{PermissionRequest.RESOURCE_VIDEO_CAPTURE});
                            break;
                        }
                    }
                }
            }
        });
        int cameraPermissionCheck = ContextCompat.checkSelfPermission(WebViewActivity.this, Manifest.permission.CAMERA);
        if (cameraPermissionCheck != PackageManager.PERMISSION_GRANTED) { // 권한이 없는 경우
            ActivityCompat.requestPermissions(WebViewActivity.this, new String[]{Manifest.permission.CAMERA}, 1000);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1000) {
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(WebViewActivity.this, "카메라/갤러리 접근 권한이 없습니다. 권한 허용 후 이용해주세요. no access permission for camera and gallery.", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    @JavascriptInterface
    public void receive(String data) throws JSONException {
        String decodedData = decodedReceiveData(data);
        JSONObject jsonObject = new JSONObject(decodedData);
        String resultData = jsonObject.getString("result");

        if (resultData.equals("success")) {
            Log.d("success", "KYC 작업이 성공했습니다.");
        } else if (resultData.equals("failed")) {
            Log.d("failed", "KYC 작업이 실패했습니다.");
        }

        if (resultData.equals("complete")) {
            Log.d("complete", "KYC가 완료되었습니다.");
        } else if (resultData.equals("close")) {
            Log.d("close", "KYC가 완료되지 않았습니다.");
        }

        finish();
    }

    public String decodedReceiveData(String data) {
        String decoded = new String(Base64.decode(data, 0));
        return decodeURIComponent(decoded);
    }

    private String decodeURIComponent(String decoded) {
        String decodedURI = null;
        try {
            decodedURI = URLDecoder.decode(decoded, "UTF-8")
                    .replaceAll("%20", "\\+")
                    .replaceAll("!", "\\%21")
                    .replaceAll("'", "\\%27")
                    .replaceAll("\\(", "\\%28")
                    .replaceAll("\\)", "\\%29")
                    .replaceAll("~", "\\%7E");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return decodedURI;
    }


}
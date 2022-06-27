package com.example.kyc_android_java_sample;

import android.Manifest;
import android.content.Intent;
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

import com.example.kyc_android_java_sample.databinding.ActivityWebViewBinding;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;

public class WebViewActivity extends AppCompatActivity {

    private ActivityWebViewBinding binding;
    private WebView webview = null;
    private Handler handler = new Handler();
    String result = "";
    String event = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_view);
        String url = "https://kyc.useb.co.kr/auth";

        // 바인딩 설정
        binding = ActivityWebViewBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // 웹뷰 설정
        webview = binding.webview;
        webview.getSettings().setJavaScriptEnabled(true);
        webview.setWebViewClient(new WebViewClient());
        webview.setWebChromeClient(new WebChromeClient());
        webview.addJavascriptInterface(this,"alcherakyc");

        // 사용자 데이터 인코딩
        String userInfo = null;
        String encodedUserInfo = encodeJson(userInfo);

        // POST
        postUserInfo(url, encodedUserInfo);
    }

    // webview가 닫히면 result를 보여주는 화면으로 전환
    @Override
    public void onStop() {

        super.onStop();

        if(result != "") {
            Intent intent = new Intent(getApplicationContext(), ReportActivity.class);
            intent.putExtra("event", event);
            intent.putExtra("result", result);
            startActivity(intent);
        }
    }

    private JSONObject getData() throws JSONException {

        String birthday = getIntent().getStringExtra("birthday");
        String name = getIntent().getStringExtra("name");
        String phoneNumber = getIntent().getStringExtra("phoneNumber");
        String email = getIntent().getStringExtra("email");
        return dataToJson(birthday, name, phoneNumber, email);
    }

    private JSONObject dataToJson(String birthday, String name, String phoneNumber, String email) throws JSONException {

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("customer_id", "12");
        jsonObject.put("id", "demoUser");
        jsonObject.put("key", "demoUser0000!");
        jsonObject.put("name", name);
        jsonObject.put("birthday",  birthday);
        jsonObject.put("phone_number", phoneNumber);
        jsonObject.put("email", email);

        return jsonObject;
    }

    private String encodeURIComponent(String encoded){

        String encodedURI = null;
        try {
            encodedURI = URLEncoder.encode(encoded, "UTF-8")
                    .replaceAll("\\+", "%20")
                    .replaceAll("\\%21", "!")
                    .replaceAll("\\%27", "'")
                    .replaceAll("\\%28", "(")
                    .replaceAll("\\%29", ")")
                    .replaceAll("\\%7E", "~");
        }
        catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return encodedURI;
    }

    private String encodeJson(String data){

        String encodedData = null;
        try {
            data = encodeURIComponent(getData().toString());
            encodedData = Base64.encodeToString(data.getBytes(), 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return encodedData;
    }

    private void postUserInfo(String url, String encodedUserInfo){

        handler.post(new Runnable() {
            @Override
            public void run() {

                // 카메라 권한 요청
                cameraAuthRequest();
                webview.loadUrl(url);
                webview.setWebViewClient(new WebViewClient(){
                    @Override
                    public void onPageFinished(WebView view, String url){

                        webview.loadUrl("javascript:alcherakycreceive('" + encodedUserInfo +"')");
                    }
                });
            }
        });
    }

    private void cameraAuthRequest(){

        webview = binding.webview;
        WebSettings ws = webview.getSettings();
        ws.setMediaPlaybackRequiresUserGesture(false);

        webview.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onPermissionRequest(final PermissionRequest request) {

                //API레벨이 21이상인 경우
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
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
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1000) {
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(WebViewActivity.this, "카메라/갤러리 접근 권한이 없습니다. 권한 허용 후 이용해주세요. no access permission for camera and gallery.", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    @JavascriptInterface
    public void receive(String data) throws UnsupportedEncodingException {

        String success = "{\"result\": \"success\"}";
        String failed = "{\"result\": \"failed\"}";
        String complete = "{\"result\": \"complete\"}";
        String close = "{\"result\": \"close\"}";

        String decodedData = decodedReceiveData(data);
        if (decodedData == success) {
            event = success;
            result = "KYC 작업이 성공했습니다.";
            Log.d("success", "KYC 작업이 성공했습니다.");
        }
        else if (decodedData == failed) {
            event = failed;
            result = "KYC 작업이 실패했습니다.";
            Log.d("failed", "KYC 작업이 실패했습니다.");
        }
        else if (decodedData == complete) {
            event = complete;
            result = "KYC가 완료되었습니다.";
            Log.d("complete", "KYC가 완료되었습니다.");
        }
        else if (decodedData == close) {
            event = close;
            result = "KYC가 완료되지 않았습니다.";
            Log.d("close", "KYC가 완료되지 않았습니다.");
        }
        else {
            result = "KYC 응답 메세지 분석에 실패했습니다.";
            Log.d("decoding failed", "KYC 응답 메세지 분석에 실패했습니다.");
        }

        try {
            webview.loadUrl("javascript:self.close();");
        } catch (Exception e) {
            finish();
        }
    }

    private String decodedReceiveData(String data) throws UnsupportedEncodingException {

        String decoded = Base64.encodeToString(data.getBytes(), 0);
        return URLDecoder.decode(decoded, "UTF-8");
    }
}

package com.example.kyc_android_java_sample;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.webkit.WebChromeClient;
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
import java.net.URLEncoder;

public class WebViewActivity extends AppCompatActivity {

    private String TAG2 = "WebViewActivity";
    private ActivityWebViewBinding binding;
    private WebView webview = null;
    private Handler handler = new Handler();

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
        webview.addJavascriptInterface(new WebBridge(),"alcherakyc");

        // 사용자 데이터 인코딩
        String userInfo = null;
        String encodedUserInfo = encodeJson(userInfo);

        // POST
        postUserInfo(url, encodedUserInfo);
    }

    @Override
    public void onBackPressed(){

        if(webview.canGoBack())
            webview.goBack();
        else
            finish();
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

                webview.loadUrl(url);
                webview.setWebViewClient(new WebViewClient(){
                    @Override
                    public void onPageFinished(WebView view, String url){

                        webview.loadUrl("javascript:alcherakycreceive('" + encodedUserInfo +"')");

                        // 카메라 권한 요청
                        cameraAuthRequest();
                    }
                });
            }
        });
    }

    private void cameraAuthRequest(){

        int cameraPermissionCheck = ContextCompat.checkSelfPermission(WebViewActivity.this, Manifest.permission.CAMERA);
        if (cameraPermissionCheck != PackageManager.PERMISSION_GRANTED) { // 권한이 없는 경우
            ActivityCompat.requestPermissions(WebViewActivity.this, new String[]{Manifest.permission.CAMERA}, 1000);
        }else { // 권한이 있는 경우
            int REQUEST_IMAGE_CAPTURE = 1;
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1000) {
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(WebViewActivity.this, "카메라/갤러리 접근 권한이 없습니다. 권한 허용 후 이용해주세요. no access permission for camera and gallery.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}

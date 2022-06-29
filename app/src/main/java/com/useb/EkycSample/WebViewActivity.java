package com.useb.EkycSample;

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

import com.useb.EkycSample.databinding.ActivityWebViewBinding;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;

public class WebViewActivity extends AppCompatActivity {

    private ActivityWebViewBinding binding;
    private WebView webview = null;
    private Handler handler = new Handler();
    private String result = "";
    private String detail = "";

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

        Intent intent = new Intent(getApplicationContext(), ReportActivity.class);
        intent.putExtra("detail", detail);
        intent.putExtra("result", result);
        startActivity(intent);
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

    @JavascriptInterface
    public void receive(String data) throws JSONException {

        String decodedData = decodedReceiveData(data);
        JSONObject JsonObject = new JSONObject(decodedData);
        String resultData = "";

        try{
            JsonObject = ModifyReviewResult(JsonObject);
            resultData = JsonObject.getString("result");
        }catch (JSONException e){
            resultData = JsonObject.getString("result");
        }

        if (resultData.equals("success")) {
            detail = JsonObject.toString(4);
            result = "KYC 작업이 성공했습니다.";
            Log.d("success", "KYC 작업이 성공했습니다.");
        }
        else if (resultData.equals("failed")) {
            detail = JsonObject.toString(4);
            result = "KYC 작업이 실패했습니다.";
            Log.d("failed", "KYC 작업이 실패했습니다.");
        }

        if (resultData.equals("complete")) {
            detail = JsonObject.toString(4);
            result = "KYC가 완료되었습니다.";
            Log.d("complete", "KYC가 완료되었습니다.");
        }
        else if (resultData.equals("close")) {
            detail = JsonObject.toString(4);
            result = "KYC가 완료되지 않았습니다.";
            Log.d("close", "KYC가 완료되지 않았습니다.");
        }
        finish();
    }

    private JSONObject ModifyReviewResult(JSONObject JsonObject) throws JSONException {

        String reviewResult = JsonObject.getString("review_result");

        JSONObject reviewResultJsonObject = new JSONObject(reviewResult);
        String image = reviewResultJsonObject.getString("id_card");

        JSONObject idCardJsonObject = new JSONObject(image);
        String idCardImage = idCardJsonObject.getString("id_card_image");
        String idCardOrigin = idCardJsonObject.getString("id_card_origin");
        String idCropImage = idCardJsonObject.getString("id_crop_image");
        if(idCardImage!="null"){
            idCardImage = idCardImage.substring(0, 20) + "...생략(omit)...";
            idCardJsonObject.put("id_card_image", idCardImage);
        }
        if(idCardOrigin!="null"){
            idCardOrigin = idCardOrigin.substring(0, 20) + "...생략(omit)...";
            idCardJsonObject.put("id_card_origin", idCardOrigin);
        }
        if(idCropImage!="null"){
            idCropImage = idCropImage.substring(0, 20) + "...생략(omit)...";
            idCardJsonObject.put("id_crop_image", idCropImage);
        }
        reviewResultJsonObject.put("id_card", idCardJsonObject);

        String faceCheck = reviewResultJsonObject.getString("face_check");
        JSONObject faceCheckObject = new JSONObject(faceCheck);
        String faceImage = faceCheckObject.getString("selfie_image");
        if(faceImage != "null"){
            faceImage = faceImage.substring(0, 20) + "...생략(omit)...";
            faceCheckObject.put("selfie_image", faceImage);
        }
        reviewResultJsonObject.put("face_check", faceCheckObject);
        JsonObject.put("review_result", reviewResultJsonObject);

        return JsonObject;
    }

    public String decodedReceiveData(String data) {

        String decoded = new String(Base64.decode(data, 0));
        return decodeURIComponent(decoded);
    }

    private String decodeURIComponent(String decoded){

        String decodedURI = null;
        try {
            decodedURI = URLDecoder.decode(decoded, "UTF-8")
                    .replaceAll("%20", "\\+")
                    .replaceAll("!", "\\%21")
                    .replaceAll("'", "\\%27")
                    .replaceAll("\\(", "\\%28")
                    .replaceAll("\\)", "\\%29")
                    .replaceAll("~", "\\%7E");
        }
        catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return decodedURI;
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
}
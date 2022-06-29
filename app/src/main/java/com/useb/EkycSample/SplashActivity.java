package com.useb.EkycSample;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        Handler handler = new Handler();
        handler.postDelayed(new SplashHandler(), 3000);
    }

    @Override
    protected void onPause(){
        super.onPause();
        finish();
    }

    private class SplashHandler implements Runnable{

        @Override
        public void run() {
            startActivity(new Intent(getApplicationContext(), MainActivity.class));
            SplashActivity.this.finish();
        }
    }
}

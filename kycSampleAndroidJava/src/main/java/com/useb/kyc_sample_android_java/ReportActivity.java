package com.useb.kyc_sample_android_java;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;


public class ReportActivity extends AppCompatActivity {

    private String result = "";
    private String detail = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);

        try {
            getData();
        } catch (Exception e) {
            e.printStackTrace();
        }
        setData(detail, result);
    }

    private void getData() throws JSONException {

        detail = getIntent().getStringExtra("detail");
        result = getIntent().getStringExtra("result");
    }

    private void setData(String detail, String result) {

        TextView detailTv = findViewById(R.id.detail);
        TextView resultTv = findViewById(R.id.result);
        detailTv.setText(detail);
        resultTv.setText(result);
    }
}
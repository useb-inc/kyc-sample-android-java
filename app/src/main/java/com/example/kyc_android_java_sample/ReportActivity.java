package com.example.kyc_android_java_sample;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import org.json.JSONException;


public class ReportActivity extends AppCompatActivity {

    String result = "";
    String event = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);

        try {
            getData();
        } catch (Exception e) {
            e.printStackTrace();
        }
        setData(event, result);
    }

    private void getData() throws JSONException {

        event = getIntent().getStringExtra("event");
        result = getIntent().getStringExtra("result");
    }

    private void setData(String event, String result){

        TextView eventTv = findViewById(R.id.event);
        TextView resultTv = findViewById(R.id.result);
        eventTv.setText(event);
        resultTv.setText(result);
    }
}
package com.example.kyc_android_java_sample;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;

import com.example.kyc_android_java_sample.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.runButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent secondIntent = new Intent(getApplicationContext(), WebViewActivity.class);
                sendDataToWebview(secondIntent);
                startActivity(secondIntent);
            }
        });
    }

    private void sendDataToWebview(Intent secondIntent){

        String yearStr = binding.year.getText().toString();
        String monthStr = binding.month.getText().toString();
        String dayStr = binding.day.getText().toString();
        String birthday;
        if(yearStr == "" || monthStr == "" || dayStr == "")
            birthday = "";
        else
            birthday = yearStr + "-" + monthStr + "-" + dayStr;

        String name = binding.name.getText().toString();
        String phoneNumber = binding.phoneNumber.getText().toString();
        String email = binding.email.getText().toString();

        secondIntent.putExtra("birthday", birthday);
        secondIntent.putExtra("name", name);
        secondIntent.putExtra("phoneNumber", phoneNumber);
        secondIntent.putExtra("email", email);
    }
}
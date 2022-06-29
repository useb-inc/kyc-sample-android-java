package com.useb.ekyc_sample;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.useb.ekyc_sample.databinding.ActivityMainBinding;

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
                if(sendDataToWebview(secondIntent))
                    startActivity(secondIntent);
            }
        });
    }

    private boolean sendDataToWebview(Intent secondIntent){

        String yearStr = binding.year.getText().toString();
        String monthStr = binding.month.getText().toString();
        String dayStr = binding.day.getText().toString();
        String birthday;
        if(yearStr.length() == 0 || monthStr.length() == 0 || dayStr.length() == 0)
            birthday = "";
        else
            birthday = yearStr + "-" + monthStr + "-" + dayStr;

        String name = binding.name.getText().toString();
        String phoneNumber = binding.phoneNumber.getText().toString();
        String email = binding.email.getText().toString();

        if(isValid(email, name, phoneNumber, birthday)) {
            secondIntent.putExtra("birthday", birthday);
            secondIntent.putExtra("name", name);
            secondIntent.putExtra("phoneNumber", phoneNumber);
            secondIntent.putExtra("email", email);
            return true;
        }else{
            return false;
        }
    }

    private boolean isValid(String email, String name, String phoneNumber, String birthday){

        InputValidator inputValidator = new InputValidator();
        boolean allowFlag = true;

        if(inputValidator.isNullOrEmpty(email) || inputValidator.isNullOrEmpty(name) || inputValidator.isNullOrEmpty(phoneNumber) || inputValidator.isNullOrEmpty(birthday)) {
            Toast.makeText(MainActivity.this, "빈 칸을 모두 채워주세요", Toast.LENGTH_SHORT).show();
            allowFlag = false;
        }
        else if (!inputValidator.isValidEmail(email)) {
            Toast.makeText(MainActivity.this, "이메일 형식에 맞지 않습니다.", Toast.LENGTH_SHORT).show();
            allowFlag = false;
        }

        return allowFlag;
    }
}
package com.example.cs401collaboration;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class OnboardActivity extends AppCompatActivity
{

    Button mContinueBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboard);

        mContinueBtn = findViewById(R.id.onboard_continue);

        mContinueBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick (View view)
            {
                startActivity(new Intent(OnboardActivity.this, LoginActivity.class));
                finish();
            }
        });
    }

    @Override
    public void onBackPressed()
    {
        moveTaskToBack(true);
    }

}
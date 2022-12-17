package com.example.soul_fight;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.widget.ImageView;

public class MainActivity extends AppCompatActivity {

    ImageView logo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        logo = findViewById(R.id.logo);

        logo.animate().translationY(-2000).setDuration(700).setStartDelay(1500);
        new CountDownTimer(1500, 1500) {

            public void onTick(long millisUntilFinished) {
            }

            public void onFinish() {
                Intent i = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(i);
            }

        }.start();

    }
}
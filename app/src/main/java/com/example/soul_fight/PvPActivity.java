package com.example.soul_fight;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

public class PvPActivity extends AppCompatActivity {

    private ImageButton backButton;
    private Button createRoom;
    private Button joinButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pv_pactivity);

        backButton = (ImageButton) findViewById(R.id.back);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), HomeActivity.class));
            }
        });

        createRoom = (Button) findViewById(R.id.create);
        createRoom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: send to server
                Intent intent = new Intent(getApplicationContext(), CreateRoom.class);

                startActivity(intent);
            }
        });

        joinButton = (Button) findViewById(R.id.join);
        joinButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), JoinRoom.class);
                startActivity(intent);
            }
        });
    }
}
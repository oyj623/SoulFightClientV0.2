package com.example.soul_fight;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import java.util.ArrayList;

public class AdvActivity extends AppCompatActivity {

    private ImageButton backButton;
    private ArrayList<Button> levelButtons;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_adv);
        backButton = (ImageButton) findViewById(R.id.back);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), HomeActivity.class));
            }
        });

        // TODO: get user level from database, set availability of level access
        levelButtons = new ArrayList<>();
        levelButtons.add((Button) findViewById(R.id.lvl1));
        levelButtons.add((Button) findViewById(R.id.lvl2));
        levelButtons.add((Button) findViewById(R.id.lvl3));
        levelButtons.add((Button) findViewById(R.id.lvl4));
        levelButtons.add((Button) findViewById(R.id.lvl5));

        for(int i = 0; i < levelButtons.size(); i++) {
            int finalI = i;
            levelButtons.get(i).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // TODO: set game setting in shared preferences
                    Intent intent = new Intent(getApplicationContext(), Adevnture.class);
                    intent.putExtra("level", finalI + 1);
                    startActivity(intent);
                }
            });
        }

    }
}
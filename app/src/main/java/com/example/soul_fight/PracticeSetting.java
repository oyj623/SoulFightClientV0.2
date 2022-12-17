package com.example.soul_fight;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;

import java.util.Date;
import java.util.Random;

public class PracticeSetting extends AppCompatActivity {

    private ImageButton backButton;
    private Switch hasAddition;
    private Switch hasSubtraction;
    private Switch hasMultiply;
    private Switch hasDivision;
    private Spinner digitSettings;
    private Spinner flashSpeedSettings;
    private Button startPractice;
    private EditText lengthPerQuestion; // TODO: inclde in setting

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_practice_setting);

        backButton = (ImageButton) findViewById(R.id.back);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), HomeActivity.class));
            }
        });

        digitSettings = (Spinner) findViewById(R.id.digitSetting);
        ArrayAdapter<CharSequence> digitSettingAdapter=ArrayAdapter.createFromResource(this, R.array.digitSetting, android.R.layout.simple_spinner_item);
        digitSettingAdapter.setDropDownViewResource(android.R.layout.simple_spinner_item);
        digitSettings.setAdapter(digitSettingAdapter);

        flashSpeedSettings = (Spinner) findViewById(R.id.flashSpeedSetting);
        ArrayAdapter<CharSequence> flashSpeedAdapter=ArrayAdapter.createFromResource(this, R.array.flashSpeedSetting, android.R.layout.simple_spinner_item);
        flashSpeedAdapter.setDropDownViewResource(android.R.layout.simple_spinner_item);
        flashSpeedSettings.setAdapter(flashSpeedAdapter);

        hasAddition = (Switch) findViewById(R.id.hasAdd);
        hasSubtraction = (Switch) findViewById(R.id.hasSubtract);
        hasMultiply = (Switch) findViewById(R.id.hasMultiply);
        hasDivision = (Switch) findViewById(R.id.hasDivision);
        Toast atLeastOneOperatorWarning = Toast.makeText(this, "At least one operator must be allowed.", Toast.LENGTH_SHORT);

        hasAddition.setChecked(true);
        hasAddition.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            // set addition on if none is on
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (!hasAddition.isChecked() && !hasSubtraction.isChecked() && !hasMultiply.isChecked() && !hasDivision.isChecked()) {
                    hasAddition.setChecked(true);
                    atLeastOneOperatorWarning.show();
                }
            }
        });
        hasSubtraction.setChecked(true);
        hasSubtraction.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            // set addition on if none is on
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (!hasAddition.isChecked() && !hasSubtraction.isChecked() && !hasMultiply.isChecked() && !hasDivision.isChecked()) {
                    hasAddition.setChecked(true);
                    atLeastOneOperatorWarning.show();
                }
            }
        });
        hasMultiply.setChecked(true);
        hasMultiply.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            // set addition on if none is on
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (!hasAddition.isChecked() && !hasSubtraction.isChecked() && !hasMultiply.isChecked() && !hasDivision.isChecked()) {
                    hasAddition.setChecked(true);
                    atLeastOneOperatorWarning.show();
                }
            }
        });
        hasDivision.setChecked(true);
        hasDivision.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            // set addition on if none is on
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (!hasAddition.isChecked() && !hasSubtraction.isChecked() && !hasMultiply.isChecked() && !hasDivision.isChecked()) {
                    hasAddition.setChecked(true);
                    atLeastOneOperatorWarning.show();
                }
            }
        });

        startPractice = (Button) findViewById(R.id.startPractice);
        startPractice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int minimumDigit = 1;
                int maximumDigit = 1;
                System.out.println(digitSettings.getSelectedItemPosition());
                switch (digitSettings.getSelectedItemPosition()) {
                    case 0:
                        minimumDigit = 1;
                        maximumDigit = 1;
                        break;
                    case 1:
                        minimumDigit = 2;
                        maximumDigit = 2;
                        break;
                    case 2:
                        minimumDigit = 3;
                        maximumDigit = 3;
                        break;
                    case 3:
                        minimumDigit = 1;
                        maximumDigit = 2;
                        break;
                    case 4:
                        minimumDigit = 1;
                        maximumDigit = 3;
                        break;
                    case 5:
                        minimumDigit = 2;
                        maximumDigit = 3;
                        break;
                } // end digit setting switch case
                int flashSpeed = 1000;
                switch (flashSpeedSettings.getSelectedItemPosition()) {
                    case 0:
                        flashSpeed = 1000;
                        break;
                    case 1:
                        flashSpeed = 1400;
                        break;
                    case 2:
                        flashSpeed = 1750;
                        break;
                } // end flash speed setting switch case
                RoomSettings thisRoomSetting = new RoomSettings(
                    10, minimumDigit, maximumDigit, flashSpeed, new boolean[] {
                        hasAddition.isChecked(),
                        hasSubtraction.isChecked(),
                        hasMultiply.isChecked(),
                        hasDivision.isChecked()
                    }
                );

                Intent intent = new Intent(getApplicationContext(), Practice.class);
                Bundle roomSettingsBundle = new Bundle();
                roomSettingsBundle.putSerializable("roomSettings", thisRoomSetting);
                intent.putExtra("roomSettings", roomSettingsBundle);
                startActivity(intent);
                // intent.putExtra("settings", sth); // tbc
            }
        });
    }
}
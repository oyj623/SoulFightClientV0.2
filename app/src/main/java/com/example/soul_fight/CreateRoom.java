package com.example.soul_fight;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.widget.*;

public class CreateRoom extends AppCompatActivity {

    private ImageButton backButton;
    private Switch hasAddition;
    private Switch hasSubtraction;
    private Switch hasMultiply;
    private Switch hasDivision;
    private Spinner digitSettings;
    private Spinner flashSpeedSettings;
    private Button createRoom;

    // Socket setting
    SocketService mBoundService;
    boolean mIsBound = false;
    private ServiceConnection mConnection = new ServiceConnection() {
        //EDITED PART
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            // TODO Auto-generated method stub
            mBoundService = ((SocketService.LocalBinder)service).getService();
            mBoundService.listenMatchStart();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            // TODO Auto-generated method stub
            mBoundService = null;
        }
    };

    private void doBindService() {
        bindService(new Intent(this, SocketService.class), mConnection, Context.BIND_AUTO_CREATE);
        mIsBound = true;
    }

    private void doUnbindService() {
        if (mIsBound) {
            // Detach our existing connection.
            unbindService(mConnection);
            mIsBound = false;
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_room);

        doBindService();

        backButton = (ImageButton) findViewById(R.id.back);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), PvPActivity.class));
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
        hasDivision = (Switch) findViewById(R.id.hasDivision);
        hasMultiply = (Switch) findViewById(R.id.hasMultiply);
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

        createRoom = (Button) findViewById(R.id.createRoom);
        createRoom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: connect to server
                int minimumDigit = 1;
                int maximumDigit = 1;
                switch (digitSettings.getSelectedItem().toString()) {
                    case "1 Digit":
                        minimumDigit = 1;
                        maximumDigit = 1;
                        break;
                    case "2 Digits":
                        minimumDigit = 2;
                        maximumDigit = 2;
                        break;
                    case "3 Digits":
                        minimumDigit = 3;
                        maximumDigit = 3;
                        break;
                    case "1 - 2 Digits":
                        minimumDigit = 1;
                        maximumDigit = 2;
                        break;
                    case "1 - 3 Digits":
                        minimumDigit = 1;
                        maximumDigit = 3;
                        break;
                    case "2 - 3 Digits":
                        minimumDigit = 2;
                        maximumDigit = 3;
                        break;
                } // end digit setting switch case
                int flashSpeed = 1000;
                switch (flashSpeedSettings.getSelectedItem().toString()) {
                    case "Fast     (1s)":
                        flashSpeed = 1000;
                        break;
                    case "Medium (1.4s)":
                        flashSpeed = 1400;
                        break;
                    case "Slow  (1.75s)":
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
                if(mBoundService!=null){
                    mBoundService.createRoom(thisRoomSetting);
                }
                Intent intent = new Intent(getApplicationContext(), RoomLobby.class);
                Bundle roomSettingBundle = new Bundle();
                roomSettingBundle.putSerializable("roomSettings", thisRoomSetting);
                intent.putExtra("roomSettings", thisRoomSetting);
                startActivity(intent);
            }
        });

    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        doUnbindService();
    }
}
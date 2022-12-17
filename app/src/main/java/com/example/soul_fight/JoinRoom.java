package com.example.soul_fight;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import java.net.Socket;

public class JoinRoom extends AppCompatActivity {

    private ImageButton backButton;
    private EditText roomNumber;
    private Button joinRoom;

    // Socket setting
    SocketService mBoundService;
    boolean mIsBound = false;
    private ServiceConnection mConnection = new ServiceConnection() {
        //EDITED PART
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            // TODO Auto-generated method stub
            mBoundService = ((SocketService.LocalBinder)service).getService();
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
        setContentView(R.layout.activity_join_room);
        doBindService();
        backButton = (ImageButton) findViewById(R.id.back);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), PvPActivity.class));
            }
        });

        roomNumber = (EditText) findViewById(R.id.enterRoomNumber);
        joinRoom = (Button) findViewById(R.id.btn_join);
        joinRoom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int roomNo = 67890;
                try {
                    roomNo = Integer.parseInt(roomNumber.getText().toString());
                } catch (NumberFormatException ex) {
                    Toast.makeText(getApplicationContext(), "Please enter a valid room number.", Toast.LENGTH_SHORT).show();
                }
                if(mBoundService!=null){
                    mBoundService.chooseRoom(roomNo);
                }
                startActivity(new Intent(getApplicationContext(), JoinLobby.class));
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        doUnbindService();
    }
}
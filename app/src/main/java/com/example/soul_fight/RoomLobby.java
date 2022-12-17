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
import android.widget.TextView;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;

public class RoomLobby extends AppCompatActivity {

    private TextView roomNumber;
    private TextView playerTwoName;
    private TextView playerTwoStatus;
    private Button startButton;

    // Socket setting
    Thread listeningForPlayerJoin;
    ObjectInputStream ois;
    ObjectOutputStream oos;
    Socket socket;
    SocketService mBoundService;
    boolean mIsBound = false;
    private ServiceConnection mConnection = new ServiceConnection() {
        //EDITED PART
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            // TODO Auto-generated method stub
            mBoundService = ((SocketService.LocalBinder)service).getService();
            startButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(mBoundService!=null){
                        mBoundService.startMatch(); //Enter the room settings here
                    }
                    Intent intent = new Intent(getApplicationContext(), PvP.class);
                    intent.putExtra("roomSettings", getIntent().getBundleExtra("roomSettings"));
                    startActivity(intent);
                }
            });
            socket = mBoundService.socket;
            ois = mBoundService.objectInputStream;
            oos = mBoundService.objectOutputStream;
            listeningForPlayerJoin = new Thread() {
                @Override
                public void run() {
                    while(socket.isConnected()) {
                        try {
                            String message = (String) ois.readObject();
                            if (!message.equals("someoneJoined")) {
                                System.out.println("Unexpected message received: " + message);
                            } else {
                                String playerTwoNameFromServer = (String) ois.readObject();
                                playerTwoName.setText(playerTwoNameFromServer);
                                playerTwoStatus.setText("(Ready)");
                                startButton.setEnabled(true);
                            }
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                }

            };
            listeningForPlayerJoin.start();
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
        setContentView(R.layout.activity_room_lobby);

        doBindService();

        roomNumber = (TextView) findViewById(R.id.roomNumber);
        roomNumber.setText("0");
        playerTwoName = (TextView) findViewById(R.id.person2);
        playerTwoStatus = (TextView) findViewById(R.id.playerTwoStatus);
        startButton = (Button) findViewById(R.id.start);
        startButton.setEnabled(false);
    }

    // TODO: player two status change according to server
    protected void playerTwoStatusChange() {
        playerTwoStatus.setText("(Ready)");
        startButton.setEnabled(true);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        doUnbindService();
        listeningForPlayerJoin.stop();
    }
}
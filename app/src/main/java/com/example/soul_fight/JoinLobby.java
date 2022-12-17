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
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;

public class JoinLobby extends AppCompatActivity {

    private Button readyButton;
    private TextView status;
    private TextView hostName;
    private TextView roomNumber;
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
        setContentView(R.layout.activity_join_lobby);
        doBindService();
        Socket socket = mBoundService.socket;

        readyButton = (Button) findViewById(R.id.ready);
        status = (TextView) findViewById(R.id.playerTwoStatus);
        hostName = (TextView) findViewById(R.id.person1);
        roomNumber = (TextView) findViewById(R.id.roomNumber);
        roomNumber.setText("0"); // TODO: get room number from server
        new Thread() {
            @Override
            public void run() {
                try {
                    Socket socket = new Socket(InetAddress.getByName(SocketService.SERVERIP), SocketService.SERVERPORT);
                    ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
                    oos.writeObject("getRoomID");
                    oos.flush();
                    int roomID = (int) mBoundService.objectInputStream.readObject();
                    roomNumber.setText(Integer.toString(roomID));
                } catch (IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }.start();
        new Thread() {
            @Override
            public void run() {
                try {
                    Socket socket = new Socket(InetAddress.getByName(SocketService.SERVERIP), SocketService.SERVERPORT);
                    ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
                    oos.writeObject("getRoomHost");
                    oos.flush();
                    String host = (String) mBoundService.objectInputStream.readObject();
                    hostName.setText(host);
                } catch (IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }.start();
        // TODO: assign host name by asking server
        mBoundService.listenMatchStart();
        readyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (status.getText().toString().equals("(Not Ready")) {
                    status.setText("(Ready)");
                    readyButton.setText("Away");
                } else if (status.getText().toString().equals("(Ready)")) {
                    status.setText("(Not Ready)");
                    readyButton.setText("Ready");
                }
                // TODO: send status to server
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        doUnbindService();
    }
}
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
import android.widget.ImageButton;
import android.widget.TextView;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;

public class JoinLobby extends AppCompatActivity {

    private Button readyButton;
    private ImageButton backButton;
    private TextView status;
    private TextView hostName;
    private TextView roomNumber;
    // Socket setting
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
            System.out.print("In onServiceConnect: ");
            socket = mBoundService.socket;
            System.out.print("socket = " + socket);
            ois = mBoundService.objectInputStream;
            oos = mBoundService.objectOutputStream;
            Thread initiatingRoom = new Thread() {
                @Override
                public void run() {
                    initiateThingsInOnCreate();
                }
            };
            initiatingRoom.start();

            try {
                System.out.println("Waiting for init room to end");
                initiatingRoom.join();
                System.out.println("Init room end");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("calling listen match start");
            mBoundService.listenMatchStart();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            // TODO Auto-generated method stub
            mBoundService = null;
        }
    };

    private void doBindService() {

        System.out.println("in doBindService()");
        System.out.println("calling bindService(...)");

        getApplicationContext().bindService(new Intent(this, SocketService.class), mConnection, Context.BIND_AUTO_CREATE);
        System.out.println("after bindService()");
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
        System.out.println("calling doBindService()");
        doBindService();

        readyButton = (Button) findViewById(R.id.ready);
        status = (TextView) findViewById(R.id.playerTwoStatus);
        hostName = (TextView) findViewById(R.id.person1);
        roomNumber = (TextView) findViewById(R.id.roomNumber);
        backButton = (ImageButton) findViewById(R.id.back);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(),JoinRoom.class);
                // TODO: tell server leave room
                startActivity(intent);
            }
        });



        // TODO: assign host name by asking server
        readyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (status.getText().toString().equals("(Not Ready)")) {
                    status.setText("(Ready)");
                    readyButton.setText("Away");
                } else if (status.getText().toString().equals("(Ready)")) {
                    status.setText("(Not Ready)");
                    readyButton.setText("Ready");
                }
                // TODO: send status to server
            }
        });
        System.out.println("Before on create ends");
    }
    String host = "null";
    int roomID = -1;
    public void initiateThingsInOnCreate() {

        Thread waitForRoomID = new Thread() {
            @Override
            public void run() {
                try {
                    oos.writeObject("getRoomID");
                    oos.flush();
                    System.out.println("Getting room id");
                    Object read = ois.readObject();
                    System.out.println("object read: " + read);
                    roomID = (int) read;

                    oos.writeObject("getRoomHost");
                    oos.flush();
                    System.out.println("getting room host");
                    host = (String) ois.readObject();

                    oos.writeObject("getRoomSettings");
                    oos.flush();
                    RoomSettings roomSettings = (RoomSettings) ois.readObject();

                } catch (IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                }

            }
        };
        waitForRoomID.start();
        try {
            waitForRoomID.join();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                roomNumber.setText(Integer.toString(roomID));
                hostName.setText(host);
            }
        });


//        new Thread() {
//            @Override
//            public void run() {
//                try {
////                    Socket socket = new Socket(InetAddress.getByName(SocketService.SERVERIP), SocketService.SERVERPORT);
////                    ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
////                    ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
//                    oos.writeObject("getRoomHost");
//                    oos.flush();
//                    System.out.println("getting room host");
//                    Object read = ois.readObject();
//                    System.out.println("object read: " + read);
//                    String host = (String) ois.readObject();
//
//                    System.out.println("Room host = " + host);
//                    hostName.setText(host);
//                } catch (IOException | ClassNotFoundException e) {
//                    e.printStackTrace();
//                }
//            }
//        }.start();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        doUnbindService();
    }
}
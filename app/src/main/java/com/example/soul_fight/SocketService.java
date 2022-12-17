package com.example.soul_fight;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;

public class SocketService extends Service {

    public static String SERVERIP = "10.71.1.19";
    public static final int SERVERPORT = 1234;
    private final IBinder myBinder = new LocalBinder();
    public int seed;
    public String clientName = "someRandomClientName";
    public PrintWriter out;
    public ObjectOutputStream objectOutputStream;
    public ObjectInputStream objectInputStream;
    public Socket socket;
    InetAddress serverAddr;
    Thread verifyAccount;
    public boolean loginCheck = false;
    public static ArrayList<String> roomName = new ArrayList<>();

    public class LocalBinder extends Binder {
        public SocketService getService() {
            return SocketService.this;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return myBinder;
    }

    @Override
    public void onCreate(){
        super.onCreate();
    }

    public void sendDamage(double damage) {
        new Thread() {
            @Override
            public void run() {
                try {
                    objectOutputStream.writeObject(damage);
                    objectOutputStream.flush();

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    public void chooseRoom(int index) {
        new Thread() {
            @Override
            public void run() {
                try {
                    objectOutputStream.writeObject("joinRoom");
                    objectOutputStream.flush();
                    objectOutputStream.writeObject(index);
                    objectOutputStream.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    public void createRoom(RoomSettings roomSettings){
        new Thread() {
            @Override
            public void run() {
                try {
                    objectOutputStream.writeObject("createRoom");
                    objectOutputStream.flush();
                    objectOutputStream.writeObject(roomSettings);
                    objectOutputStream.flush();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }.start();
    }

    public void sendAccount(Account account){
        System.out.println("In send account");
        new Thread(){
            @Override
            public void run() {
                try {
                    objectOutputStream.writeObject(account);
                    objectOutputStream.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

     public void checkAccount(){
        System.out.println("In check account");
        verifyAccount = new Thread(){
            @Override
            public void run() {
                try {
                    loginCheck = (boolean) objectInputStream.readObject();
                    System.out.println(loginCheck);
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        };
        verifyAccount.start();
    }
    //Use this with start match button
    public void startMatch(){
        new Thread(){
            @Override
            public void run() {
                System.out.println("in startMatch");
                try {
                    objectOutputStream.writeObject("startMatch");
                    objectOutputStream.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    //Put under onServiceConnected function in whichever activity that has the start match button
    public void listenMatchStart(RoomSettings roomSettings){

        new Thread(){
            @Override
            public void run() {
                System.out.println("Listening for match start");
                try {
                    Object o = objectInputStream.readObject();
                    if(o instanceof String ){
                        String s = (String)o;
                        if(s.equals("Begin Match")){
                            seed = (int) objectInputStream.readObject();
                            Intent intent = new Intent(getApplicationContext(), PvP.class);
                            Bundle roomSettingBundle = new Bundle();
                            roomSettingBundle.putSerializable("roomSettings", roomSettings);
                            intent.putExtra("roomSettings", roomSettingBundle);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                        }
                    }
                } catch (IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    //Use this if you want to retrieve a list of room creator names but most likely you wont use this
    public void requestRoomList(){
        new Thread(){
            @Override
            public void run() {
                System.out.println("in requestRoomList");
                try {
                    objectOutputStream.writeObject("sendRoomList");
                    objectOutputStream.flush();
                    int size = (int) objectInputStream.readObject(); //reads in the size of the roomList
                    if(size <=0 ){ //if no rooms display message "No rooms available at the moment."
                        String s = (String) objectInputStream.readObject();
                        System.out.println(s);
                    }
                    else{ //if there is room, display playerName who created the room
                        for(int i = 0;i<size;i++){
                            roomName.add((String) objectInputStream.readObject());
                            System.out.println(roomName.get(i));
                        }
                    }
                } catch (IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                }
                System.out.println("Exit requestRoomList");
            }
        }.start();
    }

    //receives the damage from other user
    //Put this under onServiceConnected for whichever page the game will be running on
//    public void listenForDamage(){
//        new Thread(new Runnable(){
//            @Override
//            public void run(){
//                double dmgFromServer;
//                System.out.println("Listening for Damage....");
//                while(socket.isConnected()){
//                    try{
//                        dmgFromServer = (Double)objectInputStream.readObject();
//                        if(dmgFromServer < 0){ //When game is over will send -1 or -2
//                            //game over
//                            //DO WHATEVER
//                            if(dmgFromServer == -1){
//                                //WIN
//                                System.out.println("You have won" + dmgFromServer);
//                            }
//                            if(dmgFromServer == -2){
//                                //lose
//                                System.out.println("You have lost" + dmgFromServer);
//                            }
//                        }else{
//                            System.out.println("Damage from server: "+dmgFromServer);
//                        }
//
//                    }catch(IOException | ClassNotFoundException e){
//                        System.out.println(e);
//                    }
//                }
//            }
//        }).start();
//    }
    @Override
    public int onStartCommand(Intent intent,int flags, int startId){
        super.onStartCommand(intent, flags, startId);
        System.out.println("I am in on start");
        /* clientName = intent.getStringExtra("Client");*/
        //  Toast.makeText(this,"Service created ...", Toast.LENGTH_LONG).show();
        Runnable connect = new connectSocket();
        new Thread(connect).start();
        return START_STICKY;
    }

    class connectSocket implements Runnable {
        @Override
        public void run() {
            try {

                //here you must put your computer's IP address.
                serverAddr = InetAddress.getByName(SERVERIP);
                Log.e("TCP Client", "C: Connecting...");
                //create a socket to make the connection with the server
                System.out.println(serverAddr.toString());
                socket = new Socket(serverAddr, SERVERPORT);

                try {
                    //send the message to the server
                    out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
                    objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
                    objectInputStream = new ObjectInputStream(socket.getInputStream());
                    objectOutputStream.writeObject(clientName);
                    objectOutputStream.flush();

                    Log.e("TCP Client", "C: Sent.");
                    Log.e("TCP Client", "C: Done.");
                }
                catch (Exception e) {

                    Log.e("TCP", "S: Error", e);

                }
            } catch (Exception e) {

                Log.e("TCP", "C: Error", e);

            }

        }

    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            socket.close();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        socket = null;
    }

}

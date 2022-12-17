package com.example.soul_fight;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.net.Socket;


import androidx.fragment.app.Fragment;

public class LoginTabFragment extends Fragment {

    ImageView img_user;
    ImageView img_pass;
    EditText username;
    EditText password;
    TextView forgotPass;
    Button login;
    float v=0;

    // Verify login variables
    Thread verifyAccount;
    SocketService mBoundService;
    boolean mIsBound = false;

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mBoundService = ((SocketService.LocalBinder) service).getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mBoundService = null;
        }
    };

    private void doBindService() {
        getActivity().bindService(new Intent(this.getActivity(), SocketService.class), mConnection, Context.BIND_AUTO_CREATE);
        mIsBound = true;
    }

    private void doUnbindService() {
        if (mIsBound) {
            // Detach our existing connection.
            getActivity().unbindService(mConnection);
            mIsBound = false;
        }
    }
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstancesState) {
        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.login_tab_fragment, container, false);

        img_user = root.findViewById(R.id.imageView);
        img_pass = root.findViewById(R.id.imagePass);
        username = root.findViewById(R.id.username);
        password = root.findViewById(R.id.password);
        forgotPass = root.findViewById(R.id.forgot_pass);
        login = root.findViewById(R.id.loginButton);

        img_user.setTranslationX(800);
        img_pass.setTranslationX(800);
        username.setTranslationX(800);
        password.setTranslationX(800);
        forgotPass.setTranslationX(800);
        login.setTranslationX(800);

        img_user.setAlpha(v);
        img_pass.setAlpha(v);
        username.setAlpha(v);
        password.setAlpha(v);
        forgotPass.setAlpha(v);
        login.setAlpha(v);

        img_user.animate().translationX(0).alpha(1).setDuration(800).setStartDelay(250).start();
        username.animate().translationX(0).alpha(1).setDuration(800).setStartDelay(300).start();
        img_pass.animate().translationX(0).alpha(1).setDuration(800).setStartDelay(450).start();
        password.animate().translationX(0).alpha(1).setDuration(800).setStartDelay(500).start();
        forgotPass.animate().translationX(0).alpha(1).setDuration(800).setStartDelay(500).start();
        login.animate().translationX(0).alpha(1).setDuration(800).setStartDelay(700).start();
        getActivity().startService(new Intent(getActivity(), SocketService.class));
        doBindService();
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Account account = new Account(1, username.getText().toString(), password.getText().toString());
                if(mBoundService!=null){
                    mBoundService.sendAccount(account);
                    mBoundService.checkAccount();
                    try {
                        mBoundService.verifyAccount.join();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if(mBoundService.loginCheck){
                        System.out.println(username.getText().toString());
                        Intent intent = new Intent(getActivity(), HomeActivity.class);
                        startActivity(intent);
                    }else{
                        Toast.makeText(getContext(), "Invalid username or password.", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        return root;
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        doUnbindService();
    }

}

package com.example.soul_fight;

import java.io.Serializable;


public class Account implements Serializable {
    public int userID;
    public String username;
    public String password;

    Account(int userID, String username, String password) {
        this.userID = userID;
        this.username = username;
        this.password = password;
    }
}

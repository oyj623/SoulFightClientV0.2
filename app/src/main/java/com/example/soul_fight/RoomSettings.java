package com.example.soul_fight;

import java.io.Serializable;

public class RoomSettings implements Serializable {

    public int lengthPerQuestion;
    public int minimumDigit;
    public int maximumDigit;
    public int flashSpeed;
    public boolean operators[];

    RoomSettings(int lengthPerQuestion, int min, int max, int flashSpeed, boolean op[]){
        this.lengthPerQuestion = lengthPerQuestion;
        this.minimumDigit = min;
        this.maximumDigit = max;
        this.flashSpeed = flashSpeed;
        this.operators = new boolean[4];
        operators = op;
    }
}

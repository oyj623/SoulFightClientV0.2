package com.example.soul_fight;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Date;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class Practice extends AppCompatActivity {

    Dialog confirmExitDialog;
    ImageButton exitButton;

    private TextView flashCard;
    private TextView inputAnswer;
    private int correctNumber;
    private int wrongNumber;
    private TextView correctNumberTV;
    private TextView salahNomborTV;
    private ArrayList<Button> numpad;
    private Button backspace;
    private Button enter;

    /* Game Settings */
    private int maxDigit;
    private int minDigit;
    private int lengthPerQuestion;
    private int flashSpeedInMillisecond;
    private Question currentQuestion;
    private Question nextQuestion;
    private ArrayList<Adevnture.QuestionType> availableQuestionTypes;
    private Random random;
    public class Question {
        public ArrayList<Integer> numbers;
        public Adevnture.QuestionType type;
        public int answer;
        public Question(Adevnture.QuestionType type) {
            this.type = type;
            this.numbers = new ArrayList<>();
            switch(type) {
                case ADDITION:
                    for (int i = 0; i < lengthPerQuestion; i++) {
                        numbers.add(getRandomNumber());
                    }
                    this.answer = sumArray(this.numbers);
                    break;
                case ADDITION_AND_SUBTRACTION:
                    // if sum <= 2 * maxNumber, generate positive number, else negative
                    for(int i = 0; i < lengthPerQuestion; i++) {
                        if (sumArray(this.numbers) <= 2 * Math.pow(10, maxDigit)) {
                            numbers.add(getRandomNumber());
                        } else {
                            numbers.add(-1 * getRandomNumber());
                        }
                    }
                    this.answer = sumArray(this.numbers);
                    break;
                case MULTIPLICATION:
                    int number1 = getRandomNumber();
                    int number2 = getRandomNumber();
                    numbers.add(number1);
                    numbers.add(number2);
                    this.answer = number1 * number2;
                    break;
                case DIVISION:
                    // generate two numbers, first number is the answer, second number is divisor
                    // store divisor, and (divisor * answer) into question
                    this.answer = getRandomNumber();
                    int divisor = getRandomNumber();
                    int product = this.answer * divisor;
                    numbers.add(product);
                    numbers.add(divisor);
                    break;
            } // end switch
        } // end Question(QuestionType) constructor
    } // end Question class declaration

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_practice);

        confirmExitDialog = new Dialog(Practice.this);
        confirmExitDialog.setContentView(R.layout.custom_dialog);
        confirmExitDialog.getWindow().setBackgroundDrawable(getDrawable(R.drawable.background));
        confirmExitDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        confirmExitDialog.setCancelable(false);
        // delete level view
        View levelView = confirmExitDialog.findViewById(R.id.level);
        levelView.setVisibility(View.GONE);
        TextView question = (TextView) confirmExitDialog.findViewById(R.id.pause);
        question.setText("Do you really want to quit?");
        ImageButton confirmExit = (ImageButton) confirmExitDialog.findViewById(R.id.confirmSurrender);
        ImageButton cancelExit = (ImageButton) confirmExitDialog.findViewById(R.id.cancelSurrender);
        confirmExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: maybe display result
                startActivity(new Intent(getApplicationContext(), PracticeSetting.class));
            }
        });
        cancelExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirmExitDialog.cancel();
            }
        });
        exitButton = (ImageButton) findViewById(R.id.exitButton);
        exitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirmExitDialog.show();
            }
        });


        this.random = new Random(new Date().getTime());
        this.lengthPerQuestion = 10;
        RoomSettings thisSettings = (RoomSettings) getIntent().getParcelableExtra("roomSettings");
        this.correctNumber = 0;
        this.wrongNumber = 0;
        correctNumberTV = (TextView) findViewById(R.id.correctNumber);
        correctNumberTV.setText(Integer.toString(correctNumber));
        salahNomborTV = (TextView) findViewById(R.id.salahNombor);
        salahNomborTV.setText(Integer.toString(wrongNumber));
        this.minDigit = thisSettings.minimumDigit;
        this.maxDigit = thisSettings.maximumDigit;
        availableQuestionTypes = new ArrayList<Adevnture.QuestionType>();
        if (thisSettings.operators[0]) {
            this.availableQuestionTypes.add(Adevnture.QuestionType.ADDITION);
        }
        if (thisSettings.operators[1]) {
            this.availableQuestionTypes.add(Adevnture.QuestionType.ADDITION_AND_SUBTRACTION);
        }
        if (thisSettings.operators[2]) {
            this.availableQuestionTypes.add(Adevnture.QuestionType.MULTIPLICATION);
        }
        if (thisSettings.operators[3]) {
            this.availableQuestionTypes.add(Adevnture.QuestionType.DIVISION);
        }
        this.flashSpeedInMillisecond = thisSettings.flashSpeed;
        flashCard = (TextView) findViewById(R.id.flashCard);
        inputAnswer = (TextView) findViewById(R.id.inputAnswer);
        inputAnswer.setText("");
        this.numpad = new ArrayList<>();
        numpad.add((Button) findViewById(R.id.button0));
        numpad.add((Button) findViewById(R.id.button1));
        numpad.add((Button) findViewById(R.id.button2));
        numpad.add((Button) findViewById(R.id.button3));
        numpad.add((Button) findViewById(R.id.button4));
        numpad.add((Button) findViewById(R.id.button5));
        numpad.add((Button) findViewById(R.id.button6));
        numpad.add((Button) findViewById(R.id.button7));
        numpad.add((Button) findViewById(R.id.button8));
        numpad.add((Button) findViewById(R.id.button9));
        for(int i = 0; i < numpad.size(); i++) {
            int finalI = i;
            numpad.get(i).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    inputAnswer.setText(String.format("%s%d", inputAnswer.getText().toString(), finalI));
                }
            });
        }
        backspace = (Button) findViewById(R.id.buttonBackspace);
        backspace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (inputAnswer.getText().toString().equals("")) {
                    return;
                }
                String currentAnswer = inputAnswer.getText().toString();
                inputAnswer.setText(currentAnswer.substring(0, currentAnswer.length() - 1));
            }
        });
        enter = (Button) findViewById(R.id.buttonEnter);
        enter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (inputAnswer.getText().toString().equals("")) {
                    wrongNumber++;
                    salahNomborTV.setText(Integer.toString(wrongNumber));
                    return;
                }
                if (Integer.parseInt(inputAnswer.getText().toString()) == currentQuestion.answer) {
                    // if correct answer
                    correctNumber++;
                    correctNumberTV.setText(Integer.toString(correctNumber));
                } else {
                    wrongNumber++;
                    salahNomborTV.setText(Integer.toString(wrongNumber));
                }
                inputAnswer.setText("");
                currentQuestion = nextQuestion;
                generateNextQuestion();
                flashQuestion();
            }
        });
        generateNextQuestion();
        currentQuestion = nextQuestion;
        generateNextQuestion();
        startCountdown();

    }
    // Countdown timer
    private void startCountdown() {
        Timer countdownTimer = new Timer();
        TimerTask countdownTask = new TimerTask() {
            int counter = 3;
            @Override
            public void run() {
                if (counter > 0) {
                    flashCard.setText(Integer.toString(counter));
                    flashCard.setTextColor(counter == 3? Color.GREEN: counter == 2? Color.YELLOW: counter == 1? Color.RED: Color.BLACK);
                    counter--;
                } else {
                    flashCard.setText("Go!");
                    flashCard.setTextColor(Color.BLACK);
                    countdownTimer.cancel();
                    flashQuestion();
                }
            }
        };
        countdownTimer.scheduleAtFixedRate(countdownTask, 0, 1000);
    }
    // Game question
    private void flashQuestion() {
        Timer questionTimer = new Timer();
        TimerTask showNumber = new TimerTask() {
            int counter = 0;
            @Override
            public void run() {
                if (currentQuestion.type == Adevnture.QuestionType.MULTIPLICATION) {
                    // if question is multiplication
                    flashCard.setText(String.format("%d x %d", currentQuestion.numbers.get(0), currentQuestion.numbers.get(1)));
                    questionTimer.cancel();
                } else if (currentQuestion.type == Adevnture.QuestionType.DIVISION) {
                    // if question is division
                    flashCard.setText(String.format("%d / %d", currentQuestion.numbers.get(0), currentQuestion.numbers.get(1)));
                    questionTimer.cancel();
                } else if (counter < lengthPerQuestion) {
                    flashCard.setText(currentQuestion.numbers.get(counter).toString());
                    counter++;
                } else {
                    flashCard.setText(/*Integer.toString(currentQuestion.answer)*/"Enter answer");
                    questionTimer.cancel();
                }
            }
        };
        TimerTask disappearNumber = new TimerTask() {
            int counter = 0;
            @Override
            public void run() {
                if (counter < lengthPerQuestion) {
                    flashCard.setText("");
                    counter++;
                } else {
                    this.cancel();
                }
            }
        };
        questionTimer.scheduleAtFixedRate(showNumber, 1000, this.flashSpeedInMillisecond);
        questionTimer.scheduleAtFixedRate(disappearNumber, 1750, this.flashSpeedInMillisecond);
    }
    private void generateNextQuestion() {
        Adevnture.QuestionType nextQuestionType = availableQuestionTypes.get(this.random.nextInt(availableQuestionTypes.size()));
        nextQuestion = new Question(nextQuestionType);
    }

    /* Utility functions for question generation */
    // return random number based on max digit and min digit
    private int getRandomNumber() {
        int range = (int) Math.pow(10, maxDigit) - (int) Math.pow(10, minDigit - 1); // determine range
        return (1 + (int) Math.pow(10, minDigit - 1) + random.nextInt(range - 1));
    }
    // sum the array
    private int sumArray(ArrayList<Integer> number) {
        int sum = 0;
        for(Integer i: number){
            sum += i;
        }
        return sum;
    }

}
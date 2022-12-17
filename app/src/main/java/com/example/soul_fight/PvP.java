package com.example.soul_fight;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class PvP extends AppCompatActivity {

    boolean sendDamageFlag;
    double sendingDamage;

    private ImageView playerCharacter;
    private ImageView enemyCharacter;
    private TextView playerHealthTV;
    private TextView enemyHealthTV;
    TextView flashCard;
    private TextView inputAnswer;
    private ArrayList<Button> numpad;
    private Button backspace;
    private Button enter;
    private long recordedTime;

    Dialog surrenderDialog;
    Dialog victoryDialog;
    Dialog defeatDialog;

    /* Game Settings */
    private double playerHealth;
    private double enemyHealth;
    private int maxDigit;
    private int minDigit;
    private int lengthPerQuestion;
    private int seed;
    private int flashSpeedInMillisecond;
    private ArrayList<QuestionType> availableQuestionTypes;
    private Random random;
    public enum QuestionType {
        ADDITION,
        ADDITION_AND_SUBTRACTION,
        MULTIPLICATION,
        DIVISION
    }
    private int correctStreak;
    public class Question {
        public ArrayList<Integer> numbers;
        public QuestionType type;
        public int answer;
        public Question(QuestionType type) {
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
    private Question currentQuestion;
    private Question nextQuestion;

    boolean thereIsAWinner = false;
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
            socket = mBoundService.socket;
            try {
                ois = new ObjectInputStream(socket.getInputStream());
                oos = new ObjectOutputStream(socket.getOutputStream());
            } catch (IOException e) {
                e.printStackTrace();
            }
            Thread listeningToSeed = new Thread() {
                @Override
                public void run() {
                    try {
                        seed = (int) ois.readObject();
                        random = new Random(seed);
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            };
            listeningToSeed.start();
            try {
                listeningToSeed.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            new Thread() {
                @Override
                public void run() {
                    double dmgFromServer;
                    while(socket.isConnected() && !thereIsAWinner){
                        try{
                            dmgFromServer = (Double) ois.readObject();
                            if(dmgFromServer < 0){ //When game is over will send -1 or -2
                                //game over
                                thereIsAWinner = true;
                                if(dmgFromServer == -1){
                                    victoryDialog.show();
                                    System.out.println("You have won" + dmgFromServer);
                                }
                                if(dmgFromServer == -2){
                                    defeatDialog.show();
                                    System.out.println("You have lost" + dmgFromServer);
                                }
                            }else{
                                System.out.println("Damage from server: "+dmgFromServer);
                                receivedDamage(dmgFromServer);
                            }

                        }catch(IOException | ClassNotFoundException e){
                            System.out.println(e);
                        }
                    }
                }
            }.start();

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
        setContentView(R.layout.activity_pv_p);

        doBindService();

        // dialogs setup
        surrenderDialog = new Dialog(PvP.this);
        victoryDialog = new Dialog(PvP.this);
        defeatDialog = new Dialog(PvP.this);
        surrenderDialog.setContentView(R.layout.custom_dialog);
        victoryDialog.setContentView(R.layout.win);
        defeatDialog.setContentView(R.layout.lose);
        surrenderDialog.getWindow().setBackgroundDrawable(getDrawable(R.drawable.background));
        victoryDialog.getWindow().setBackgroundDrawable(getDrawable(R.drawable.background));
        defeatDialog.getWindow().setBackgroundDrawable(getDrawable(R.drawable.background));

        surrenderDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        victoryDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        defeatDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        surrenderDialog.setCancelable(false);
        victoryDialog.setCancelable(false);
        defeatDialog.setCancelable(false);

        ImageButton homeButton = (ImageButton) defeatDialog.findViewById(R.id.home);
        // exit to home
        homeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), HomeActivity.class));
            }
        });
        ImageButton exitButton = (ImageButton) defeatDialog.findViewById(R.id.exit);
        // exit to create/join room selection
        exitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), PvPActivity.class));
            }
        });

        surrenderDialog.findViewById(R.id.level).setVisibility(View.GONE); // disable level view
        ImageButton surrenderButton = (ImageButton) findViewById(R.id.surrenderButton);
        surrenderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                surrenderDialog.show();
            }
        });
        ImageButton cancelSurrender = (ImageButton) surrenderDialog.findViewById(R.id.cancelSurrender);
        ImageButton confirmSurrender = (ImageButton) surrenderDialog.findViewById(R.id.confirmSurrender);
        cancelSurrender.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                surrenderDialog.cancel();
            }
        });
        confirmSurrender.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: send surrender to server !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!

                defeatDialog.show();
            }
        });

        playerCharacter = (ImageView) findViewById(R.id.playerCharacter);
        enemyCharacter = (ImageView) findViewById(R.id.enemyCharacter);
        playerHealthTV = (TextView) findViewById(R.id.playerHealthTV);
        enemyHealthTV = (TextView) findViewById(R.id.enemyHealthTV);
        playerHealth = 100;
        enemyHealth = 100;
        flashCard = findViewById(R.id.flashCard);
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
                    correctStreak = 0;
                } else if (Integer.parseInt(inputAnswer.getText().toString()) == currentQuestion.answer) {
                    // if correct answer
                    long currentTime = (long) System.currentTimeMillis();
                    long deltaTime = currentTime - recordedTime;
                    long damage = (long) 20000 - deltaTime;
                    int convertedDamage = damage < 0? 0: (int) (damage / 1000) * 2;
                    if (currentQuestion.type == QuestionType.MULTIPLICATION || currentQuestion.type == QuestionType.DIVISION) {
                        // if question is multiplication or division, double the damage
                        convertedDamage *= 2;
                    }
                    correctStreak++;
                    Timer attackTimer = new Timer();
                    TimerTask playerAttackTask = new TimerTask() {
                        int counter = 0;
                        @Override
                        public void run() {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if (counter == 0) {
                                        playerCharacter.setImageResource(R.drawable.player_attack);
                                        counter++;
                                    } else if (counter == 1) {
                                        playerCharacter.setImageResource(R.drawable.player_idle);
                                        counter++;
                                    } else {
                                        attackTimer.cancel();
                                    }
                                }
                            });

                        }
                    };
                    attackTimer.scheduleAtFixedRate(playerAttackTask, 0, 750);
                    enemyHealth -= convertedDamage;
                    if (enemyHealth <= 0) {
                        victoryDialog.show();
                        ImageButton homeButton = (ImageButton) victoryDialog.findViewById(R.id.home);
                        homeButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                startActivity(new Intent(getApplicationContext(), HomeActivity.class));
                            }
                        });
                        ImageButton exitButton = (ImageButton) victoryDialog.findViewById(R.id.exit);
                        exitButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                startActivity(new Intent(getApplicationContext(), PvPActivity.class));
                            }
                        });
                    }
                    enemyHealthTV.setText(String.format("%d/100", (int)enemyHealth));
                    // TODO: send converted damage to server
                    System.out.println("deltaTime = " + deltaTime);
                    System.out.println("damage = " + convertedDamage);
                } else {
                    correctStreak = 0;
                }
                inputAnswer.setText("");
                currentQuestion = nextQuestion;
                generateNextQuestion();
                flashQuestion();
            }
        });
        this.lengthPerQuestion = 10;
        RoomSettings thisSettings = (RoomSettings) getIntent().getParcelableExtra("roomSettings");
        this.minDigit = thisSettings.minimumDigit;
        this.maxDigit = thisSettings.maximumDigit;
        availableQuestionTypes = new ArrayList<QuestionType>();
        if (thisSettings.operators[0]) {
            this.availableQuestionTypes.add(QuestionType.ADDITION);
        }
        if (thisSettings.operators[1]) {
            this.availableQuestionTypes.add(QuestionType.ADDITION_AND_SUBTRACTION);
        }
        if (thisSettings.operators[2]) {
            this.availableQuestionTypes.add(QuestionType.MULTIPLICATION);
        }
        if (thisSettings.operators[3]) {
            this.availableQuestionTypes.add(QuestionType.DIVISION);
        }

        // generate first two questions
        generateNextQuestion();
        currentQuestion = nextQuestion;
        generateNextQuestion();

        // start countdown, after countdown start first question
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
                    // TODO: perhaps set animation
                } else {
                    flashCard.setText("Go!");
                    flashCard.setTextColor(Color.BLACK);
                    countdownTimer.cancel();
                    // start the first question
                    flashQuestion();
                }
            }
        };
        countdownTimer.scheduleAtFixedRate(countdownTask, 0, 1000);
    }
    public void receivedDamage(double damage) {
        // enemy attack
        Timer attackTimer = new Timer();
        TimerTask enemyAttackTask = new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        enemyCharacter.setImageResource(R.drawable.enemy_attack);
                    }
                });
            }
        };
        TimerTask enemyIdleTask = new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        enemyCharacter.setImageResource(R.drawable.enemy_idle);
                    }
                });
            }
        };
        attackTimer.schedule(enemyAttackTask, 0);
        attackTimer.schedule(enemyIdleTask, 750);
        playerHealth -= damage;
        if (playerHealth <= 0) {
            // TODO: display defeated message, exit to PvPActivity
        }
        playerHealthTV.setText(String.format("%d/100", (int)playerHealth));
    }
    // Game question
    private void flashQuestion() {
        Timer questionTimer = new Timer();
        TimerTask showNumber = new TimerTask() {
            int counter = 0;
            @Override
            public void run() {
                if (currentQuestion.type == QuestionType.MULTIPLICATION) {
                    // if question is multiplication
                    flashCard.setText(String.format("%d x %d", currentQuestion.numbers.get(0), currentQuestion.numbers.get(1)));
                    questionTimer.cancel();
                } else if (currentQuestion.type == QuestionType.DIVISION) {
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
        recordedTime = (long) System.currentTimeMillis();
        questionTimer.scheduleAtFixedRate(showNumber, 1000, this.flashSpeedInMillisecond);
        questionTimer.scheduleAtFixedRate(disappearNumber, 1750, this.flashSpeedInMillisecond);
    }
    // if correctStreak < 5, generate addition or addition with subtraction question, else generate division or multiplication question
    private void generateNextQuestion() {
        QuestionType nextQuestionType;
        if (this.correctStreak < 5) {
            nextQuestionType = random.nextInt(2) < 1? QuestionType.ADDITION: QuestionType.ADDITION_AND_SUBTRACTION;
        } else {
            correctStreak = 0;
            nextQuestionType = random.nextInt(2) < 1? QuestionType.MULTIPLICATION: QuestionType.DIVISION;
        }
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        doUnbindService();
    }
}
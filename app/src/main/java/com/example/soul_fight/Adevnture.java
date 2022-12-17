package com.example.soul_fight;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Date;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class Adevnture extends AppCompatActivity {

    private ImageView playerCharacter;
    private ImageView enemyCharacter;
    private TextView playerHealthTV;
    private TextView enemyHealthTV;
    private ImageButton button;
    Dialog dialog;
    TextView flashCard;
    private TextView inputAnswer;
    private ArrayList<Button> numpad;
    private Button backspace;
    private Button enter;
    private long recordedTime;

    /* Game Settings */
    private double playerHealth;
    private double enemyHealth;
    private int maxDigit;
    private int minDigit;
    private int lengthPerQuestion;
    private int flashSpeedInMillisecond;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_adevnture);

        dialog = new Dialog(Adevnture.this);
        dialog.setContentView(R.layout.custom_dialog);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            dialog.getWindow().setBackgroundDrawable(getDrawable(R.drawable.background));
        }

        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.setCancelable(false);
        ImageButton restart = dialog.findViewById(R.id.restart);
        ImageButton play = dialog.findViewById(R.id.play);
        ImageButton home = dialog.findViewById(R.id.home);
        ImageButton exit = dialog.findViewById(R.id.exit);

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
                    playerHealth -= 10.0;
                    if (playerHealth <= 0) {
                        // TODO: display defeated message, exit to level selection
                    }
                    playerHealthTV.setText(String.format("%d/100", (int)playerHealth));

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
                        // TODO: display victory message, exit to level selection
                    }
                    enemyHealthTV.setText(String.format("%d/100", (int)enemyHealth));
                    System.out.println("deltaTime = " + deltaTime);
                    System.out.println("damage = " + convertedDamage);
                } else {
                    correctStreak = 0;
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
                    playerHealth -= 10;
                    if (playerHealth <= 0) {
                        // TODO: display defeated message, exit to level selection
                    }
                    playerHealthTV.setText(String.format("%d/100", (int)playerHealth));
                }
                inputAnswer.setText("");
                currentQuestion = nextQuestion;
                generateNextQuestion();
                flashQuestion();
            }
        });

        restart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // this is restart btn to restart the game, in custom_dialog

            }
        });
        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // this is play btn, resume the game

            }
        });
        home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // this is home btn, to back to home

            }
        });
        exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // this is exit btn, to back to adv page
            }
        });
        button = findViewById(R.id.imageButton);

        /* Initialize Game Settings
            +-------+---------------+---------------+-----------------+-------------------+
            | Level | Minimum Digit | Maximum Digit | Question Length | Flash Speed (sec) |
            +-------+---------------+---------------+-----------------+-------------------+
            |     0 |             1 |             1 |               5 |                 1 | // TODO: may act as tutorial level
            +-------+---------------+---------------+-----------------+-------------------+
            |     1 |             1 |             1 |              12 |                 1 |
            +-------+---------------+---------------+-----------------+-------------------+
            |     2 |             1 |             2 |              11 |                 1 |
            +-------+---------------+---------------+-----------------+-------------------+
            |     3 |             2 |             2 |              10 |               1.4 |
            +-------+---------------+---------------+-----------------+-------------------+
            |     4 |             2 |             3 |               9 |               1.4 |
            +-------+---------------+---------------+-----------------+-------------------+
            |     5 |             3 |             3 |               8 |              1.75 |
            +-------+---------------+---------------+-----------------+-------------------+
         */
        Intent lastIntent = getIntent();
        int level = lastIntent.getIntExtra("level", 0);
        int levelSetMinDigit[] = {1, 1, 1, 2, 2, 3};
        int levelSetMaxDigit[] = {1, 1, 2, 2, 3, 3};
        int levelSetQuestionLength[] = {5, 12, 11, 10, 9, 8};
        int levelSetFlashSpeed[] = {1000, 1000, 1000, 1400, 1400, 1750};
        this.minDigit = levelSetMinDigit[level];
        this.maxDigit = levelSetMaxDigit[level];
        this.lengthPerQuestion = levelSetQuestionLength[level];
        this.flashSpeedInMillisecond = levelSetFlashSpeed[level];
        long seed = (long) new Date().getTime();
        random = new Random(seed);

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
}
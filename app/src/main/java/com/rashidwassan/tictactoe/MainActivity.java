package com.rashidwassan.tictactoe;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;


import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.tomer.fadingtextview.FadingTextView;

import java.util.Random;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity
{


    TextView status, plr1sts, plr2sts;
    String winnerStr;

    Dialog dialog;

    // For our custom toast...
    TextView toastText;
    ImageView toastImage;
    View layout;

    String player1name;
    String player2name;

    int p1WinStreak = 0;
    int p2WinStreak = 0;

    boolean gameActive = true;

    // considering players
    // 0 - X
    // 1 - o
    int activePlayer = 0; // starting turn is of X.

    // Box states
    // 0 - x
    // 1 - 0
    // 2 - null, therefore all array variables are set 2 i.e blank.
    int[] gameState = {2, 2, 2, 2, 2, 2, 2, 2, 2};

    int[][] winPositions = {{0,1,2},    {3,4,5},    {6,7,8}, // horizontal win positions.
            {0,3,6},    {1,4,7},    {2,5,8}, // vertical win positions.
            {0,4,8},    {2,4,6}};



    // For sound effects...
    private SoundPool soundPool;
    // Creating ids for sound files to be used. can be reused.
    private int cupshockwave;
    private int tadawinsound;

    MediaPlayer player;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // For background music...
        if(player == null)
        {

            player = MediaPlayer.create(this, R.raw.mainbgmusic);

        }
        player.start();


        // Giving first turn to random opponent...
         Random random = new Random();
        int randomnum = random.nextInt(2);

        if(randomnum == 0)
            {

                player1name = playerNames.player1name;
                player2name = playerNames.player2name;

            }

            else
            {

                player2name = playerNames.player1name;
                player1name = playerNames.player2name;

            }


         //Creating our custom toast. furhter settings can be made in xml file.
        LayoutInflater inflater = getLayoutInflater();
        layout = inflater.inflate(R.layout.toast_layout, (ViewGroup) findViewById(R.id.toast_root));
        toastText = layout.findViewById(R.id.toast_text);
        toastImage = layout.findViewById(R.id.toast_image);

        // checking which version of android device is running...
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            {

                // New way of creating sound pool.
                AudioAttributes audioAttributes = new AudioAttributes.Builder()
                 // Usage best for gaming.
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();

                soundPool = new SoundPool.Builder()
                .setMaxStreams(3)
                .setAudioAttributes(audioAttributes)
                .build();
            }
        else
            {
               // if device has api level less than 21 (Lollipoyp).
               // Using legacy method for sound pool creation.
                soundPool = new SoundPool(3, AudioManager.STREAM_MUSIC, 0);
            }

        // Loading sounds...
        cupshockwave = soundPool.load(this, R.raw.playerwon, 1);
        tadawinsound = soundPool.load(this, R.raw.tadawinner, 1);

        Toast t = Toast.makeText(this, ""+ player1name + " Got \'X\' \n" + player2name + " Got \'O\'", Toast.LENGTH_LONG);
        t.setGravity(Gravity.CENTER, 0, 0);
        t.show();

        status = findViewById(R.id.status);
        status.setText(player1name + "\'s Turn");


        // For Displaying names beside Scorecard.
        plr1sts = findViewById(R.id.player1sts);
        plr1sts.setText(player1name + ":  " + p1WinStreak + "pts");
        plr2sts = findViewById(R.id.player2sts);
        plr2sts.setText(player2name + ":  " + p2WinStreak + "pts");

    }

    // For custom dialog...
    void showWinDialog()
    {

        LayoutInflater inflater = LayoutInflater.from(this);
        View view = inflater.inflate(R.layout.playerwindialog, null);

        TextView wintext = view.findViewById(R.id.winnerdiagtext);
        wintext.setText(winnerStr);

        Button continuebtn = view.findViewById(R.id.winscrcontinuebtn);
        continuebtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                dialog.dismiss();
            }
        });


        dialog = new Dialog(this, android.R.style.Theme_DeviceDefault_Light_NoActionBar_Fullscreen);
        dialog.setContentView(view);

        dialog.show();

    }


    public void playerTap(View view)
    {

        ImageView img = (ImageView) view;
        int tappedImage = Integer.parseInt(img.getTag().toString());

        if(!gameActive)
       {
            gameReset(view);
       }

        if(gameState[tappedImage] == 2 && gameActive)
            {
                gameState[tappedImage] = activePlayer;
                img.setTranslationY(-1000f);

                if(activePlayer == 0) // 0 - X
                    {
                         img.setImageResource(R.drawable.x);
                         activePlayer = 1;

                        status.setText(player2name + "\'s Turn");
                    }

                else
                    {
                        img.setImageResource(R.drawable.o);
                        activePlayer = 0;

                        status.setText(player1name + "\'s Turn"); // O turn
                    }

                img.animate().translationYBy(1000f).setDuration(300);
            }

        // Check if any player has won
        for(int[] winPosition: winPositions)
        {
           if(gameState[winPosition[0]] == gameState[winPosition[1]] &&
                   gameState[winPosition[1]] == gameState[winPosition[2]] &&
                    gameState[winPosition[0]]!=2)
           {
               //somebody has won, lets find out!
               gameActive = false;

               if(gameState[winPosition[0]] == 0)
               {

                   // For playing sound playerwon. this line should be duplicated for other sounds.
                   soundPool.play(tadawinsound,1,1, 1, 0, 1);
                   soundPool.play(cupshockwave,1,1, 1, 0, 1);



                   winnerStr = player1name + " has Won!";
                   ++p1WinStreak;

                   showWinDialog();

                   plr1sts.setText(player1name + ":  " + p1WinStreak + "pts");

               }

               else
               {

                   // For playing sound playerwon. this line should be duplicated for other sounds.
                   soundPool.play(tadawinsound,1,1, 1, 0, 1);
                   soundPool.play(cupshockwave,1,1, 1, 0, 1);


                   winnerStr = player2name + " has Won!";
                   ++p2WinStreak;

                 //  toast.show();
                   showWinDialog();

                   plr2sts.setText(player2name + ":  " + p2WinStreak + "pts");

               }
                status.setText(winnerStr);
           }
        }
    }

    public void gameReset(View view)
    {
        gameActive = true;
        activePlayer = 0;

        toastText.setText("Game Reset!");
        toastImage.setImageResource(R.drawable.reseticon);
        toastImage.setTranslationY(1500f);
        toastImage.animate().translationYBy(-1500f).setDuration(400);
        Toast toast = new Toast(getApplicationContext());
       toast.setGravity(Gravity.FILL, 0, 0);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setView(layout);


        toast.show();

        for(int i = 0; i < gameState.length; i++)
        {
            gameState[i] = 2;
        }
        ((ImageView)findViewById(R.id.imageView0)).setImageResource(0);
        ((ImageView)findViewById(R.id.imageView1)).setImageResource(0);
        ((ImageView)findViewById(R.id.imageView2)).setImageResource(0);
        ((ImageView)findViewById(R.id.imageView3)).setImageResource(0);
        ((ImageView)findViewById(R.id.imageView4)).setImageResource(0);
        ((ImageView)findViewById(R.id.imageView5)).setImageResource(0);
        ((ImageView)findViewById(R.id.imageView6)).setImageResource(0);
        ((ImageView)findViewById(R.id.imageView7)).setImageResource(0);
        ((ImageView)findViewById(R.id.imageView8)).setImageResource(0);

        status.setText(player1name + "\'s Turn");

    }

    // Creating alert dialog when back key is pressed...
    @Override
    public void onBackPressed()
    {

        new AlertDialog.Builder(this)
                .setTitle("Quit Game!!!")
                .setMessage("Are you sure you want to quit?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        finish();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        dialog.dismiss();
                    }
                })
                .create().show();

    }

    @Override
    protected void onResume()
    {
        player.start();
        super.onResume();
    }

    @Override
    protected void onPause()
    {
        player.pause();
        super.onPause();
    }

    @Override
    protected void onDestroy()
    {

        // cleaning sound pool to freeup memory,
        soundPool.release();
        soundPool = null;

        // releasing media player...

        if(player != null)
        {
            player.release();
            player = null;
        }
        super.onDestroy();

    }
}

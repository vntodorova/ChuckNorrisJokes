package com.example.venetatodorova.chucknorrisjokes;

import android.app.Activity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

public class MainActivity extends Activity {

    private CountdownView countdownView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        countdownView = (CountdownView) findViewById(R.id.countdown);
    }

    public void startCountdown(View view) {
        countdownView.start(5);
    }
}

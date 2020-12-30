package com.example.recorderhrv;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import org.w3c.dom.Text;

public class MainActivity4 extends AppCompatActivity {

    TextView t1,t2,t3,t4,t5,t6;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main4);


        Intent intent = getIntent();
        double max = Math.round(intent.getDoubleExtra("max",0));
        double min = Math.round(intent.getDoubleExtra("min",0));
        double SDNN = Math.round(intent.getDoubleExtra("SDNN",0));
        double MSSD = Math.round(intent.getDoubleExtra("MSSD",0));
        double mean = Math.round(intent.getDoubleExtra("mean",0));
        int numberOfIntervals = intent.getIntExtra("numOfInt", 0);

        t1 = (TextView)findViewById(R.id.textView6);
        t2 = (TextView)findViewById(R.id.textView7);
        t3 = (TextView)findViewById(R.id.textView8);
        t4 = (TextView)findViewById(R.id.textView9);
        t5 = (TextView)findViewById(R.id.textView10);
        t6 = (TextView)findViewById(R.id.textView11);

        t1.append(String.valueOf(mean));
        t2.append(String.valueOf(max));
        t3.append(String.valueOf(min));
        t4.append(String.valueOf(SDNN));
        t5.append(String.valueOf(MSSD));
        t6.append(String.valueOf(numberOfIntervals));

    }
}
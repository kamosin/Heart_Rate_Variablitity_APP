package com.example.recorderhrv;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;

import java.util.ArrayList;

public class MainActivity2 extends AppCompatActivity {

    BarChart barChart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        barChart = (BarChart)findViewById(R.id.barGraph);

        Intent intent = getIntent();
        int []a = intent.getIntArrayExtra("ARRAY");
        show(a);


    }

    void show (int []array){
        ArrayList<BarEntry> entries = new ArrayList<>();

        for(int i = 0; i<90;i++){
            int x = 250+i*25;
            int y = array[i];
            entries.add(new BarEntry(x, y));
        }
        BarDataSet barDataSet = new BarDataSet(entries, "Liczba uderzeń w poszczególnych przedziałach");

        ArrayList<String> timestamps = new ArrayList<>();
        for(int i = 0; i<90;i++){
            StringBuilder builder = new StringBuilder();
            builder.append(250+ 25*i);
            builder.append(" - ");
            builder.append(250+25*(i+1));
            timestamps.add(builder.toString());
        }



        BarData theData = new BarData(barDataSet);
        theData.setBarWidth(25f);
        theData.setDrawValues(false);
        barChart.setData(theData);


        barChart.setTouchEnabled(true);
        barChart.setDragEnabled(true);
        barChart.setScaleEnabled(true);
        barChart.setFitBars(true); // make the x-axis fit exactly all bars
        barChart.invalidate(); // refresh
    }
}
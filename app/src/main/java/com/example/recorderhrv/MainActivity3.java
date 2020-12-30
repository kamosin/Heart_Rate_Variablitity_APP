package com.example.recorderhrv;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.DefaultAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.listener.OnChartGestureListener;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;

import java.util.ArrayList;

public class MainActivity3 extends AppCompatActivity{


    LineChart lineChart;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main3);

        Intent intent = getIntent();
        float[] audioBytes =intent.getFloatArrayExtra("floats");


        lineChart = findViewById(R.id.linechart);
        LineDataSet lineDataSet = new LineDataSet((lineChartDataSet(audioBytes)), "data set");
        ArrayList<ILineDataSet> iLineDataSets = new ArrayList<>();
        iLineDataSets.add(lineDataSet);

        LineData lineData = new LineData(iLineDataSets);
        lineChart.setData(lineData);
        lineDataSet.setDrawCircles(false);
        lineDataSet.setColor(Color.RED);
        XAxis xAxis = lineChart.getXAxis();
        xAxis.setAxisMaximum(30);
        lineChart.invalidate();

    }

    private ArrayList<Entry> lineChartDataSet(float[] audiobytes){
        ArrayList<Entry> dataSet = new ArrayList<Entry>();

        for(int i = 0;i<audiobytes.length;i++){
            float x = i/44.1f;
            float y = audiobytes[i];
            dataSet.add(new Entry(x,y));
        }




        return dataSet;

    }

}
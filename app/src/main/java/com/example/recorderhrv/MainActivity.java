package com.example.recorderhrv;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;


import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.List;


import be.tarsos.dsp.AudioEvent;

import be.tarsos.dsp.GainProcessor;

import be.tarsos.dsp.filters.HighPass;
import be.tarsos.dsp.filters.LowPassFS;

import be.tarsos.dsp.io.TarsosDSPAudioFormat;


import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.Manifest.permission.RECORD_AUDIO;

public class MainActivity extends AppCompatActivity implements Serializable{
    Thread recordingThread;
    boolean isRecording = false;

    List<Double> time_intervals = new ArrayList<>();
    Double maxInterval;
    Double minInterval;
    double SDNN;
    double MSSD;
    int numbersOfIntervals;
    double mean;

    Button btnRecord,  btnStats, btnHistogram, btnPlot, btnAnalyze;

    TextView text, text2;
    private static final int RECORDER_SAMPLERATE = 44100;
    private static final int RECORDER_CHANNELS = AudioFormat.CHANNEL_IN_MONO;
    private static final int RECORDER_AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;
    int bufferSizeInBytes = AudioRecord.getMinBufferSize(RECORDER_SAMPLERATE, RECORDER_CHANNELS, RECORDER_AUDIO_ENCODING);
    AudioRecord audioRecorder;

    public static final int REQUEST_AUDIO_PERMISSION_CODE = 1;
    byte Data[] = new byte[bufferSizeInBytes];
    float[] audioFloats;
    byte[] audioBytes;
    TarsosDSPAudioFormat format;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        text = (TextView)findViewById(R.id.textView);
        text2 = (TextView)findViewById(R.id.leftTime);
        text.setText("30");
        btnRecord = (Button)findViewById(R.id.btnRecord);
        btnStats = (Button)findViewById(R.id.statistics);
        btnHistogram = (Button)findViewById(R.id.button);
        btnPlot = (Button)findViewById(R.id.wykres);
        btnAnalyze = (Button)findViewById(R.id.analyze);



        btnStats.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openActivityStats();

            }
        });

        btnHistogram.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openActivityHistogram();
            }
        });

        btnPlot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openActivityPlot();
            }
        });



        btnRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(CheckPermissions()) {
                    if(audioRecorder ==null){
                        audioRecorder = new AudioRecord(MediaRecorder.AudioSource.CAMCORDER,
                                RECORDER_SAMPLERATE, RECORDER_CHANNELS,
                                RECORDER_AUDIO_ENCODING, bufferSizeInBytes);
                    }
                    startRecording();
                    Toast.makeText(getApplicationContext(), "Rozpoczęto nagrywanie", Toast.LENGTH_LONG).show();
                    btnAnalyze.setEnabled(true);

                }
                else
                {
                    RequestPermissions();
                }

            }
        });

        btnAnalyze.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                analyzePCM();
            }
        });
    }

    public void openActivityStats(){

        Intent intent = new Intent(this, MainActivity4.class);
        intent.putExtra("max", maxInterval );
        intent.putExtra("min", minInterval);
        intent.putExtra("SDNN", SDNN );
        intent.putExtra("MSSD", MSSD );
        intent.putExtra("numOfInt", numbersOfIntervals);
        intent.putExtra("mean", mean);

        startActivity(intent);

    }
    public void openActivityHistogram(){
        int []array = new int[90];
        array = histogram(time_intervals);
        Intent intent = new Intent(this,MainActivity2.class);
        intent.putExtra("ARRAY", array);
        startActivity(intent);

    }

    public void openActivityPlot(){
        int len = (audioFloats.length)/1000+100;
        float [] audio = new float[len];
        int licznik = 0;
        for(int i =0;i<audioFloats.length;i++){
            if(i%1000==0){
                audio[licznik]=audioFloats[i];
                licznik++;
            }
        }

        Intent intent = new Intent(this,MainActivity3.class);
        intent.putExtra("floats", audio);
        startActivity(intent);

    }

    public void analyzePCM(){

        format = new TarsosDSPAudioFormat((float) RECORDER_SAMPLERATE, 16, 1, true, false);
        ShortBuffer sbuf = ByteBuffer.wrap(audioBytes).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer();
        short[] audioShorts = new short[sbuf.capacity()];
        sbuf.get(audioShorts);
        audioFloats = new float[audioShorts.length];
        for (int i = 0; i < audioShorts.length; i++) {
            audioFloats[i] = ((float)audioShorts[i])/0x8000;
        }
        AudioEvent audioEvent = new AudioEvent(format);
        audioEvent.setFloatBuffer(audioFloats);

        GainProcessor gainProcessor = new GainProcessor(3.0);
        gainProcessor.process(audioEvent);

        LowPassFS lowPassFilter = new LowPassFS(200,RECORDER_SAMPLERATE);
        lowPassFilter.process(audioEvent);

        HighPass highPassFilter = new HighPass(50, RECORDER_SAMPLERATE);
        highPassFilter.process(audioEvent);

        double rms = AudioEvent.calculateRMS(audioEvent.getFloatBuffer());
        rms = 20.0 * Math.log10(rms);
        int window = 1024;
        int samples = audioFloats.length;
        int intervals = samples / window;
        double[] war_rms = new double[intervals];
        for(int i = 10; i < intervals; i++){
            if (i*window > samples){
                break;
            }
            float[] buffer = new float[window];
            for(int j = 0; j < window ;j++){
                buffer[j] = audioFloats[i*window+j];
            }
            war_rms[i] = AudioEvent.calculateRMS(buffer);
            war_rms[i]= 20.0 * Math.log10(war_rms[i]);
        }

        // 44100 probek to 1 sekunda
        // 1024 probki to okolo 23 ms
        // 50 BPM to uderzenia co 1200ms, czyli co okolo 51.68 (okienek) (min)
        // 180 BPM to uderzenia co 333ms, czyli co okolo 14.35 probek (max)
        int beats = 0;

        List<Integer> index = new ArrayList<>();

        // odfiltrowanie syfu poprzez zliczenie probek przekraczajacych srednie RMS,
        // oraz spelniajace warunek ze BPM nie przekroczy 180 BPM

        for(int i = 0; i < war_rms.length; i++) {
            if (war_rms[i] > rms) {
                beats++;
                index.add(i);
                i = i + 18;
            }
        }

        double x1 =Double.valueOf(window)*Double.valueOf(index.get(0))+10*intervals;
        double x2 =0;
        double odstep=0;
        for(int n =1; n<index.size(); n++){

            x2 = Double.valueOf(window)*Double.valueOf(index.get(n));
            odstep = x2-x1;
            odstep = 1000*odstep/RECORDER_SAMPLERATE;
            double ods = Math.round(odstep);
            time_intervals.add(ods);
            x1=x2;
        }
        double sum=0;
        mean=0;
        for(int i =0; i<time_intervals.size();i++){
            sum+=time_intervals.get(i);
        }
        mean = sum/time_intervals.size();

        double BPM = Math.round(60000/mean);

        String s = String.valueOf(BPM);

        text.setTextSize(24);
        text.setText("\n" + "Uderzenia " + beats + "\n");
        text.append("BPM " + s + "\n");

        maxInterval = CountMaxInterval(time_intervals);
        minInterval = CountMinInterval(time_intervals);
        SDNN = CountSDNN(time_intervals);
        MSSD = CountMSSD(time_intervals);
        numbersOfIntervals = CountMoreThan50(time_intervals);

        btnPlot.setEnabled(true);
        btnHistogram.setEnabled(true);
        btnStats.setEnabled(true);

    }
    public double CountMaxInterval (List<Double> table){
        double max = table.get(0);
        for(int i =1; i<table.size();i++){
            if(table.get(i)>max){
                max = table.get(i);
            }
        }
        return  max;

    }

    public double CountMinInterval (List<Double> table){
        double min = table.get(0);
        for(int i =1; i<table.size();i++){
            if(table.get(i)<min){
                min = table.get(i);
            }
        }
        return  min;

    }

    public double CountSDNN (List<Double> table){
        double sum =0;
        double mean = 0;
        double element = 0.0;
        for(int i =0; i<table.size();i++){
            sum+=table.get(i);
        }
        mean = sum/(table.size());
        sum=0;

        for(int i =0;i<table.size();i++){
            element = (mean-table.get(i))*(mean-table.get(i));
            sum+=element;
        }
        sum=sum/(double)table.size();
        sum=Math.sqrt(sum);
        return sum;
    }

    public double CountMSSD (List<Double> table){
        double element1 = 0;
        double element2 = 0;
        double sum = 0;
        for(int i =0;i<table.size() -1; i++){
            element1 = table.get(i);
            element2 = table.get(i+1);
            sum += (element2-element1)*(element2-element1);

        }
        sum = sum/(double)(table.size()-1);
        sum = Math.sqrt(sum);

        return sum;

    }

    public int CountMoreThan50 (List<Double> table){
        int number=0;
        double e1 = 0;
        double e2 = 0;
        for(int i=0; i<table.size()-1;i++){
            e1 = table.get(i);
            e2 = table.get(i+1);
            if(Math.abs(e2-e1)>50){
                number++;
            }
        }
        return number;
    }

    public int [] histogram (List<Double> table){

        int counter = 0;
        int start_window = 250;
        int[] intArray = new int[90];
        double val=0;
        for(int i =0; i<90;i++){
            for(int j = 0;j<table.size();j++){
                val = table.get(j);
                if(val>=(start_window + 25*i) && val<(start_window +25*(i+1))){
                    counter++;
                }
            }
            intArray[i]=counter;
            counter=0;
        }
        return intArray;
    }

    public void startRecording(){
        audioRecorder.startRecording();
        isRecording = true;
        recordingThread = new Thread(new Runnable() {
            public void run() {
                int counter = 0;
                int x =1;
                int turn_off = 0;
                int left = 30;
                String z;
                int second = RECORDER_SAMPLERATE/(bufferSizeInBytes/2);
                String filepath = Environment.getExternalStorageDirectory().getPath();
                FileOutputStream os = null;
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                try {
                    os = new FileOutputStream(filepath+"/record.pcm");
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                while(isRecording) {
                    audioRecorder.read(Data, 0, Data.length);
                    try {
                        os.write(Data, 0, bufferSizeInBytes);
                        baos.write(Data, 0, bufferSizeInBytes);
                        counter++;
                        turn_off++;
                        if (counter >second ){
                            if(turn_off>second*30) {
                                left--;
                                stopRecording();
                            }
                            left --;
                            counter=0;
                            z = String.valueOf(left);
                            text.setText(z);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                text2.setText("Kliknij przycisk: DOKONAJ ANALIZY aby zobaczyć wyniki!");
                text.setText("");
                try {
                    os.close();
                    audioBytes = baos.toByteArray();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        recordingThread.start();
        try {
            recordingThread.sleep(0);
            Toast.makeText(getApplicationContext(), "Rozpoczeto nagrywanie", Toast.LENGTH_LONG).show();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    public void stopRecording() {
        if (audioRecorder != null) {
            isRecording = false;
            audioRecorder.stop();
            audioRecorder.release();
            audioRecorder = null;
            recordingThread = null;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_AUDIO_PERMISSION_CODE:
                if (grantResults.length> 0) {
                    boolean permissionToRecord = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean permissionToStore = grantResults[1] ==  PackageManager.PERMISSION_GRANTED;
                    if (permissionToRecord && permissionToStore) {
                        Toast.makeText(getApplicationContext(), "Permission Granted", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(getApplicationContext(),"Permission Denied",Toast.LENGTH_LONG).show();
                    }
                }
                break;
        }
    }
    public boolean CheckPermissions() {
        int result = ContextCompat.checkSelfPermission(getApplicationContext(), WRITE_EXTERNAL_STORAGE);
        int result1 = ContextCompat.checkSelfPermission(getApplicationContext(), RECORD_AUDIO);
        return result == PackageManager.PERMISSION_GRANTED && result1 == PackageManager.PERMISSION_GRANTED;
    }
    private void RequestPermissions() {
        ActivityCompat.requestPermissions(MainActivity.this, new String[]{RECORD_AUDIO, WRITE_EXTERNAL_STORAGE}, REQUEST_AUDIO_PERMISSION_CODE);
    }
}
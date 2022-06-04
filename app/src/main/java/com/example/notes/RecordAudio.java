package com.example.notes;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Chronometer;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textview.MaterialTextView;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.Timer;

import static android.content.ContentValues.TAG;

public class RecordAudio extends AppCompatActivity {
    private MediaRecorder recorder;
    private FloatingActionButton fabRecord;
    private FloatingActionButton fabSave;
    private FloatingActionButton fabCancel;
    private MaterialTextView tvSave;
    private MaterialTextView tvCancel;
    private Chronometer chronometer;
    private View containerView;
    private boolean isRecording = false;
    private boolean isPaused = false;
    private boolean ACTION_LONG_PRESS=false;
    private String path;
    private Timer timer;
    private long millisSecond = 0;
    private long seconds = 0;
    private long minutes = 0;
    private long tStart; // start recording time
    private long tPause;
    private Handler handler;
    private Handler mLongPressedHandler;


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.RecordTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_audio);

        //method to shape record activity to be suitable for different screen sizes
        chronometer = findViewById(R.id.chronometeer);
        chronometer.setText(String.format("%02d", 00) + ":" + String.format("%02d", 00));
        fabRecord = findViewById(R.id.fabRecord);
        fabSave = findViewById(R.id.fabSave);
        fabCancel = findViewById(R.id.fabCancel);
        tvCancel = findViewById(R.id.cancelTextView);
        tvSave = findViewById(R.id.saveTextView);
        containerView= findViewById(R.id.viewContainer);
        fabSave.setColorFilter(getResources().getColor(R.color.black));
        fabCancel.setColorFilter(getResources().getColor(R.color.black));
        recorder = new MediaRecorder();
        //method to define where record file will be stored in device
        buildRecordFileDestination();
        timer = new Timer();
        handler = new Handler(Looper.getMainLooper());
        mLongPressedHandler= new Handler(Looper.getMainLooper());
        fabRecord.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View v) {
                if (isRecording == false)
                    startRecord();
                else if (isPaused)
                    resumeRecord();
                else
                    pauseRecord();
            }
        });
        fabSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveRecord();
            }
        });
        fabCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                discardRecord();
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void startRecord() {
        // Record1  OR Record2
        initializeRecord();
        fabRecord.setImageResource(R.drawable.ic_pause);

        try {
            recorder.prepare();
            recorder.start();
            tStart = SystemClock.uptimeMillis();
            handler.postDelayed(runnable, 80);
            // timer.schedule(runnable,0,100);
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
        setVisibility(View.VISIBLE);
        isRecording = true;
    }

    private void initializeRecord() {
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        Log.e(TAG, "initializeRecord: PATH      "+path );
        recorder.setOutputFile(path);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void pauseRecord() {
        recorder.pause();
        tPause = SystemClock.uptimeMillis();
        Log.e("tPause", tPause + "");
        handler.removeCallbacks(runnable);
        fabRecord.setImageResource(R.drawable.ic_play);
        fabRecord.setColorFilter(getResources().getColor(R.color.white));
        isPaused = true;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void resumeRecord() {
        recorder.resume();
        tPause = SystemClock.uptimeMillis() - tPause;
        tStart = tStart + tPause;
        handler.postDelayed(runnable, 60);
        fabRecord.setImageResource(R.drawable.ic_pause);

        isPaused = false;
    }

    private void saveRecord() {
        recorder.stop();
        Intent result = new Intent();
        result.putExtra("path", path);
        setResult(RESULT_OK, result);
        handler.removeCallbacks(runnable);
        finish();
    }

    private void discardRecord() {
        recorder.reset();
        fabRecord.setImageResource(R.drawable.ic_mic);
        fabRecord.setColorFilter(getResources().getColor(R.color.white));
        setVisibility(View.INVISIBLE);
        handler.removeCallbacks(runnable);
        chronometer.setText(String.format("%02d", 00) + ":" + String.format("%02d", 00));
        isRecording = false;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void buildRecordFileDestination() {
        Date date = new Date();
        long milliSeconds = date.getTime();
        String recordName = "/Record" + milliSeconds + ".3gp";
      //  File file=Environment.getExternalStorageState(getPackageName() + "/Recordings/" + recordName);
        String dirPath=getExternalFilesDir("/Recordings/" ).getAbsolutePath();
        File file=new File(dirPath+recordName);
     //   File file = Environment.getExternalStoragePublicDirectory(getPackageName() + "/Recordings/" + recordName);
        path = file.getAbsolutePath();
        Log.e(TAG, "buildRecordFileDestination: Path     "+path );
    }

    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            long millisSecond = SystemClock.uptimeMillis() - tStart;
            seconds = (millisSecond / 1000) % 60;
            minutes = millisSecond / 60000;
            millisSecond = (millisSecond % 1000) / 10;
            chronometer.setText(String.format("%02d", minutes) + ":" + String.format("%02d", seconds) + "." + String.format("%02d", millisSecond));
            Log.e("Handler", "run: 1" );
            handler.postDelayed(this, 80);

        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.record_activity_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (R.id.cancel_record_activity == item.getItemId())
            finish();
        return  true;
    }

    Runnable mLongPressed = new Runnable() {
        public void run() {
            ACTION_LONG_PRESS=true;
        }
    };
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(event.getY()<=containerView.getY()) {
            if (event.getAction() == MotionEvent.ACTION_DOWN)
                handler.postDelayed(mLongPressed, android.view.ViewConfiguration.getLongPressTimeout());
            if ((event.getAction() == MotionEvent.ACTION_MOVE) || (event.getAction() == MotionEvent.ACTION_UP)) {
                if (!ACTION_LONG_PRESS)
                    finishActivity();
                ACTION_LONG_PRESS = false;
            }
        }
        return super.onTouchEvent(event);
    }

    @Override
    protected void onDestroy() {
        if(handler!=null)
            handler.removeCallbacks(runnable);
        recorder.release();
        super.onDestroy();

    }
    protected void finishActivity()
    {
        if(handler!=null)
            handler.removeCallbacks(runnable);
        recorder.release();
        finish();
    }
    void setVisibility(int visibility)
    {
        fabSave.setVisibility(visibility);
        fabCancel.setVisibility(visibility);
        tvCancel.setVisibility(visibility);
        tvSave.setVisibility(visibility);
    }

}

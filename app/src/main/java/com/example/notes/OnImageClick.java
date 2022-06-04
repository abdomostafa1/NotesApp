package com.example.notes;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GestureDetectorCompat;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;
import com.bumptech.glide.Glide;
import com.github.chrisbanes.photoview.PhotoView;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.Date;
import java.util.TimerTask;

public class OnImageClick extends AppCompatActivity {
    PhotoView image;
    MaterialToolbar toolbar;
    int RESULT_DELETE_IMAGE=10;
    private boolean toolbarStatus=true;
    private boolean moveAction=false;
    private boolean upAction=false;
    private Date date;
    private long lastActionDownDate=0; //date of action in milliSeconds
    private long oneSec=1000;  //1000 milliSeconds
    private GestureDetectorCompat mDetector;
    private AlertDialog.Builder dialog;
    View.OnTouchListener imageTouchListener=new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            return mDetector.onTouchEvent(event);
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_on_image_click);
        toolbar=findViewById(R.id.imageToolBar);
        setSupportActionBar(toolbar);
        dialog=new AlertDialog.Builder(OnImageClick.this);
        image=findViewById(R.id.fullImage);
        //image.setOnTouchListener(imageTouchListener);
      //  mDetector = new GestureDetectorCompat(this, new MyGestureListener());

        Intent intent=getIntent();
        String strUri=intent.getStringExtra("imageUri");
        Uri imageUri=Uri.parse(strUri);
        Glide.with(OnImageClick.this).load(imageUri).into(image);
        image.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               View decorView = getWindow().getDecorView();
               int uiOptions;
               if (toolbarStatus) {
                   toolbar.setVisibility(View.INVISIBLE);
                   toolbarStatus = false;
               } else {
                   toolbar.setVisibility(View.VISIBLE);
                   toolbarStatus = true;
               }
           }
       });

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.imageoptions, menu);
        menu.getItem(0).setIconTintList(ColorStateList.valueOf(Color.WHITE));
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (R.id.delete == item.getItemId()) {
            showDialog();
        }

        return true;
    }

    private void  showDialog() {
        new MaterialAlertDialogBuilder(OnImageClick.this)
                .setTitle(getString(R.string.dialogTitleForImage))
                .setMessage(getString(R.string.dialogMessageForImage))
                .setNegativeButton(getString(R.string.delete), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        setResult( RESULT_DELETE_IMAGE);
                        finish();
                    }
                })
                .setPositiveButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).show();
    }
    class MyGestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            finish();
            return super.onScroll(e1, e2, distanceX, distanceY);
        }
    }

}
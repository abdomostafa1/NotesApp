package com.example.notes;

import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;

import androidx.constraintlayout.widget.ConstraintLayout;

import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textview.MaterialTextView;

import java.io.IOException;

import android.view.ViewParent;
import android.widget.LinearLayout;
import android.widget.SeekBar;

import androidx.annotation.RequiresApi;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.Group;
import androidx.core.view.GestureDetectorCompat;

import static android.content.ContentValues.TAG;

public class Audio {
    private Context context;
    private LinearLayout linearLayout;
    public static MediaPlayer player=null;
    private Handler handler;
    private ConstraintLayout lastAudio = null;
    private ConstraintLayout currentAudio = null;
    public static FloatingActionButton fabPlay;
    private FloatingActionButton fabRemove;
    private ConstraintLayout audioLayout;
    private SeekBar seekBar=null;
    private MaterialTextView tvDuration;
    private DeleteData deleteData;
    private ViewSelection viewSelection;
    private GestureDetectorCompat mDetector;
    private GestureDetectorCompat fabPlayDetector;
    private GestureDetectorCompat fabRemoveDetector;

    String audioFileLocation;
    private int indexOfPressedAudio;
    public static boolean AudioLongPress = false;
    View pressedView;

    public Audio(Context context, LinearLayout linearLayout, ViewSelection viewSelection) {

        this.context = context;
        this.linearLayout = linearLayout;
        this.deleteData = deleteData;
        this.viewSelection = viewSelection;
        player = new MediaPlayer();
        handler = new Handler(Looper.getMainLooper());
        mDetector = new GestureDetectorCompat(context, new GestureListener());
        fabPlayDetector = new GestureDetectorCompat(context, new FabPlayGestureListener());
        fabRemoveDetector = new GestureDetectorCompat(context, new FabRemoveGestureListener());

        player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                fabPlay.setImageResource(R.drawable.ic_play);
                int millisSecond = player.getDuration();
                int seconds = (millisSecond / 1000) % 60;
                int minutes = millisSecond / (1000 * 60);
                tvDuration.setText(String.format("%01d", minutes )+ ":" + String.format("%02d", seconds));
                seekBar.removeCallbacks(runnable);
                seekBar.setProgress(0);
                Log.e(TAG, "onCompletion: " + minutes + ":" + String.format("%02d", seconds));
            }
        });

    }

    View.OnTouchListener audioOnTouchListener = new View.OnTouchListener() {
        @RequiresApi(api = Build.VERSION_CODES.M)
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            // i am using this to listener just to get Motion events
            // like ACTION_DOWN ,ACTION_UP ,ACTION_MOVE ,ACTION_CANCEL
            // and i will handle it in method onInterceptTouchEvent(MotionEvent ev)
            // in my custom class "MyConstrainLayout"
            pressedView = v;

            return mDetector.onTouchEvent(event);
        }
    };

    View.OnTouchListener fabPlayOnTouchListener = new View.OnTouchListener() {
        @RequiresApi(api = Build.VERSION_CODES.M)
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            pressedView = v;
            return fabPlayDetector.onTouchEvent(event);
        }
    };

    View.OnTouchListener fabRemoveOnTouchListener = new View.OnTouchListener() {
        @RequiresApi(api = Build.VERSION_CODES.M)
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            pressedView = v;

            return fabRemoveDetector.onTouchEvent(event);
        }
    };

    View.OnTouchListener seekBarOnTouchListener = new View.OnTouchListener() {
        @RequiresApi(api = Build.VERSION_CODES.M)
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                if (NewNote.selectionMode) {
                    NewNote.viewSelection.checkSelection((ConstraintLayout) v.getParent());
                    return true;
                }
            }
        return false;
        }

    };

    protected ConstraintLayout createAudioLayout(String audioFileLocation) {
        ConstraintLayout audioLayout = (ConstraintLayout) LayoutInflater.from(context).inflate(R.layout.audio_layout, null);
     //   audioLayout.setOnTouchListener(audioOnTouchListener);
        fabPlay = (FloatingActionButton) audioLayout.findViewById(R.id.play_audio);
        fabRemove = (FloatingActionButton) audioLayout.findViewById(R.id.remove_audio);
        seekBar = (SeekBar) audioLayout.findViewById(R.id.audio_seekBar);
        seekBar.setOnTouchListener(seekBarOnTouchListener);
        tvDuration = (MaterialTextView) audioLayout.findViewById(R.id.tv_audio_duration);
      //  View audioBackground = audioLayout.findViewById(R.id.audio_background);
        fabPlay.setOnTouchListener(fabPlayOnTouchListener);
        fabRemove.setOnTouchListener(fabRemoveOnTouchListener);
        tvDuration.setOnTouchListener(audioOnTouchListener);

        seekBar.setOnSeekBarChangeListener(seekBarChangeListener);
        getAudioDuration(audioFileLocation);
        lastAudio = audioLayout;
        return audioLayout;
    }

    private void SameAudioFile() {
        if (player.isPlaying())
            pauseAudio();
        else
            resumeAudio();
    }

    private void playAudio() {
        fabPlay.setImageResource(R.drawable.ic_pause);
        String audioLocation = (String) audioLayout.getTag(R.string.path);
        try {
            if (lastAudio != null)
                resetToDefaultState();
            player.reset();
            player.setAudioStreamType(AudioManager.STREAM_MUSIC);
            player.setDataSource(audioLocation);
            player.prepare();
            seekToBarProgress();
            player.start();
            seekBar.postDelayed(runnable, 60);
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }

    private void resumeAudio() {
        fabPlay.setImageResource(R.drawable.ic_pause);
        int progress=seekBar.getProgress();
        player.seekTo(progress);
        player.start();
        seekBar.postDelayed(runnable, 60);
    }

    private void pauseAudio() {
        player.pause();
        fabPlay.setImageResource(R.drawable.ic_play);
        seekBar.removeCallbacks(runnable);
    }


    private void getAudioDuration(String audioFileLocation) {
        player.reset();
        try {
            player.setDataSource(audioFileLocation);
            player.prepare();
            int millisSecond = player.getDuration();
            Log.e(TAG, "getAudioDuration: " + String.valueOf(millisSecond));
            seekBar.setMax(millisSecond);
            int seconds = (millisSecond / 1000) % 60;
            int minutes = millisSecond / (1000 * 60);
            Log.e(TAG, "getAudioDuration: ");
            tvDuration.setText(String.format("%01d", minutes )+ ":" + String.format("%02d", seconds));

        } catch (IOException ioException) {
            ioException.printStackTrace();
        }

    }

    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            updateSeekBar();
            seekBar.postDelayed(runnable, 60);
        }
    };

    private void updateSeekBar() {
        int position = player.getCurrentPosition();
        Log.e(TAG, "positionSeekBar: "+String.valueOf(position));
        seekBar.setProgress(position);
        int seconds = (position / 1000) % 60;
        int minutes = (position / 60000);
        tvDuration.setText(String.format("%01d", minutes )+ ":" + String.format("%02d", seconds));

    }

    void resetToDefaultState() {
        FloatingActionButton lastFab = (FloatingActionButton) lastAudio.getChildAt(1);
        lastFab.setImageResource(R.drawable.ic_play);
        SeekBar lastSeekBar = (SeekBar) lastAudio.getChildAt(2);
        lastSeekBar.setProgress(0);
        seekBar.removeCallbacks(runnable);
    }


    protected void resetPlayer() {
        player.reset();
    }

    protected void releasePlayer() {
        player.release();
    }

    private void seekToBarProgress() {
        int progress = seekBar.getProgress();
        player.seekTo(progress);

    }

    void releaseAudio(){
        if (seekBar!=null)
            seekBar.removeCallbacks(runnable);
        if (player!=null)
            player.release();
    }
    SeekBar.OnSeekBarChangeListener seekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (fromUser) {
                Log.e(TAG, "onProgressChanged: "+String.valueOf(progress) );
                seekBar.setProgress(progress);
                ConstraintLayout pressedSeekBarLayout = (ConstraintLayout) seekBar.getParent();
                if (currentAudio == pressedSeekBarLayout) {
                    Log.e(TAG, "job done" + seekBar.getProgress());
                    player.seekTo(progress);
                }
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    };


    class GestureListener extends GestureDetector.SimpleOnGestureListener {
        @RequiresApi(api = Build.VERSION_CODES.M)
        @Override
        public void onLongPress(MotionEvent e) {
            ViewParent viewParent=pressedView.getParent();
            Log.e(TAG, "NewNote.selectionMode: "+NewNote.selectionMode );
            if(viewParent.getClass().toString().contains("LinearLayout"))
                NewNote.viewSelection.checkSelection(pressedView);
            else
                NewNote.viewSelection.checkSelection((ConstraintLayout) pressedView.getParent());
            Log.e(TAG, "onLongPress: Tv Duration" );
        }

        @RequiresApi(api = Build.VERSION_CODES.M)

        @Override
        public boolean onDown(MotionEvent event) {
            return false;
        }

        @RequiresApi(api = Build.VERSION_CODES.M)
        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            Log.e(TAG, "NewNote.selectionMode: "+NewNote.selectionMode );
            if (NewNote.selectionMode)
                NewNote.viewSelection.checkSelection((ConstraintLayout) pressedView.getParent());

            Log.e(TAG, "onSingleTapUp: Tv Duration" );

            return true;
        }
    }

    class FabPlayGestureListener extends GestureDetector.SimpleOnGestureListener {
        @RequiresApi(api = Build.VERSION_CODES.M)
        public void onLongPress(MotionEvent e) {
            NewNote.viewSelection.checkSelection((ConstraintLayout) pressedView.getParent());
            super.onLongPress(e);
        }


        @Override
        public boolean onDown(MotionEvent event) {
            return false;
        }

        @RequiresApi(api = Build.VERSION_CODES.M)
        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            fabPlay = (FloatingActionButton) pressedView;
            audioLayout = (ConstraintLayout) fabPlay.getParent();
            if (NewNote.selectionMode) {
                //Log.e( "pressedView.getClass() ", pressedView.getClass().toString());
                viewSelection.checkSelection(audioLayout);
            } else {
                seekBar = (SeekBar) audioLayout.findViewById(R.id.audio_seekBar);
                tvDuration = (MaterialTextView) audioLayout.findViewById(R.id.tv_audio_duration);
                fabRemove = (FloatingActionButton) audioLayout.findViewById(R.id.remove_audio);
                lastAudio = currentAudio;
                currentAudio = audioLayout;
                if (currentAudio == lastAudio)
                    // same audio file
                    SameAudioFile();
                else {
                    seekBar.removeCallbacks(runnable);
                    playAudio();
                }
            }

            return super.onSingleTapUp(e);
        }

    }

    class FabRemoveGestureListener extends GestureDetector.SimpleOnGestureListener {
        @RequiresApi(api = Build.VERSION_CODES.M)
        public void onLongPress(MotionEvent e) {
            NewNote.viewSelection.checkSelection((ConstraintLayout) pressedView.getParent());
            super.onLongPress(e);
        }


        @Override
        public boolean onDown(MotionEvent event) {
            return false;
        }

        @RequiresApi(api = Build.VERSION_CODES.M)
        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            ConstraintLayout constraintLayout = (ConstraintLayout) pressedView.getParent();
            if (NewNote.selectionMode) {
                //Log.e( "pressedView.getClass() ", pressedView.getClass().toString());
                viewSelection.checkSelection(constraintLayout);
            } else {
                int index = linearLayout.indexOfChild(constraintLayout);
                NewNote.deleteData.deleteAudio(index);
            }
            return super.onSingleTapUp(e);
        }

    }
}


package com.example.notes;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.view.GestureDetectorCompat;
import androidx.core.widget.NestedScrollView;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.text.Layout;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.ActionMode;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.textfield.TextInputEditText;

import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent;
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEventListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


import java.io.File;
import java.io.IOException;
import java.util.Date;

import static android.content.ContentValues.TAG;

public class NewNote extends AppCompatActivity {
    public static boolean newNote;
    Note note = null;
    LinearLayout linearLayout;
    public static ConstraintLayout horizontalLayout;
    MaterialToolbar toolbar;
    TextInputEditText title;
    TextInputEditText message;
    View space;
    public static ActionMode actionMode = null;
    Factory factory;
    StoreNote storeNote;
    NestedScrollView scrollView;
    public static ViewSelection viewSelection;
    static DeleteData deleteData;
    private GestureDetectorCompat mDetector;
    private GestureDetectorCompat spaceDetector;
    Uri cameraCapturedImageUri;
    Handler handler;
    private int REQUEST_CAMERA_PERMISSIONS = 1;
    private int REQUEST_RECORD_PERMISSIONS = 2;
    private long newNoteId;
    private int numbOfChildedBefore = 0;
    private int numbOfChildernAfter = 0;
    private int indexOfPressedImage;
    private int RESULT_DELETE = 2;
    private int RESULT_DELETE_IMAGE = 10;
    protected int focusInEditTextTitle = 1;
    public static boolean changeInTextInputEditText = false;
    public static boolean changeInTitle = false;
    public static boolean changeInImage = false;
    public static boolean changeInAudio = false;
    public static boolean changeInCheckBox = false;
    boolean deleteNote = false;
    public static boolean selectionMode = false;
    private String cameraCapturedImagePath;
    FloatingActionButton checkBox_btn;
    FloatingActionButton image_btn;
    FloatingActionButton camera_btn;
    FloatingActionButton record_btn;
    FloatingActionButton numbers_btn;
    FloatingActionButton dots_btn;

    ActivityResultLauncher<Intent> mShowImage = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {

                    if (result.getResultCode() == RESULT_DELETE_IMAGE)
                        deleteData.deleteImage(indexOfPressedImage);
                }
            });
    View.OnTouchListener imageOnTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            indexOfPressedImage = linearLayout.indexOfChild(v);
            return mDetector.onTouchEvent(event);
        }
    };
    View.OnTouchListener spaceTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            //   Log.e(TAG, "onSingleTapUp: SPACE 1" );
            if (selectionMode)
                return true;
            else
                return spaceDetector.onTouchEvent(event);
        }
    };

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_note);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().setStatusBarColor(getResources().getColor(R.color.white, this.getTheme()));
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(getResources().getColor(R.color.white));
        }
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        toolbar.getOverflowIcon().setTint(Color.BLACK);
        mDetector = new GestureDetectorCompat(this, new ImageGestureListener());
        spaceDetector = new GestureDetectorCompat(this, new SpaceGestureListener());
        linearLayout = (LinearLayout) findViewById(R.id.linearlayout);
        scrollView=(NestedScrollView)findViewById(R.id.scroll_view);
        horizontalLayout=(ConstraintLayout) findViewById(R.id.horizontal_layout);
        linearLayout.setOnHierarchyChangeListener(onHierarchyChangeListener);
        title = findViewById(R.id.oooooo);
        message = findViewById(R.id.note_body);
        handler = new Handler(Looper.getMainLooper());
        Intent intent = getIntent();
        newNote = intent.getBooleanExtra("NewNote", false);
        viewSelection = new ViewSelection(NewNote.this, linearLayout);
        factory = new Factory(NewNote.this, linearLayout, viewSelection);
        factory.addClickListener(title);
        title.setPadding((int) Factory.padding, (int) Factory.padding, (int) Factory.padding, (int) Factory.padding);
        if (newNote) {
            title.requestFocus();
            factory.addClickListener(message);
            message.setPadding((int) Factory.padding, 0, (int) Factory.padding, (int) Factory.padding);
            // initialize  storeNote Object
            Date date=new Date();
            newNoteId=date.getTime();
            storeNote = new StoreNote(NewNote.this, linearLayout,newNoteId);
            linearLayout.requestFocus();

        } else {

            note = (Note) intent.getSerializableExtra("note");
            int position = intent.getIntExtra("position", 0);
            // show note in background thread
            DisplayNote displayNote = new DisplayNote();
            displayNote.execute(note);
            // initialize  storeNote Object
            storeNote = new StoreNote(NewNote.this, linearLayout, note);
        }
        deleteData = new DeleteData(NewNote.this, factory, linearLayout, note);
        space = findViewById(R.id.white_space);
        space.setLayoutParams(Factory.space);
        space.setOnTouchListener(spaceTouchListener);
        intializeHorizontalLayoutBtns();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.new_note_menu, menu);
        if(menu instanceof MenuBuilder){
            MenuBuilder m = (MenuBuilder) menu;
            //noinspection RestrictedApi
            m.setOptionalIconsVisible(true);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (R.id.delete == item.getItemId()) {

            if(newNote){
                deleteNote=true;
                finish();
            }
            setResult(RESULT_DELETE);
            deleteNote=true;
            deleteData.execute((Note) null);
            finish();
        }
        if (R.id.select_view == item.getItemId())
            activateSelectionMode();
        if (R.id.attach == item.getItemId()) {
            if(horizontalLayout.getVisibility()==View.VISIBLE)
                horizontalLayout.setVisibility(View.INVISIBLE);
            else
                horizontalLayout.setVisibility(View.VISIBLE);
        }
        return true;
    }

    void activateSelectionMode() {
        selectionMode = true;
    }

    void intializeHorizontalLayoutBtns(){
        checkBox_btn=(FloatingActionButton)findViewById(R.id.checkBox_btn);
        image_btn=(FloatingActionButton)findViewById(R.id.image_btn);
        camera_btn=(FloatingActionButton)findViewById(R.id.camera_btn);
        record_btn=(FloatingActionButton)findViewById(R.id.record_btn);
        numbers_btn=(FloatingActionButton)findViewById(R.id.numbers_btn);
        dots_btn=(FloatingActionButton)findViewById(R.id.dots_btn);

        checkBox_btn.setColorFilter(getResources().getColor(R.color.light_gray));
        image_btn.setColorFilter(getResources().getColor(R.color.light_gray));
        camera_btn.setColorFilter(getResources().getColor(R.color.light_gray));
        record_btn.setColorFilter(getResources().getColor(R.color.light_gray));
        numbers_btn.setColorFilter(getResources().getColor(R.color.light_gray));
        dots_btn.setColorFilter(getResources().getColor(R.color.light_gray));
        checkBox_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addCheckBox();
            }
        });
        image_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                photosProvider();
            }
        });
        camera_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
              checkCameraPermission();
            }
        });
        record_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkRecordPermission();
            }
        });
        numbers_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addNumberedList();
            }
        });
        dots_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addDotsList();
            }
        });
        KeyboardVisibilityEvent.setEventListener(
                NewNote.this,
                new KeyboardVisibilityEventListener() {
                    @Override
                    public void onVisibilityChanged(boolean isOpen) {
                        float px=45 * ((float) NewNote.this.getResources().getDisplayMetrics().densityDpi / DisplayMetrics.DENSITY_DEFAULT);
                        int line=6;
                        TextInputEditText editText=null;
                        try {

                             editText = (TextInputEditText) getCurrentFocus();
                        int pos = editText.getSelectionStart();
                        Layout layout = editText.getLayout();
                        line = layout.getLineForOffset(pos);

                        }
                        catch (Exception e){
                            e.printStackTrace();
                        }
                        if (isOpen) {
                            horizontalLayout.setVisibility(View.VISIBLE);
                            int finalLine = line;
                            final Runnable r = new Runnable() {
                                public void run() {

                                    if(finalLine <4)
                                        return;
                                    else
                                      scrollView.smoothScrollBy(0, (int) px);
                                }
                            };
                            handler.postDelayed(r, 550);
                        }
                        else
                            horizontalLayout.setVisibility(View.INVISIBLE);
                    }
                });
    }
    void checkRecordPermission(){
       if( ContextCompat.checkSelfPermission(NewNote.this,Manifest.permission.WRITE_EXTERNAL_STORAGE)== PackageManager.PERMISSION_GRANTED
          &&ContextCompat.checkSelfPermission(NewNote.this,Manifest.permission.READ_EXTERNAL_STORAGE)== PackageManager.PERMISSION_GRANTED
          &&ContextCompat.checkSelfPermission(NewNote.this,Manifest.permission.RECORD_AUDIO)== PackageManager.PERMISSION_GRANTED)
           launchRecordingActivity();
        else{
            String permission=Manifest.permission.WRITE_EXTERNAL_STORAGE;

           if( ContextCompat.checkSelfPermission(NewNote.this,Manifest.permission.WRITE_EXTERNAL_STORAGE)== PackageManager.PERMISSION_DENIED)
               ActivityCompat.requestPermissions(this,new String[] {permission},REQUEST_RECORD_PERMISSIONS);

           permission=Manifest.permission.READ_EXTERNAL_STORAGE;
           if( ContextCompat.checkSelfPermission(NewNote.this,Manifest.permission.READ_EXTERNAL_STORAGE)== PackageManager.PERMISSION_DENIED)
               ActivityCompat.requestPermissions(this,new String[] {permission},REQUEST_RECORD_PERMISSIONS);

           permission=Manifest.permission.RECORD_AUDIO;
           if( ContextCompat.checkSelfPermission(NewNote.this,Manifest.permission.RECORD_AUDIO)== PackageManager.PERMISSION_DENIED)
               ActivityCompat.requestPermissions(this,new String[] {permission },REQUEST_RECORD_PERMISSIONS);

       }

    }
    void checkCameraPermission(){
        if(     ContextCompat.checkSelfPermission(NewNote.this,Manifest.permission.WRITE_EXTERNAL_STORAGE)== PackageManager.PERMISSION_GRANTED
                &&ContextCompat.checkSelfPermission(NewNote.this,Manifest.permission.READ_EXTERNAL_STORAGE)== PackageManager.PERMISSION_GRANTED) {
            try {
                cameraCapture();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }

        }
        else{
            String permission=Manifest.permission.WRITE_EXTERNAL_STORAGE;

            if( ContextCompat.checkSelfPermission(NewNote.this,Manifest.permission.WRITE_EXTERNAL_STORAGE)== PackageManager.PERMISSION_DENIED)
                ActivityCompat.requestPermissions(this,new String[] {permission},REQUEST_CAMERA_PERMISSIONS);

            permission=Manifest.permission.READ_EXTERNAL_STORAGE;
            if( ContextCompat.checkSelfPermission(NewNote.this,Manifest.permission.READ_EXTERNAL_STORAGE)== PackageManager.PERMISSION_DENIED)
                ActivityCompat.requestPermissions(this,new String[] {permission},REQUEST_CAMERA_PERMISSIONS);

        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode==REQUEST_RECORD_PERMISSIONS){
            if(grantResults.length>0&&grantResults[0]==PackageManager.PERMISSION_GRANTED)
                checkRecordPermission();
        }
        if(requestCode==REQUEST_CAMERA_PERMISSIONS){
            if(grantResults.length>0&&grantResults[0]==PackageManager.PERMISSION_GRANTED)
                checkCameraPermission();
        }

    }

    ActivityResultLauncher<Intent> mGetRecord = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                @Override
                public void onActivityResult(ActivityResult result) {

                    if (result.getResultCode() == RESULT_OK) {
                        Intent intent = result.getData();
                        String path = intent.getStringExtra("path");
                        addRecord(path);
                    }
                }
            });

    private void launchRecordingActivity() {
        Intent intent = new Intent(NewNote.this, RecordAudio.class);
        mGetRecord.launch(intent);

    }


    ActivityResultLauncher<Intent> mGetPhotos = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                @Override
                public void onActivityResult(ActivityResult result) {

                    if (result.getResultCode() == RESULT_OK) {
                        Intent intent = result.getData();
                        Uri imageUri = intent.getData();
                        addImage(imageUri);
                    }
                }
            });


    private void photosProvider() {
//        Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("image/*");
        mGetPhotos.launch(intent);

    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private ShapeableImageView addImage(Uri uri) {
        Log.e("Image Uri :: ", String.valueOf(uri));
        ShapeableImageView newImageView;
        int index = setIndexOfNewView();
        newImageView = factory.createImageView(uri);
        linearLayout.addView(newImageView, index);
        newImageView.setLongClickable(true);
        newImageView.setClickable(true);
        newImageView.setOnTouchListener(imageOnTouchListener);
        newImageView.setLongClickable(true);
        isLastViewEditText();
        return newImageView;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void addRecord(String path) {
        // determine where to put new record
        int index = setIndexOfNewView();
        ConstraintLayout newAudioLayout = factory.createAudioLayout(path);
        linearLayout.addView(newAudioLayout, index);
        isLastViewEditText();
    }

    private void addCheckBox() {
        int index = setIndexOfNewView();
        Log.e(TAG, "NewView: " + index);

        //factory.createCheckBox(false, "", index);
        ConstraintLayout constraintLayout1 = factory.createCheckBox(false, "");
        linearLayout.addView(constraintLayout1, index);
        TextInputEditText textInputEditText = (TextInputEditText) constraintLayout1.findViewById(R.id.checkbox_text_input);
        textInputEditText.requestFocus();

    }

    private void addNumberedList() {
        int index = setIndexOfNewView();
        Log.e(TAG, "NewView: " + index);
        //factory.createCheckBox(false, "", index);
        String prefix = Factory.itemNumberInList + ".";
        ConstraintLayout constraintLayout1 = factory.createList(prefix, "");
        linearLayout.addView(constraintLayout1, index);
        TextInputEditText textInputEditText = (TextInputEditText) constraintLayout1.findViewById(R.id.list_editText);
        textInputEditText.requestFocus();
        Factory.itemNumberInList++;

    }

    private void addDotsList() {
        int index = setIndexOfNewView();
        Log.e(TAG, "NewView: " + index);
        //factory.createCheckBox(false, "", index);
        String prefix = "•";
        ConstraintLayout constraintLayout1 = factory.createList(prefix, "");
        linearLayout.addView(constraintLayout1, index);
        TextInputEditText textInputEditText = (TextInputEditText) constraintLayout1.findViewById(R.id.list_editText);
        textInputEditText.requestFocus();

    }

    private int setIndexOfNewView() {

        int newViewIndex;
        View view = getCurrentFocus();
        //focus in LinearLayout
        if (view.getClass().toString().contains("LinearLayout")) {
            newViewIndex = linearLayout.getChildCount() - 1;
            return newViewIndex;
        }
        // focus in one of textInputEditTexts
        TextInputEditText textInputEditText = (TextInputEditText) view;
        int index = linearLayout.indexOfChild(textInputEditText);
        // case of adding Checkbox
        if (textInputEditText.getParent().getClass().toString().contains("ConstraintLayout")) {
            newViewIndex = linearLayout.indexOfChild((ConstraintLayout) textInputEditText.getParent()) + 1;
            return newViewIndex;
        }
        Log.e(TAG, "setIndexOfNewView: " + index);
        if (index == focusInEditTextTitle) {
            newViewIndex = 2;
            return newViewIndex;
        } else {

            int cursorPosition = textInputEditText.getSelectionStart();
            boolean isEmpty = textInputEditText.getText().toString().trim().isEmpty();
            if (isEmpty) {
                if (index == 1)  // first textInputEditText
                    newViewIndex = index + 1;
                else
                    newViewIndex = index;
            } else {

                if (!(cursorPosition >= textInputEditText.getText().toString().trim().length()))
                    factory.divideEditText(textInputEditText, index);

                newViewIndex = index + 1;
            }
            return newViewIndex;
        }
    }


    private void updateNote() {
        numbOfChildernAfter = linearLayout.getChildCount();
        if (numbOfChildernAfter != numbOfChildedBefore || changeInTextInputEditText || changeInImage || changeInAudio || changeInCheckBox || changeInTitle)  //  clean up old note to update
            storeNote.execute();
    }

    ActivityResultLauncher<Intent> mGetCameraCapture = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == RESULT_OK) {
                        // store image path and url so when user deletes image ,
                        // i can delete it online from server and offline from user device storage
                        ShapeableImageView newImageView = addImage(cameraCapturedImageUri);
                        newImageView.setTag(R.string.path, cameraCapturedImagePath);
                    }
                }
            });

    private void cameraCapture() throws IOException {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        Date date = new Date();
        long x = date.getTime();
        String outputFilePath = "abdo";
        cameraCapturedImagePath = "/Photos/image" + x + ".jpg";
        String dirPath = getExternalFilesDir("/Photos/").getAbsolutePath()  ;
        File file=new File(dirPath+"/image" + x + ".jpg");
        Uri photoURI = FileProvider.getUriForFile(NewNote.this,
                "com.example.android.fileprovider",
                file);
        cameraCapturedImageUri = photoURI;
        cameraCapturedImagePath = file.getAbsolutePath();
        intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
        mGetCameraCapture.launch(intent);
    }

    private void isLastViewEditText() {
        // add editText before space
        View view = linearLayout.getChildAt(linearLayout.getChildCount() - 2);
        if (view.getClass().toString().contains("TextInputEditText"))
            return;
        else {
            TextInputEditText textInputEditText = factory.createEditText("");
            linearLayout.addView(textInputEditText, linearLayout.getChildCount() - 1);
            textInputEditText.requestFocus();
        }
    }

    ViewGroup.OnHierarchyChangeListener onHierarchyChangeListener = new ViewGroup.OnHierarchyChangeListener() {
        @Override
        public void onChildViewAdded(View parent, View child) {

            if (linearLayout.getChildCount() > 3)
                if (message != null)
                    message.setHint("");
        }

        @Override
        public void onChildViewRemoved(View parent, View child) {

        }
    };

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_DEL) {
            if(!selectionMode)
               setOnKeyDeleteClick();
            Log.e(TAG, "onKeyDown: backSpace");
        }
        return super.onKeyDown(keyCode, event);
    }

    void setOnKeyDeleteClick() {
        TextInputEditText textInputEditText;
        try {
            textInputEditText = (TextInputEditText) getCurrentFocus();
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        int index;

        if (textInputEditText.getParent().getClass().toString().contains("ConstraintLayout")) {
            ConstraintLayout parent=(ConstraintLayout)textInputEditText.getParent();
            index= linearLayout.indexOfChild(parent);
            String text=textInputEditText.getText().toString().trim();
            if(text.isEmpty())
                deleteConstrainLayout(index);
            else {

                TextInputEditText newTextInputEditText=factory.createEditText(text);
                linearLayout.addView(textInputEditText,index);
                newTextInputEditText.requestFocus();
                newTextInputEditText.setSelection(0);
                linearLayout.removeViewAt(index+1);
            }

        }
        else {
            index= linearLayout.indexOfChild(textInputEditText);
            if(index>1)
            deleteView(index);
        }

    }

    void deleteConstrainLayout(int index) {
        deleteData.deleteCheckBox(index);
        TextInputEditText textInputEditText1 = factory.createEditText("");
        linearLayout.addView(textInputEditText1, index);
        textInputEditText1.requestFocus();
        return;

    }

    void deleteView(int index) {

        View view=linearLayout.getChildAt(index-1);
        TextInputEditText textInputEditText=(TextInputEditText) linearLayout.getChildAt(index);
        if(view.getClass().toString().contains("TextInputEditText"))
            removeEditText(index);

        else if(view.getClass().toString().contains("ConstraintLayout")) {
            if(view.getId()==R.id.audio_Constrain)
                deleteData.deleteAudio(index-1);
            else{

                ConstraintLayout constraintLayout=(ConstraintLayout) view;
                TextInputEditText textInputEditText1=(TextInputEditText) constraintLayout.getChildAt(1);
                textInputEditText1.requestFocus();
                textInputEditText1.setSelection(textInputEditText1.getText().length());
            }

        }

        else if (view.getClass().toString().contains("ImageView"))
            deleteData.deleteImage(index-1);

    }

    void removeEditText(int index){
        View view=linearLayout.getChildAt(index-1);
        TextInputEditText textInputEditText=(TextInputEditText) linearLayout.getChildAt(index);
        TextInputEditText textInputEditText1=(TextInputEditText)view;
        String text=textInputEditText1.getText().toString();
        int selection=textInputEditText1.getText().length();
        linearLayout.removeView(textInputEditText1);
        textInputEditText.append(text,0,text.length());
        textInputEditText.setSelection(selection);
    }
    void removeMessage()
    {
        TextInputEditText title=(TextInputEditText) linearLayout.getChildAt(0);
        TextInputEditText message=(TextInputEditText) linearLayout.getChildAt(1);
        String text=title.getText().toString();
        int selection=text.length();
        title.requestFocus();
        title.setSelection(selection);
        linearLayout.removeView(message);
    }
    @Override
    public boolean onKeyLongPress(int keyCode, KeyEvent event) {
        Log.e(TAG, "onKeyLongPress: ");
        return false;
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        return super.onKeyUp(keyCode, event);
    }

    @Override
    public boolean onKeyMultiple(int keyCode, int repeatCount, KeyEvent event) {
        Log.e(TAG, "onKeyMultiple: " );
        return super.onKeyMultiple(keyCode, repeatCount, event);
    }

    @Override
    protected void onPause() {
        if(Audio.player!=null) {
            if (Audio.player.isPlaying()) {
                Audio.player.pause();
                Audio.fabPlay.setImageResource(R.drawable.ic_play);
            }
        }
        super.onPause();
    }


//    @Override
//    protected void onResume() {
//        if (Audio.player.isPlaying()) {
//            Audio.player.pause();
//            Audio.fabPlay.setImageResource(R.drawable.ic_play);
//        }
//        super.onResume();
//    }

    @Override
    protected void onDestroy() {

            try {
                factory.releaseAudio();
                Factory.itemNumberInList = 1;
            }
            catch (Exception exception){
            exception.printStackTrace();
            }
            selectionMode = false;
            if (!deleteNote) {
                if (newNote)
                    storeNote.execute();
                else
                    updateNote();
            }

        super.onDestroy();

    }

    class ImageGestureListener extends GestureDetector.SimpleOnGestureListener {
        @RequiresApi(api = Build.VERSION_CODES.M)
        @Override
        public void onLongPress(MotionEvent e) {
            ImageView imageView = (ImageView) linearLayout.getChildAt(indexOfPressedImage);
            viewSelection.checkSelection(imageView);

            super.onLongPress(e);
        }

        private static final String DEBUG_TAG = "Gestures";

        @Override
        public boolean onDown(MotionEvent event) {
            Log.d(DEBUG_TAG, "onDown: " + event.toString());
            return true;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            Log.e(TAG, "onTouchEvent: SCROLL");
            return super.onScroll(e1, e2, distanceX, distanceY);
        }

        @RequiresApi(api = Build.VERSION_CODES.M)
        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            ImageView imageView = (ImageView) linearLayout.getChildAt(indexOfPressedImage);

            if (selectionMode) {
                viewSelection.checkSelection(imageView);
                return true;
            }
            // get image uri
            Uri uri = (Uri) imageView.getTag(R.string.uri);
            Intent intent = new Intent(NewNote.this, OnImageClick.class);
            intent.putExtra("imageUri", uri.toString());
            mShowImage.launch(intent);
            return super.onSingleTapUp(e);
        }
    }

    class SpaceGestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            Log.e(TAG, "onSingleTapUp: SPACE 2");
            isLastViewEditText();
            int index = linearLayout.getChildCount() - 2;
            TextInputEditText lastEditText = (TextInputEditText) linearLayout.getChildAt(index);
            lastEditText.requestFocus();
            lastEditText.setSelection(lastEditText.getText().length());
            InputMethodManager iM = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            iM.showSoftInput(lastEditText, InputMethodManager.SHOW_IMPLICIT);
            return super.onSingleTapUp(e);
        }
    }

    class DisplayNote extends AsyncTask<Note, Void, Void> {
        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @Override
        protected Void doInBackground(Note... notes) {
            Note note = notes[0];
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    title.setText(note.getTitle());
                    title.addTextChangedListener(Factory.titleTextWatcher);
                    linearLayout.removeView(message);
                    title.clearFocus();
                    linearLayout.requestFocus();
                }
            });

            String jsonNoteBody = note.getNoteBody();
            JSONObject root = null;
            try {
                root = new JSONObject(jsonNoteBody);
                JSONArray noteBody = root.getJSONArray("noteBody");
                for (int i = 0; i < noteBody.length(); i++) {

                    JSONObject child = noteBody.getJSONObject(i);
                    String type = child.getString("type");
                    if (type.contains("Text"))
                        displayText(child);
                    else if (type.contains("Audio"))
                        displayAudio(child);
                    else if (type.contains("Image"))
                        displayImage(child);
                    else if (type.contains("CheckBox"))
                        displayCheckBox(child);
                    else
                        displayListItem(child);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    numbOfChildedBefore = linearLayout.getChildCount();
                }
            });

            return null;
        }

        private void displayText(JSONObject child) throws JSONException {
            String text = child.getString("text");
            TextInputEditText textInputEditText = factory.createEditText(text);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    linearLayout.addView(textInputEditText, linearLayout.getChildCount() - 1);
                }
            });
        }

        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        private void displayAudio(JSONObject child) throws JSONException {
            String path = child.getString("path");
            ConstraintLayout newAudioLayout = factory.createAudioLayout(path);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    linearLayout.addView(newAudioLayout, linearLayout.getChildCount() - 1);
                }
            });
        }

        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        private void displayImage(JSONObject child) throws JSONException {
            String string = child.getString("uri");
            Uri uri = Uri.parse(string);
            Log.e("displayImage:Uri--->", string);
            ShapeableImageView imageView = factory.createImageView(uri);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    linearLayout.addView(imageView, linearLayout.getChildCount() - 1);
                }
            });
            if (child.has("path")) {
                String path = child.getString("path");
                imageView.setTag(R.string.path, path);
            }
            imageView.setOnTouchListener(imageOnTouchListener);
        }

        private void displayCheckBox(JSONObject child) throws JSONException {
            String task = child.getString("task");
            boolean isChecked = child.getBoolean("isChecked");
            ConstraintLayout constraintLayout1 = factory.createCheckBox(isChecked, task);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    linearLayout.addView(constraintLayout1, linearLayout.getChildCount() - 1);
                }
            });
        }

        private void displayListItem(JSONObject child) throws JSONException {
            String task = child.getString("task");
            String prefix = child.getString("prefix");

            ConstraintLayout constraintLayout1 = factory.createList(prefix, task);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    linearLayout.addView(constraintLayout1, linearLayout.getChildCount() - 1);
                }
            });
        }
    }

    public static ActionMode.Callback actionModeCallback = new ActionMode.Callback() {

        // Called when the action mode is created; startActionMode() was called
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            // Inflate a menu resource providing context menu items
            mode.getMenuInflater().inflate(R.menu.selection_menu, menu);
            selectionMode = true;
            return true;
        }

        // Called each time the action mode is shown. Always called after onCreateActionMode, but
        // may be called multiple times if the mode is invalidated.
        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false; // Return false if nothing is done
        }

        // Called when the user selects a contextual menu item
        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            if (item.getItemId() == R.id.delete_items) {
                viewSelection.deleteSelectedItems();
                mode.finish();
                selectionMode = false;
                return true;
            }
            return false;
        }

        // Called when the user exits the action mode
        @RequiresApi(api = Build.VERSION_CODES.M)
        @Override
        public void onDestroyActionMode(ActionMode mode) {
            Log.e(TAG, "onDestroyActionMode: ");
            viewSelection.clearSelection();
            selectionMode = false;
            actionMode = null;
        }
    };
}


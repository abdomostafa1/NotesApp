package com.example.notes;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewParent;
import android.view.inputmethod.InputMethodManager;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.os.Looper;
import androidx.annotation.RequiresApi;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.view.GestureDetectorCompat;
import com.bumptech.glide.Glide;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.shape.CornerFamily;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textview.MaterialTextView;

import static android.content.ContentValues.TAG;

public class Factory {

    private LinearLayout linearLayout;
    //private LinearLayout audioLayout;
    private Audio audio;
    ViewSelection viewSelection;
    private GestureDetectorCompat selectionDetector;
    private GestureDetectorCompat edtTextDetector;
    private GestureDetectorCompat checkBoxEdtTextDetector;
    private Context context;
    private GestureDetectorCompat checkBoxDetectorListener;
    View pressedView;
    View pressedCheckBox;
    View pressedEditText;
    Handler handler;
    private float dpScreenWidth;
    private float dpScreenHeight;
    private float pixelDensity;
    private float dpImageWidth;
    private float dpImageHeight;
    private float dpLargeMargin;
    private float dpSmallMargin;
    public static float padding;
    public static float editTextPadding;

    public static final int textDirectionLTR = 0;
    public static final int textDirectionRTL = 1;
    public static int itemNumberInList = 1;
    boolean editTxtLongPress=false;
    boolean actionMove=false;
    boolean actionCancel=false;
    LinearLayout.LayoutParams wrapContent = new LinearLayout.LayoutParams
            ((int) LinearLayout.LayoutParams.MATCH_PARENT, (int) LinearLayout.LayoutParams.WRAP_CONTENT);
    LinearLayout.LayoutParams imageParams;
   // public static LinearLayout.LayoutParams audioParams;
    public static LinearLayout.LayoutParams space;
    public static TextWatcher titleTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void afterTextChanged(Editable s) {

            NewNote.changeInTitle = true;
        }

    };
    public static TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void afterTextChanged(Editable s) {

            NewNote.changeInTextInputEditText = true;
        }

    };
    private TextWatcher checkBoxTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            NewNote.changeInCheckBox = true;
            if (s.toString().contains("\n")) {
                TextInputEditText textInputEditText = (TextInputEditText) ((Activity) context).getCurrentFocus();
                ConstraintLayout constraintLayout = (ConstraintLayout) textInputEditText.getParent();
                int index = linearLayout.indexOfChild(constraintLayout);
                ConstraintLayout newCheckBox = createCheckBox(false, "");
                linearLayout.addView(newCheckBox, index + 1);
                textInputEditText.setText(s.toString().replace("\n", ""));
                TextInputEditText newTextInputEditText = (TextInputEditText) newCheckBox.findViewById(R.id.checkbox_text_input);
                newTextInputEditText.requestFocus();
            }
            NewNote.changeInTextInputEditText = true;
        }
    };
    private TextWatcher numberedListTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            NewNote.changeInCheckBox = true;
            if (s.toString().contains("\n")) {
                TextInputEditText textInputEditText = (TextInputEditText) ((Activity) context).getCurrentFocus();
                ConstraintLayout constraintLayout = (ConstraintLayout) textInputEditText.getParent();
                int index = linearLayout.indexOfChild(constraintLayout);
                String prefix = itemNumberInList + ".";
                ConstraintLayout newItem = createList(prefix, "");
                linearLayout.addView(newItem, index + 1);
                textInputEditText.setText(s.toString().replace("\n", ""));
                TextInputEditText newTextInputEditText = (TextInputEditText) newItem.findViewById(R.id.list_editText);
                newTextInputEditText.requestFocus();
                itemNumberInList++;
            }
            NewNote.changeInTextInputEditText = true;
        }
    };
    private TextWatcher dotsListTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            NewNote.changeInCheckBox = true;
            if (s.toString().contains("\n")) {
                TextInputEditText textInputEditText = (TextInputEditText) ((Activity) context).getCurrentFocus();
                ConstraintLayout constraintLayout = (ConstraintLayout) textInputEditText.getParent();
                int index = linearLayout.indexOfChild(constraintLayout);
                ConstraintLayout newItem = createList("•", "");
                linearLayout.addView(newItem, index + 1);
                textInputEditText.setText(s.toString().replace("\n", ""));
                TextInputEditText newTextInputEditText = (TextInputEditText) newItem.findViewById(R.id.list_editText);
                newTextInputEditText.requestFocus();
            }
            NewNote.changeInTextInputEditText = true;
        }
    };

    View.OnTouchListener selectionListener = new View.OnTouchListener() {
        @RequiresApi(api = Build.VERSION_CODES.M)
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (NewNote.selectionMode) {
                if (event.getAction() == MotionEvent.ACTION_UP) {

                    //ConstraintLayout view = (ConstraintLayout) v.getParent();
                    viewSelection.checkSelection(v);
                }
                return false;
            }
            Log.e("selectionListener: ", v.getClass().toString());
            return false;
        }
    };
    View.OnLongClickListener prefixLongClickListener = new View.OnLongClickListener() {
        @RequiresApi(api = Build.VERSION_CODES.M)
        @Override
        public boolean onLongClick(View v) {
            ConstraintLayout view = (ConstraintLayout) v.getParent();
            viewSelection.checkSelection(view);
            return false;
        }
    };
    CompoundButton.OnCheckedChangeListener checkedChangeListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            ConstraintLayout constraintLayout = (ConstraintLayout) buttonView.getParent();
            TextInputEditText textInputEditText = (TextInputEditText) constraintLayout.findViewById(R.id.checkbox_text_input);
            if (isChecked)
                textInputEditText.setPaintFlags(textInputEditText.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            else
                textInputEditText.setPaintFlags(Paint.ANTI_ALIAS_FLAG);
        }
    };
    Factory(Context context, LinearLayout linearLayout, ViewSelection viewSelection) {
        this.linearLayout = linearLayout;
        audio = new Audio(context, linearLayout, viewSelection);
        this.context = context;
        this.viewSelection = viewSelection;
        handler = new Handler(Looper.getMainLooper());
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        pixelDensity = displayMetrics.density;
        dpScreenHeight = displayMetrics.heightPixels;
        dpScreenWidth = displayMetrics.widthPixels;
        dpImageWidth = (dpScreenWidth * 3.2f) / 4;
        padding=(dpScreenWidth*7f)/100;
        editTextPadding=(dpScreenHeight*3f)/100;
        dpLargeMargin = (dpScreenWidth - dpImageWidth) * (3.3f / 4.0f);
        dpSmallMargin = (dpScreenWidth - dpImageWidth) * (0.7f /             4.0f);
        dpImageHeight = dpScreenHeight / 2;
        space = new LinearLayout.LayoutParams((int) dpScreenWidth, (int) (dpScreenHeight * 60 / 100));
        imageParams = new LinearLayout.LayoutParams((int) dpScreenWidth,LinearLayout.LayoutParams.WRAP_CONTENT);
        imageParams.setMargins(0,0,0,(int)padding);
        selectionDetector = new GestureDetectorCompat(context, new SelectionGesture());
        edtTextDetector = new GestureDetectorCompat(context, new EditTextGestureListener());
        checkBoxEdtTextDetector = new GestureDetectorCompat(context, new CheckBoxEditTextGesture());
        checkBoxDetectorListener= new GestureDetectorCompat(context, new CheckBoxGestureListener());

    }

    View.OnTouchListener specialTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (NewNote.selectionMode)
                return true;
            pressedView = (ConstraintLayout) v.getParent();
            return selectionDetector.onTouchEvent(event);
        }
    };

    @SuppressLint("ResourceAsColor")
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    protected ConstraintLayout createCheckBox(boolean isChecked, String task) {
        ConstraintLayout constraintLayout = (ConstraintLayout) LayoutInflater.from(context).inflate(R.layout.check_box_layout, null);
        TextInputEditText textInputEditText = (TextInputEditText) constraintLayout.findViewById(R.id.checkbox_text_input);
        MaterialCheckBox checkBox = (MaterialCheckBox) constraintLayout.findViewById(R.id.checkbox);
        textInputEditText.setText(task);
        textInputEditText.setTextSize(TypedValue.COMPLEX_UNIT_SP,context.getResources().getDimension(R.dimen._9ssp));
        checkBox.setChecked(isChecked);
        checkBox.setButtonTintList(context.getResources().getColorStateList(R.color.checkbox_color));
        checkBox.setOnCheckedChangeListener(checkedChangeListener);
        checkBox.setOnTouchListener(checkBoxTouchListener);
        textInputEditText.addTextChangedListener(checkBoxTextWatcher);
        textInputEditText.setOnTouchListener(checkBoxEditTxtTouchListener);
        if (isChecked)
            textInputEditText.setPaintFlags(textInputEditText.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        return constraintLayout;
    }

    protected ConstraintLayout createList(String prefix, String task) {
        ConstraintLayout constraintLayout = (ConstraintLayout) LayoutInflater.from(context).inflate(R.layout.list_layout, null);
        constraintLayout.setPadding(0, 0, 0,(int) editTextPadding);
        //constraintLayout.setOnTouchListener(selectionListener);
        TextInputEditText textInputEditText = (TextInputEditText) constraintLayout.findViewById(R.id.list_editText);
        MaterialTextView tvPrefix = (MaterialTextView) constraintLayout.findViewById(R.id.tv_prefix);
        textInputEditText.setBackgroundResource(R.drawable.rectangle);
        textInputEditText.setText(task);
        textInputEditText.setTextSize(TypedValue.COMPLEX_UNIT_SP,context.getResources().getDimension(R.dimen._9ssp));
      //  textInputEditText.setOnTouchListener(editTxtTouchListener);
    //    tvPrefix.setOnLongClickListener(prefixLongClickListener);
        tvPrefix.setText(prefix);
        if (prefix.equals("•"))
            textInputEditText.addTextChangedListener(dotsListTextWatcher);
        else {
            textInputEditText.addTextChangedListener(numberedListTextWatcher);
            tvPrefix.setTypeface(tvPrefix.getTypeface(), Typeface.BOLD_ITALIC);
        }

        tvPrefix.setOnTouchListener(checkBoxTouchListener);
        textInputEditText.setOnTouchListener(checkBoxEditTxtTouchListener);
        return constraintLayout;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    protected TextInputEditText createEditText(String text) {
        TextInputEditText textInputEditText = new TextInputEditText(context);
        textInputEditText.setLayoutParams(wrapContent);
        textInputEditText.setPadding((int) context.getResources().getDimension(R.dimen._16sdp), 0, (int) padding, (int)context.getResources().getDimension(R.dimen._16sdp));
        textInputEditText.getTextDirection();
        textInputEditText.setBackgroundResource(R.drawable.rectangle);
        textInputEditText.setText(text);
        textInputEditText.setTextSize(TypedValue.COMPLEX_UNIT_SP,context.getResources().getDimension(R.dimen._9ssp));
        textInputEditText.addTextChangedListener(textWatcher);
        textInputEditText.setOnTouchListener(editTxtTouchListener);
        return textInputEditText;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    protected ConstraintLayout createAudioLayout(String path) {

        ConstraintLayout newAudioLayout = audio.createAudioLayout(path);
        newAudioLayout.setPadding(0,0 ,0,0);
        newAudioLayout.setTag(R.string.path, path);
        //newAudioLayout.setOnTouchListener(selectionListener);
        return newAudioLayout;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    protected ShapeableImageView createImageView(Uri uri) {
        ShapeableImageView imageView = new ShapeableImageView(context);
        imageView.setLayoutParams(imageParams);
        imageView.setPadding((int) context.getResources().getDimension(R.dimen._16sdp),0 , (int) context.getResources().getDimension(R.dimen._16sdp), 0);
        imageView.setScaleType(ImageView.ScaleType.FIT_XY);
        imageView.setTag(R.string.uri, uri);
        float radius = context.getResources().getDimension(R.dimen.corner_radius);
        imageView.setShapeAppearanceModel(imageView.getShapeAppearanceModel()
                .toBuilder()
                .setAllCorners(CornerFamily.ROUNDED,radius)
                .build());
        ((Activity) context).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Glide.with(context).load(uri).into(imageView);
            }
        });
        return imageView;
    }

    void addClickListener(TextInputEditText view) {

        view.setOnTouchListener(editTxtTouchListener);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    protected void divideEditText(TextInputEditText textInputEditText, int index) {

        int cursorPosition = textInputEditText.getSelectionStart();
        String str = textInputEditText.getText().toString();
        String str1 = "", str2 = "";
        str1 = str.substring(0, cursorPosition);
        str2 = str.substring(cursorPosition, str.length());
        textInputEditText.setText(str1);
        TextInputEditText textInputEditText2 = createEditText(str2);
        linearLayout.addView(textInputEditText2, index + 1);
        Log.e(TAG, "divideEditText: requestFocus()");
        textInputEditText2.requestFocus();
    }

    View.OnTouchListener checkBoxTouchListener = new View.OnTouchListener() {
        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            pressedCheckBox = v;
            if (NewNote.selectionMode)
                return checkBoxDetectorListener.onTouchEvent(event);
            else
                return false;
        }
    };

    void releaseAudio(){
        audio.releaseAudio();
    }
    View.OnTouchListener editTxtTouchListener = new View.OnTouchListener() {
        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            pressedEditText = v;
            TextInputEditText textInputEditText = (TextInputEditText) v;
            if (NewNote.selectionMode)
                return edtTextDetector.onTouchEvent(event);
            else {
                //NewNote.horizontalLayout.setVisibility(View.VISIBLE);
                Log.e(TAG, "NewNote.horizontalLayout.setVisibility(View.VISIBLE)" );
                textInputEditText.setClickable(true);
                return false;
            }
        }
    };
    View.OnTouchListener checkBoxEditTxtTouchListener = new View.OnTouchListener() {
        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            pressedEditText = v;
            TextInputEditText textInputEditText = (TextInputEditText) v;
            if (NewNote.selectionMode)
                return checkBoxEdtTextDetector.onTouchEvent(event);
            else {
                //NewNote.horizontalLayout.setVisibility(View.VISIBLE);
                textInputEditText.setClickable(true);
                return false;
            }
        }
    };

    class SelectionGesture extends GestureDetector.SimpleOnGestureListener {
        private static final String DEBUG_TAG = "Gestures";

        @RequiresApi(api = Build.VERSION_CODES.M)
        @Override
        public boolean onDown(MotionEvent event) {
            Log.d(DEBUG_TAG, "onDown: " + event.toString());
            if (NewNote.selectionMode) {
                return true;
            }
            return false;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            Log.e(TAG, "onTouchEvent: SCROLL");
            if (NewNote.selectionMode) {
                return true;
            } else
                return false;
        }

        @RequiresApi(api = Build.VERSION_CODES.M)
        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            if (NewNote.selectionMode) {
                Log.e("pressedView.getClass() ", pressedView.getClass().toString());
                viewSelection.checkSelection(pressedView);
                return true;
            }
            return false;
        }
    }
    class EditTextGestureListener extends GestureDetector.SimpleOnGestureListener {
        @RequiresApi(api = Build.VERSION_CODES.M)
        @Override
        public void onLongPress(MotionEvent e) {
            TextInputEditText textInputEditText = (TextInputEditText) pressedEditText;
            if (textInputEditText.getParent().getClass().toString().contains("LinearLayout"))
                viewSelection.checkSelection(textInputEditText);

            textInputEditText.setClickable(false);
            super.onLongPress(e);
        }

        private static final String DEBUG_TAG = "Gestures";

        @Override
        public boolean onDown(MotionEvent event) {
            TextInputEditText textInputEditText = (TextInputEditText) pressedEditText;
            Log.d(DEBUG_TAG,"onDown: " + event.toString());
            textInputEditText.setClickable(false);
            return true;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            Log.e(TAG, "ACTION Move1: "+String.valueOf(e1.getAction()) );
            Log.e(TAG, "ACTION Move2: "+String.valueOf(e2.getAction()) );

            return super.onScroll(e1, e2, distanceX, distanceY);
        }

        @RequiresApi(api = Build.VERSION_CODES.M)
        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            TextInputEditText textInputEditText = (TextInputEditText) pressedEditText;
            if (textInputEditText.getParent().getClass().toString().contains("LinearLayout")){
                 viewSelection.checkSelection(textInputEditText);
                textInputEditText.setClickable(false);
                return true;
            }
            return super.onSingleTapUp(e);
        }
    }

    class CheckBoxEditTextGesture extends GestureDetector.SimpleOnGestureListener {
        ConstraintLayout parent;
        @RequiresApi(api = Build.VERSION_CODES.M)
        @Override
        public void onLongPress(MotionEvent e) {
            TextInputEditText textInputEditText = (TextInputEditText) pressedEditText;
            textInputEditText.setClickable(false);
            parent=(ConstraintLayout) textInputEditText.getParent();
            viewSelection.checkSelection(parent);

            super.onLongPress(e);
        }

        private static final String DEBUG_TAG = "Gestures";

        @Override
        public boolean onDown(MotionEvent event) {
            TextInputEditText textInputEditText = (TextInputEditText) pressedEditText;
            Log.d(DEBUG_TAG,"onDown: " + event.toString());
            textInputEditText.setClickable(false);
            return true;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            Log.e(TAG, "ACTION Move1: "+String.valueOf(e1.getAction()) );
            Log.e(TAG, "ACTION Move2: "+String.valueOf(e2.getAction()) );

            return super.onScroll(e1, e2, distanceX, distanceY);
        }

        @RequiresApi(api = Build.VERSION_CODES.M)
        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            TextInputEditText textInputEditText = (TextInputEditText) pressedEditText;
            parent=(ConstraintLayout) textInputEditText.getParent();
            viewSelection.checkSelection(parent);
                textInputEditText.setClickable(false);
                return true;
        }
    }

    class CheckBoxGestureListener extends GestureDetector.SimpleOnGestureListener {
        ConstraintLayout parent;
        @RequiresApi(api = Build.VERSION_CODES.M)
        @Override
        public boolean onSingleTapUp(MotionEvent e) {

                parent =(ConstraintLayout) pressedCheckBox.getParent();
                viewSelection.checkSelection(parent);
                return true;
        }

        @RequiresApi(api = Build.VERSION_CODES.M)
        @Override
        public void onLongPress(MotionEvent e) {
            super.onLongPress(e);
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            return super.onScroll(e1, e2, distanceX, distanceY);
        }

        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }
    }

}

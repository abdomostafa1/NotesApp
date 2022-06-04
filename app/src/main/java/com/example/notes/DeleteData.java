package com.example.notes;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

import static android.content.ContentValues.TAG;

public class DeleteData extends AsyncTask<Note, Void, Boolean> {

    private Context context;
    private Factory factory;
    private Activity activity;
    private LinearLayout linearLayout;
    private Note note;
    int i=0;
    DeleteData(Context context, Factory factory, LinearLayout linearLayout, Note note) {
        this.context = context;
        this.factory = factory;
        activity = (Activity) context;
        this.linearLayout = linearLayout;
        this.note = note;
    }

    public DeleteData(Context context) {
        this.context = context;
    }


    @Override
    protected void onPostExecute(Boolean aVoid) {
        if (aVoid.booleanValue()) {
            int position = MainActivity.noteList.indexOf(MainActivity.pressedNote);
            Log.e(TAG, "onNoteClick: note position  " + position);

            MainActivity.noteList.remove(MainActivity.pressedNote);
            MainActivity.adapter.notifyItemRemoved(position);
            MainActivity.recyclerView.setLayoutAnimation(MainActivity.fastController);
            MainActivity.recyclerView.scheduleLayoutAnimation();
        }
    }
    @Override
    protected Boolean doInBackground(Note... notes) {
        if(notes[0]==null) {
            deleteNote();
            return true;
        }
        else
            deleteNote(notes[0]);
        return false;
    }

    public void deleteNote() {


        for (int index = 0; index < linearLayout.getChildCount(); index++) {
            View view = linearLayout.getChildAt(index);
            // check  Class Type of view
            String className = view.getClass().toString();

            if(index==0)
            Log.e(TAG, "Titlllle: "+((TextInputEditText)view).getText().toString() );

            if (className.contains("ConstraintLayout") && view.getId() == R.id.audio_Constrain) {
                ConstraintLayout audioLayout = (ConstraintLayout) view;
                String path = (String) audioLayout.getTag(R.string.path);
                File file = new File(path);
                file.delete();
            } else if (className.contains("ImageView")) {
                ShapeableImageView imageView = (ShapeableImageView) view;
                if (imageView.getTag(R.string.path) != null) {
                    String path = (String) imageView.getTag(R.string.path);
                    File file = new File(path);
                    file.delete();
                }
            }

        }
            if (NewNote.newNote)
                return;
             else {
                Uri uri = Uri.parse(NoteProvider.providerConstPath + "/" + Table.Table_Name);
                String selection = Table.COLUMN_ID + "=" + note.getNoteId();
                int isDeleted = context.getContentResolver().delete(uri, selection, null);
            }
    }

    private void deleteNote(Note note) {
        Log.e(TAG, "inside deleteNotes "+String.valueOf(i++) );
        String jsonNoteBody = note.getNoteBody();
        JSONObject root = null;
        try {
            root = new JSONObject(jsonNoteBody);
            JSONArray noteBody = root.getJSONArray("noteBody");
            for (int i = 0; i < noteBody.length(); i++) {

                JSONObject child = noteBody.getJSONObject(i);
                String type = child.getString("type");
                if (type.contains("Audio")) {
                    String path = child.getString("path");
                    new File(path).delete();
                } else if (type.contains("Image")) {
                    if (child.has("path")) {
                        String path = child.getString("path");
                        new File(path).delete();
                    }
                } else
                    continue;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Uri uri = Uri.parse(NoteProvider.providerConstPath + "/" + Table.Table_Name);
        String selection = Table.COLUMN_ID + "=" + note.getNoteId();
        int isDeleted = context.getContentResolver().delete(uri, selection, null);


    }

    public void deleteImage(int viewIndex) {
        ImageView imageView = (ImageView) linearLayout.getChildAt(viewIndex);
        if (imageView.getTag(R.string.path) != null) {
            String path = (String) imageView.getTag(R.string.path);
            File file = new File(path);
            boolean isDeleted = file.delete();

        }

        linearLayout.removeView(imageView);
        NewNote.changeInImage = true;
        TextInputEditText textInputEditText = (TextInputEditText) factory.createEditText("");
        linearLayout.addView(textInputEditText, viewIndex );
        textInputEditText.requestFocus();
    }

    public void deleteAudio(int index) {

        View audioLayout =  linearLayout.getChildAt(index);
        String path = (String) audioLayout.getTag(R.string.path);
        File file = new File(path);
        boolean isDeleted = file.delete();

        linearLayout.removeView(audioLayout);
        NewNote.changeInAudio = true;
        TextInputEditText textInputEditText = (TextInputEditText) factory.createEditText("");
        linearLayout.addView(textInputEditText, index );
        //remove Audio from firebase
        textInputEditText.requestFocus();

    }

    public void deleteCheckBox(int index) {
        linearLayout.removeViewAt(index);
        NewNote.changeInCheckBox = true;

    }

    // recursive function
    private void mergeTwoEditText(int index) {
        //linearLayout.getChildCount()-1=6
        Log.e("lnLay.ChildCount()-1: ", linearLayout.getChildCount() - 1 + "");
        Log.e("index: ", index + "");
        if (index <= linearLayout.getChildCount() - 1 && index != 2) {
            View view1 = linearLayout.getChildAt(index - 1);
            View view2 = linearLayout.getChildAt(index);
            if (view1.getClass().toString().contains("TextInputEditText") && view2.getClass().toString().contains("TextInputEditText")) {
                TextInputEditText textInputEditText1 = (TextInputEditText) view1;
                TextInputEditText textInputEditText2 = (TextInputEditText) view2;
                // add new line between two texts
                String text2 = "\n" + textInputEditText2.getText().toString();
                textInputEditText1.append(text2);
                linearLayout.removeView(textInputEditText2);
                mergeTwoEditText(index);
            }
            mergeTwoEditText(index + 1);
        }
    }
}


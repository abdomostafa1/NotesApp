package com.example.notes;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import androidx.annotation.RequiresApi;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textview.MaterialTextView;

import java.util.Date;

import static android.content.ContentValues.TAG;

public class StoreNote extends AsyncTask<Void, Void, Void> {
    private Context context;
    private LinearLayout linearLayout;
    private Note note;
    private ContentValues values;
    ListSort listSort = null;
    private String title;
    private StringBuilder jsonNoteBody;
    private Date date;
    private long id;

    StoreNote(Context context, LinearLayout linearLayout, Note note) {
        this.context = context;
        this.linearLayout = linearLayout;
        this.note = note;
        values = new ContentValues();
        jsonNoteBody = new StringBuilder();
        id = note.getNoteId();
        listSort = new ListSort(MainActivity.noteList, MainActivity.adapter);
    }

    StoreNote(Context context, LinearLayout linearLayout, long newNoteId) {
        this.context = context;
        this.linearLayout = linearLayout;
        values = new ContentValues();
        jsonNoteBody = new StringBuilder();
        id = newNoteId;
        listSort = new ListSort(MainActivity.noteList, MainActivity.adapter);
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        if (NewNote.newNote) {
            // add new note
            addNoteToList();
        } else {
            // update note
            updateNoteInList();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected Void doInBackground(Void... voids) {
        // in case of new note i will store date of it
        storeToDevice();
        return null;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    protected void storeToDevice() {
        jsonNoteBody.append("{ noteBody:[ ");
        View view1 = linearLayout.getChildAt(0);
        getTitle(view1);
        for (int index = 1; index < linearLayout.getChildCount() - 1; index++) {
            if (index > 1)
                jsonNoteBody.append(",");
            View view = linearLayout.getChildAt(index);
            // check  Class Type of view
            String className = view.getClass().toString();

            if (className.contains("TextInputEditText"))
                storeText(view);

            else if (className.contains("ConstraintLayout")) {
                if (view.getId() == R.id.audio_Constrain)
                    storeAudio(view);
                else if (view.getId() == R.id.checkbox_Constrain)
                    storeCheckBox(view);
                else   // if (view.getId()==R.id.list_Constrain)
                    storeList(view);

            } else     // condition of else -----> view_type.contains("ImageView")
                storeImage(view);

        }
        jsonNoteBody.append("] }");
        values.put(Table.COLUMN_TITLE, title);
        values.put(Table.COLUMN_Body, String.valueOf(jsonNoteBody));
        Log.e("jsonNoteBody: ", String.valueOf(jsonNoteBody));
        Uri uri = Uri.parse(NoteProvider.providerConstPath + "/" + Table.Table_Name);
        if (NewNote.newNote) {
            // add new note
            values.put(Table.COLUMN_ID, id);
            context.getContentResolver().insert(uri, values);
            note = new Note(id, title, jsonNoteBody.toString());
        } else {
            // update note
            Log.e("Message: ", note.getNoteId() + "");
            values.put(Table.COLUMN_ID, note.getNoteId());
            String selection = Table.COLUMN_ID + "=" + String.valueOf(note.getNoteId());
            Log.e("Selection: ", selection);
            context.getContentResolver().update(uri, values, selection, null);
            note = new Note(id, title, jsonNoteBody.toString());
        }

    }

    void addNoteToList() {
        MainActivity.noteList.add(note);
        MainActivity.checkEmptyList();
        if (MainActivity.orderBy == 1)
            listSort.sortToNewest_order();
        else if (MainActivity.orderBy == 2)
            listSort.sortToOldest_order();
        else if (MainActivity.orderBy == 3)
            listSort.sortToAZ_order();
        else
            listSort.sortToZA_order();
        int position = MainActivity.noteList.indexOf(note) ;
        Log.e(TAG, "onNoteClick: note position  "+position );
        MainActivity.adapter.notifyItemInserted(position);
        MainActivity.recyclerView.setLayoutAnimation(MainActivity.fastController);
        MainActivity.recyclerView.scheduleLayoutAnimation();
    }

    void updateNoteInList() {

        MainActivity.noteList.set(MainActivity.position, note);

        try {
            if (MainActivity.orderBy == 1)
                listSort.sortToNewest_order();
            else if (MainActivity.orderBy == 2)
                listSort.sortToOldest_order();
            else if (MainActivity.orderBy == 3)
                listSort.sortToAZ_order();
            else
                listSort.sortToZA_order();

            int position = MainActivity.noteList.indexOf(note);
            MainActivity.adapter.notifyItemChanged(position);
        }
        catch (Exception e){
            e.printStackTrace();
        }

    }

    @SuppressLint("Range")
    private Note createNewNote(Cursor cursor) {
        long id = cursor.getLong(cursor.getColumnIndex(Table.COLUMN_ID));
        String title = cursor.getString(cursor.getColumnIndex(Table.COLUMN_TITLE));
        String noteBody = cursor.getString(cursor.getColumnIndex(Table.COLUMN_Body));
        return new Note(id, title, noteBody);
    }

    private void getTitle(View view) {
        //cast view to TextInputEditText
        TextInputEditText titleEdt = (TextInputEditText) view;
        title = titleEdt.getText().toString();
        title= title.replaceAll("(?<=[{:,])|(?=[:,}])", "\"");

    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void storeText(View view) {
        TextInputEditText message = (TextInputEditText) view;
        String text = message.getText().toString();
        text=text.replaceAll("(?<=[{:,])|(?=[:,}])", "\"");
        text=text.replace("\"","\\\"");
        text=text.replace("\'","\\\'");

//        Gson gson=new Gson();
//        text=gson.toJson(text);

        Log.e(TAG, "String Value : "+text);
        String jsonStr = "{ type:\"Text\", " +
                "text:\"" + text + "\" " +
                "}";
        jsonNoteBody.append(jsonStr);

    }

    private void storeAudio(View view) {
        //upload Record

        String path = (String) view.getTag(R.string.path);
        String jsonStr = "{ type:\"Audio\" ," +
                " path:\"" + path + "\"" +
                " }";
        jsonNoteBody.append(jsonStr);

    }

    private void storeImage(View view) {
        ShapeableImageView image = (ShapeableImageView) view;
        Uri imageUri = (Uri) image.getTag(R.string.uri);
        String jsonStr = "{type:\"Image\" ," +
                " uri:\"" + imageUri.toString() + "\" ,";
        Log.e("jsonStrImage: ", jsonStr);
        if (image.getTag(R.string.path) != null) {
            String path = (String) image.getTag(R.string.path);
            jsonStr += "imageSource:\"camera\" ," +
                    " path: \"" + path + "\" }";
        } else {
            jsonStr += "imageSource:\"photosProvider\" }";
        }
        jsonNoteBody.append(jsonStr);
    }

    private void storeCheckBox(View view) {
        ConstraintLayout constraintLayout = (ConstraintLayout) view;
        MaterialCheckBox checkBox = (MaterialCheckBox) constraintLayout.findViewById(R.id.checkbox);
        TextInputEditText textInputEditText = (TextInputEditText) constraintLayout.findViewById(R.id.checkbox_text_input);
        boolean isChecked = checkBox.isChecked();
        String task = textInputEditText.getText().toString();
        String jsonStr = "{ type:\"CheckBox\", " +
                "isChecked:" + isChecked + "," +
                " task:\"" + task + "\" }";
        jsonNoteBody.append(jsonStr);
    }

    private void storeList(View view) {
        ConstraintLayout constraintLayout = (ConstraintLayout) view;
        MaterialTextView tvPrefix = (MaterialTextView) constraintLayout.findViewById(R.id.tv_prefix);
        TextInputEditText textInputEditText = (TextInputEditText) constraintLayout.findViewById(R.id.list_editText);
        String prefix = tvPrefix.getText().toString();
        String task = textInputEditText.getText().toString();
        String jsonStr = "{ type:\"List\", " +
                "prefix:\"" + prefix + "\"," +
                " task:\"" + task + "\" }";
        jsonNoteBody.append(jsonStr);
    }
//    protected void updateNote() {
//        // in this method i will clear all items of note and reupload them again
//        databaseReference.child("title").removeValue();
//        messagesRef.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                ValueEventListener valueEventListener=this;
//                // next line of code to remove this listener which i'm in so when i delete items
//                // it will not be triggered and take the app into infinite loop
//                messagesRef.removeEventListener(valueEventListener);
//                int counter = 1;
//                //get each child reference to delete them and start uploading data when i reach last element
//                for (DataSnapshot child : snapshot.getChildren()) {
//                    DatabaseReference childReference = child.getRef();
//                    int finalCounter = counter;
//                    childReference.removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
//                        @Override
//                        public void onSuccess(Void aVoid) {
//                            if (finalCounter == snapshot.getChildrenCount())
//                            // last element
//                            {
//                                MainActivity.ACTION_CHANGE=true;
//                                uploadToFirebase();
//                            }
//                        }
//                    });
//                    counter++;
//                }
//            }
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//                Toast.makeText(context, "Error no internet", Toast.LENGTH_SHORT).show();
//            }
//        });
//    }
}

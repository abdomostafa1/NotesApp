/*

      this class is not used in this application
      i just keep it here and ine day i will add saving data to cloud
*/
package com.example.notes;

import android.content.Context;
import android.net.Uri;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;

public class UploadData {
    private DatabaseReference databaseReference;
    private DatabaseReference messagesRef;
    private StorageReference storageReference;
    private Context context;
    private LinearLayout linearLayout;

    UploadData(Context context, LinearLayout linearLayout, DatabaseReference databaseReference, DatabaseReference messagesRef, StorageReference storageReference) {
        this.databaseReference = databaseReference;
        this.messagesRef = messagesRef;
        this.storageReference = storageReference;
        this.context=context;
        this.linearLayout = linearLayout;
    }

    protected void uploadToFirebase() {

        for (int index = 1; index < linearLayout.getChildCount(); index++) {
            View view = linearLayout.getChildAt(index);
            // check  Class Type of view
            String className = view.getClass().toString();

            if (index == 1)
                uploadTitle(view);
            else if (className.contains("TextInputEditText"))
                uploadText(view);
            else if (className.contains("LinearLayout"))
                uploadAudio(view);
            else if(className.contains("ConstraintLayout"))
                uploadCheckBox(view);
            else     // condition of else -----> view_type.contains("ImageView")
            uploadImage(view);

        }

     }

    private void uploadTitle(View view)
    {
        //cast view to TextInputEditText
        TextInputEditText title = (TextInputEditText) view;
        String text = title.getText().toString();
        databaseReference.child("title").setValue(text);
    }
    private void uploadText(View view)
    {
        TextInputEditText message = (TextInputEditText) view;
        String text = message.getText().toString();
        messagesRef.push().setValue(text);
    }

    private void uploadAudio(View view)
    {
        //upload Record
        LinearLayout audioLayout = (LinearLayout) view;
        DatabaseReference audioReference = messagesRef.push();
        String audioLocation =(String) audioLayout.getTag(R.string.location);
        Uri audioUri;
        if (audioLocation.contains("https://")){
            audioReference.setValue(audioLocation);
            return;
        }

        audioUri = Uri.fromFile(new File(audioLocation));
        StorageMetadata metadata = new StorageMetadata.Builder()
                .setContentType("audio/3gp")
                .build();

        // set Empty temporary  value until we get audio link
        audioReference.setValue("");
        String key = audioReference.getKey();
        UploadTask uploadTask = storageReference.child("Recordings").child(key).putFile(audioUri);
        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                taskSnapshot.getStorage().getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        String uri = task.getResult().toString();
                        messagesRef.child(key).setValue(uri.toString());
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(context, "Failed to upload record", Toast.LENGTH_SHORT).show();
            }
        });

    }
    private void uploadImage(View view)
    {
        ImageView image = (ImageView) view;
        Uri imageUri = (Uri) image.getTag(R.string.uri);
        DatabaseReference imageReference = messagesRef.push();
        // set Empty temporary  value until we get image link
        if(imageUri.getScheme().equals("https")){
            imageReference.setValue(imageUri.toString());
            return;
        }
        imageReference.setValue("");
        String key = imageReference.getKey();
        UploadTask uploadTask = storageReference.child("Images").child(key).putFile(imageUri);

        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                taskSnapshot.getStorage().getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        String uri = task.getResult().toString();
                        messagesRef.child(key).setValue(uri);
                    }
                });
            }
        })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(context, "Failed to upload image", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void uploadCheckBox(View view)
    {
        ConstraintLayout constraintLayout=(ConstraintLayout)view;
        MaterialCheckBox checkBox=(MaterialCheckBox)constraintLayout.findViewById(R.id.checkbox);
        TextInputEditText textInputEditText=(TextInputEditText) constraintLayout.findViewById(R.id.checkbox_text_input);
        boolean isChecked=checkBox.isChecked();
        String task=textInputEditText.getText().toString();
        DatabaseReference checkBoxReference=messagesRef.push();
        checkBoxReference.child("isChecked").setValue(Boolean.valueOf(isChecked));
        checkBoxReference.child("task").setValue(task);
    }

    protected void updateNote() {
        // in this method i will clear all items of note and reupload them again
        databaseReference.child("title").removeValue();
        messagesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ValueEventListener valueEventListener=this;
                // next line of code to remove this listener which i'm in so when i delete items
                // it will not be triggered and take the app into infinite loop
                    messagesRef.removeEventListener(valueEventListener);
                int counter = 1;
                //get each child reference to delete them and start uploading data when i reach last element
                for (DataSnapshot child : snapshot.getChildren()) {
                    DatabaseReference childReference = child.getRef();
                    int finalCounter = counter;
                    childReference.removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            if (finalCounter == snapshot.getChildrenCount())
                                // last element
                            {
                                MainActivity.ACTION_CHANGE=true;
                                uploadToFirebase();
                            }
                        }
                    });
                    counter++;
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(context, "Error no internet", Toast.LENGTH_SHORT).show();
            }
        });
    }
}

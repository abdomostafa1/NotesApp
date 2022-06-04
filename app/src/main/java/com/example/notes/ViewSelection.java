package com.example.notes;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import androidx.annotation.RequiresApi;

import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;

import static android.content.ContentValues.TAG;

public class ViewSelection {
    ArrayList<View> selectionList;
    Context context;
    LinearLayout linearLayout;
    public ViewSelection(Context context,LinearLayout linearLayout) {
        this.context=context;
        this.linearLayout=linearLayout;
        selectionList=new ArrayList<View>();
    }
    @RequiresApi(api = Build.VERSION_CODES.M)
    void checkSelection(View view){
        // check if view is in selection list
       boolean isFound = selectionList.contains(view);
       if(isFound)
           removeSelection(view);
        else
            addSelection(view);
        Log.e(TAG, "checkSelection: "+String.valueOf(selectionList.size()) );

    }
    @RequiresApi(api = Build.VERSION_CODES.M)
    void removeSelection(View view){

        view.setForeground(context.getDrawable(R.drawable.transparent_rect));
        selectionList.remove(view);
        NewNote.actionMode.setTitle(selectionList.size()+"  "+context.getResources().getString(R.string.selected));
        if(selectionList.size()==0) {
            NewNote.actionMode.finish();
            NewNote.selectionMode = false;
            Log.e(TAG, "SelectionMode: "+String.valueOf(NewNote.selectionMode) );

        }

    }
    @RequiresApi(api = Build.VERSION_CODES.M)
    void addSelection(View view){
        if(selectionList.size()==0){
            View focusedView=((Activity)context).getCurrentFocus();
            if(focusedView!=null)
             focusedView.clearFocus();
            ;;
            if(NewNote.actionMode==null)
                NewNote.actionMode=((Activity)context).startActionMode(NewNote.actionModeCallback);

            NewNote.selectionMode=true;
        }
        view.setForeground(context.getDrawable(R.drawable.selection_rect));
        selectionList.add(view);
        NewNote.actionMode.setTitle(selectionList.size()+"  "+context.getResources().getString(R.string.selected));
    }
    void deleteSelectedItems(){

        for (View view:selectionList) {
            if(linearLayout.indexOfChild(view)==0){
                TextInputEditText title=(TextInputEditText)view;
                title.setText("");
                continue;
            }
            linearLayout.removeView(view);
        }

    }
    @RequiresApi(api = Build.VERSION_CODES.M)
    void clearSelection(){

        Log.e(TAG, "clearSelection: "+selectionList.size() );
        for (int i=0;i<selectionList.size();i++) {
            View v=selectionList.get(i);
            v.setForeground(context.getDrawable(R.drawable.transparent_rect));
        }
        selectionList.clear();
        NewNote.selectionMode=false;
    }

}


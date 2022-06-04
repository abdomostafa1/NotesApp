package com.example.notes;

import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import static android.content.ContentValues.TAG;

public class ListSort {
    ArrayList<Note> noteList;
    NoteAdapter adapter;

    public ListSort(ArrayList<Note> noteList, NoteAdapter adapter) {
        this.noteList = noteList;
        this.adapter = adapter;
    }

    void sortToNewest_order(){

        if(MainActivity.orderBy==2)
            Collections.reverse(noteList);
        else {
            Collections.sort(noteList, new Comparator<Note>() {
                @Override
                public int compare(Note o1, Note o2) {

                    long c=o2.getNoteId()-o1.getNoteId();
                    if (c>0)
                        return 1;
                    else
                        return -1;
                }
            });
        }
        adapter.submitList(noteList);
        adapter.notifyDataSetChanged();

    }
    void sortToOldest_order(){
        Log.e(TAG, "sortToOldest_order: " );
        if(MainActivity.orderBy==1)
            Collections.reverse(noteList);
        else {
            Collections.sort(noteList, new Comparator<Note>() {
                @Override
                public int compare(Note o1, Note o2) {

                    long c=o1.getNoteId()-o2.getNoteId();
                    if (c>0)
                        return 1;
                    else
                        return -1;
                }
            });
        }
        adapter.submitList(noteList);
        adapter.notifyDataSetChanged();
    }
    void sortToAZ_order(){
        Log.e(TAG, "sortToAZ_order: " );
        if(MainActivity.orderBy==4)
            Collections.reverse(noteList);
        else {
            Collections.sort(noteList, new Comparator<Note>() {
                @Override
                public int compare(Note o1, Note o2) {

                    return o1.getTitle().compareToIgnoreCase(o2.getTitle());
                }
            });
        }
        adapter.submitList(noteList);
        adapter.notifyDataSetChanged();

    }
    void sortToZA_order(){
        Log.e(TAG, "sortToZA_order: " );
        if(MainActivity.orderBy==3)
            Collections.reverse(noteList);
        else {
            Collections.sort(noteList, new Comparator<Note>() {
                @Override
                public int compare(Note o1, Note o2) {

                    return o2.getTitle().compareToIgnoreCase(o1.getTitle());
                }
            });
        }
        adapter.submitList(noteList);
        adapter.notifyDataSetChanged();
    }
}

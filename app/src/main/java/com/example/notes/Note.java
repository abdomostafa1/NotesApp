package com.example.notes;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.snapshot.IndexedNode;

import java.io.Serializable;
import java.util.Map;

public class Note implements  Serializable{

    private long noteId;
    private String title;
    private String noteBody;
    private String date;
    Note(long noteId, String title, String noteBody)
    {
        this.noteId=noteId;
        this.title=title;
        this.noteBody=noteBody;

    }
    protected String getTitle()
    {
        return title;
    }
    protected String getNoteBody()
    {
        return  noteBody;
    }
    protected long getNoteId()
    {
        return noteId;
    }
}

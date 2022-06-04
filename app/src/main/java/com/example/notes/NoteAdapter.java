package com.example.notes;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.util.LayoutDirection;
import android.util.Log;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import static android.content.ContentValues.TAG;

public class NoteAdapter extends ListAdapter<Note, NoteAdapter.MyViewHolder> {
    public static ArrayList<Note> selectionList;
    public static ActionMode actionMode=null;
    SimpleDateFormat dateFormatLTR;
    SimpleDateFormat dateFormatRTL;
    Date date;
    OnNoteClickListener onNoteClickListener;
    public static Context context;
    boolean selectionMode = false;
    public static boolean linearLayoutManager = true;
    public static boolean gridLayoutManager = false;
    boolean searchMode=false;
    public NoteAdapter(@NonNull DiffUtil.ItemCallback<Note> diffCallback, OnNoteClickListener onNoteClickListener, boolean searchMode) {
        super(diffCallback);
        this.onNoteClickListener = onNoteClickListener;
        this.searchMode=searchMode;
        dateFormatLTR = new SimpleDateFormat("d/MM/yyyy  K:mma");
        dateFormatRTL = new SimpleDateFormat("yyyy/MM/d  K:mma");
        selectionList = new ArrayList<Note>();
        date = new Date();

    }

    @NonNull
    @Override
    public NoteAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View convertView = null;
        convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_view_row, parent, false);
        context = parent.getContext();
        return new MyViewHolder(convertView, onNoteClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull NoteAdapter.MyViewHolder holder, int position) {
        Note note = getCurrentList().get(position);
        String title = note.getTitle();
        holder.title.setText(title);
        long id = note.getNoteId();
        date.setTime(id);
        if (context.getResources().getConfiguration().getLayoutDirection() == LayoutDirection.LTR)
            holder.date.setText(dateFormatLTR.format(date));
        else
            holder.date.setText(dateFormatRTL.format(date));
        String noteBody = getCurrentList().get(position).getNoteBody();
        try {
            JSONObject root = new JSONObject(noteBody);
            JSONArray array = root.getJSONArray("noteBody");
            JSONObject message = array.getJSONObject(0);
            String text = message.getString("text");
            holder.body.setText(text);
        } catch (JSONException e) {
        }
        if (linearLayoutManager)
            holder.body.setMaxLines(3);
        else
            holder.body.setMaxLines(35);

        if(selectionList.contains(note))
            holder.itemView.setBackgroundResource(R.drawable.selected_item_background);
        else
            holder.itemView.setBackgroundResource(R.drawable.recycler_item_background);


        if(searchMode)
            return ;
        else {
            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @RequiresApi(api = Build.VERSION_CODES.M)
                @Override
                public boolean onLongClick(View v) {
                    Log.e(TAG, "onLongClick: position " + position);
                    Note note = MainActivity.noteList.get(position);
                    MainActivity.selectionMode = true;
                        checkSelection(position);
                    return true;
                }
            });
        }

    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    static void checkSelection(int position) {

        Note note = MainActivity.noteList.get(position);
        boolean isFound = selectionList.contains(note);
        if (isFound)
            removeSelection(note, position);
        else
            addSelection(note, position);
        Log.e(TAG, "checkSelection: " + String.valueOf(selectionList.size()));

    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    static void removeSelection(Note note, int position) {

        RecyclerView.ViewHolder holder = MainActivity.recyclerView.findViewHolderForAdapterPosition(position);
        holder.itemView.setBackgroundResource(R.drawable.recycler_item_background);
        selectionList.remove(note);

        actionMode.setTitle(selectionList.size() +"  "+ context.getResources().getString(R.string.selected));
        if (selectionList.size() == 0) {
            actionMode.finish();
            MainActivity.selectionMode = false;

        }

    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    static void addSelection(Note note, int position) {
        Log.e(TAG, "addSelection: new note" );
        if (selectionList.size() == 0) {

            Log.e(TAG, "turn actionMode ON" );
            if (actionMode == null) {
                actionMode = ((Activity) context).startActionMode(MainActivity.callback);
            if (actionMode == null)
                Log.e(TAG, "actionMode == null" );
            }

            MainActivity.selectionMode = true;
        }

        RecyclerView.ViewHolder holder = MainActivity.recyclerView.findViewHolderForAdapterPosition(position);
        holder.itemView.setBackgroundResource(R.drawable.selected_item_background);
        selectionList.add(note);
        actionMode.setTitle(selectionList.size()+"  " + context.getResources().getString(R.string.selected));
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    static void clearSelection() {

        Log.e(TAG, "clearSelection: " + selectionList.size());
        for (Note note : selectionList) {
            int position = MainActivity.noteList.indexOf(note);
            RecyclerView.ViewHolder holder = MainActivity.recyclerView.findViewHolderForAdapterPosition(position);
            if (holder!=null)
              holder.itemView.setBackgroundResource(R.drawable.recycler_item_background);
        }
        selectionList.clear();
        MainActivity.selectionMode = false;
    }


    @Override
    public int getItemCount() {
        return getCurrentList().size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView title;
        TextView date;
        TextView body;

        OnNoteClickListener onNoteClickListener;

        public MyViewHolder(@NonNull View itemView, OnNoteClickListener onNoteClickListener) {
            super(itemView);
            title = itemView.findViewById(R.id.note_title);
            date = itemView.findViewById(R.id.note_date);
            body = itemView.findViewById(R.id.note_Body);

            this.onNoteClickListener = onNoteClickListener;
            itemView.setOnClickListener(this);

        }

        @Override
        public void onClick(View v) {
            onNoteClickListener.onNoteClick(getAdapterPosition());
        }

    }

    interface OnNoteClickListener {
        void onNoteClick(int position);
    }
}


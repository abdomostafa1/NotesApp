package com.example.notes;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;

import static android.content.ContentValues.TAG;

public class SearchActivity extends AppCompatActivity implements NoteAdapter.OnNoteClickListener {

    ActivityResultLauncher<Intent> launcher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    // delete note
                    if (result.getResultCode() == RESULT_DELETE && pressedNote != null) {
                        Snackbar snackbar=Snackbar.make(coordinatorLayout, R.string.note_is_deleted, Snackbar.LENGTH_LONG);
                        View view = snackbar.getView();
                        TextView textView = view.findViewById(com.google.android.material.R.id.snackbar_text);
                        snackbar.show();

                    }
                    handler.postDelayed(runnable,2000);
                }
            });
    CoordinatorLayout coordinatorLayout;
    RecyclerView recycler;
    NoteAdapter adapter;
    MaterialToolbar toolbar;
    TextInputEditText searchView;
    ConstraintLayout emptyView;
    Note pressedNote;
    FloatingActionButton fab;
    private Handler handler;

    private int RESULT_DELETE = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

//
//        toolbar = (MaterialToolbar) findViewById(R.id.sr_toolbar);
//        setSupportActionBar(toolbar);
        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinator_layout);
        recycler = (RecyclerView) findViewById(R.id.search_recycler_view);
        searchView = (TextInputEditText) findViewById(R.id.search_view);
        emptyView=(ConstraintLayout)findViewById(R.id.empty_constrain);
        adapter = new NoteAdapter(new noteListDiffCallback(), this,true);
        recycler.setAdapter(adapter);

        SharedPreferences preferences = getPreferences(MODE_PRIVATE);
        int i = preferences.getInt("RecyclerLayoutManager", 1);
        if (i == 1) {
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
            linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
            recycler.setLayoutManager(linearLayoutManager);
        } else
            recycler.setLayoutManager(new StaggeredGridLayoutManager(2, 1));

        searchView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

                String newText = s.toString();
                adapter.submitList(getFilterList(newText));
            }
        });

        fab = (FloatingActionButton) findViewById(R.id.x_image);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        handler = new Handler(Looper.getMainLooper());

    }

    @Override
    public void onNoteClick(int position) {
        Intent intent = new Intent(SearchActivity.this, NewNote.class);
        pressedNote = (Note) adapter.getCurrentList().get(position);
        long id= pressedNote.getNoteId();
        for (int i = 0; i<MainActivity.noteList.size(); i++){
            if(id==MainActivity.noteList.get(i).getNoteId()) {
                MainActivity.position = i;
                break;
            }
        }
        Log.e(TAG, "Position: "+String.valueOf(MainActivity.position) );
        intent.putExtra("note", pressedNote);
        intent.putExtra("position", position);
        launcher.launch(intent);
    }

    protected ArrayList<Note> getFilterList(String newText) {
        ArrayList<Note> filteredList = new ArrayList<Note>();
        if (!newText.isEmpty()) {
            String filterPattern = newText.trim().toLowerCase();
            for (Note item : MainActivity.noteList) {
                if (item.getTitle().toLowerCase().contains(filterPattern)) {
                    filteredList.add(item);
                }
            }
        }

        if(filteredList.size()==0) {
            recycler.setVisibility(View.GONE);
            emptyView.setVisibility(View.VISIBLE);
        }
        else {
            recycler.setVisibility(View.VISIBLE);
            emptyView.setVisibility(View.GONE);
        }
        return filteredList;
    }

    Runnable runnable=new Runnable() {
        @Override
        public void run() {
            adapter.submitList(getFilterList(searchView.getText().toString()));
        handler.postDelayed(runnable,500);
        }
    };

    @Override
    protected void onDestroy() {
        handler.removeCallbacks(runnable);
        super.onDestroy();
    }
}

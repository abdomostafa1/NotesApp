
package com.example.notes;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.TextView;

import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.io.File;
import java.util.ArrayList;

import static android.content.ContentValues.TAG;
import static com.example.notes.NoteProvider.providerConstPath;

public class MainActivity extends AppCompatActivity implements NoteAdapter.OnNoteClickListener , MyBottomSheetDialog.OnBottomSheetListener {

    public static CoordinatorLayout mainCoordinator;
    private MaterialToolbar toolbar;
    private BottomAppBar bottomAppBar;
    public static FloatingActionButton fab;
    public static RecyclerView recyclerView;
    public static ConstraintLayout empty_view;
    public static NoteAdapter adapter;
    public static ArrayList<Note> noteList;
    private ConstraintLayout bottom_sheet;
    public static LayoutAnimationController fastController;
    public static LayoutAnimationController slowController;
    ListSort listSort;
    public static DeleteData deleteData = null;
    private int RESULT_DELETE = 2;
    private int RESULT_ADD = 3;
    private int RESULT_UPDATE = 4;
    String manager = "RecyclerLayoutManager";
    static int counter = 0;
    public static int position;
    public static boolean ACTION_ADD = true;
    public static boolean ACTION_CHANGE = false;
    public static boolean ACTION_REMOVE = false;
    public static boolean selectionMode = false;
    private FloatingActionButton fabPlay;
    public static Note pressedNote = null;
    public static Context context;
    public static int orderBy = 1;  // newest notes first

    ActivityResultLauncher<Intent> launcher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    // delete note
                    if (result.getResultCode() == RESULT_DELETE && pressedNote != null) {
                        Snackbar.make(recyclerView, R.string.note_is_deleted, Snackbar.LENGTH_LONG).setAnchorView(fab);
                        Snackbar snackbar=Snackbar.make(recyclerView, R.string.note_is_deleted, Snackbar.LENGTH_LONG).setAnchorView(fab);
                        View view = snackbar.getView();
                        TextView textView = view.findViewById(com.google.android.material.R.id.snackbar_text);
                        snackbar.show();

                    }
                    checkEmptyList();
                }
            });
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //toolbar = findViewById(R.id.main_activity_toolbar);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().setStatusBarColor(getResources().getColor(R.color.white, this.getTheme()));
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(getResources().getColor(R.color.white));
        }
        bottomAppBar= findViewById(R.id.bottom_app_bar);
        setSupportActionBar(bottomAppBar);
        context = MainActivity.this;

        bottomAppBar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showBottomDialog();
            }
        });
        //create SharedPreferences file
        SharedPreferences preferences = getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        // create folders in phone storage to store Records and Photos
        File photosDir = getExternalFilesDir("/Photos");
        photosDir.getParentFile().mkdirs();
        File recordsDir = getExternalFilesDir("/Recordings");
        recordsDir.getParentFile().mkdirs();


        mainCoordinator = (CoordinatorLayout) findViewById(R.id.mainCoordinator);
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        empty_view=(ConstraintLayout)findViewById(R.id.empty_view);
        fab = (FloatingActionButton) findViewById(R.id.fab);

        noteList = new ArrayList<Note>();
        //    noteArrayListCopy= new ArrayList<Note>();

        adapter = new NoteAdapter(new noteListDiffCallback(), this,false);

        recyclerView.setAdapter(adapter);
        setRecyclerLayoutManager();

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(MainActivity.this, NewNote.class);
                intent.putExtra("NewNote", true);
    //            ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(this,mainCoordinator,"shared_element_container");
                pressedNote = null;
                launcher.launch(intent);
            }
        });


        showNotesList();
        listSort = new ListSort(noteList, adapter);
        executeSort();

        slowController = AnimationUtils.loadLayoutAnimation(this, R.anim.layout_animation_slow_fall_down);
        fastController = AnimationUtils.loadLayoutAnimation(this, R.anim.layout_animation_fast_fall_down);
        recyclerView.setLayoutAnimation(slowController);
        deleteData = new DeleteData(MainActivity.this);



    }

    @Override
        public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_activity, menu);

        SharedPreferences preferences = getPreferences(MODE_PRIVATE);
        MenuItem menuItem = menu.getItem(1);
        if (preferences.getInt("RecyclerLayoutManager", 1) == 1)
            menuItem.setIcon(R.drawable.ic__grid);
        else
            menuItem.setIcon(R.drawable.ic_linear_bars);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {


        if (item.getItemId() == R.id.recycler_manager) {

            SharedPreferences preferences = getPreferences(MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();
            if (preferences.getInt(manager, 1) == 1) {
                editor.putInt(manager, 2);
                item.setIcon(R.drawable.ic_linear_bars);
            } else {
                editor.putInt(manager, 1);
                item.setIcon(R.drawable.ic__grid);
            }

            editor.apply();
            setRecyclerLayoutManager();
        }
        if(item.getItemId()==R.id.search){

            Intent intent=new Intent(MainActivity.this,SearchActivity.class);
            startActivity(intent);
        }
        return true;
    }

    void showBottomDialog() {

        MyBottomSheetDialog bottomSheetDialog = new MyBottomSheetDialog();
        bottomSheetDialog.show(getSupportFragmentManager(), "Bottom Sheet Dialog");

    }

    void executeSort() {
        SharedPreferences preferences = getPreferences(MODE_PRIVATE);
        int order = preferences.getInt("SortAlgorithm", 1);
        if (order == 1)
            listSort.sortToNewest_order();

        else if (order == 2)
            listSort.sortToOldest_order();
        else if (order == 3)
            listSort.sortToAZ_order();

        else
            listSort.sortToZA_order();

        recyclerView.setLayoutAnimation(slowController);
        recyclerView.scheduleLayoutAnimation();

    }

//    void createSharedPreferencesFile() {
//        SharedPreferences preferences = getPreferences( Context.MODE_PRIVATE);
//        editor.putInt("SortAlgorithm", 1);
//        editor.putInt("RecyclerLayoutManager", 1);
//        editor.apply();
//   }

    void setRecyclerLayoutManager() {
        SharedPreferences preferences = getPreferences(MODE_PRIVATE);
        if (preferences.getInt(manager, 1) == 1) {

            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
            linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
            recyclerView.setLayoutManager(linearLayoutManager);


        } else {
            NoteAdapter.linearLayoutManager = false;
            NoteAdapter.gridLayoutManager = true;
            recyclerView.setLayoutManager(new StaggeredGridLayoutManager(2, 1));
        }
        fastController = AnimationUtils.loadLayoutAnimation(this, R.anim.layout_animation_fast_fall_down);
        recyclerView.setLayoutAnimation(fastController);

    }



    private void showNotesList() {

        String[] projection = {Table.COLUMN_ID, Table.COLUMN_TITLE, Table.COLUMN_Body};
        Uri uri = Uri.parse(providerConstPath);
        //   Uri uri=providerConstUri;
        Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
        Log.e(TAG, "showNotesList: " + cursor.getCount());

        while (cursor.moveToNext())
            noteList.add(createNewNote(cursor));

        checkEmptyList();

    }

    static void checkEmptyList(){
        if(noteList.size()==0){
            recyclerView.setVisibility(View.GONE);
            empty_view.setVisibility(View.VISIBLE);
        }
        else{
            recyclerView.setVisibility(View.VISIBLE);
            empty_view.setVisibility(View.GONE);
        }

    }
    @SuppressLint("Range")
    private Note createNewNote(Cursor cursor) {
        long id = cursor.getLong(cursor.getColumnIndex(Table.COLUMN_ID));
        String title = cursor.getString(cursor.getColumnIndex(Table.COLUMN_TITLE));
        String noteBody = cursor.getString(cursor.getColumnIndex(Table.COLUMN_Body));
        return new Note(id, title, noteBody);

    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onNoteClick(int position) {
        if (selectionMode)
            adapter.checkSelection(position);
        else {
            Intent intent = new Intent(MainActivity.this, NewNote.class);
            pressedNote = (Note) adapter.getCurrentList().get(position);
            Log.e(TAG, "MainActivityClick: note position  " + position);
            intent.putExtra("note", pressedNote);
            intent.putExtra("position", position);
            MainActivity.position = position;
            launcher.launch(intent);
        }
    }

    public static ActionMode.Callback callback = new ActionMode.Callback() {
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {


            mode.getMenuInflater().inflate(R.menu.selection_menu, menu);

            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            if (item.getItemId() == R.id.delete_items) {
                // delete selected notes
                for (Note note : NoteAdapter.selectionList) {

                    new DeleteData(context).execute(note);
                }

                for (Note note : NoteAdapter.selectionList) {
                    noteList.remove(note);
                }
                mode.finish();
                adapter.submitList(noteList
                );
                NoteAdapter.selectionList.clear();
                adapter.notifyDataSetChanged();
                Log.e(TAG, "selectionList size: "+String.valueOf(NoteAdapter.selectionList.size()) );
                Snackbar snackbar=Snackbar.make(MainActivity.mainCoordinator, R.string.note_are_deleted, Snackbar.LENGTH_LONG).setAnchorView(fab);
                View view = snackbar.getView();
                TextView textView = view.findViewById(com.google.android.material.R.id.snackbar_text);
                snackbar.show();
                checkEmptyList();

            }
            return false;
        }

        @RequiresApi(api = Build.VERSION_CODES.M)
        @Override
        public void onDestroyActionMode(ActionMode mode) {
            NoteAdapter.clearSelection();
            NoteAdapter.actionMode = null;
        }
    };

    @Override
    public void onItemClickListener(int order) {
        SharedPreferences preferences = getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        if (order == 1) {
            if (preferences.getInt("SortAlgorithm", 1) != 1) {
                editor.putInt("SortAlgorithm", 1);
                editor.apply();
                executeSort();
            }
        } else if (order == 2) {
            if (preferences.getInt("SortAlgorithm", 1) != 2) {
                editor.putInt("SortAlgorithm", 2);
                editor.apply();
                executeSort();
            }
        } else if (order == 3) {
            if (preferences.getInt("SortAlgorithm", 1) != 3) {
                editor.putInt("SortAlgorithm", 3);
                editor.apply();
                executeSort();
            }
        } else {
            if (preferences.getInt("SortAlgorithm", 1) != 4) {
                editor.putInt("SortAlgorithm", 4);
                editor.apply();
                executeSort();
            }
        }
    }
}
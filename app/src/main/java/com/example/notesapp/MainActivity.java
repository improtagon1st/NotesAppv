package com.example.notesapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    public static final int ADD_NOTE_REQUEST  = 1;
    public static final int EDIT_NOTE_REQUEST = 2;

    private MaterialToolbar toolbar;
    private NotesViewModel notesViewModel;
    private NotesAdapter adapter;
    private boolean showFavorites = false;
    private String currentQuery = "";
    private LiveData<List<Note>> notesLiveData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // RecyclerView + Adapter
        RecyclerView rv = findViewById(R.id.recycler_view);
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setHasFixedSize(true);
        adapter = new NotesAdapter();
        rv.setAdapter(adapter);

        // ViewModel
        notesViewModel = new ViewModelProvider(this).get(NotesViewModel.class);

        // Редактирование по клику
        adapter.setOnItemClickListener(note -> {
            Intent i = new Intent(this, EditNoteActivity.class);
            i.putExtra(EditNoteActivity.EXTRA_ID, note.getId());
            i.putExtra(EditNoteActivity.EXTRA_TITLE, note.getTitle());
            i.putExtra(EditNoteActivity.EXTRA_CONTENT, note.getContent());
            i.putExtra(EditNoteActivity.EXTRA_IS_FAVORITE, note.isFavorite());
            startActivityForResult(i, EDIT_NOTE_REQUEST);
        });

        // Метка «избранное» по клику
        adapter.setOnFavoriteClickListener(note -> {
            boolean newFav = !note.isFavorite();
            note.setFavorite(newFav);
            note.setLastUpdated(System.currentTimeMillis());
            notesViewModel.update(note);

            // после отметки сразу показываем эту вкладку
            showFavorites = newFav;
            currentQuery = "";
            loadNotes();
            invalidateOptionsMenu();
        });

        // Свайп-влево с подтверждением удаления
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            @Override public boolean onMove(@NonNull RecyclerView rv,
                                            @NonNull RecyclerView.ViewHolder vh,
                                            @NonNull RecyclerView.ViewHolder t) {
                return false;
            }
            @Override public void onSwiped(@NonNull RecyclerView.ViewHolder vh, int dir) {
                int pos = vh.getAdapterPosition();
                Note noteToDelete = adapter.getNoteAt(pos);
                adapter.notifyItemChanged(pos); // отменяем визуальный свайп

                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("Подтвердите удаление")
                        .setMessage("Вы уверены, что хотите удалить эту заметку?")
                        .setPositiveButton("Да", (dlg, which) -> {
                            notesViewModel.delete(noteToDelete);
                            Toast.makeText(
                                    MainActivity.this,            // <-- здесь контекст Activity
                                    "Заметка удалена",
                                    Toast.LENGTH_SHORT
                            ).show();
                            // После удаления остаёмся в том же режиме
                            loadNotes();
                            invalidateOptionsMenu();
                        })
                        .setNegativeButton("Отмена", (dlg, which) -> dlg.dismiss())
                        .show();
            }
        }).attachToRecyclerView(rv);

        // FAB — создание новой заметки
        FloatingActionButton fab = findViewById(R.id.button_add_note);
        fab.setOnClickListener(v -> {
            startActivityForResult(
                    new Intent(this, EditNoteActivity.class),
                    ADD_NOTE_REQUEST
            );
        });

        loadNotes();
    }

    private void loadNotes() {
        if (notesLiveData != null) {
            notesLiveData.removeObservers(this);
        }
        if (!currentQuery.isEmpty()) {
            notesLiveData = notesViewModel.searchNotes("%" + currentQuery + "%");
        } else if (showFavorites) {
            notesLiveData = notesViewModel.getFavoriteNotes();
        } else {
            notesLiveData = notesViewModel.getAllNotes();
        }
        notesLiveData.observe(this, notes -> adapter.setNotes(notes));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);

        // SearchView
        MenuItem si = menu.findItem(R.id.action_search);
        SearchView sv = (SearchView) si.getActionView();
        sv.setQueryHint("Поиск заметок…");
        sv.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override public boolean onQueryTextSubmit(String q) {
                currentQuery = q.trim();
                loadNotes();
                return false;
            }
            @Override public boolean onQueryTextChange(String t) {
                currentQuery = t.trim();
                loadNotes();
                return false;
            }
        });

        // Инициализируем иконку/заголовок вкладки «Избранные»
        updateFavoriteUi(menu.findItem(R.id.action_favorite));
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_favorite) {
            showFavorites = !showFavorites;
            currentQuery = "";
            loadNotes();
            updateFavoriteUi(item);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void updateFavoriteUi(MenuItem favItem) {
        if (showFavorites) {
            favItem.setIcon(R.drawable.ic_star);
            getSupportActionBar().setTitle("Избранные заметки");
        } else {
            favItem.setIcon(R.drawable.ic_star_border);
            getSupportActionBar().setTitle("Все заметки");
        }
    }

    @Override
    protected void onActivityResult(int req, int res, @Nullable Intent data) {
        super.onActivityResult(req, res, data);
        if (res != RESULT_OK || data == null) return;

        String title   = data.getStringExtra(EditNoteActivity.EXTRA_TITLE);
        String content = data.getStringExtra(EditNoteActivity.EXTRA_CONTENT);
        boolean isFav  = data.getBooleanExtra(EditNoteActivity.EXTRA_IS_FAVORITE, false);
        long ts        = System.currentTimeMillis();

        if (req == ADD_NOTE_REQUEST) {
            notesViewModel.insert(new Note(title, content, ts, isFav));
            Toast.makeText(this, "Заметка сохранена", Toast.LENGTH_SHORT).show();
            showFavorites = false; // после создания возвращаемся к «Все заметки»
        } else if (req == EDIT_NOTE_REQUEST) {
            int id = data.getIntExtra(EditNoteActivity.EXTRA_ID, -1);
            if (id != -1) {
                Note n = new Note(title, content, ts, isFav);
                n.setId(id);
                notesViewModel.update(n);
                Toast.makeText(this, "Заметка обновлена", Toast.LENGTH_SHORT).show();
                showFavorites = isFav; // если сделали избранным — показываем избранное
            } else {
                Toast.makeText(this, "Ошибка обновления", Toast.LENGTH_SHORT).show();
            }
        }

        currentQuery = "";
        loadNotes();
        invalidateOptionsMenu();
    }
}

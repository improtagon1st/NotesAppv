package com.example.notesapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
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
import java.util.function.Consumer;

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

        // 1) RecyclerView + Adapter
        RecyclerView rv = findViewById(R.id.recycler_view);
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setHasFixedSize(true);
        adapter = new NotesAdapter();
        rv.setAdapter(adapter);

        // 2) ViewModel
        notesViewModel = new ViewModelProvider(this).get(NotesViewModel.class);

        // 3) Клик по элементу — открываем редактор (с проверкой PIN)
        adapter.setOnItemClickListener(note -> {
            int id = note.getId();
            if (note.isLocked()) {
                showEnterPinDialog(entered -> {
                    if (entered.equals(PinManager.getPin(this, id))) {
                        openEditor(note);
                    } else {
                        Toast.makeText(this, "Неверный PIN", Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                openEditor(note);
            }
        });

        // 4) Клик по «избранному» — переключаем флаг, обновляем список
        adapter.setOnFavoriteClickListener(note -> {
            boolean newFav = !note.isFavorite();
            note.setFavorite(newFav);
            note.setLastUpdated(System.currentTimeMillis());
            notesViewModel.update(note);
            showFavorites = newFav;
            currentQuery = "";
            loadNotes();
            invalidateOptionsMenu();
        });

        // 5) Клик по «замочку» — per-note PIN
        adapter.setOnLockClickListener(note -> {
            int id = note.getId();
            if (!PinManager.hasPin(this, id)) {
                // ещё нет PIN для этой заметки — установим новый
                showSetPinDialog(pin -> {
                    PinManager.savePin(this, id, pin);
                    toggleLock(note);
                });
            } else {
                // PIN уже есть — запрашиваем ввод
                showEnterPinDialog(entered -> {
                    if (entered.equals(PinManager.getPin(this, id))) {
                        toggleLock(note);
                    } else {
                        Toast.makeText(this, "Неверный PIN", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        // 6) Свайп-влево — проверяем блокировку и подтверждаем удаление
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
                int id = noteToDelete.getId();

                if (noteToDelete.isLocked()) {
                    // запрос PIN перед удалением этой заметки
                    showEnterPinDialog(entered -> {
                        if (entered.equals(PinManager.getPin(MainActivity.this, id))) {
                            confirmDelete(noteToDelete);
                        } else {
                            Toast.makeText(
                                    MainActivity.this,
                                    "Неверный PIN, удаление отменено",
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    });
                } else {
                    confirmDelete(noteToDelete);
                }
            }
        }).attachToRecyclerView(rv);

        // 7) FAB — создание новой заметки
        FloatingActionButton fab = findViewById(R.id.button_add_note);
        fab.setOnClickListener(v -> startActivityForResult(
                new Intent(this, EditNoteActivity.class),
                ADD_NOTE_REQUEST
        ));

        // 8) Первичный показ списка
        loadNotes();
    }

    /** Загружает список заметок в зависимости от фильтра и поиска */
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

    /** Меню: поиск и переключение «Избранное» */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
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
        MenuItem fav = menu.findItem(R.id.action_favorite);
        updateFavoriteUi(fav);
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

    /** Обрабатывает результат из Add/Edit */
    @Override
    protected void onActivityResult(int req, int res, @Nullable Intent data) {
        super.onActivityResult(req, res, data);
        if (res != RESULT_OK || data == null) return;

        String title   = data.getStringExtra(EditNoteActivity.EXTRA_TITLE);
        String content = data.getStringExtra(EditNoteActivity.EXTRA_CONTENT);
        boolean isFav  = data.getBooleanExtra(EditNoteActivity.EXTRA_IS_FAVORITE, false);
        long ts        = System.currentTimeMillis();

        if (req == ADD_NOTE_REQUEST) {
            notesViewModel.insert(new Note(title, content, ts, isFav, false));
            Toast.makeText(this, "Заметка сохранена", Toast.LENGTH_SHORT).show();
            showFavorites = false;
        } else if (req == EDIT_NOTE_REQUEST) {
            int id = data.getIntExtra(EditNoteActivity.EXTRA_ID, -1);
            if (id == -1) {
                Toast.makeText(this, "Ошибка обновления", Toast.LENGTH_SHORT).show();
            } else {
                Note n = new Note(title, content, ts, isFav, false);
                n.setId(id);
                notesViewModel.update(n);
                Toast.makeText(this, "Заметка обновлена", Toast.LENGTH_SHORT).show();
                showFavorites = isFav;
            }
        }

        currentQuery = "";
        loadNotes();
        invalidateOptionsMenu();
    }

    /** Переключает флаг locked и сохраняет */
    private void toggleLock(Note note) {
        note.setLocked(!note.isLocked());
        notesViewModel.update(note);
        loadNotes();
        invalidateOptionsMenu();
    }

    /** Запускает EditNoteActivity */
    private void openEditor(Note note) {
        Intent i = new Intent(this, EditNoteActivity.class);
        i.putExtra(EditNoteActivity.EXTRA_ID, note.getId());
        i.putExtra(EditNoteActivity.EXTRA_TITLE, note.getTitle());
        i.putExtra(EditNoteActivity.EXTRA_CONTENT, note.getContent());
        i.putExtra(EditNoteActivity.EXTRA_IS_FAVORITE, note.isFavorite());
        startActivityForResult(i, EDIT_NOTE_REQUEST);
    }

    /** Запрос ввода PIN */
    private void showEnterPinDialog(Consumer<String> onEntered) {
        EditText pinInput = new EditText(this);
        pinInput.setInputType(
                InputType.TYPE_CLASS_NUMBER |
                        InputType.TYPE_NUMBER_VARIATION_PASSWORD
        );
        pinInput.setHint("••••");
        new AlertDialog.Builder(this)
                .setTitle("Введите PIN")
                .setView(pinInput)
                .setPositiveButton("OK", (d, w) ->
                        onEntered.accept(pinInput.getText().toString())
                )
                .setNegativeButton("Отмена", null)
                .show();
    }

    /** Диалог установки PIN */
    private void showSetPinDialog(Consumer<String> onPinSet) {
        View v = getLayoutInflater().inflate(R.layout.dialog_set_pin, null);
        EditText p1 = v.findViewById(R.id.pin1);
        EditText p2 = v.findViewById(R.id.pin2);
        new AlertDialog.Builder(this)
                .setTitle("Установите PIN")
                .setView(v)
                .setPositiveButton("OK", (d, w) -> {
                    String s1 = p1.getText().toString();
                    String s2 = p2.getText().toString();
                    if (s1.length() == 4 && s1.equals(s2)) {
                        onPinSet.accept(s1);
                    } else {
                        Toast.makeText(this,
                                "PIN должен состоять из 4 цифр и совпадать",
                                Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Отмена", null)
                .show();
    }

    /** Подтверждение удаления */
    private void confirmDelete(Note note) {
        new AlertDialog.Builder(this)
                .setTitle("Подтвердите удаление")
                .setMessage("Вы уверены, что хотите удалить эту заметку?")
                .setPositiveButton("Да", (dlg, which) -> {
                    notesViewModel.delete(note);
                    Toast.makeText(
                            MainActivity.this,
                            "Заметка удалена",
                            Toast.LENGTH_SHORT
                    ).show();
                    loadNotes();
                    invalidateOptionsMenu();
                })
                .setNegativeButton("Отмена", null)
                .show();
    }
}

package com.example.notesapp;

import android.app.Application;

import androidx.lifecycle.LiveData;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NotesRepository {
    private NoteDao noteDao;
    private LiveData<List<Note>> allNotes;
    private LiveData<List<Note>> favoriteNotes;

    // Пул потоков для операций с БД
    private static final int NUMBER_OF_THREADS = 4;
    static final ExecutorService databaseWriteExecutor =
            Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    // Конструктор: получаем DAO из нашей базы
    public NotesRepository(Application application) {
        NotesDatabase db = NotesDatabase.getInstance(application);
        noteDao = db.noteDao();
        allNotes = noteDao.getAllNotes();
        favoriteNotes = noteDao.getFavoriteNotes();
    }

    // Методы для ViewModel
    public LiveData<List<Note>> getAllNotes() {
        return allNotes;
    }

    public LiveData<List<Note>> getFavoriteNotes() {
        return favoriteNotes;
    }

    public LiveData<List<Note>> searchNotes(String query) {
        return noteDao.searchNotes(query);
    }

    public void insert(Note note) {
        databaseWriteExecutor.execute(() -> noteDao.insertNote(note));
    }

    public void update(Note note) {
        databaseWriteExecutor.execute(() -> noteDao.updateNote(note));
    }

    public void delete(Note note) {
        databaseWriteExecutor.execute(() -> noteDao.deleteNote(note));
    }
}

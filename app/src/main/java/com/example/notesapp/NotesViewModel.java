package com.example.notesapp;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;

public class NotesViewModel extends AndroidViewModel {
    private NotesRepository repository;
    private LiveData<List<Note>> allNotes;
    private LiveData<List<Note>> favoriteNotes;

    public NotesViewModel(@NonNull Application application) {
        super(application);
        repository = new NotesRepository(application);
        allNotes = repository.getAllNotes();
        favoriteNotes = repository.getFavoriteNotes();
    }

    // Методы для MainActivity
    public LiveData<List<Note>> getAllNotes() {
        return allNotes;
    }

    public LiveData<List<Note>> getFavoriteNotes() {
        return favoriteNotes;
    }

    public LiveData<List<Note>> searchNotes(String query) {
        return repository.searchNotes(query);
    }

    public void insert(Note note) {
        repository.insert(note);
    }

    public void update(Note note) {
        repository.update(note);
    }

    public void delete(Note note) {
        repository.delete(note);
    }
}

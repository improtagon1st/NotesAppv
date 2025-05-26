package com.example.notesapp;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface NoteDao {

    // Получить все заметки, отсортированные по дате изменения (свежие сверху)
    @Query("SELECT * FROM notes ORDER BY last_updated DESC")
    LiveData<List<Note>> getAllNotes();

    // Получить только «избранные»
    @Query("SELECT * FROM notes WHERE is_favorite = 1 ORDER BY last_updated DESC")
    LiveData<List<Note>> getFavoriteNotes();

    // Поиск по заголовку или содержимому
    // Передавайте параметр вида "%ключевое_слово%"
    @Query("SELECT * FROM notes WHERE title LIKE :query OR content LIKE :query ORDER BY last_updated DESC")
    LiveData<List<Note>> searchNotes(String query);

    // Вставить новую заметку
    @Insert
    void insertNote(Note note);

    // Обновить существующую
    @Update
    void updateNote(Note note);

    // Удалить заметку
    @Delete
    void deleteNote(Note note);
}

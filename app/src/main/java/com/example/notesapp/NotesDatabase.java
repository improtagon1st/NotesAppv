package com.example.notesapp;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

// Указываем все Entity и версию схемы
@Database(entities = {Note.class}, version = 1, exportSchema = false)
public abstract class NotesDatabase extends RoomDatabase {

    // DAO, через который будут запросы
    public abstract NoteDao noteDao();

    // Синглтон для доступа к БД
    private static volatile NotesDatabase INSTANCE;

    // Метод для получения экземпляра БД
    public static NotesDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (NotesDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    NotesDatabase.class,
                                    "notes_database"
                            )
                            // При изменении схемы удалять всё (для первых версий — безопасно)
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}

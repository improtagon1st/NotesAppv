package com.example.notesapp;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

@Database(entities = {Note.class}, version = 2, exportSchema = false)
public abstract class NotesDatabase extends RoomDatabase {
    private static volatile NotesDatabase instance;

    public abstract NoteDao noteDao();

    // Migration from v1 to v2: добавляем колонку is_locked
    private static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override public void migrate(@NonNull SupportSQLiteDatabase db) {
            db.execSQL("ALTER TABLE notes ADD COLUMN is_locked INTEGER NOT NULL DEFAULT 0");
        }
    };

    public static synchronized NotesDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(
                            context.getApplicationContext(),
                            NotesDatabase.class,
                            "notes_db"
                    )
                    .addMigrations(MIGRATION_1_2)
                    .build();
        }
        return instance;
    }
}

package com.example.notesapp;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "notes")
public class Note {

    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "title")
    private String title;

    @ColumnInfo(name = "content")
    private String content;

    @ColumnInfo(name = "last_updated")
    private long lastUpdated;

    @ColumnInfo(name = "is_favorite")
    private boolean isFavorite;

    // Конструктор (пустой нужен Room)
    public Note() { }

    // При необходимости можно добавить конструктор с полями (кроме id):
    public Note(String title, String content, long lastUpdated, boolean isFavorite) {
        this.title = title;
        this.content = content;
        this.lastUpdated = lastUpdated;
        this.isFavorite = isFavorite;
    }

    // Геттеры и сеттеры

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public long getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(long lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public boolean isFavorite() {
        return isFavorite;
    }

    public void setFavorite(boolean favorite) {
        isFavorite = favorite;
    }
}

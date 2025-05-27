package com.example.notesapp;

import android.content.Context;

public class PinManager {
    private static final String PREFS = "pin_prefs";
    private static final String KEY_PIN = "key_pin_";  // добавим суффикс для noteId

    /** Сохраняем PIN именно для этой заметки */
    public static void savePin(Context ctx, int noteId, String pin) {
        ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .edit()
                .putString(KEY_PIN + noteId, pin)
                .apply();
    }

    /** Получаем PIN для этой заметки, или null если не задан */
    public static String getPin(Context ctx, int noteId) {
        return ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .getString(KEY_PIN + noteId, null);
    }

    /** Проверяем, задан ли PIN для этой заметки */
    public static boolean hasPin(Context ctx, int noteId) {
        return getPin(ctx, noteId) != null;
    }
}

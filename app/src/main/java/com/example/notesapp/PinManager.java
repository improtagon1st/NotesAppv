package com.example.notesapp;

import android.content.Context;

public class PinManager {
    private static final String PREFS = "pin_prefs";
    private static final String KEY_PIN = "key_pin";

    public static void savePin(Context ctx, String pin) {
        ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .edit()
                .putString(KEY_PIN, pin)
                .apply();
    }

    public static String getPin(Context ctx) {
        return ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .getString(KEY_PIN, null);
    }

    public static boolean hasPin(Context ctx) {
        return getPin(ctx) != null;
    }
}

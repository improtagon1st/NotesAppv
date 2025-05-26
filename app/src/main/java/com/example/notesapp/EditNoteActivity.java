package com.example.notesapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class EditNoteActivity extends AppCompatActivity {

    public static final String EXTRA_ID = "com.example.notesapp.EXTRA_ID";
    public static final String EXTRA_TITLE = "com.example.notesapp.EXTRA_TITLE";
    public static final String EXTRA_CONTENT = "com.example.notesapp.EXTRA_CONTENT";
    public static final String EXTRA_IS_FAVORITE = "com.example.notesapp.EXTRA_IS_FAVORITE";

    private EditText editTextTitle;
    private EditText editTextContent;
    private Button buttonSave;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_note);

        editTextTitle   = findViewById(R.id.edit_text_title);
        editTextContent = findViewById(R.id.edit_text_content);
        buttonSave      = findViewById(R.id.button_save);

        // Если передан EXTRA_ID — мы редактируем, иначе создаём новую
        Intent intent = getIntent();
        if (intent.hasExtra(EXTRA_ID)) {
            setTitle("Редактировать заметку");
            editTextTitle.setText(intent.getStringExtra(EXTRA_TITLE));
            editTextContent.setText(intent.getStringExtra(EXTRA_CONTENT));
        } else {
            setTitle("Новая заметка");
        }

        buttonSave.setOnClickListener(v -> saveNote());
    }

    private void saveNote() {
        String title   = editTextTitle.getText().toString().trim();
        String content = editTextContent.getText().toString().trim();

        if (title.isEmpty() || content.isEmpty()) {
            Toast.makeText(this,
                    "Пожалуйста, введите заголовок и содержание",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        Intent data = new Intent();
        data.putExtra(EXTRA_TITLE, title);
        data.putExtra(EXTRA_CONTENT, content);
        // По умолчанию без «избранного»
        data.putExtra(EXTRA_IS_FAVORITE, false);

        if (getIntent().hasExtra(EXTRA_ID)) {
            data.putExtra(EXTRA_ID, getIntent().getIntExtra(EXTRA_ID, -1));
        }

        setResult(RESULT_OK, data);
        finish();
    }
}

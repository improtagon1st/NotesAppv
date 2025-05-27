package com.example.notesapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.tabs.TabLayout;

import io.noties.markwon.Markwon;
import io.noties.markwon.core.CorePlugin;
import io.noties.markwon.ext.strikethrough.StrikethroughPlugin;
import io.noties.markwon.ext.tasklist.TaskListPlugin;
import io.noties.markwon.linkify.LinkifyPlugin;

public class EditNoteActivity extends AppCompatActivity {

    public static final String EXTRA_ID          = "com.example.notesapp.EXTRA_ID";
    public static final String EXTRA_TITLE       = "com.example.notesapp.EXTRA_TITLE";
    public static final String EXTRA_CONTENT     = "com.example.notesapp.EXTRA_CONTENT";
    public static final String EXTRA_IS_FAVORITE = "com.example.notesapp.EXTRA_IS_FAVORITE";

    private EditText       editTextTitle;
    private EditText       editMarkdown;
    private LinearLayout   formatPanel;
    private TabLayout      tabs;
    private ScrollView     scrollPreview;
    private TextView       textPreview;
    private Button         buttonSave;
    private FrameLayout    editContainer;
    private Markwon        markwon;

    private int     noteId;
    private boolean noteFav;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_note);

        // 1) Инициализация Markwon
        markwon = Markwon.builder(this)
                .usePlugin(CorePlugin.create())
                .usePlugin(StrikethroughPlugin.create())
                .usePlugin(TaskListPlugin.create(this))
                .usePlugin(LinkifyPlugin.create())
                .build();

        // 2) Связываем View
        editTextTitle  = findViewById(R.id.edit_text_title);
        editMarkdown   = findViewById(R.id.edit_markdown);
        formatPanel    = findViewById(R.id.format_panel);
        tabs           = findViewById(R.id.tab_layout);
        scrollPreview  = findViewById(R.id.scroll_preview);
        textPreview    = findViewById(R.id.text_preview);
        buttonSave     = findViewById(R.id.button_save);
        editContainer  = findViewById(R.id.edit_container);

        // 3) Получаем данные из MainActivity
        Intent in = getIntent();
        noteId   = in.getIntExtra(EXTRA_ID, -1);
        String title   = in.getStringExtra(EXTRA_TITLE);
        String content = in.getStringExtra(EXTRA_CONTENT);
        noteFav  = in.getBooleanExtra(EXTRA_IS_FAVORITE, false);

        if (title != null) {
            editTextTitle.setText(title);
        }
        if (content != null) {
            editMarkdown.setText(content);
            markwon.setMarkdown(textPreview, content);
        }

        // 4) Настройка вкладок
        tabs.addTab(tabs.newTab().setText("Редактор"));
        tabs.addTab(tabs.newTab().setText("Превью"));

        // По умолчанию — Превью
        tabs.getTabAt(1).select();
        enterPreviewMode();

        tabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == 0) {
                    enterEditMode();
                } else {
                    enterPreviewMode();
                }
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) { }
            @Override public void onTabReselected(TabLayout.Tab tab) { }
        });

        // 5) Клик по любой области preview — переходим в редактор
        editContainer.setOnClickListener(v -> {
            if (tabs.getSelectedTabPosition() == 1) {
                tabs.selectTab(tabs.getTabAt(0));
            }
        });

        // 6) Кнопки форматирования
        findViewById(R.id.btn_heading).setOnClickListener(v ->
                insertMarkdownAtCursor(editMarkdown, "## ", "")
        );
        findViewById(R.id.btn_list).setOnClickListener(v ->
                insertMarkdownAtCursor(editMarkdown, "- ", "")
        );
        findViewById(R.id.btn_link).setOnClickListener(v ->
                insertMarkdownAtCursor(editMarkdown, "[текст](url)", "")
        );

        // 7) Сохранение
        buttonSave.setOnClickListener(v -> {
            String newTitle   = editTextTitle.getText().toString().trim();
            String newContent = editMarkdown.getText().toString().trim();
            if (newTitle.isEmpty() && newContent.isEmpty()) {
                Toast.makeText(this, "Нечего сохранять", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent data = new Intent();
            data.putExtra(EXTRA_ID,           noteId);
            data.putExtra(EXTRA_TITLE,        newTitle);
            data.putExtra(EXTRA_CONTENT,      newContent);
            data.putExtra(EXTRA_IS_FAVORITE,  noteFav);
            setResult(RESULT_OK, data);
            finish();
        });
    }

    /** Переключаемся в режим редактирования **/
    private void enterEditMode() {
        editTextTitle.setEnabled(true);
        editMarkdown.setVisibility(View.VISIBLE);
        scrollPreview.setVisibility(View.GONE);
        formatPanel.setVisibility(View.VISIBLE);
    }

    /** Переключаемся в режим превью **/
    private void enterPreviewMode() {
        editTextTitle.setEnabled(false);
        markwon.setMarkdown(textPreview, editMarkdown.getText().toString());
        editMarkdown.setVisibility(View.GONE);
        scrollPreview.setVisibility(View.VISIBLE);
        formatPanel.setVisibility(View.GONE);
    }

    /**
     * Вставляет в EditText перед текстом before и after,
     * сохраняя выделение
     */
    private void insertMarkdownAtCursor(EditText et, String before, String after) {
        int start = et.getSelectionStart();
        int end   = et.getSelectionEnd();
        String text = et.getText().toString();

        String updated = text.substring(0, start)
                + before
                + text.substring(start, end)
                + after
                + text.substring(end);

        et.setText(updated);
        int selStart = start + before.length();
        int selEnd   = selStart + (end - start);
        et.setSelection(selStart, selEnd);
    }
}

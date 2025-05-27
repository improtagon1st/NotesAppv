package com.example.notesapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.tabs.TabLayout;

import io.noties.markwon.Markwon;

public class EditNoteActivity extends AppCompatActivity {

    /* --- ключи для Intent --- */
    public static final String EXTRA_ID          = "com.example.notesapp.EXTRA_ID";
    public static final String EXTRA_TITLE       = "com.example.notesapp.EXTRA_TITLE";
    public static final String EXTRA_CONTENT     = "com.example.notesapp.EXTRA_CONTENT";
    public static final String EXTRA_IS_FAVORITE = "com.example.notesapp.EXTRA_IS_FAVORITE";
    public static final String EXTRA_IS_LOCKED   = "com.example.notesapp.EXTRA_IS_LOCKED";

    /* --- View-элементы --- */
    private EditText   editTextTitle;   // id/edit_text_title
    private EditText   editMarkdown;    // id/edit_markdown
    private TextView   textPreview;     // id/text_preview
    private ScrollView scrollPreview;   // id/scroll_preview
    private TabLayout  tabLayout;       // id/tab_layout

    /* --- служебные флаги --- */
    private boolean isFavorite;
    private boolean isLocked;

    /* --- markdown движок --- */
    private Markwon markwon;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_note);

        // ——— 1) Нахождение View ——————————————————————————————————————————
        editTextTitle  = findViewById(R.id.edit_text_title);
        editMarkdown   = findViewById(R.id.edit_markdown);
        textPreview    = findViewById(R.id.text_preview);
        scrollPreview  = findViewById(R.id.scroll_preview);
        tabLayout      = findViewById(R.id.tab_layout);
        Button buttonSave = findViewById(R.id.button_save);

        // ——— 2) Добавляем сами вкладки! ————————————————————————————————
        tabLayout.addTab(tabLayout.newTab().setText("Редактор"));
        tabLayout.addTab(tabLayout.newTab().setText("Превью"));

        markwon = Markwon.create(this);

        // 2. Получаем данные из Intent (если редактирование)
        Intent intent = getIntent();
        if (intent.hasExtra(EXTRA_ID)) {
            editTextTitle.setText(intent.getStringExtra(EXTRA_TITLE));
            String md = intent.getStringExtra(EXTRA_CONTENT);
            editMarkdown.setText(md);
            renderMarkdown(md);

            isFavorite = intent.getBooleanExtra(EXTRA_IS_FAVORITE, false);
            isLocked   = intent.getBooleanExtra(EXTRA_IS_LOCKED,   false);
        } else {
            isFavorite = false;
            isLocked   = false;
        }

        // 3. Если заметка заблокирована — редактор скрыт, TextView доступен
        applyLockStateInitially();

        // 4. Переключение вкладок
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == 0) {                 // «Редактор»
                    if (isLocked) {          // заблокирована? показываем toast и уходим на превью
                        Toast.makeText(EditNoteActivity.this,
                                "Заметка заблокирована", Toast.LENGTH_SHORT).show();
                        tabLayout.getTabAt(1).select();
                    } else {
                        editMarkdown.setVisibility(View.VISIBLE);
                        scrollPreview.setVisibility(View.GONE);
                    }
                } else {                                      // «Превью»
                    renderMarkdown(editMarkdown.getText().toString());
                    editMarkdown.setVisibility(View.GONE);
                    scrollPreview.setVisibility(View.VISIBLE);
                }
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) { }
            @Override public void onTabReselected(TabLayout.Tab tab) { }
        });
        // по умолчанию открываем Редактор/Превью в зависимости от lock
        tabLayout.getTabAt(isLocked ? 1 : 0).select();

        // 5. Кнопка «Сохранить»
        buttonSave.setOnClickListener(v -> saveAndFinish(intent));
        tabLayout.getTabAt(isLocked ? 1 : 0).select();
    }

    /** Первичное отображение/скрытие для заблокированной заметки */
    private void applyLockStateInitially() {
        if (isLocked) {
            editMarkdown.setVisibility(View.GONE);
            scrollPreview.setVisibility(View.VISIBLE);
            editTextTitle.setEnabled(false);
        } else {
            scrollPreview.setVisibility(View.GONE);
            editMarkdown.setVisibility(View.VISIBLE);
            editTextTitle.setEnabled(true);
        }
        // убираем курсор и клавиатуру у EditText в режиме превью
        if (isLocked) {
            editMarkdown.setInputType(InputType.TYPE_NULL);
        }
    }

    /** Рендеринг markdown в превью */
    private void renderMarkdown(String md) {
        markwon.setMarkdown(textPreview, md == null ? "" : md);
    }

    /** Собираем результат и возвращаем в MainActivity */
    private void saveAndFinish(Intent startIntent) {
        String title   = editTextTitle.getText().toString().trim();
        String content = editMarkdown.getText().toString().trim();

        if (title.isEmpty() && content.isEmpty()) {
            Toast.makeText(this,
                    "Введите текст или заголовок", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent data = new Intent();
        data.putExtra(EXTRA_TITLE,       title);
        data.putExtra(EXTRA_CONTENT,     content);
        data.putExtra(EXTRA_IS_FAVORITE, isFavorite);
        data.putExtra(EXTRA_IS_LOCKED,   isLocked);

        if (startIntent.hasExtra(EXTRA_ID)) {
            data.putExtra(EXTRA_ID, startIntent.getIntExtra(EXTRA_ID, -1));
        }
        setResult(RESULT_OK, data);
        finish();
    }
}

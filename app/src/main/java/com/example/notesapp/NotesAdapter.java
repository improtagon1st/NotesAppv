package com.example.notesapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class NotesAdapter extends RecyclerView.Adapter<NotesAdapter.NoteHolder> {

    private List<Note> notes = new ArrayList<>();
    private OnItemClickListener clickListener;
    private OnFavoriteClickListener favListener;

    @NonNull
    @Override
    public NoteHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_note, parent, false);
        return new NoteHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull NoteHolder holder, int position) {
        Note current = notes.get(position);
        holder.textViewTitle.setText(current.getTitle());

        // Форматирование и отображение даты
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault());
        holder.textViewDate.setText(sdf.format(new Date(current.getLastUpdated())));

        // Всегда показываем иконку; меняем ресурс в зависимости от состояния isFavorite
        if (current.isFavorite()) {
            holder.imageViewFavorite.setImageResource(R.drawable.ic_star);
        } else {
            holder.imageViewFavorite.setImageResource(R.drawable.ic_star_border);
        }

        // Клик по элементу (редактирование)
        holder.itemView.setOnClickListener(v -> {
            int pos = holder.getAdapterPosition();
            if (clickListener != null && pos != RecyclerView.NO_POSITION) {
                clickListener.onItemClick(current);
            }
        });

        // Клик по звёздочке — переключаем избранное
        holder.imageViewFavorite.setOnClickListener(v -> {
            int pos = holder.getAdapterPosition();
            if (favListener != null && pos != RecyclerView.NO_POSITION) {
                favListener.onFavoriteClick(current);
            }
        });
    }

    @Override
    public int getItemCount() {
        return notes.size();
    }

    /** Обновляет список и уведомляет адаптер */
    public void setNotes(List<Note> notes) {
        this.notes = notes;
        notifyDataSetChanged();
    }

    /** Возвращает заметку по позиции (для swipe и кликов) */
    public Note getNoteAt(int position) {
        return notes.get(position);
    }

    // Интерфейс клика по элементу
    public interface OnItemClickListener {
        void onItemClick(Note note);
    }
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.clickListener = listener;
    }

    // Интерфейс клика по иконке «избранное»
    public interface OnFavoriteClickListener {
        void onFavoriteClick(Note note);
    }
    public void setOnFavoriteClickListener(OnFavoriteClickListener listener) {
        this.favListener = listener;
    }

    class NoteHolder extends RecyclerView.ViewHolder {
        private final TextView textViewTitle;
        private final TextView textViewDate;
        private final ImageView imageViewFavorite;

        public NoteHolder(@NonNull View itemView) {
            super(itemView);
            textViewTitle     = itemView.findViewById(R.id.text_view_title);
            textViewDate      = itemView.findViewById(R.id.text_view_date);
            imageViewFavorite = itemView.findViewById(R.id.image_view_favorite);
        }
    }
}

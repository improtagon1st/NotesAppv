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

    // Слушатели кликов
    private OnItemClickListener clickListener;
    private OnFavoriteClickListener favListener;
    private OnLockClickListener lockListener;

    @NonNull
    @Override
    public NoteHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_note, parent, false);
        return new NoteHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull NoteHolder holder, int position) {
        Note note = notes.get(position);

        // Заголовок
        holder.textViewTitle.setText(note.getTitle());

        // Дата последнего обновления
        String formattedDate = new SimpleDateFormat(
                "dd.MM.yyyy HH:mm", Locale.getDefault())
                .format(new Date(note.getLastUpdated()));
        holder.textViewDate.setText(formattedDate);

        // Иконка «избранное»
        int favRes = note.isFavorite()
                ? R.drawable.ic_star    // закрашенная звезда
                : R.drawable.ic_star_border; // контур звезды
        holder.imageFavorite.setImageResource(favRes);

        // Иконка «замок»
        int lockRes = note.isLocked()
                ? R.drawable.ic_lock     // закрытый замок
                : R.drawable.ic_lock_open; // открытый замок
        holder.imageLock.setImageResource(lockRes);

        // Клик по карточке — открытие/редактирование
        holder.itemView.setOnClickListener(v -> {
            if (clickListener != null &&
                    holder.getAdapterPosition() != RecyclerView.NO_POSITION) {
                clickListener.onItemClick(note);
            }
        });

        // Клик по «звёздочке» — переключение избранного
        holder.imageFavorite.setOnClickListener(v -> {
            if (favListener != null &&
                    holder.getAdapterPosition() != RecyclerView.NO_POSITION) {
                favListener.onFavoriteClick(note);
            }
        });

        // Клик по замочку — блокировка/разблокировка
        holder.imageLock.setOnClickListener(v -> {
            if (lockListener != null &&
                    holder.getAdapterPosition() != RecyclerView.NO_POSITION) {
                lockListener.onLockClick(note);
            }
        });
    }

    @Override
    public int getItemCount() {
        return notes.size();
    }

    /** Обновить список заметок */
    public void setNotes(List<Note> notes) {
        this.notes = notes;
        notifyDataSetChanged();
    }

    /** Получить заметку по позиции (для swipe и т.д.) */
    public Note getNoteAt(int position) {
        return notes.get(position);
    }

    // Интерфейсы слушателей

    public interface OnItemClickListener {
        void onItemClick(Note note);
    }
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.clickListener = listener;
    }

    public interface OnFavoriteClickListener {
        void onFavoriteClick(Note note);
    }
    public void setOnFavoriteClickListener(OnFavoriteClickListener listener) {
        this.favListener = listener;
    }

    public interface OnLockClickListener {
        void onLockClick(Note note);
    }
    public void setOnLockClickListener(OnLockClickListener listener) {
        this.lockListener = listener;
    }

    /** ViewHolder для одной карточки заметки */
    static class NoteHolder extends RecyclerView.ViewHolder {
        TextView textViewTitle;
        TextView textViewDate;
        ImageView imageFavorite;
        ImageView imageLock;

        public NoteHolder(@NonNull View itemView) {
            super(itemView);
            textViewTitle    = itemView.findViewById(R.id.text_view_title);
            textViewDate     = itemView.findViewById(R.id.text_view_date);
            imageFavorite    = itemView.findViewById(R.id.image_favorite);
            imageLock        = itemView.findViewById(R.id.image_lock);
        }
    }
}

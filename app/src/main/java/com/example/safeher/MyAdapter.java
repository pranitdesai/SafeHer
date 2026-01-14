package com.example.safeher;

import android.content.Context;
import android.media.MediaPlayer;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.io.IOException;
import java.util.ArrayList;

public class MyAdapter extends RecyclerView.Adapter<MyViewHolder> {

    private final Context context;
    private final ArrayList<String> items;
    private final ArrayList<String> paths;
    private MediaPlayer mediaPlayer;
    private int currentlyPlayingPosition = -1;

    public MyAdapter(Context context, ArrayList<String> items, ArrayList<String> paths) {
        this.context = context;
        this.items = items;
        this.paths = paths;
        this.mediaPlayer = new MediaPlayer();
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.item_view, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        holder.recName.setText(items.get(position));

        holder.playButton.setIconResource(position == currentlyPlayingPosition ?
                R.drawable.pausebutton : R.drawable.playbutton);

        holder.playButton.setOnClickListener(view -> handleAudioPlayback(position));
    }

    private void handleAudioPlayback(int position) {
        if (currentlyPlayingPosition == position) {
            stopAudio();
            currentlyPlayingPosition = -1;
        } else {
            stopAudio();
            playAudio(position);
        }
        notifyDataSetChanged();
    }

    private void playAudio(int position) {
        try {
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(paths.get(position));
            mediaPlayer.setOnPreparedListener(mp -> {
                mediaPlayer.start();
                currentlyPlayingPosition = position;
                notifyDataSetChanged();
            });

            mediaPlayer.setOnCompletionListener(mp -> {
                stopAudio();
                currentlyPlayingPosition = -1;
                notifyDataSetChanged();
            });

            mediaPlayer.prepareAsync();
        } catch (IOException e) {
            Log.e("TAG", "Error playing audio: " + e.getMessage());
            Toast.makeText(context, "Error playing audio", Toast.LENGTH_SHORT).show();
        }
    }

    private void stopAudio() {
        if (mediaPlayer != null) {
            mediaPlayer.reset();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }
}

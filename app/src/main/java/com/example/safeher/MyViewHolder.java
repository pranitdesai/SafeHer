package com.example.safeher;

import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.material.button.MaterialButton;

public class MyViewHolder extends RecyclerView.ViewHolder {

    MaterialButton playButton;
    TextView recName;

    public MyViewHolder(@NonNull View itemView) {
        super(itemView);
        playButton = itemView.findViewById(R.id.playButton);
        recName = itemView.findViewById(R.id.recName);
    }
}

package com.flyjingfish.hollowtextview;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.flyjingfish.hollowtextview.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.setBackground.setOnClickListener(v -> {
            binding.hollowTextView.setBackground(getResources().getDrawable(R.drawable.bg_hollow2));
        });
        binding.setBackgroundColor.setOnClickListener(v -> {
            binding.hollowTextView.setBackgroundColor(getResources().getColor(R.color.purple_200));
        });
        binding.setBackgroundDrawable.setOnClickListener(v -> {
            binding.hollowTextView.setBackgroundDrawable(getResources().getDrawable(R.drawable.bg_hollow4));
        });
        binding.setBackgroundResource.setOnClickListener(v -> {
            binding.hollowTextView.setBackgroundResource(R.drawable.bg_hollow3);
        });
    }
}
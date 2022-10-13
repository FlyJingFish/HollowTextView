package com.flyjingfish.hollowtextview;

import androidx.appcompat.app.AppCompatActivity;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.view.WindowManager;

import com.flyjingfish.hollowtextview.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {
    private boolean isRunAnim;
    private ObjectAnimator runAnim;

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
        binding.runAnim.setOnClickListener(v -> {
            if (!isRunAnim){
                binding.runAnim.setText("停止运动");

                if (runAnim == null){
                    float height = binding.hollowTextView.getHeight();
                    float screenHeight = getScreenHeight();
                    runAnim = ObjectAnimator.ofFloat(binding.llHollowTextView,"translationY",0,screenHeight/2 - height,0);
                    runAnim.setRepeatCount(ValueAnimator.INFINITE);
                    runAnim.setRepeatMode(ValueAnimator.RESTART);
                    runAnim.setDuration(6000);
                }
                if (!runAnim.isStarted()){
                    runAnim.start();
                }else {
                    runAnim.resume();
                }
            }else {
                runAnim.pause();
                binding.runAnim.setText("开始运动");
            }
            isRunAnim = !isRunAnim;
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (runAnim != null){
            runAnim.cancel();
        }
    }

    public int getScreenHeight() {
        WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        Point point = new Point();
        wm.getDefaultDisplay().getRealSize(point);
        return point.y;
    }
}
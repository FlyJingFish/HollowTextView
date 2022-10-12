package com.flyjingfish.hollowtextviewlib;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.text.TextPaint;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;

public class HollowTextView extends AppCompatTextView {

    private Drawable bgDrawable;

    public HollowTextView(@NonNull Context context) {
        this(context, null);
    }

    public HollowTextView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public HollowTextView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        bgDrawable = getBackground();
        setBackground(null);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        TextPaint textPaint = getPaint();
        textPaint.setColor(Color.BLACK);
        textPaint.setXfermode(null);
        canvas.saveLayer(new RectF(0, 0, canvas.getWidth(), canvas.getHeight()), textPaint, Canvas.ALL_SAVE_FLAG);
        drawBackground(canvas);
        textPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_OUT));
        super.onDraw(canvas);
    }

    @Override
    public void setBackground(Drawable background) {
        super.setBackground(background);
        bgDrawable = getBackground();
        super.setBackground(null);
    }

    @Override
    public void setBackgroundDrawable(@Nullable Drawable background) {
        super.setBackgroundDrawable(background);
    }

    @Override
    public void setBackgroundColor(int color) {
        super.setBackgroundColor(color);
    }

    @Override
    public void setBackgroundResource(int resId) {
        super.setBackgroundResource(resId);
    }


    private void drawBackground(Canvas canvas) {
        final Drawable background = bgDrawable;
        if (background == null) {
            return;
        }

        background.setBounds(0, 0, getRight() - getLeft(), getBottom() - getTop());

        background.draw(canvas);
    }

}

package com.flyjingfish.hollowtextviewlib;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.LayoutDirection;
import android.util.Log;
import android.view.Gravity;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.text.TextUtilsCompat;

import java.util.Locale;

public class HollowTextView extends AppCompatTextView {

    private Drawable bgDrawable;
    private final AppCompatTextView backGroundText;
    private int strokeWidth;
    private int[] gradientStrokeColors;
    private float[] gradientStrokePositions;
    private boolean gradientStrokeColor;
    private float strokeAngle;
    private boolean strokeRtlAngle;
    private boolean isRtl;
    private int strokeTextColor;

    public HollowTextView(@NonNull Context context) {
        this(context, null);
    }

    public HollowTextView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public HollowTextView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
            isRtl = TextUtilsCompat.getLayoutDirectionFromLocale(Locale.getDefault()) == LayoutDirection.RTL;
        }
        backGroundText = new AppCompatTextView(context, attrs, defStyleAttr);

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.HollowTextView);
        strokeWidth = typedArray.getDimensionPixelSize(R.styleable.HollowTextView_hollow_stroke_strokeWidth, 0);
        int startStrokeColor = typedArray.getColor(R.styleable.HollowTextView_hollow_stroke_startColor, 0);
        int centerStrokeColor = typedArray.getColor(R.styleable.HollowTextView_hollow_stroke_centerColor, 0);
        int endStrokeColor = typedArray.getColor(R.styleable.HollowTextView_hollow_stroke_endColor, 0);
        strokeTextColor = typedArray.getColor(R.styleable.HollowTextView_hollow_stroke_textColor, getCurrentTextColor());
        strokeAngle = typedArray.getFloat(R.styleable.HollowTextView_hollow_stroke_angle, 0);
        strokeRtlAngle = typedArray.getBoolean(R.styleable.HollowTextView_hollow_stroke_rtl_angle, false);


        typedArray.recycle();

        if (startStrokeColor != 0 || centerStrokeColor != 0 || endStrokeColor != 0){
            if (centerStrokeColor != 0) {
                gradientStrokeColors = new int[]{startStrokeColor, centerStrokeColor, endStrokeColor};
            } else {
                gradientStrokeColors = new int[]{startStrokeColor, endStrokeColor};
            }
            gradientStrokeColor = true;
        }else {
            gradientStrokeColor = false;
        }
        
        TextPaint textPaint = backGroundText.getPaint();
        textPaint.setStrokeWidth(strokeWidth);
        textPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        backGroundText.setTextColor(strokeTextColor);
        backGroundText.setText(getText());
        backGroundText.setGravity(getGravity());
        backGroundText.setBackground(null);

        initCompoundDrawables();

        backGroundText.setCompoundDrawablePadding(getCompoundDrawablePadding());
    }



    @Override
    public void setLayoutParams(ViewGroup.LayoutParams params) {
        backGroundText.setLayoutParams(params);
        super.setLayoutParams(params);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        CharSequence tt = backGroundText.getText();
        if (tt == null || !tt.equals(this.getText())) {
            backGroundText.setText(getText());
        }
        backGroundText.measure(widthMeasureSpec, heightMeasureSpec);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        backGroundText.layout(left, top, right, bottom);
        super.onLayout(changed, left, top, right, bottom);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        TextPaint textPaint = getPaint();
        textPaint.setColor(Color.BLACK);
        textPaint.setXfermode(null);
        canvas.saveLayer(new RectF(0, 0, canvas.getWidth(), canvas.getHeight()), textPaint, Canvas.ALL_SAVE_FLAG);
        drawBackground(canvas);
        TextPaint backGroundTextPaint = backGroundText.getPaint();
        if (gradientStrokeColor){
            float currentAngle = strokeAngle;
            if (strokeRtlAngle && isRtl){
                currentAngle = - strokeAngle;
            }
            float[] xy = getAngleXY(currentAngle);

            @SuppressLint("DrawAllocation") LinearGradient linearGradient = new LinearGradient(xy[0], xy[1], xy[2], xy[3],  gradientStrokeColors, gradientStrokePositions, Shader.TileMode.CLAMP);
            backGroundTextPaint.setShader(linearGradient);
        }else {
            backGroundTextPaint.setShader(null);
        }
        backGroundText.draw(canvas);
        textPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_OUT));
        super.onDraw(canvas);
    }

    @Override
    public void setBackground(Drawable background) {
        bgDrawable = background;
        invalidate();
    }

    @Override
    public void setBackgroundDrawable(@Nullable Drawable background) {
        setBackground(background);
    }

    @Override
    public void setBackgroundResource(int resId) {
        setBackground(getContext().getResources().getDrawable(resId));
    }

    private void drawBackground(Canvas canvas) {
        final Drawable background = bgDrawable;
        if (background == null) {
            return;
        }

        background.setBounds(0, 0, getRight() - getLeft(), getBottom() - getTop());

        background.draw(canvas);
    }

    protected float[] getAngleXY(float currentAngle){
        int[] paddings = getCompoundDrawablesPaddings();
        int height = getHeight() - paddings[3] - paddings[1];
        int width = getWidth() - paddings[2] - paddings[0];

        float angle = currentAngle % 360;
        if (angle < 0) {
            angle = 360 + angle;
        }
        float x0, y0, x1, y1;
        if (angle >= 0 && angle <= 45) {
            float percent = angle / 45;
            x0 = width / 2f + width / 2f * percent;
            y0 = 0;
        } else if (angle <= 90) {
            float percent = (angle - 45) / 45;
            x0 = width;
            y0 = height / 2f * percent;
        } else if (angle <= 135) {
            float percent = (angle - 90) / 45;
            x0 = width;
            y0 = height / 2f * percent + height / 2f;
        } else if (angle <= 180) {
            float percent = (angle - 135) / 45;
            x0 = width / 2f + width / 2f * (1-percent);
            y0 = height;
        } else if (angle <= 225) {
            float percent = (angle - 180) / 45;
            x0 = width / 2f - width / 2f * percent;
            y0 = height;
        } else if (angle <= 270) {
            float percent = (angle - 225) / 45;
            x0 = 0;
            y0 = height - height / 2f * percent;
        } else if (angle <= 315) {
            float percent = (angle - 270) / 45;
            x0 = 0;
            y0 = height / 2f - height / 2f * percent;
        } else {
            float percent = (angle - 315) / 45;
            x0 = width / 2f * percent;
            y0 = 0;
        }

        x1 = width - x0;
        y1 = height - y0;

        return new float[]{x0, y0, x1, y1};
    }

    public int getStrokeWidth() {
        return strokeWidth;
    }

    public void setStrokeWidth(int strokeWidth) {
        this.strokeWidth = strokeWidth;
        TextPaint textPaint = backGroundText.getPaint();
        textPaint.setStrokeWidth(strokeWidth);
        invalidate();
    }

    public int[] getGradientStrokeColors() {
        return gradientStrokeColors;
    }

    public void setGradientStrokeColors(int[] gradientStrokeColors) {
        this.gradientStrokeColors = gradientStrokeColors;
        gradientStrokeColor = gradientStrokeColors != null;
        if (gradientStrokePositions != null && gradientStrokeColors != null && gradientStrokeColors.length != gradientStrokePositions.length){
            this.gradientStrokePositions = null;
        }
        invalidate();
    }

    public float[] getGradientStrokePositions() {
        return gradientStrokePositions;
    }

    public void setGradientStrokePositions(float[] gradientStrokePositions) {
        this.gradientStrokePositions = gradientStrokePositions;

        invalidate();
    }

    public float getStrokeAngle() {
        return strokeAngle;
    }

    public void setStrokeAngle(float strokeAngle) {
        this.strokeAngle = strokeAngle;
        invalidate();
    }

    public boolean isStrokeRtlAngle() {
        return strokeRtlAngle;
    }

    public void setStrokeRtlAngle(boolean strokeRtlAngle) {
        this.strokeRtlAngle = strokeRtlAngle;
        invalidate();
    }

    public int getStrokeTextColor() {
        return strokeTextColor;
    }

    public void setStrokeTextColor(int strokeTextColor) {
        this.strokeTextColor = strokeTextColor;
        backGroundText.setTextColor(strokeTextColor);
        gradientStrokeColor = false;
        invalidate();
    }

    @Override
    public void setCompoundDrawables(@Nullable Drawable left, @Nullable Drawable top, @Nullable Drawable right, @Nullable Drawable bottom) {
        super.setCompoundDrawables(left, top, right, bottom);
        initCompoundDrawables();
    }

    @Override
    public void setCompoundDrawablesRelative(@Nullable Drawable start, @Nullable Drawable top, @Nullable Drawable end, @Nullable Drawable bottom) {
        super.setCompoundDrawablesRelative(start, top, end, bottom);
        initCompoundDrawables();
    }

    @Override
    public void setCompoundDrawablePadding(int pad) {
        super.setCompoundDrawablePadding(pad);
        if (backGroundText != null){
            backGroundText.setCompoundDrawablePadding(pad);
        }
    }

    private void initCompoundDrawables(){
        if (backGroundText == null){
            return;
        }
        Drawable[] drawablesRelative = getCompoundDrawablesRelative();

        Drawable[] drawables = getCompoundDrawables();

        Drawable drawableLeft;
        Drawable drawableRight;
        Drawable drawableTop = null;
        Drawable drawableBottom = null;
        if (isRtl){
            if (drawablesRelative[0] != null || drawablesRelative[2] != null){
                drawableLeft = drawablesRelative[2];
                drawableRight = drawablesRelative[0];
            }else {
                drawableLeft = drawables[0];
                drawableRight = drawables[2];
            }

        }else {
            if (drawablesRelative[0] != null || drawablesRelative[2] != null){
                drawableLeft = drawablesRelative[0];
                drawableRight = drawablesRelative[2];
            }else {
                drawableLeft = drawables[0];
                drawableRight = drawables[2];
            }

        }

        if (drawablesRelative[1] != null){
            drawableTop = drawablesRelative[1];
        }else if (drawables[1] != null){
            drawableTop = drawables[1];
        }

        if (drawablesRelative[3] != null){
            drawableBottom = drawablesRelative[3];
        }else if (drawables[3] != null){
            drawableBottom = drawables[3];
        }

        backGroundText.setCompoundDrawables(drawableLeft,drawableTop,drawableRight,drawableBottom);
    }

    private int[] getCompoundDrawablesPaddings(){
        Drawable[] drawablesRelative = getCompoundDrawablesRelative();

        Drawable[] drawables = getCompoundDrawables();

        Drawable drawableLeft;
        Drawable drawableRight;
        Drawable drawableTop = null;
        Drawable drawableBottom = null;
        if (isRtl){
            if (drawablesRelative[0] != null || drawablesRelative[2] != null){
                drawableLeft = drawablesRelative[2];
                drawableRight = drawablesRelative[0];
            }else {
                drawableLeft = drawables[0];
                drawableRight = drawables[2];
            }

        }else {
            if (drawablesRelative[0] != null || drawablesRelative[2] != null){
                drawableLeft = drawablesRelative[0];
                drawableRight = drawablesRelative[2];
            }else {
                drawableLeft = drawables[0];
                drawableRight = drawables[2];
            }

        }

        if (drawablesRelative[1] != null){
            drawableTop = drawablesRelative[1];
        }else if (drawables[1] != null){
            drawableTop = drawables[1];
        }

        if (drawablesRelative[3] != null){
            drawableBottom = drawablesRelative[3];
        }else if (drawables[3] != null){
            drawableBottom = drawables[3];
        }

        int[] paddings = new int[4];
        paddings[0] = ViewUtils.getViewPaddingLeft(this);
        paddings[1] = getPaddingTop();
        paddings[2] = ViewUtils.getViewPaddingRight(this);
        paddings[3] = getPaddingBottom();
        int drawablePadding = getCompoundDrawablePadding();
        if (drawableLeft != null){
            paddings[0] = drawableLeft.getMinimumWidth()+paddings[0]+drawablePadding;
        }
        if (drawableTop != null){
            paddings[1] = drawableTop.getMinimumWidth()+paddings[1]+drawablePadding;
        }
        if (drawableRight != null){
            paddings[2] = drawableRight.getMinimumWidth()+paddings[2]+drawablePadding;
        }

        if (drawableBottom != null){
            paddings[3] = drawableBottom.getMinimumWidth()+paddings[3]+drawablePadding;
        }

        return paddings;
    }
}

package com.flyjingfish.hollowtextviewlib;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
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
import android.text.Layout;
import android.text.SpannableString;
import android.text.TextPaint;
import android.text.style.LeadingMarginSpan;
import android.util.AttributeSet;
import android.util.LayoutDirection;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.text.TextUtilsCompat;

import java.util.Locale;

public class HollowTextView extends AppCompatTextView {

    private Drawable bgDrawable;
    private int strokeWidth;
    private int[] gradientStrokeColors;
    private float[] gradientStrokePositions;
    private boolean gradientStrokeColor;
    private float strokeAngle;
    private boolean strokeRtlAngle;
    private boolean isRtl;
    private int strokeTextColor;
    private Paint.Join strokeJoin;

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

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.HollowTextView);
        strokeWidth = typedArray.getDimensionPixelSize(R.styleable.HollowTextView_hollow_stroke_strokeWidth, 0);
        int startStrokeColor = typedArray.getColor(R.styleable.HollowTextView_hollow_stroke_startColor, 0);
        int centerStrokeColor = typedArray.getColor(R.styleable.HollowTextView_hollow_stroke_centerColor, 0);
        int endStrokeColor = typedArray.getColor(R.styleable.HollowTextView_hollow_stroke_endColor, 0);
        strokeTextColor = typedArray.getColor(R.styleable.HollowTextView_hollow_stroke_textColor, getCurrentTextColor());
        strokeAngle = typedArray.getFloat(R.styleable.HollowTextView_hollow_stroke_angle, 0);
        strokeRtlAngle = typedArray.getBoolean(R.styleable.HollowTextView_hollow_stroke_rtl_angle, false);
        int strokeJoinInt = typedArray.getInt(R.styleable.HollowTextView_hollow_stroke_join, Paint.Join.ROUND.ordinal());

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

        if (strokeJoinInt >=0 && strokeJoinInt<=2){
            strokeJoin = Paint.Join.values()[strokeJoinInt];
        }else {
            strokeJoin = Paint.Join.ROUND;
        }

        CharSequence text = getText();
        setText(text);
    }

    static CharSequence createIndentedText(CharSequence text, int marginFirstLine, int marginNextLines) {
        SpannableString result = new SpannableString(text);
        result.setSpan(new LeadingMarginSpan.Standard(marginFirstLine, marginNextLines), 0, text.length(), 0);
        return result;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (widthMode == MeasureSpec.AT_MOST && strokeWidth > 0){
            int measureWidth = getMeasuredWidth();
            int width = MeasureSpec.getSize(widthMeasureSpec);
            if (measureWidth < width){
                int measureHeight = getMeasuredHeight();
//                int height = MeasureSpec.getSize(heightMeasureSpec);
                int heightMode = MeasureSpec.getMode(heightMeasureSpec);
                int newWidth = MeasureSpec.makeMeasureSpec(measureWidth+Math.min(strokeWidth/2,width-measureWidth), widthMode);
                setMeasuredDimension(newWidth,MeasureSpec.makeMeasureSpec(measureHeight, heightMode));
            }
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        TextPaint textPaint = getPaint();
        Paint.Style oldStyle = textPaint.getStyle();
        textPaint.setColor(Color.BLACK);
        textPaint.setXfermode(null);
        canvas.saveLayer(new RectF(0, 0, getWidth(), getHeight()), textPaint, Canvas.ALL_SAVE_FLAG);
        drawBackground(canvas);
        textPaint.setStrokeWidth(strokeWidth);
        textPaint.setStrokeJoin(strokeJoin);
        textPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        if (gradientStrokeColor){
            float currentAngle = strokeAngle;
            if (strokeRtlAngle && isRtl){
                currentAngle = - strokeAngle;
            }
            float[] xy = getAngleXY(currentAngle);

            @SuppressLint("DrawAllocation") LinearGradient linearGradient = new LinearGradient(xy[0], xy[1], xy[2], xy[3],  gradientStrokeColors, gradientStrokePositions, Shader.TileMode.CLAMP);
            textPaint.setShader(linearGradient);
        }else {
            @SuppressLint("DrawAllocation") LinearGradient linearGradient = new LinearGradient(0, 0, getWidth(), getHeight(),  new int[]{strokeTextColor,strokeTextColor}, null, Shader.TileMode.CLAMP);
            textPaint.setShader(linearGradient);
        }
        super.onDraw(canvas);

        textPaint.setStyle(oldStyle);
        textPaint.setStrokeWidth(0);
        textPaint.setShader(null);
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
        Layout layout = getLayout();
        int height = layout.getHeight();
        int width = layout.getWidth();

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
    @Override
    public void setText(CharSequence text, BufferType type) {
        if (strokeWidth > 0){
            text = createIndentedText(text, strokeWidth/2, strokeWidth/2);
        }
        super.setText(text, type);
    }

    public int getStrokeWidth() {
        return strokeWidth;
    }

    public void setStrokeWidth(int strokeWidth) {
        this.strokeWidth = strokeWidth;
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
        gradientStrokeColor = false;
        invalidate();
    }

    /**
     * 请于{@link android.widget.TextView#setText}之前调用，否则不起效果
     * @param join 粗边样式
     */
    public void setStrokeJoin(Paint.Join join){
        strokeJoin = join;
        invalidate();
    }
}

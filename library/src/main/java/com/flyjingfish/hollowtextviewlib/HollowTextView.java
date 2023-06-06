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

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.text.TextUtilsCompat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class HollowTextView extends AppCompatTextView {

    private Drawable bgDrawable;
    private int strokeWidth;
    private int[] gradientStrokeColors;
    private final List<ColorStateList> gradientStrokeColorStates = new ArrayList<>();
    private float[] gradientStrokePositions;
    private boolean gradientStrokeColor;
    private float strokeAngle;
    private boolean strokeRtlAngle;
    private boolean isRtl;
    private ColorStateList strokeTextColor;
    private int curStrokeTextColor;
    private Paint.Join strokeJoin;
    private final PorterDuffXfermode DST_OUT = new PorterDuffXfermode(PorterDuff.Mode.DST_OUT);
    private Float defaultStrokeMiter;

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
        ColorStateList startStrokeColorState = typedArray.getColorStateList(R.styleable.HollowTextView_hollow_stroke_startColor);
        ColorStateList centerStrokeColorState = typedArray.getColorStateList(R.styleable.HollowTextView_hollow_stroke_centerColor);
        ColorStateList endStrokeColorState = typedArray.getColorStateList(R.styleable.HollowTextView_hollow_stroke_endColor);
        strokeTextColor = typedArray.getColorStateList(R.styleable.HollowTextView_hollow_stroke_textColor);
        strokeAngle = typedArray.getFloat(R.styleable.HollowTextView_hollow_stroke_angle, 0);
        strokeRtlAngle = typedArray.getBoolean(R.styleable.HollowTextView_hollow_stroke_rtl_angle, false);
        int strokeJoinInt = typedArray.getInt(R.styleable.HollowTextView_hollow_stroke_join, Paint.Join.ROUND.ordinal());

        typedArray.recycle();

        if (strokeTextColor == null){
            strokeTextColor = getTextColors();
        }

        if (startStrokeColorState != null){
            gradientStrokeColorStates.add(startStrokeColorState);
        }
        if (centerStrokeColorState != null){
            gradientStrokeColorStates.add(centerStrokeColorState);
        }
        if (endStrokeColorState != null){
            gradientStrokeColorStates.add(endStrokeColorState);
        }
        if (gradientStrokeColorStates.size() == 1){
            gradientStrokeColorStates.add(ColorStateList.valueOf(Color.TRANSPARENT));
        }
        gradientStrokeColor = gradientStrokeColorStates.size() > 0;
        updateColors();

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
    protected void drawableStateChanged() {
        super.drawableStateChanged();
        updateColors();
        final int[] state = getDrawableState();
        boolean changed = false;

        final Drawable bg = bgDrawable;
        if (bg != null && bg.isStateful()) {
            changed |= bg.setState(state);
        }

        if (changed) {
            invalidate();
        }
    }

    private boolean updateColors(){
        boolean inval = false;
        final int[] drawableState = getDrawableState();
        int color = strokeTextColor.getColorForState(drawableState, 0);
        if (color != curStrokeTextColor) {
            curStrokeTextColor = color;
            inval = true;
        }

        if (gradientStrokeColorStates != null && gradientStrokeColorStates.size() > 0){
            int[] gradientColors = new int[gradientStrokeColorStates.size()];
            for (int i = 0; i < gradientStrokeColorStates.size(); i++) {
                int gradientColor = gradientStrokeColorStates.get(i).getColorForState(drawableState, 0);
                gradientColors[i] = gradientColor;
            }
            if (gradientStrokeColors == null) {
                gradientStrokeColors = gradientColors;
                inval = true;
            } else if (gradientStrokeColors.length != gradientColors.length){
                gradientStrokeColors = gradientColors;
                inval = true;
            } else {
                boolean equals = true;
                for (int i = 0; i < gradientStrokeColors.length; i++) {
                    if (gradientStrokeColors[i] != gradientColors[i]){
                        equals = false;
                        break;
                    }
                }
                if (!equals){
                    gradientStrokeColors = gradientColors;
                    inval = true;
                }
            }
        }

        if (inval){
            invalidate();
        }
        return inval;
    }

    @SuppressLint("DrawAllocation")
    @Override
    protected void onDraw(Canvas canvas) {
        TextPaint textPaint = getPaint();
        Paint.Style oldStyle = textPaint.getStyle();
        textPaint.setColor(getCurrentTextColor());
        textPaint.setXfermode(null);
        canvas.saveLayer(new RectF(0, 0, getWidth(), getHeight()), textPaint, Canvas.ALL_SAVE_FLAG);
        drawBackground(canvas);
        textPaint.setStrokeWidth(strokeWidth);
        textPaint.setStrokeJoin(strokeJoin);
        if (defaultStrokeMiter == null){
            defaultStrokeMiter = textPaint.getStrokeMiter();
        }
        if (strokeJoin == Paint.Join.MITER){
            textPaint.setStrokeMiter(2.6f);
        }else {
            textPaint.setStrokeMiter(defaultStrokeMiter);
        }
        textPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        LinearGradient linearGradient;
        if (gradientStrokeColor && gradientStrokeColors != null && gradientStrokeColors.length > 1){
            float currentAngle = strokeAngle;
            if (strokeRtlAngle && isRtl){
                currentAngle = - strokeAngle;
            }
            float[] xy = getAngleXY(currentAngle);

            linearGradient = new LinearGradient(xy[0], xy[1], xy[2], xy[3],  gradientStrokeColors, gradientStrokePositions, Shader.TileMode.CLAMP);
        }else {
            linearGradient = new LinearGradient(0, 0, getWidth(), getHeight(),  new int[]{curStrokeTextColor,curStrokeTextColor}, null, Shader.TileMode.CLAMP);
        }
        textPaint.setShader(linearGradient);
        super.onDraw(canvas);

        textPaint.setStyle(oldStyle);
        textPaint.setStrokeWidth(0);
        textPaint.setShader(null);
        textPaint.setXfermode(DST_OUT);
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

    public List<ColorStateList> getGradientStrokeColorStates() {
        return gradientStrokeColorStates;
    }

    public void setGradientStrokeColors(@Nullable @ColorInt int[] gradientStrokeColors) {
        ColorStateList[] colorStateLists;
        if (gradientStrokeColors != null){
            colorStateLists = new ColorStateList[gradientStrokeColors.length];
            for (int i = 0; i < gradientStrokeColors.length; i++) {
                colorStateLists[i] = ColorStateList.valueOf(gradientStrokeColors[i]);
            }
        }else {
            colorStateLists = null;
        }
        setGradientStrokeColors(colorStateLists);
    }

    public void setGradientStrokeColors(@Nullable ColorStateList[] colorStateLists) {
        gradientStrokeColorStates.clear();
        if (colorStateLists != null){
            gradientStrokeColorStates.addAll(Arrays.asList(colorStateLists));
            if (gradientStrokeColorStates.size() == 1){
                gradientStrokeColorStates.add(ColorStateList.valueOf(Color.TRANSPARENT));
            }
            gradientStrokeColor = gradientStrokeColorStates.size() > 0;
            if (gradientStrokePositions != null && gradientStrokeColorStates.size() != gradientStrokePositions.length){
                this.gradientStrokePositions = null;
            }
            updateColors();
        }else {
            gradientStrokeColor = false;
            if (!updateColors()){
                invalidate();
            }
        }
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
        return curStrokeTextColor;
    }
    public ColorStateList getStrokeTextColors() {
        return strokeTextColor;
    }

    public void setStrokeTextColor(@ColorInt int strokeTextColor) {
        setStrokeTextColors(ColorStateList.valueOf(strokeTextColor));
    }

    public void setStrokeTextColors(ColorStateList strokeTextColor) {
        if (strokeTextColor == null){
            return;
        }
        this.strokeTextColor = strokeTextColor;
        gradientStrokeColor = false;
        updateColors();
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

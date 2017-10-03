package com.bytebazar.timeketchup;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.DashPathEffect;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

public class TimerView extends View {

    private float formalTime;

    final private Paint pointPaint;
    final private Paint circlePaint;
    final private Paint indicatorArcPaint;
    final private Paint scaleLinePaint;
    final private Paint indicatorCursorPaint;
    final private Paint timeTextPaint;

    final private RectF circleRectF;
    final private RectF indicatorArcRectF;
    final private Rect textBounds;

    final private Path indicatorCursorPath;

    private LinearGradient sweepGradient;
    final private Matrix gradientMatrix;

    private final float circleStrokeWidth = 0;

    private final int accentColor;
    private final int elementColor;

    private float radius;
    private float scaleLength;
    private float secondDegree;

    private float paddingLeft;
    private float paddingTop;
    private float paddingRight;
    private float paddingBottom;

    private String time = "";
    private float remainingSeconds;

    public TimerView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TimerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.TimerView, defStyleAttr, 0);
        final int backgroundColor = array.getColor(R.styleable.TimerView_backgroundColor, getResources().getColor(R.color.colorPrimary));
//        setBackgroundColor(backgroundColor);
        accentColor = array.getColor(R.styleable.TimerView_accentColor, getResources().getColor(R.color.colorAccent));
        elementColor = array.getColor(R.styleable.TimerView_elementColor, getResources().getColor(R.color.white));
        final float textSize = array.getDimension(R.styleable.TimerView_textSize, spToPx(context, 60));
        final int textColor = array.getColor(R.styleable.TimerView_textColor, getResources().getColor(R.color.black));
        array.recycle();

        indicatorCursorPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        indicatorCursorPaint.setStyle(Paint.Style.STROKE);
        indicatorCursorPaint.setColor(accentColor);

        scaleLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        scaleLinePaint.setStyle(Paint.Style.STROKE);
        scaleLinePaint.setColor(backgroundColor);

        indicatorArcPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        indicatorArcPaint.setStyle(Paint.Style.STROKE);

        pointPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        pointPaint.setStyle(Paint.Style.FILL);
        pointPaint.setColor(elementColor);

        circlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        circlePaint.setPathEffect(new DashPathEffect(new float[]{10, 2}, 0.2f));
        circlePaint.setStyle(Paint.Style.STROKE);
        circlePaint.setStrokeWidth(circleStrokeWidth);
        circlePaint.setColor(elementColor);

        timeTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        timeTextPaint.setStyle(Paint.Style.FILL);
        timeTextPaint.setColor(textColor);
        timeTextPaint.setStrokeWidth(5);
        timeTextPaint.setTypeface(Typeface.createFromAsset(getContext().getAssets(), "roboto_slab_thin.ttf"));
        timeTextPaint.setTextSize(textSize);
        timeTextPaint.setStrokeCap(Paint.Cap.ROUND);
        timeTextPaint.setStrokeJoin(Paint.Join.ROUND);

        circleRectF = new RectF();
        indicatorArcRectF = new RectF();
        indicatorCursorPath = new Path();
        textBounds = new Rect();

        gradientMatrix = new Matrix();
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(measureDimension(widthMeasureSpec), measureDimension(heightMeasureSpec));
    }

    private int measureDimension(int measureSpec) {
        int result;
        int mode = MeasureSpec.getMode(measureSpec);
        int size = MeasureSpec.getSize(measureSpec);
        if (mode == MeasureSpec.EXACTLY) {
            result = size;
        } else {
            result = 800;
            if (mode == MeasureSpec.AT_MOST) {
                result = Math.min(result, size);
            }
        }
        return result;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        radius = Math.min(w - getPaddingLeft() - getPaddingRight(), h - getPaddingTop() - getPaddingBottom()) / 2;
        final float defaultPadding = 0.12f * radius;
        paddingLeft = defaultPadding + w / 2 - radius + getPaddingLeft();
        paddingTop = defaultPadding + h / 2 - radius + getPaddingTop();
        paddingRight = paddingLeft;
        paddingBottom = paddingTop;
        scaleLength = 0.12f * radius;
        indicatorArcPaint.setStrokeWidth(scaleLength);
        scaleLinePaint.setStrokeWidth(0.012f * radius);

        sweepGradient = new LinearGradient(w / 2, h / 2, 5, 5,
                new int[]{elementColor, accentColor}, new float[]{0.75f, 1}, Shader.TileMode.MIRROR);

    }

    @Override
    protected void onDraw(Canvas canvas) {
        drawArcsAndPoints(canvas);
        drawIndicatorCircle(canvas);
        drawIndicatorCursor(canvas);
        drawTimeText(canvas, time);
    }

    private void drawTimeText(Canvas canvas, String text) {
        canvas.save();
        timeTextPaint.getTextBounds(text, 0, text.length(), textBounds);
        Paint.FontMetrics fm = timeTextPaint.getFontMetrics();
        float height = -fm.descent - fm.ascent;
        float width = (int) timeTextPaint.measureText(text);

        canvas.drawText(text, getWidth() / 2 - width / 2, getHeight() / 2 + height / 2, timeTextPaint);
        canvas.restore();
    }

    private void drawArcsAndPoints(Canvas canvas) {
        canvas.drawCircle(getWidth() / 2, paddingTop, 5, pointPaint);
        canvas.drawCircle(getWidth() - paddingRight, getHeight() / 2, 5, pointPaint);
        canvas.drawCircle(getWidth() / 2, getHeight() - paddingBottom, 5, pointPaint);
        canvas.drawCircle(paddingLeft, getHeight() / 2, 5, pointPaint);

        circleRectF.set(paddingLeft + circleStrokeWidth / 2,
                paddingTop + circleStrokeWidth / 2,
                getWidth() - paddingRight + circleStrokeWidth / 2,
                getHeight() - paddingBottom + circleStrokeWidth / 2);
        for (int i = 0; i < 4; i++) {
            canvas.drawArc(circleRectF, 5 + 90 * i, 80, false, circlePaint);
        }
    }

    private void drawIndicatorCircle(Canvas canvas) {
        canvas.save();
        indicatorArcRectF.set(paddingLeft + 1.5f * scaleLength,
                paddingTop + 1.5f * scaleLength,
                getWidth() - paddingRight - 1.5f * scaleLength,
                getHeight() - paddingBottom - 1.5f * scaleLength);
        gradientMatrix.setRotate(-secondDegree - 90, getWidth() / 2, getHeight() / 2);
        sweepGradient.setLocalMatrix(gradientMatrix);
        indicatorArcPaint.setShader(sweepGradient);
        canvas.drawArc(indicatorArcRectF, 0, 360, false, indicatorArcPaint);
        for (int i = 0; i < 200 - (200 - (remainingSeconds * 200 / formalTime)); i++) {
            canvas.drawLine(getWidth() / 2, paddingTop + scaleLength,
                    getWidth() / 2, paddingTop + 2 * scaleLength, scaleLinePaint);
            canvas.rotate(-1.8f, getWidth() / 2, getHeight() / 2);
        }
        canvas.restore();
    }

    private void drawIndicatorCursor(Canvas canvas) {
        canvas.save();
        canvas.rotate(-secondDegree, getWidth() / 2, getHeight() / 2);
        indicatorCursorPath.reset();
        final float offset = paddingTop + 5;

        indicatorCursorPath.addArc(new RectF(getWidth() / 2 - 0.25f * radius, offset + 0.26f * radius
                , getWidth() / 2 + 0.25f * radius, offset + 0.44f * radius), 230, 80);

        indicatorCursorPaint.setColor(accentColor);
        indicatorCursorPaint.setStrokeWidth(3);
        canvas.drawPath(indicatorCursorPath, indicatorCursorPaint);
        canvas.restore();
    }


    public void updateTime(String time, float seconds, int formalSeconds) {
        this.time = time;
        remainingSeconds = seconds;
        secondDegree = seconds * 6 / formalSeconds;
        formalTime = formalSeconds * 60;
        invalidate();
    }

    private int spToPx(Context context, float spVal) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP,
                spVal, context.getResources().getDisplayMetrics());
    }
}

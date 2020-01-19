package com.example.hrvmore;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

public class AnimView extends View {

    private boolean isBezierBackDone;
    private int mWidth;
    private int mHeight;
    private int pullWidth;
    private int pullDelta;
    private float top;
    private long start;
    private long stop;
    private float bezierBackRatio = 0f;
    private int bezierDelta;
    private long bezierBackDur;
    private Paint backPaint;
    private Path path;
    private RectF mRectF;
    private float radius;
    private AnimView.AnimatorStatus animStatus;

    public AnimView(Context context) {
        this(context, null);
    }

    public AnimView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AnimView(@NonNull Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mRectF = new RectF();
        radius = 20.0F;
        animStatus = AnimView.AnimatorStatus.PULL_LEFT;
        pullWidth = (int) TypedValue.applyDimension(1, 50.0F, getResources().getDisplayMetrics());
        pullDelta = (int) TypedValue.applyDimension(1, 80.0F, getResources().getDisplayMetrics());
        path = new Path();
        backPaint = new Paint();
        backPaint.setAntiAlias(true);
        backPaint.setStyle(Paint.Style.FILL);
    }

    public void setTop(float top) {
        this.top = top;
    }

    private float getBezierBackRatio() {
        if (System.currentTimeMillis() >= stop) {
            return 1.0F;
        } else {
            float ratio = (float) (System.currentTimeMillis() - start) / (float) this.bezierBackDur;
            return Math.min(1.0F, ratio);
        }
    }

    private int getBezierDelta() {
        return (int) ((float) bezierDelta * getBezierBackRatio());
    }

    public long getBezierBackDur() {
        return this.bezierBackDur;
    }

    public void setBezierBackDur(long bezierBackDur) {
        this.bezierBackDur = bezierBackDur;
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMSpec = widthMeasureSpec;
        int width = MeasureSpec.getSize(widthMeasureSpec);
        if (width > this.pullDelta + this.pullWidth) {
            widthMSpec = MeasureSpec.makeMeasureSpec(this.pullDelta + this.pullWidth, MeasureSpec.getMode(widthMeasureSpec));
        }
        super.onMeasure(widthMSpec, heightMeasureSpec);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (changed) {
            mWidth = getWidth();
            mHeight = getHeight();
            if (mWidth < pullWidth) {
                animStatus = AnimatorStatus.PULL_LEFT;
            }

            if (animStatus == AnimatorStatus.PULL_LEFT) {
                if (mWidth >= pullWidth) {
                    animStatus = AnimatorStatus.DRAG_LEFT;
                }
            }
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (animStatus == AnimatorStatus.PULL_LEFT) {
            mRectF.left = 0.0F;
            mRectF.top = 0.0F;
            mRectF.right = (float) this.mWidth + this.radius;
            mRectF.bottom = (float) this.mHeight;
            if (canvas != null) {
                canvas.drawRoundRect(this.mRectF, this.radius, this.radius, this.backPaint);
            }
        } else if (animStatus == AnimatorStatus.DRAG_LEFT) {
            drawDrag(canvas);
        } else if (animStatus == AnimatorStatus.RELEASE) {
            drawBack(canvas, getBezierDelta());
        }
    }

    private void drawBack(Canvas canvas, int delta) {
        path.reset();
        path.moveTo((float) mWidth, top);
        path.lineTo((float) (mWidth - pullWidth), top);
        path.quadTo((float) delta, (float) (mHeight / 2), (float) (mWidth - pullWidth), (float) mHeight - top);
        path.lineTo((float) mWidth, (float) mHeight - top);
        if (canvas != null) {
            canvas.drawPath(this.path, this.backPaint);
        }

        this.invalidate();
        if (this.getBezierBackRatio() == 1.0F) {
            this.isBezierBackDone = true;
        }

        if (this.isBezierBackDone && this.mWidth <= this.pullWidth) {
            this.drawFooterBack(canvas);
        }

    }

    private void drawFooterBack(Canvas canvas) {
        mRectF.left = 0.0F;
        mRectF.top = 0.0F;
        mRectF.right = (float) mWidth + radius;
        mRectF.bottom = (float) mHeight;
        if (canvas != null) {
            canvas.drawRoundRect(mRectF, radius, radius, backPaint);
        }
    }

    private void drawDrag(Canvas canvas) {
        if (canvas != null) {
            canvas.drawRect((float) (mWidth - pullWidth), top, (float) mWidth,
                    (float) mHeight - top, backPaint);
        }

        path.reset();
        path.moveTo((float) (mWidth - pullWidth), top);
        path.quadTo(0.0F, (float) (mHeight / 2), (float) (mWidth - pullWidth),
                (float) mHeight - top);
        if (canvas != null) {
            canvas.drawPath(path, backPaint);
        }

    }

    public void releaseDrag() {
        this.animStatus = AnimView.AnimatorStatus.RELEASE;
        this.start = System.currentTimeMillis();
        this.stop = this.start + this.bezierBackDur;
        this.bezierDelta = this.mWidth - this.pullWidth;
        this.isBezierBackDone = false;
        this.requestLayout();
    }

    public void setBgColor(int color) {
        this.backPaint.setColor(color);
    }

    public void setBgRadius(float bgRadius) {
        this.radius = bgRadius;
    }

    public void setPullWidth(int pullWidth) {
        this.pullWidth = pullWidth;
    }

    enum AnimatorStatus {
        PULL_LEFT,
        DRAG_LEFT,
        RELEASE
    }
}


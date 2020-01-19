package com.example.hrvmore;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;


public class HorizontalLoadMoreView extends FrameLayout {

    private float touchStartX = 0F;
    private float touchCurrX = 0F;
    private float touchLastX = 0F;

    private float pullWidth;//拉伸距离
    private int moreViewMarginRight = 0;//more_view右边距

    private int footerVerticalMargin = 0;//脚布局垂直方向边距
    private float footerWidth = 0F;//脚布局宽度
    private float footerPullMaxTop = 0F;//脚布局拉动时最大缩放高度

    private int footerViewBgColor = Color.GRAY;//脚局部背景颜色
    private float footerViewBgRadius = 0F;//脚布局矩形圆角
    private float animStartTop = 0F;

    private boolean isRefresh;
    private boolean scrollState;

    private View childView;
    private AnimView footerView;
    private View moreView;

    private TextView moreText;//加载更多文字
    private ImageView arrowIv;//可显示拖拽方向图标

    private ValueAnimator backAnimator;
    private RotateAnimation arrowRotateAnim;
    private RotateAnimation arrowRotateBackAnim;
    private ValueAnimator mOffsetAnimator;

    private boolean isFooterViewShow = false;
    private boolean isNeedScroll = false;//嵌套滑动后是否需要继续滚动

    private OnScrollListener scrollListener;
    private OnRefreshListener refreshListener;
    private DecelerateInterpolator interpolator = new DecelerateInterpolator(10.0F);

    private static final long BACK_ANIM_DUR = 500L;////动画时长
    private static final long SCROLL_ANIM_DUR = 600L;
    private static final long BEZIER_ANIM_DUR = 350L;
    private static final long ROTATION_ANIM_DUR = 200L;
    private static final float DEFAULT_MARGIN_RIGHT = 10.0F;
    private static final float DEFAULT_PULL_WIDTH = 100.0F;
    private static final float DEFAULT_MOVE_MAX_DIMEN = 50.0F;
    private static final float DEFAULT_MORE_VIEW_TEXT_SIZE = 15.0F;
    private static final float DEFAULT_FOOTER_WIDTH = 50.0F;
    private static final float DEFAULT_FOOTER_PULL_MAX_TOP = 30.0F;
    private static final float DEFAULT_FOOTER_BG_RADIUS = 20.0F;
    private static final float DEFAULT_FOOTER_VERTICAL_MARGIN = 10.0F;
    private static final float DEFAULT_VISIBLE_WIDTH = 40.0F;
    private static final float ROTATION_ANGLE = 180.0F;
    private static final LinearInterpolator animationInterpolator = new LinearInterpolator();

    private float moreViewMoveMaxDimen = 0F;//滑动最大距离
    private int moreViewTextColor = Color.BLACK;
    private float moreViewTextSize = 0F;

    private String scanMore = "查看更多";
    private String releaseScanMore = "释放查看";

    private float defaultOffsetX;//默认"查看更多"可见值

    public final void setOnScrollListener(OnScrollListener listener) {
        this.scrollListener = listener;
    }

    public final void setOnRefreshListener(OnRefreshListener listener) {
        this.refreshListener = listener;
    }

    public HorizontalLoadMoreView(Context context) {
        this(context, null);
    }

    public HorizontalLoadMoreView(@NonNull Context context, @Nullable AttributeSet attr) {
        this(context, attr, 0);
    }

    public HorizontalLoadMoreView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        float defaultPullWidth = TypedValue.applyDimension(1, DEFAULT_PULL_WIDTH, displayMetrics);
        float defaultMoreViewMoveMaxDimen = TypedValue.applyDimension(1, DEFAULT_MOVE_MAX_DIMEN, displayMetrics);
        float defaultMoreViewTextSize = TypedValue.applyDimension(1, DEFAULT_MORE_VIEW_TEXT_SIZE, displayMetrics);
        float defaultFooterWidth = TypedValue.applyDimension(1, DEFAULT_FOOTER_WIDTH, displayMetrics);
        float defaultFooterTop = TypedValue.applyDimension(1, DEFAULT_FOOTER_PULL_MAX_TOP, displayMetrics);
        float defaultFooterRadius = TypedValue.applyDimension(1, DEFAULT_FOOTER_BG_RADIUS, displayMetrics);
        float defaultFooterVerticalMargin = TypedValue.applyDimension(1, DEFAULT_FOOTER_VERTICAL_MARGIN, displayMetrics);
        float defaultMoreViewMarginRight = TypedValue.applyDimension(1, DEFAULT_MARGIN_RIGHT, displayMetrics);
        float defaultVisibleWidth = TypedValue.applyDimension(1, DEFAULT_VISIBLE_WIDTH, displayMetrics);

        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.HorizontalLoadMoreView);
        pullWidth = ta.getDimension(R.styleable.HorizontalLoadMoreView_pullWidth, defaultPullWidth);
        moreViewMoveMaxDimen = ta.getDimension(R.styleable.HorizontalLoadMoreView_moreViewMoveMaxDimen,
                defaultMoreViewMoveMaxDimen);
        moreViewTextColor = ta.getColor(R.styleable.HorizontalLoadMoreView_moreViewTextColor,
                Color.BLACK);
        moreViewTextSize = ta.getDimension(R.styleable.HorizontalLoadMoreView_moreViewTestSize,
                defaultMoreViewTextSize);
        defaultOffsetX = ta.getDimension(R.styleable.HorizontalLoadMoreView_moreViewVisibleWidth,
                defaultVisibleWidth);
        moreViewMarginRight = -((int) ta.getDimension(R.styleable.HorizontalLoadMoreView_moreViewMarginRight,
                defaultMoreViewMarginRight));
        footerViewBgColor = ta.getColor(R.styleable.HorizontalLoadMoreView_footerBgColor, Color.GRAY);
        footerWidth = ta.getDimension(R.styleable.HorizontalLoadMoreView_footerWidth, defaultFooterWidth);
        footerPullMaxTop = ta.getDimension(R.styleable.HorizontalLoadMoreView_footerPullMaxTop, defaultFooterTop);
        footerViewBgRadius = ta.getDimension(R.styleable.HorizontalLoadMoreView_footerBgRadius, defaultFooterRadius);
        footerVerticalMargin = (int) ta.getDimension(R.styleable.HorizontalLoadMoreView_footerVerticalMargin, defaultFooterVerticalMargin);
        if (ta.hasValue(R.styleable.HorizontalLoadMoreView_scanMoreText)) {
            scanMore = ta.getString(R.styleable.HorizontalLoadMoreView_scanMoreText);
        }
        if (ta.hasValue(R.styleable.HorizontalLoadMoreView_releaseScanMoreText)) {
            releaseScanMore = ta.getString(R.styleable.HorizontalLoadMoreView_releaseScanMoreText);
        }

        ta.recycle();

        post((new Runnable() {
            @Override
            public void run() {
                childView = getChildAt(0);
                if (childView instanceof RecyclerView) {
                    RecyclerView recyclerView = (RecyclerView) childView;
                    recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                        @Override
                        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                            super.onScrollStateChanged(recyclerView, newState);

                            //滑动停止后，最后一条item显示时滑动到底部，展示查看更多
                            if (newState == RecyclerView.SCROLL_STATE_IDLE &&
                                    !canScrollRight()) {
                                isFooterViewShow = true;
                                if (childView != null) {
                                    childView.setTranslationX(-defaultOffsetX);
                                }

                                if (footerView != null) {
                                    ViewGroup.LayoutParams footerViewLayoutParams = footerView.getLayoutParams();
                                    if (footerViewLayoutParams != null) {
                                        footerViewLayoutParams.width = (int) defaultOffsetX;
                                    }
                                }
                                if (footerView != null) {
                                    footerView.requestLayout();
                                }

                                moveMoreView(defaultOffsetX, false, false);
                                animateScroll(0.0F, (int) SCROLL_ANIM_DUR, false);
                                isNeedScroll = false;
                            }

                        }
                    });
                }

                addFooterView();
                addMoreView();
                initBackAnim();
                initRotateAnim();
            }
        }));
    }

    public void reset() {
        removeView(footerView);
        removeView(moreView);
        if (childView != null) {
            childView.setTranslationX(0.0F);
        }

        if (getScrollX() != 0) {
            setScrollX(0);
        }

        addFooterView();
        addMoreView();
        initBackAnim();
        initRotateAnim();
    }

    private void addFooterView() {
        LayoutParams params = new LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT);
        params.topMargin = footerVerticalMargin;
        params.bottomMargin = footerVerticalMargin;
        params.gravity = Gravity.RIGHT;

        footerView = new AnimView(getContext());
        footerView.setLayoutParams(params);
        footerView.setBgColor(footerViewBgColor);
        footerView.setBgRadius(footerViewBgRadius);
        footerView.setPullWidth((int) footerWidth);
        footerView.setBezierBackDur(BEZIER_ANIM_DUR);

        addViewInternal(footerView);
    }

    private void addMoreView() {
        LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.RIGHT | Gravity.CENTER_VERTICAL;
        params.setMargins(0, 0, moreViewMarginRight, 0);

        moreView = LayoutInflater.from(this.getContext()).inflate(R.layout.item_load_more, this, false);
        moreView.setLayoutParams(params);
        moreText = moreView.findViewById(R.id.tv_more_text);
        moreText.setTextSize(TypedValue.COMPLEX_UNIT_PX, moreViewTextSize);
        moreText.setTextColor(moreViewTextColor);

        addViewInternal(moreView);
    }

    private void initBackAnim() {
        if (childView == null) return;

        backAnimator = ValueAnimator.ofFloat(new float[]{this.pullWidth, 0.0F});
        backAnimator.addListener((new AnimListener()));
        backAnimator.addUpdateListener(mAnimatorUpdateListener);
        backAnimator.setDuration(BACK_ANIM_DUR);
    }

    private void moveMoreView(float offsetX, boolean release, boolean move) {
        float dx = offsetX / (float) 2;
        if (moreView != null) {
            moreView.setVisibility(move ? View.VISIBLE : View.INVISIBLE);
        }

        if (dx <= moreViewMoveMaxDimen) {
            if (moreView != null) {
                moreView.setTranslationX(-dx);
            }

            if (!release && this.switchMoreText(scanMore)) {
                if (arrowIv != null) {
                    arrowIv.clearAnimation();
                    arrowIv.startAnimation(arrowRotateBackAnim);
                }
            }
        } else if (switchMoreText(releaseScanMore)) {
            if (arrowIv != null) {
                arrowIv.clearAnimation();
                arrowIv.startAnimation(arrowRotateAnim);
            }
        }
    }

    private final boolean switchMoreText(String text) {
        if (TextUtils.equals(text, String.valueOf(moreText != null ? moreText.getText() : null))) {
            return false;
        } else {
            if (moreText != null) {
                moreText.setText(text);
            }
            return true;
        }
    }

    private void initRotateAnim() {
        int pivotType = Animation.RELATIVE_TO_SELF;
        float pivotValue = 0.5F;
        arrowRotateAnim = new RotateAnimation(0.0F,
                ROTATION_ANGLE, pivotType, pivotValue, pivotType, pivotValue);

        arrowRotateAnim.setInterpolator(animationInterpolator);
        arrowRotateAnim.setDuration(ROTATION_ANIM_DUR);
        arrowRotateAnim.setFillAfter(true);

        arrowRotateBackAnim = new RotateAnimation(ROTATION_ANGLE,
                0.0F, pivotType, pivotValue, pivotType, pivotValue);
        arrowRotateBackAnim.setInterpolator(animationInterpolator);
        arrowRotateBackAnim.setDuration(ROTATION_ANIM_DUR);
        arrowRotateBackAnim.setFillAfter(true);
    }

    private void addViewInternal(@NonNull View child) {
        super.addView(child);
    }

    public void addView(@Nullable View child) {
        if (getChildCount() >= 1) {
//            throw RuntimeException("only can attach one child")
        }
        childView = child;
        super.addView(child);
    }

    @Override
    public boolean onInterceptTouchEvent(@Nullable MotionEvent ev) {
        if (isRefresh) return true;

        int evAction = ev.getAction();
        if (evAction == MotionEvent.ACTION_DOWN) {
            touchStartX = ev.getX();
            touchLastX = ev.getX();
            touchCurrX = touchStartX;
            setScrollState(false);
        } else if (evAction == MotionEvent.ACTION_MOVE) {
            float currX = ev.getX();
            float dx = touchStartX - currX;
            touchLastX = currX;

            //拦截条件
            if (dx > (float) 10 && !this.canScrollRight() && this.getScrollX() >= 0) {
                getParent().requestDisallowInterceptTouchEvent(true);
                this.setScrollState(true);
                return true;
            }
        }

        return super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (this.isRefresh) {
            return super.onTouchEvent(event);
        } else {
            int eventAction = event.getAction();
            if (eventAction == MotionEvent.ACTION_MOVE) {
                this.touchCurrX = event.getX();
                float dx = touchStartX - touchCurrX;
                if (this.childView == null) {
                    return true;
                }

                dx = Math.min(pullWidth * (float) 2, dx);
                dx = Math.max(0.0F, dx);
                float unit = dx / (float) 2;

                //计算偏移量
                float offsetX = this.interpolator.getInterpolation(unit / this.pullWidth) * unit;
                float offsetY = this.interpolator.getInterpolation(unit / (float)
                        this.getHeight()) * unit - moreViewMoveMaxDimen;
                if (offsetY >= this.footerPullMaxTop) {
                    offsetY = this.footerPullMaxTop;
                }

                //偏移量加上默认脚布局宽度
                if (isFooterViewShow) {
                    offsetX += defaultOffsetX;
                }

                float tranX = offsetX;
                //位移最大值。超过的部分缩短为滑动距离的一半
                float max = pullWidth * 0.8F + defaultOffsetX;
                if (offsetX >= max) {
                    tranX = (offsetX - max) * 0.5F + max;
                }

                if (childView != null) {
                    childView.setTranslationX(-tranX);
                }

                if (footerView != null) {
                    ViewGroup.LayoutParams footerViewLayoutParams = footerView.getLayoutParams();
                    if (footerViewLayoutParams != null) {
                        footerViewLayoutParams.width = (int) offsetX;
                    }

                    footerView.setTop(offsetY);

                    footerView.requestLayout();
                }

                moveMoreView(offsetX, false, true);
                return super.onTouchEvent(event);
            } else if (eventAction == MotionEvent.ACTION_UP || eventAction == MotionEvent.ACTION_CANCEL) {

                if (childView == null || childView.getTranslationX() >= 0) {
                    return true;
                }

                float childDx = Math.abs(childView.getTranslationX());

                if (reachReleasePoint()) {
                    this.isFooterViewShow = true;
                    this.isRefresh = true;
                }

                if (backAnimator != null) {
                    backAnimator.setFloatValues(new float[]{childDx, 0.0F});
                    backAnimator.start();
                }

                if (childDx >= footerWidth) {
                    if (footerView != null) {
                        footerView.releaseDrag();
                    }
                }

                this.setScrollState(false);
                return true;
            }


            return super.onTouchEvent(event);
        }
    }

    private final boolean reachReleasePoint() {
        return TextUtils.equals(releaseScanMore, String.valueOf(moreText != null ? moreText.getText() : null));
    }

    private final boolean canScrollRight() {
        return childView != null ? childView.canScrollHorizontally(1) : false;
    }

    private void setScrollState(boolean scrollState) {
        if (this.scrollState != scrollState) {
            this.scrollState = scrollState;
            if (scrollListener != null) {
                scrollListener.onScrollState(scrollState);
            }
        }
    }

    public boolean onStartNestedScroll(@Nullable View child, @Nullable View target, int nestedScrollAxes) {
        return (nestedScrollAxes & ViewCompat.SCROLL_AXIS_HORIZONTAL) != 0;
    }

    @Override
    public void onNestedPreScroll(@Nullable View target, int dx, int dy, int[] consumed) {

        if (footerView == null) return;
        boolean hiddenMoreView = dx < 0 && this.getScrollX() > -((int) defaultOffsetX)
                && !this.canScrollRight() && footerView.getWidth() != 0;
        boolean showMoreView = dx > 0 && this.getScrollX() < 0 && !this.canScrollRight();
        if (hiddenMoreView || showMoreView) {
            this.isNeedScroll = true;
            this.scrollBy(dx, 0);
            consumed[0] = dx;
        }

    }

    /**
     * 停止嵌套滑动后修正处理
     */
    @Override
    public void onStopNestedScroll(@Nullable View child) {
        super.onStopNestedScroll(child);
        if (this.isNeedScroll) {
            this.animateScroll(0.0F, (int) SCROLL_ANIM_DUR, false);
        }

    }

    @Override
    public boolean onNestedFling(@Nullable View target, float velocityX, float velocityY, boolean consumed) {
        boolean realConsumed = consumed;
        if (target instanceof RecyclerView && velocityX > (float) 0) {
            View firstChild = ((RecyclerView) target).getChildAt(0);
            int childAdapterPosition = ((RecyclerView) target).getChildAdapterPosition(firstChild);
            realConsumed = childAdapterPosition > 3;
        }

        if (!realConsumed) {
            this.animateScroll(velocityX, computeDuration(0.0F), realConsumed);
        } else {
            this.animateScroll(velocityX, computeDuration(velocityX), realConsumed);
        }

        return true;
    }

    private int computeDuration(float velocityX) {
        int distance = velocityX > (float) 0 ? Math.abs((int) defaultOffsetX -
                this.getScrollX()) : Math.abs((int) defaultOffsetX -
                ((int) defaultOffsetX - this.getScrollX()));

        int duration;
        float realVelocityX = Math.abs(velocityX);

        if (realVelocityX > 0) {
            duration = 3 * Math.round((float) 1000 * ((float) distance / realVelocityX));
        } else {
            float distanceRatio = (float) distance / (float) this.getWidth();
            duration = (int) ((distanceRatio + (float) 1) * (float) 150);
        }
        return duration;
    }

    @Override
    public boolean onNestedPreFling(@Nullable View target, float velocityX, float velocityY) {
        //隐藏moreView过程中消费掉fling
        boolean isAnimRunning = false;
        if (this.mOffsetAnimator != null) {
            isAnimRunning = mOffsetAnimator.isRunning();
        }

        return velocityX < (float) 0 && this.getScrollX() >= -((int) defaultOffsetX)
                && !this.canScrollRight() || isAnimRunning;
    }

    private final void animateScroll(float velocityX, int duration, boolean consumed) {
        if (canScrollRight()) {
            return;
        }

        int currentOffset = this.getScrollX();
        if (this.mOffsetAnimator == null) {
            this.mOffsetAnimator = new ValueAnimator();
            mOffsetAnimator.addListener((new AnimatorListener() {
                public void onAnimationRepeat(@Nullable Animator p0) {
                }

                public void onAnimationStart(@Nullable Animator p0) {
                }

                public void onAnimationEnd(@Nullable Animator p0) {
                    isNeedScroll = false;
                }

                public void onAnimationCancel(@Nullable Animator p0) {
                }
            }));

            mOffsetAnimator.addUpdateListener((new AnimatorUpdateListener() {
                public final void onAnimationUpdate(ValueAnimator animation) {
                    if (animation.getAnimatedValue() instanceof Integer) {
                        scrollTo((Integer) animation.getAnimatedValue(), 0);
                    }
                }
            }));
        } else {
            if (mOffsetAnimator != null) {
                mOffsetAnimator.cancel();
            }
        }

        if (mOffsetAnimator != null) {
            mOffsetAnimator.setDuration((long) Math.min(duration, (int) 600L));
        }

        if (velocityX >= (float) 0) {
            if (mOffsetAnimator != null) {
                mOffsetAnimator.setIntValues(new int[]{currentOffset, 0});
                mOffsetAnimator.start();
            }
        } else if (!consumed) {
            //如果子View没有消耗down事件 那么就让自身滑倒0位置
            if (mOffsetAnimator != null) {
                mOffsetAnimator.setIntValues(new int[]{currentOffset, 0});
                mOffsetAnimator.start();
            }
        }

    }

    /**
     * 限定滚动的范围，scrollBy默认调用scrollTo
     */
    @Override
    public void scrollTo(int x, int y) {
        int realX = x;
        if (x >= 0) {
            realX = 0;
        }

        if ((float) x <= -defaultOffsetX) {
            realX = -((int) defaultOffsetX);
        }

        if (realX != this.getScrollX()) {
            super.scrollTo(realX, y);
        }
    }

    /**
     * 获取嵌套滑动的轴
     *
     * @see ViewCompat.SCROLL_AXIS_HORIZONTAL 水平
     * @see ViewCompat.SCROLL_AXIS_VERTICAL 垂直
     * @see ViewCompat.SCROLL_AXIS_NONE 都支持
     */
    public int getNestedScrollAxes() {
        return SCROLL_AXIS_HORIZONTAL;
    }

    public final class AnimListener implements AnimatorListener {

        public void onAnimationRepeat(@Nullable Animator animation) {
        }

        public void onAnimationEnd(@Nullable Animator animation) {
            if (moreText != null) {
                moreText.setText(scanMore);
            }

            if (arrowIv != null) {
                arrowIv.clearAnimation();
            }

            isRefresh = false;
        }

        public void onAnimationCancel(@Nullable Animator animation) {
        }

        public void onAnimationStart(@Nullable Animator animation) {
            if (isRefresh) {
                if (refreshListener != null) {
                    refreshListener.onRightRefresh();
                }
            }

            animStartTop = footerView != null ? footerView.getTop() : 0F;
        }
    }

    private AnimatorUpdateListener mAnimatorUpdateListener = new AnimatorUpdateListener() {
        @Override
        public void onAnimationUpdate(ValueAnimator valueAnimator) {

            Object animatedValue = valueAnimator.getAnimatedValue();
            if (animatedValue != null) {
                float value = (Float) animatedValue;
                float offsetX = value;
                float offsetY = interpolator.getInterpolation(value / (float) getHeight()) * value;

                if (value <= footerWidth) {
                    offsetX *= interpolator.getInterpolation(value / footerWidth);
                    if (offsetX <= defaultOffsetX) {
                        offsetX = defaultOffsetX;
                    }

                    if (footerView != null) {
                        ViewGroup.LayoutParams footerViewLayoutParams = footerView.getLayoutParams();
                        if (footerViewLayoutParams != null) {
                            footerViewLayoutParams.width = (int) offsetX;
                        }
                        footerView.setTop(offsetY);
                        footerView.requestLayout();
                    }
                } else {
                    //记录当前收缩动画的宽高
                    if (offsetY >= animStartTop) {
                        offsetY = animStartTop;
                    }

                    if (footerView != null) {
                        footerView.setTop(offsetY);
                        ViewGroup.LayoutParams footerViewLayoutParams = footerView.getLayoutParams();
                        if (footerViewLayoutParams != null) {
                            footerViewLayoutParams.width = (int) value;
                        }
                    }
                }

                if (childView != null) {
                    childView.setTranslationX(-offsetX);
                }

                moveMoreView(offsetX, true, false);
            }

        }
    };
}

package com.example.hrvmore.demo;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.hrvmore.R;

public class PullLeftToRefreshLayout extends LinearLayout {

    private static final Interpolator ANIMATION_INTERPOLATOR = new LinearInterpolator();
    private static final long BACK_ANIM_DUR = 500;//设置返回滑动松开childView返回的时间
    private static final long BEZIER_BACK_DUR = 350;//设置贝塞尔曲线的长度
    private static final int ROTATION_ANIM_DUR = 200;//设置箭头的旋转的时间
    private OnScrollListener mScrollListener;
    private OnRefreshListener mOnRefreshListener;

    /**
     * MoreView移动的最大距离
     */
    private static float MORE_VIEW_MOVE_DIMEN;
    private static final int ROTATION_ANGLE = 180;

    private static String SCAN_MORE;
    private static String RELEASE_SCAN_MORE;

    private float mTouchStartX;
    private float mTouchCurX;

    private float mPullWidth;
    private float mFooterWidth;

    /**
     * 目的是为了将moreView隐藏以便滑动
     */
    private int moreViewMarginRight;
    private int footerViewBgColor;

    private boolean isRefresh = false;
    private boolean scrollState = false;

    private View mChildView;
    private AnimView footerView;
    private View moreView;
    private TextView moreText;

    private ValueAnimator mBackAnimator;
    private RotateAnimation mArrowRotateAnim;
    private RotateAnimation mArrowRotateBackAnim;
    private DecelerateInterpolator interpolator = new DecelerateInterpolator(10);

    public void setScrollListener(OnScrollListener scrollListener) {
        mScrollListener = scrollListener;
    }

    public void setOnRefreshListener(OnRefreshListener onRefreshListener) {
        mOnRefreshListener = onRefreshListener;
    }

    public PullLeftToRefreshLayout(@NonNull Context context) {
        this(context, null);
    }

    public PullLeftToRefreshLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PullLeftToRefreshLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context,attrs);
    }

    private void init(Context context,AttributeSet attrs) {
        mPullWidth = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 100, context.getResources().getDisplayMetrics());
        MORE_VIEW_MOVE_DIMEN = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 40, context.getResources().getDisplayMetrics());
        mFooterWidth = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 20, context.getResources().getDisplayMetrics());
        //设置childView为-26dp隐藏它
        moreViewMarginRight = getResources().getDimensionPixelSize(R.dimen.dp_26);
        SCAN_MORE = getResources().getString(R.string.scan_more);
        RELEASE_SCAN_MORE = getResources().getString(R.string.release_scan_more);
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.PullLeftToRefreshLayout);
        footerViewBgColor = ta.getColor(R.styleable.PullLeftToRefreshLayout_footer_bgColor, Color.rgb(243, 242, 242));
        ta.recycle();
        setOrientation(HORIZONTAL);
        this.post(new Runnable() {
            @Override
            public void run() {
                //获取PullLeftToRefreshLayout中的第一个view有且只有一个
                mChildView = getChildAt(0);
                //设置MoreView背景色
                addFooterView();
                //添加Moreview
                addMoreView();
                //设置回弹view动画
                initBackAnim();
                //设置刷新箭头动画
                initRotateAnim();
            }
        });
    }

    //添加背景实现滑动时候显示贝塞尔曲线效果
    private void addFooterView() {
        //设置显示位置
        LayoutParams params = new LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT);
        params.gravity = Gravity.RIGHT;

        footerView = new AnimView(getContext());
        footerView.setLayoutParams(params);
        footerView.setBgColor(Color.RED);
        footerView.setBezierBackDur(BEZIER_BACK_DUR);//设置贝塞尔曲线的在终点位置
        addViewInternal(footerView);//添加到FrameLayout容器中
    }

    //添加加载更多的视图view
    private void addMoreView() {
        LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.RIGHT | Gravity.CENTER_VERTICAL;
        params.setMargins(0, 0, moreViewMarginRight, 0);

        moreView = LayoutInflater.from(getContext()).inflate(R.layout.item_load_more2, this, false);
        moreView.setLayoutParams(params);
        moreText = (TextView) moreView.findViewById(R.id.tvMoreText);
        addViewInternal(moreView);
    }

    private void initBackAnim() {
        if (mChildView == null) {
            return;
        }

        //当手指放开的时候执行这个动画，使得展开的刷新view和childview做回弹效果，也就是恢复到原来位置
        mBackAnimator = ValueAnimator.ofFloat(mPullWidth, 0);
        mBackAnimator.addListener(new AnimListener());
        mBackAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float val = (float) animation.getAnimatedValue();
                if (val <= mFooterWidth) {
                    val = interpolator.getInterpolation(val / mFooterWidth) * val;
                    footerView.getLayoutParams().width = (int) val;
                    footerView.requestLayout();
                }
                if (mChildView != null) {
                    mChildView.setTranslationX(-val);
                }
                moveMoreView(val, true);
            }
        });
        mBackAnimator.setDuration(BACK_ANIM_DUR);
    }
    @Override
    public void addView(View child) {
        /**
         * 当前刷新控件包裹中的view只能只有一个，必须是能滚动的控件，
         * 因为需要用canScrollHorizontally来判断是否内部控件已经滚到边缘
         */
        if (getChildCount() >= 1) {
            throw new RuntimeException("you can only attach one child");
        }
        mChildView = child;
        super.addView(child);
    }

    ///设置
    private void initRotateAnim() {
        int pivotType = Animation.RELATIVE_TO_SELF;
        float pivotValue = 0.5f;
        //当是手指滑动到左边最大距离的时候箭头做旋转动画
        mArrowRotateAnim = new RotateAnimation(0, ROTATION_ANGLE, pivotType, pivotValue, pivotType, pivotValue);
        mArrowRotateAnim.setInterpolator(ANIMATION_INTERPOLATOR);
        mArrowRotateAnim.setDuration(ROTATION_ANIM_DUR);
        mArrowRotateAnim.setFillAfter(true);

        mArrowRotateBackAnim = new RotateAnimation(ROTATION_ANGLE, 0, pivotType, pivotValue, pivotType, pivotValue);
        mArrowRotateBackAnim.setInterpolator(ANIMATION_INTERPOLATOR);
        mArrowRotateBackAnim.setDuration(ROTATION_ANIM_DUR);
        mArrowRotateBackAnim.setFillAfter(true);

    }

    private void addViewInternal(@NonNull View child) {
        super.addView(child);
    }


    public interface OnScrollListener {
        void onScrollChange(boolean scroll);
    }

    public interface OnRefreshListener {
        void onRefresh();
    }


    //当内部控件滑动到边缘的时候调用该方法实现移动moreview使其跟随手指滑动显示出来
    private void moveMoreView(float offsetx, boolean release) {
        float dx = offsetx / 2;
        if (dx <= MORE_VIEW_MOVE_DIMEN) {
            moreView.setTranslationX(-dx);//主要关键代码
            //箭头移动
            if (!release && switchMoreText(SCAN_MORE)) {
//                arrowIv.clearAnimation();
//                arrowIv.startAnimation(mArrowRotateBackAnim);
            }
        } else {
            //箭头移动
            if (switchMoreText(RELEASE_SCAN_MORE)) {
//                arrowIv.clearAnimation();
//                arrowIv.startAnimation(mArrowRotateAnim);
            }
        }
    }
    //判断当前文字是哪个来执行箭头动画
    private boolean switchMoreText(String text) {
        if (text.equals(moreText.getText().toString())) {
            return false;
        }
        moreText.setText(text);
        return true;
    }
    //设置arrowIv（箭头图片）的图片动画监听,就是实现刷新
    private class AnimListener implements Animator.AnimatorListener {
        @Override
        public void onAnimationStart(Animator animator) {
        }
        @Override
        public void onAnimationEnd(Animator animator) {
            if (mOnRefreshListener != null && isRefresh) {
                mOnRefreshListener.onRefresh();
            }
            moreText.setText(SCAN_MORE);
//            arrowIv.clearAnimation();
            isRefresh = false;
        }
        @Override
        public void onAnimationCancel(Animator animator) {
        }
        @Override
        public void onAnimationRepeat(Animator animator) {
        }
    }
    //设置滚动的监听
    private void setScrollState(boolean scrollState) {
        if (this.scrollState == scrollState) {
            return;
        }
        this.scrollState = scrollState;
        if (mScrollListener != null) {
            mScrollListener.onScrollChange(scrollState);
        }
    }
    //判断滑动到底部
    private boolean canScrollRight() {
        if (mChildView == null) {
            return false;
        }
        //第一个代表滑动的view,第二个参数代表方向，负的代表左，正的代表右
        return ViewCompat.canScrollHorizontally(mChildView, 1);
    }
    //这里的拦截主要是做滚动监听
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        //如果处于刷新状态就拦截，使其进入onTouchEvent里面
        if (isRefresh) {
            return true;
        }
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mTouchStartX = ev.getX();
                mTouchCurX = mTouchStartX;
                setScrollState(false);
                break;
            case MotionEvent.ACTION_MOVE:
                float curX = ev.getX();
                float dx = curX - mTouchCurX;
                if (dx < -10 && !canScrollRight()) {
                    setScrollState(true);
                    return true;
                }
                break;
        }
        return super.onInterceptTouchEvent(ev);
    }
    //moreview能出现和实现刷新都是要这里面（关键代码）
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (isRefresh) {
            //还处于刷新状态吧这个时间向上传递，也就是传递给childView使其能滚动
            return super.onTouchEvent(event);
        }
        switch (event.getAction()) {
            case MotionEvent.ACTION_MOVE:
                mTouchCurX = event.getX();
                float dx = mTouchStartX - mTouchCurX;
                dx = Math.min(mPullWidth * 2, dx);
                dx = Math.max(0, dx);
                //如果mChildView不存在或者dx<=0（说明是手指向右滑动）
                if (mChildView == null || dx <= 0) {
                    return true;
                }
                float unit = dx / 2;
                float offsetx = interpolator.getInterpolation(unit / mPullWidth) * unit;
                mChildView.setTranslationX(-offsetx);
                footerView.getLayoutParams().width = (int) offsetx;//随着手指滑动而逐渐展开这里是背景色
                footerView.requestLayout();//刷新当前view
                moveMoreView(offsetx, false);//这里moreview也开始逐渐展开
                return true;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if (mChildView == null) {
                    return true;
                }
                //手指松开就回弹效果也就是footerview和moreview回到最初位置，就是mBackAnimator动画里面最回弹的
                float childDx = Math.abs(mChildView.getTranslationX());
                if (childDx >= mFooterWidth) {
                    //滑动到最大值 开始执行动画而在当前动画的监听里面就可以可以根据isRefresh来判断是否执行刷新方法
                    mBackAnimator.setFloatValues(childDx, 0);
                    mBackAnimator.start();
                    footerView.releaseDrag();

                    //根据moreText.getText和RELEASE_SCAN_MORE(释放刷新）
                    if (reachReleasePoint()) {
                        //改变刷新状态
                        isRefresh = true;
                    }
                } else {
                    //如果没有滑动到最大值 不改变刷新状态，也要最回弹
                    mBackAnimator.setFloatValues(childDx, 0);
                    mBackAnimator.start();
                }
                //设置滚动监听为false说明没有滚动
                setScrollState(false);

                return true;
        }
        return super.onTouchEvent(event);
    }

    //拿到text 如果是：RELEASE_SCAN_MORE(释放刷新)其实也可以不要这个，加了也不会错
    private boolean reachReleasePoint() {
        return RELEASE_SCAN_MORE.equals(moreText.getText().toString());

    }
}

package nl.dionsegijn.steppertouch;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewParent;
import android.view.animation.Animation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.view.ViewCompat;
import androidx.dynamicanimation.animation.DynamicAnimation;
import androidx.dynamicanimation.animation.SpringAnimation;
import androidx.dynamicanimation.animation.SpringForce;

/**
 * 双向门锁view
 */
public class StepperTouchView extends ConstraintLayout {

    // Animation properties
    private Float stiffness = 200f;
    private Float damping = 0.5f;
    private Float startX = 0f;
    private Float startY = 0f;

    // Indication if tapping positive and negative sides is allowed
    boolean sideTapEnabled = false;
    private boolean allowDragging = false;

    private int stepperTextSize = 20;
    private boolean allowNegative = true;
    private boolean allowPositive = true;
    private boolean isVertical = false;
    private boolean isSingle = false;//单项选择

    private FrameLayout viewCounter;
    private ConstraintLayout viewBackground;
    private ImageView viewCounterIcon;//按钮icon
    private ImageView viewNegative;//上方img
    private TextView textNegative;//上方文字
    private ImageView viewPositive;//下方img
    private TextView textPositive;//下方文字
    private FrameLayout viewBtnBg;//按钮背景颜色
    private int maxDistance = -1;

    public StepperTouchView(Context context) {
        this(context, null);
    }

    public StepperTouchView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public StepperTouchView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initAttrs(context, attrs);
        initView(context);
    }

    private void initAttrs(Context context, AttributeSet attrs) {
        TypedArray styles = context.getTheme().obtainStyledAttributes(attrs, R.styleable.StepperTouch, 0, 0);
        try {
            isSingle = styles.getBoolean(R.styleable.StepperTouch_stepperSingle, false);
            isVertical = styles.getInt(R.styleable.StepperTouch_stepperOrientation, 0) == 1;
            stepperTextSize = styles.getDimensionPixelSize(R.styleable.StepperTouch_stepperTextSize, stepperTextSize);
            allowNegative = styles.getBoolean(R.styleable.StepperTouch_stepperAllowNegative, true);
            allowPositive = styles.getBoolean(R.styleable.StepperTouch_stepperAllowPositive, true);
        } finally {
            styles.recycle();
        }
    }

    private void initView(Context context) {
        View view;
        if (isVertical) {
            if (isSingle) {
                view = LayoutInflater.from(context).inflate(R.layout.stepper_touch_vertical_single, this, true);
            } else {
                view = LayoutInflater.from(context).inflate(R.layout.stepper_touch_vertical, this, true);
            }
        } else {
            view = LayoutInflater.from(context).inflate(R.layout.stepper_touch, this, true);
        }
        viewCounter = view.findViewById(R.id.viewCounter);
        viewBackground = view.findViewById(R.id.viewBackground);
        viewCounterIcon = view.findViewById(R.id.viewCounterText);
        viewNegative = view.findViewById(R.id.textViewNegative);
        textNegative = view.findViewById(R.id.textNegative);
        viewBtnBg = view.findViewById(R.id.btn_icon_bg);
        if (!isSingle) {
            viewPositive = view.findViewById(R.id.textViewPositive);
            textPositive = view.findViewById(R.id.textPositive);
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        maxDistance = h / 2 - w / 2;
        if (isSingle)
            maxDistance = h - w;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                if (isInBounds(event, viewCounter)) {
                    startX = event.getX();
                    startY = event.getY();
                    allowDragging = true;
                } else if (sideTapEnabled) {
                    viewCounter.setX(event.getX() - viewCounter.getWidth() * 0.5f);
                }
                getParent().requestDisallowInterceptTouchEvent(true);
                return true;
            }
            case MotionEvent.ACTION_MOVE: {
                if (allowDragging) {
                    if (isVertical) {
                        updateBtnBg(event);
                    } else {
                        viewCounter.setTranslationX(event.getX() - startX);
                    }
                }
                return true;
            }
            case MotionEvent.ACTION_UP: {
                allowDragging = false;
                showWaterAnimation(event);
                if (isVertical) {
                    if (viewCounter.getTranslationY() > viewCounter.getHeight() * 0.5 && allowPositive) {
                        //下
                        Log.e("lake", "onTouchEvent: 下");
                    }
                    if (viewCounter.getTranslationY() < -viewCounter.getHeight() * 0.5 && allowPositive) {
                        //上
                        Log.e("lake", "onTouchEvent: 上");
                    }
                } else {
                    if (viewCounter.getTranslationX() > viewCounter.getWidth() * 0.5 && allowPositive) {
                        if (isLTR(viewCounter)) {
                            //右
                        } else {
                            //左
                        }
                    }
                    if (viewCounter.getTranslationX() < -(viewCounter.getWidth() * 0.5) && allowNegative) {
                        if (isLTR(viewCounter)) {
                            //左
                        } else {
                            //右
                        }
                    }
                }
                if (isVertical) {
                    if (viewCounter.getTranslationY() != 0f) {

                    }
                } else {
                    if (viewCounter.getTranslationX() != 0f) {
                        SpringAnimation animY = new SpringAnimation(viewCounter, SpringAnimation.TRANSLATION_X, 0f);
                        SpringForce springForce = animY.getSpring();
                        springForce.setStiffness(stiffness);
                        springForce.setDampingRatio(damping);
                        animY.setSpring(springForce);
                        animY.start();
                    }
                }
                return true;
            }
            default: {
                getParent().requestDisallowInterceptTouchEvent(false);
                return false;
            }
        }
    }

    /**
     * 更新按钮背景颜色
     *
     * @param event
     */
    private void updateBtnBg(MotionEvent event) {
        float disY = event.getY() - startY;
        if (isSingle && disY > 0) {
            return;
        }
        viewCounter.setTranslationY(disY > 0 ? Math.min(maxDistance, disY) : Math.max(-maxDistance, disY));
        float move = Math.abs(disY);
        if (move >= maxDistance) {
            viewBtnBg.setAlpha(0f);
            viewCounterIcon.setAlpha(0f);
        } else {
            viewBtnBg.setAlpha(1f - move / maxDistance);
            viewCounterIcon.setAlpha(1f - move / maxDistance);
        }
        viewBackground.setBackgroundResource(R.drawable.round_circle_bg_selector);
        if (disY != 0) {
            allowNegative(disY < 0);
            allowPositive(disY > 0);
        } else {
            allowNegative(true);
            allowPositive(true);
        }
    }

    private void showWaterAnimation(MotionEvent event) {
        if (viewCounter.getTranslationY() == 0f) {
            return;
        }
        float disY = event.getY() - startY;
        if (isSingle && disY > 0) {
            return;
        }
        float move = Math.abs(disY);
        ViewParent parent = getParent();
        if (move >= (maxDistance - viewCounter.getWidth() / 2)) {//自吸到端位置
            viewCounter.setTranslationY(disY > 0 ? maxDistance : -maxDistance);
            viewBtnBg.setAlpha(0f);
            viewCounterIcon.setAlpha(0f);
            if (parent instanceof WaterViewLayout) {
                final WaterViewLayout waterViewLayout = (WaterViewLayout) parent;
                if (disY > 0) {
                    waterViewLayout.showBottomWaterAnimation(new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) {

                        }

                        @Override
                        public void onAnimationEnd(Animation animation) {
                            showSpringAnimation();
                            waterViewLayout.hideBottomWaterAnimation();
                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) {

                        }
                    });
                } else {
                    waterViewLayout.showTopWaterAnimation(new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) {

                        }

                        @Override
                        public void onAnimationEnd(Animation animation) {
                            showSpringAnimation();
                            waterViewLayout.hideTopWaterAnimation();
                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) {

                        }
                    });
                }

            }
        } else {
            showSpringAnimation();
        }
    }

    private void showSpringAnimation() {
        SpringAnimation animY = new SpringAnimation(viewCounter, SpringAnimation.TRANSLATION_Y, 0f);
        SpringForce springForce = animY.getSpring();
        springForce.setStiffness(stiffness);
        springForce.setDampingRatio(isSingle ? 1 : damping);
        animY.setSpring(springForce);
        animY.addUpdateListener(new DynamicAnimation.OnAnimationUpdateListener() {
            @Override
            public void onAnimationUpdate(DynamicAnimation animation, float value, float velocity) {
                Log.e("lake", "onAnimationUpdate: " + (1f - value / maxDistance));
                float alpha = 1f - value / maxDistance;
                viewBtnBg.setAlpha(alpha);
                viewCounterIcon.setAlpha(alpha);
            }
        });
        animY.addEndListener(new DynamicAnimation.OnAnimationEndListener() {
            @Override
            public void onAnimationEnd(DynamicAnimation animation, boolean canceled, float value, float velocity) {
                viewBackground.setBackgroundResource(R.drawable.round_circle_bg);
                allowNegative(true);
                allowPositive(true);
            }
        });
        animY.start();
    }

    /**
     * Allow interact with negative section, if you disallow, the negative section will hide,
     * and it's not working
     *
     * @param allow true if allow to use negative, false to disallow
     */
    public void allowNegative(boolean allow) {
        allowNegative = allow;
        updateSideControls();
    }

    /**
     * Allow interact with positive section, if you disallow, the positive section will hide,
     * and it's not working
     *
     * @param allow true if allow to use positive, false to disallow
     */
    void allowPositive(boolean allow) {
        allowPositive = allow;
        updateSideControls();
    }

    /**
     * Update visibility of the negative and positive views
     */
    private void updateSideControls() {
        viewNegative.setVisibility(allowNegative ? VISIBLE : GONE);
        if (viewPositive != null)
            viewPositive.setVisibility(allowPositive ? VISIBLE : GONE);

        textNegative.setVisibility(allowNegative ? VISIBLE : GONE);
        if (textPositive != null)
            textPositive.setVisibility(allowPositive ? VISIBLE : GONE);
    }

    private boolean isInBounds(MotionEvent event, View view) {
        Rect rect = new Rect();
        view.getHitRect(rect);
        return rect.contains((int) event.getX(), (int) event.getY());
    }

    private boolean isLTR(View view) {
        return ViewCompat.getLayoutDirection(this) == ViewCompat.LAYOUT_DIRECTION_LTR;
    }

}

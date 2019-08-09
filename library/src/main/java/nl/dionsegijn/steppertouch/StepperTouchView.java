package nl.dionsegijn.steppertouch;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Region;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.dynamicanimation.animation.DynamicAnimation;
import androidx.dynamicanimation.animation.SpringAnimation;
import androidx.dynamicanimation.animation.SpringForce;

public class StepperTouchView extends ConstraintLayout {

    // Drawing properties
    private RectF clippingBounds = new RectF();
    private Path clipPath = new Path();

    // Animation properties
    private Float stiffness = 200f;
    private Float damping = 0.6f;
    private Float startX = 0f;
    private Float startY = 0f;

    // Indication if tapping positive and negative sides is allowed
    boolean sideTapEnabled = false;
    private boolean allowDragging = false;
    private boolean isTapped = false;

    // Style properties
    private int stepperBackground = R.color.stepper_background;
    private int stepperActionColor = R.color.stepper_actions;
    private int stepperActionColorDisabled = R.color.stepper_actions_disabled;
    private int stepperTextColor = R.color.stepper_text;
    private int stepperButtonColor = R.color.stepper_button;
    private int stepperTextSize = 20;
    private boolean allowNegative = true;
    private boolean allowPositive = true;
    private boolean isVertical = false;

    private FrameLayout viewCounter;
    private ConstraintLayout viewBackground;
    private ImageView viewCounterIcon;
    private ImageView viewNegative;
    private ImageView viewPositive;
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
            isVertical = styles.getInt(R.styleable.StepperTouch_stepperOrientation, 0) == 1;
            stepperBackground =
                    styles.getResourceId(R.styleable.StepperTouch_stepperBackgroundColor, R.color.stepper_background);
            stepperActionColor =
                    styles.getResourceId(R.styleable.StepperTouch_stepperActionsColor, R.color.stepper_actions);
            stepperActionColorDisabled = styles.getResourceId(R.styleable.StepperTouch_stepperActionsDisabledColor, R.color.stepper_actions_disabled);
            stepperTextColor = styles.getResourceId(R.styleable.StepperTouch_stepperTextColor, R.color.stepper_text);
            stepperButtonColor =
                    styles.getResourceId(R.styleable.StepperTouch_stepperButtonColor, R.color.stepper_button);
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
            view = LayoutInflater.from(context).inflate(R.layout.stepper_touch_vertical, this, true);
        } else {
            view = LayoutInflater.from(context).inflate(R.layout.stepper_touch, this, true);
        }
        setClipChildren(true);
        viewCounter = view.findViewById(R.id.viewCounter);
        viewBackground = view.findViewById(R.id.viewBackground);
        viewCounterIcon = view.findViewById(R.id.viewCounterText);
        viewNegative = view.findViewById(R.id.textViewNegative);
        viewPositive = view.findViewById(R.id.textViewPositive);
        viewBtnBg = view.findViewById(R.id.btn_icon_bg);

        setWillNotDraw(false);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        maxDistance = h / 2 - w / 2;
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
                    isTapped = true;
                    viewCounter.setX(event.getX() - viewCounter.getWidth() * 0.5f);
                }
                getParent().requestDisallowInterceptTouchEvent(true);
                return true;
            }
            case MotionEvent.ACTION_MOVE: {
                if (allowDragging) {
                    if (isVertical) {
                        float move = event.getY() - startY;
                        viewCounter.setTranslationY(move > 0 ? Math.min(maxDistance, move) : Math.max(-maxDistance, move));
                        updateBtnBg(event);
                    } else {
                        viewCounter.setTranslationX(event.getX() - startX);
                    }
                }
                return true;
            }
            case MotionEvent.ACTION_UP: {
                allowDragging = false;
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
                        SpringAnimation animY = new SpringAnimation(viewCounter, SpringAnimation.TRANSLATION_Y, 0f);
                        SpringForce springForce = animY.getSpring();
                        springForce.setStiffness(stiffness);
                        springForce.setDampingRatio(damping);
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
                            }
                        });
                        animY.start();
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
        float move = Math.abs(event.getY() - startY);
        if (move >= maxDistance) {
            viewBtnBg.setAlpha(0f);
            viewCounterIcon.setAlpha(0f);
        } else {
            viewBtnBg.setAlpha(1f - move / maxDistance);
            viewCounterIcon.setAlpha(1f - move / maxDistance);
        }
        viewBackground.setBackgroundResource(R.drawable.round_circle_bg_selector);
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
        viewPositive.setVisibility(allowPositive ? VISIBLE : GONE);
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

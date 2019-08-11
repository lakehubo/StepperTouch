package nl.dionsegijn.steppertouch;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * 支持水波纹子控件的容器
 */
public class WaterViewLayout extends FrameLayout {
    private int waterWidth;
    private StepperTouchView itemView;
    private List<ImageView> topWaterImgs = new ArrayList<>();
    private List<ImageView> bottomWaterImgs = new ArrayList<>();
    private static final int ANIMATION_EACH_OFFSET = 240; // 每个动画的播放时间间隔
    private static final int RIPPLE_VIEW_COUNT = 2;//波纹view的个数
    private static final float DEFAULT_SCALE = 1.6f;//波纹放大后的大小
    private float mScale = DEFAULT_SCALE;
    private int margin = (int) getResources().getDimension(R.dimen.dp_4);

    public WaterViewLayout(@NonNull Context context) {
        this(context, null);
    }

    public WaterViewLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public WaterViewLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        for (int i = 0; i < RIPPLE_VIEW_COUNT; i++) {
            ImageView img = createWaterImg();
            topWaterImgs.add(img);
            img.setVisibility(GONE);
        }
        for (int i = 0; i < RIPPLE_VIEW_COUNT; i++) {
            ImageView img = createWaterImg();
            bottomWaterImgs.add(img);
            img.setVisibility(GONE);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    public void addView(View child, int index, ViewGroup.LayoutParams params) {
        super.addView(child, index, params);
        if (child instanceof StepperTouchView) {
            itemView = (StepperTouchView) child;
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (itemView != null) {
            Log.e("lake", "onLayout: " + itemView.getWidth());
            waterWidth = itemView.getWidth();
            for (ImageView imageView : topWaterImgs) {
                imageView.layout(itemView.getLeft() + margin, itemView.getTop() + margin, itemView.getRight() - margin, itemView.getTop() + waterWidth - margin);
            }
            for (ImageView imageView : bottomWaterImgs) {
                imageView.layout(itemView.getLeft() + margin, itemView.getBottom() - waterWidth + margin, itemView.getRight() - margin, itemView.getBottom() - margin);
            }
        }
    }

    private ImageView createWaterImg() {
        ImageView imageView = new ImageView(getContext());
        imageView.setImageResource(R.drawable.circle_translate);
        ViewGroup.LayoutParams layoutParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        addView(imageView, layoutParams);
        return imageView;
    }

    private AnimationSet getNewAnimationSet() {
        AnimationSet as = new AnimationSet(true);
        ScaleAnimation sa = new ScaleAnimation(1f, mScale, 1f, mScale,
                ScaleAnimation.RELATIVE_TO_SELF, 0.5f,
                ScaleAnimation.RELATIVE_TO_SELF, 0.5f);
        sa.setDuration(ANIMATION_EACH_OFFSET * 3);
        sa.setRepeatCount(5);// 设置循环
        AlphaAnimation aniAlp = new AlphaAnimation(1, 0.1f);
        aniAlp.setRepeatCount(5);// 设置循环
        as.setDuration(ANIMATION_EACH_OFFSET * 3);
        as.addAnimation(sa);
        as.addAnimation(aniAlp);
        return as;
    }

    public void showTopWaterAnimation(Animation.AnimationListener listener) {
        Log.e("lake", "showWaterAnimation: ");
        if (topWaterImgs != null) {
            int i = 0;
            for (final ImageView imageView : topWaterImgs) {
                imageView.setVisibility(VISIBLE);
                final Animation animation = getNewAnimationSet();
                animation.setAnimationListener(listener);
                getHandler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        imageView.startAnimation(animation);
                    }
                }, ANIMATION_EACH_OFFSET * i);
                i++;
            }
        }
    }

    public void hideTopWaterAnimation() {
        if (topWaterImgs != null) {
            for (ImageView imageView : topWaterImgs) {
                imageView.clearAnimation();
                imageView.setVisibility(GONE);
            }
        }
    }

    public void showBottomWaterAnimation(Animation.AnimationListener listener) {
        Log.e("lake", "showWaterAnimation: ");
        if (bottomWaterImgs != null) {
            int i = 0;
            for (final ImageView imageView : bottomWaterImgs) {
                imageView.setVisibility(VISIBLE);
                final Animation animation = getNewAnimationSet();
                animation.setAnimationListener(listener);
                getHandler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        imageView.startAnimation(animation);
                    }
                }, ANIMATION_EACH_OFFSET * i);
                i++;
            }
        }
    }

    public void hideBottomWaterAnimation() {
        if (bottomWaterImgs != null) {
            for (ImageView imageView : bottomWaterImgs) {
                imageView.clearAnimation();
                imageView.setVisibility(GONE);
            }
        }
    }
}

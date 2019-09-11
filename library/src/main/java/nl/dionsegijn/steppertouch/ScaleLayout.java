package nl.dionsegijn.steppertouch;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.OverScroller;
import android.widget.Scroller;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * 缩放控件
 */
public class ScaleLayout extends FrameLayout {

    private ScaleGestureDetector mScaleDetector;
    private GestureDetector mGestureDetector;
    private static final float SUPER_MIN_MULTIPLIER = .75f;
    private static final float SUPER_MAX_MULTIPLIER = 1.25f;

    public enum FixedPixel {CENTER, TOP_LEFT, BOTTOM_RIGHT}

    private FixedPixel orientationChangeFixedPixel = FixedPixel.CENTER;
    private FixedPixel viewSizeChangeFixedPixel = FixedPixel.CENTER;
    private boolean orientationJustChanged = false;
    private boolean imageRenderedAtLeastOnce;

    private enum State {NONE, DRAG, ZOOM, FLING, ANIMATE_ZOOM}

    private boolean maxScaleIsSetByMultiplier = false;
    private State state;
    public static final float AUTOMATIC_MIN_ZOOM = -1.0f;
    private float userSpecifiedMinScale;
    private float maxScale, minScale;
    private float normalizedScale;
    private Matrix matrix, prevMatrix;
    private float superMinScale;
    private float superMaxScale;
    private float[] m;
    private Fling fling;
    private Context context;
    private int viewWidth, viewHeight, prevViewWidth, prevViewHeight;
    private float matchViewWidth, matchViewHeight, prevMatchViewWidth, prevMatchViewHeight;
    private boolean onDrawReady;
    private float maxScaleMultiplier;
    private ZoomVariables delayedZoomVariables;

    public ScaleLayout(@NonNull Context context) {
        this(context, null);
    }

    public ScaleLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ScaleLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        initView(context);
    }

    private void initView(Context context) {
        mScaleDetector = new ScaleGestureDetector(context, new ScaleListener());
        mGestureDetector = new GestureDetector(context, new GestureListener());

        matrix = new Matrix();
        prevMatrix = new Matrix();

        m = new float[9];
        normalizedScale = 1;

        minScale = 1;
        maxScale = 3;

        superMinScale = SUPER_MIN_MULTIPLIER * minScale;
        superMaxScale = SUPER_MAX_MULTIPLIER * maxScale;

        setState(State.NONE);

        onDrawReady = false;

        super.setOnTouchListener(new PrivateOnTouchListener());
    }

    private void setState(State state) {
        this.state = state;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        viewHeight = h;
        viewWidth = w;
    }


    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
    }

    @Override
    public boolean canScrollHorizontally(int direction) {
        Log.e("lake", "canScrollHorizontally: " + direction);
        return true;
    }

    @Override
    public boolean canScrollVertically(int direction) {
        Log.e("lake", "canScrollVertically: " + direction);
        return true;
    }

    /**
     * Responsible for all touch events. Handles the heavy lifting of drag and also sends
     * touch events to Scale Detector and Gesture Detector.
     *
     * @author Ortiz
     */
    private class PrivateOnTouchListener implements View.OnTouchListener {

        //
        // Remember last point position for dragging
        //
        private PointF last = new PointF();

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (getChildCount() == 0) {
                setState(State.NONE);
                return false;
            }
            mScaleDetector.onTouchEvent(event);
            mGestureDetector.onTouchEvent(event);
            PointF curr = new PointF(event.getX(), event.getY());

            if (state == State.NONE || state == State.DRAG || state == State.FLING) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        last.set(curr);
                        if (fling != null)
                            fling.cancelFling();
                        setState(State.DRAG);
                        break;

                    case MotionEvent.ACTION_MOVE:
                        if (state == State.DRAG) {
                            float deltaX = curr.x - last.x;
                            float deltaY = curr.y - last.y;
                            last.set(curr.x, curr.y);
                        }
                        break;

                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_POINTER_UP:
                        setState(State.NONE);
                        break;
                }
            }
            //
            // indicate event was handled
            //
            return true;
        }
    }

    /**
     * ScaleListener detects user two finger scaling and scales image.
     *
     * @author Ortiz
     */
    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector) {
            setState(State.ZOOM);
            return true;
        }

        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            scaleChild(detector.getScaleFactor(), detector.getFocusX(), detector.getFocusY(), true);
            //
            // OnTouchImageViewListener is set: TouchImageView pinch zoomed by user.
            //
            return true;
        }

        @Override
        public void onScaleEnd(ScaleGestureDetector detector) {
            super.onScaleEnd(detector);
            setState(State.NONE);
        }
    }

    private void scaleChild(double deltaScale, float focusX, float focusY, boolean stretchImageToSuper) {
        matrix.postScale((float) deltaScale, (float) deltaScale, focusX, focusY);
        invalidate();
    }


    /**
     * Gesture Listener detects a single click or long click and passes that on
     * to the view's listener.
     *
     * @author Ortiz
     */
    private class GestureListener extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            return performClick();
        }

        @Override
        public void onLongPress(MotionEvent e) {
            performLongClick();
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            if (fling != null) {
                //
                // If a previous fling is still active, it should be cancelled so that two flings
                // are not run simultaenously.
                //
                fling.cancelFling();
            }
            fling = new Fling((int) velocityX, (int) velocityY);
            compatPostOnAnimation(fling);
            return super.onFling(e1, e2, velocityX, velocityY);
        }

    }

    /**
     * Fling launches sequential runnables which apply
     * the fling graphic to the image. The values for the translation
     * are interpolated by the Scroller.
     *
     * @author Ortiz
     */
    private class Fling implements Runnable {

        CompatScroller scroller;
        int currX, currY;

        Fling(int velocityX, int velocityY) {
            setState(State.FLING);
            scroller = new CompatScroller(context);
            matrix.getValues(m);

            int startX = (int) m[Matrix.MTRANS_X];
            int startY = (int) m[Matrix.MTRANS_Y];
            int minX, maxX, minY, maxY;

            if (getImageWidth() > viewWidth) {
                minX = viewWidth - (int) getImageWidth();
                maxX = 0;

            } else {
                minX = maxX = startX;
            }

            if (getImageHeight() > viewHeight) {
                minY = viewHeight - (int) getImageHeight();
                maxY = 0;

            } else {
                minY = maxY = startY;
            }

            scroller.fling(startX, startY, (int) velocityX, (int) velocityY, minX, maxX, minY, maxY);
            currX = startX;
            currY = startY;
        }

        public void cancelFling() {
            if (scroller != null) {
                setState(State.NONE);
                scroller.forceFinished(true);
            }
        }

        @Override
        public void run() {
            //
            // OnTouchImageViewListener is set: TouchImageView listener has been flung by user.
            // Listener runnable updated with each frame of fling animation.
            //

            if (scroller.isFinished()) {
                scroller = null;
                return;
            }

            if (scroller.computeScrollOffset()) {
                int newX = scroller.getCurrX();
                int newY = scroller.getCurrY();
                int transX = newX - currX;
                int transY = newY - currY;
                currX = newX;
                currY = newY;
                matrix.postTranslate(transX, transY);
                compatPostOnAnimation(this);
            }
        }
    }

    private float getImageHeight() {
        return getChildAt(0).getHeight();
    }

    private float getImageWidth() {
        return getChildAt(0).getWidth();
    }

    private class CompatScroller {
        Scroller scroller;
        OverScroller overScroller;

        CompatScroller(Context context) {
            overScroller = new OverScroller(context);
        }

        void fling(int startX, int startY, int velocityX, int velocityY, int minX, int maxX, int minY, int maxY) {
            overScroller.fling(startX, startY, velocityX, velocityY, minX, maxX, minY, maxY);
        }

        void forceFinished(boolean finished) {
            overScroller.forceFinished(finished);
        }

        public boolean isFinished() {
            return overScroller.isFinished();
        }

        boolean computeScrollOffset() {
            overScroller.computeScrollOffset();
            return overScroller.computeScrollOffset();
        }

        int getCurrX() {
            return overScroller.getCurrX();
        }

        int getCurrY() {
            return overScroller.getCurrY();
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void compatPostOnAnimation(Runnable runnable) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            postOnAnimation(runnable);

        } else {
            postDelayed(runnable, 1000 / 60);
        }
    }

    private class ZoomVariables {
        float scale;
        float focusX;
        float focusY;
        ImageView.ScaleType scaleType;

        ZoomVariables(float scale, float focusX, float focusY) {
            this.scale = scale;
            this.focusX = focusX;
            this.focusY = focusY;
        }
    }
}

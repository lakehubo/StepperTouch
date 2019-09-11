package nl.dionsegijn.steppertouch;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import androidx.annotation.Nullable;

/**
 * 滑动选择 by lake
 */
public class ButtonPopProgressView extends View {

    private Paint linePaint, colorPaint, imgPaint, btnPaint, textPaint, btnTextPaint;
    private float width, height;
    private float centerX, baseLineY;
    private float btnW, btnH;
    private float popW, popH;
    private RectF lineRectF = new RectF();
    private RectF colorRectF = new RectF();
    private RectF btnRectF = new RectF();
    private RectF imgRectF = new RectF();
    private RectF btnImgRectF = new RectF();
    private float lineWidth;
    private float progress = 100f;
    private Bitmap popImg;
    private Bitmap btnImg;
    private float padding;
    private float distance;
    private long lastPassedEventTime = 0;
    private int minInterval = 100 / 60;//16ms
    private boolean canScroll = false;
    private PointF last = new PointF();
    private float btnTextHeight;
    public static final int TYPE_MID = 0;
    public static final int TYPE_NORMAL = 1;
    private int STYLE_TYPE = TYPE_MID; //TYPE_MID 中分模式 TYPE_NORMAL 正常模式
    private String unitStr = "%";//显示的单位字符
    private int MAX_VALUES = 100;//最大值
    private int UNIT_V = 1;//单位长度

    public ButtonPopProgressView(Context context) {
        this(context, null);
    }

    public ButtonPopProgressView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ButtonPopProgressView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initStyle(context, attrs, defStyleAttr);
        initPaint();
    }

    private void initStyle(Context context, AttributeSet attrs, int defStyleAttr) {
        final TypedArray attributes = context.getTheme().obtainStyledAttributes(attrs, R.styleable.PopProgressView, defStyleAttr, 0);
        try {
            if (attributes != null) {
                STYLE_TYPE = attributes.getInteger(R.styleable.PopProgressView_styleType, TYPE_MID);
                unitStr = attributes.getString(R.styleable.PopProgressView_style_unit);
                MAX_VALUES = attributes.getInteger(R.styleable.PopProgressView_max_values, 100);
                UNIT_V = attributes.getInteger(R.styleable.PopProgressView_unit_values, 1);
            }
        } finally {
            if (attributes != null) {
                attributes.recycle();
            }
        }
    }

    private void initPaint() {
        lineWidth = getResources().getDimension(R.dimen.dp_4);
        btnW = getResources().getDimension(R.dimen.dp_48);
        btnH = getResources().getDimension(R.dimen.dp_32);

        popW = getResources().getDimension(R.dimen.dp_88);
        popH = getResources().getDimension(R.dimen.dp_50);
        padding = getResources().getDimension(R.dimen.dp_16);


        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(getResources().getDimension(R.dimen.sp_18));
        textPaint.setTextAlign(Paint.Align.CENTER);

        btnTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        btnTextPaint.setColor(Color.BLACK);
        btnTextPaint.setTextSize(getResources().getDimension(R.dimen.sp_14));
        btnTextPaint.setTextAlign(Paint.Align.CENTER);

        linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        linePaint.setStyle(Paint.Style.FILL);
        linePaint.setColor(Color.parseColor("#f1eeee"));

        colorPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        colorPaint.setStyle(Paint.Style.FILL);
        colorPaint.setColor(Color.parseColor("#ffa200"));

        imgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        imgPaint.setFilterBitmap(true);

        btnPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        btnPaint.setStyle(Paint.Style.FILL);
        btnPaint.setColor(Color.parseColor("#ffffff"));
        btnPaint.setShadowLayer(lineWidth / 2, 0, 3, Color.parseColor("#2b000000"));

        popImg = BitmapFactory.decodeResource(getResources(), R.mipmap.img_devicesetting_adjust_show);
        btnImg = BitmapFactory.decodeResource(getResources(), R.mipmap.icon_slider_move);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int height = MeasureSpec.makeMeasureSpec((int) (btnH + popH + padding * 2), MeasureSpec.AT_MOST);//最小高度
        super.onMeasure(widthMeasureSpec, height);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        width = w - padding * 2 - btnW;
        height = h - padding * 2;
        centerX = (float) w / 2;
        baseLineY = h - btnH / 2 - padding - lineWidth / 2;

        lineRectF.left = centerX - width / 2;
        lineRectF.right = lineRectF.left + width;
        lineRectF.bottom = h - btnH / 2 - padding;
        lineRectF.top = lineRectF.bottom - lineWidth;

        distance = lineRectF.width() / 100f;

        Rect mBounds = new Rect();
        btnTextPaint.getTextBounds("0", 0, "0".length(), mBounds);
        btnTextHeight = mBounds.height();

        updateProgress(progress);
    }

    /**
     * 中分模式
     *
     * @param progress
     */
    public void updateSize(float progress) {
        float position = progress - 50;

        float progressDis = distance * position;

        colorRectF.left = position < 0 ? (centerX + progressDis) : centerX;
        colorRectF.right = position < 0 ? centerX : (centerX + progressDis);
        colorRectF.bottom = lineRectF.bottom;
        colorRectF.top = lineRectF.top;

        btnRectF.left = position < 0 ? (colorRectF.left - btnW / 2) : (colorRectF.right - btnW / 2);
        btnRectF.right = btnRectF.left + btnW;
        btnRectF.bottom = colorRectF.bottom + btnH / 2 - lineWidth / 2;
        btnRectF.top = btnRectF.bottom - btnH;

        updateImage();
    }

    /**
     * 正常模式
     *
     * @param progress
     */
    public void updateNormalSize(float progress) {
        float progressDis = distance * progress;

        colorRectF.left = lineRectF.left;
        colorRectF.right = colorRectF.left + progressDis;
        colorRectF.bottom = lineRectF.bottom;
        colorRectF.top = lineRectF.top;

        btnRectF.left = colorRectF.right - btnW / 2;
        btnRectF.right = btnRectF.left + btnW;
        btnRectF.bottom = colorRectF.bottom + btnH / 2 - lineWidth / 2;
        btnRectF.top = btnRectF.bottom - btnH;

        updateImage();
    }

    /**
     * 更新pop图片位置以及按钮的图片位置
     */
    private void updateImage() {
        imgRectF.bottom = btnRectF.top;
        imgRectF.top = imgRectF.bottom - popH;
        float centerX = btnRectF.left + btnW / 2;
        imgRectF.left = centerX - popW / 2;
        imgRectF.right = imgRectF.left + popW;

        btnImgRectF.left = btnRectF.left + 20;
        btnImgRectF.right = btnRectF.right - 20;
        btnImgRectF.top = btnRectF.top;
        btnImgRectF.bottom = btnRectF.bottom;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawRoundRect(lineRectF, lineWidth / 2, lineWidth / 2, linePaint);
        canvas.drawRoundRect(colorRectF, lineWidth / 2, lineWidth / 2, colorPaint);

        canvas.drawRoundRect(btnRectF, lineWidth / 2, lineWidth / 2, btnPaint);
        String pro = "";
        if (STYLE_TYPE == TYPE_MID)
            pro = String.format("%.1f", (progress - 50) / 50 * 20) + "%";
        if (STYLE_TYPE == TYPE_NORMAL)
            pro = (int) (progress / 100 * 100) + "%";
        if (canScroll) {
            canvas.drawBitmap(popImg, null, imgRectF, imgPaint);
            canvas.drawText(pro, imgRectF.centerX(), imgRectF.centerY() + 4, textPaint);
            canvas.drawBitmap(btnImg, null, btnImgRectF, imgPaint);
        } else {
            canvas.drawText(pro, btnRectF.centerX(), btnRectF.centerY() + btnTextHeight / 2, btnTextPaint);
        }
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getActionMasked();
        float x = event.getX();
        float y = event.getY();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                if (x > btnRectF.left && x < btnRectF.right && y > btnRectF.top && y < btnRectF.bottom) {
                    canScroll = true;
                    last.x = x;
                    last.y = y;
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (updateable() && canScroll) {
                    float transX = event.getX() - last.x;
                    float p = progress + transX / distance;
                    if (p < 0)
                        p = 0;
                    if (p > 100)
                        p = 100;
                    progress = p;
                    updateProgress(progress);
                    last.x = x;
                    last.y = y;
                }
                break;
            case MotionEvent.ACTION_UP:
                canScroll = false;
                break;
        }
        invalidate();
        return true;
    }

    private void updateProgress(float progress) {
        switch (STYLE_TYPE) {
            case TYPE_MID:
                updateSize(progress);
                break;
            case TYPE_NORMAL:
                updateNormalSize(progress);
                break;
        }
    }

    private boolean updateable() {
        long current = System.currentTimeMillis();
        if (current - lastPassedEventTime <= minInterval) {
            return false;
        }
        lastPassedEventTime = current;
        return true;
    }
}

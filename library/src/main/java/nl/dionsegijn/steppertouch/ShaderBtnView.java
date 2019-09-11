package nl.dionsegijn.steppertouch;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import androidx.annotation.Nullable;

public class ShaderBtnView extends View {
    private Paint imgPaint, shaderPaint, textPaint;
    private int btnW;
    private int btnH;
    private int btnType = 0;
    private int srcImg = -1;
    private int srcPressImg = -1;
    private String text = "";
    private float radius = 0;//弧角
    private RectF rectF = new RectF();
    private float centerX = 0;
    private float centerY = 0;
    private float shadowDx = 0f;
    private float shadowDy = 6f;
    private int solid = Color.WHITE;
    private int solidPress = Color.parseColor("#ffa200");
    private int shadowColor = Color.parseColor("#1a000000");
    private int textColor = Color.BLACK;
    private float shadowBottom = getResources().getDimension(R.dimen.dp_17);
    private float shadowLeft = getResources().getDimension(R.dimen.dp_11);
    private float shadowRight = getResources().getDimension(R.dimen.dp_11);
    private float shadowTop = getResources().getDimension(R.dimen.dp_5);
    private float corner = 0;//圆角按钮半径

    private float imgWidth = getResources().getDimension(R.dimen.dp_11);
    private float imgHeight = getResources().getDimension(R.dimen.dp_11);

    private float textSize = getResources().getDimension(R.dimen.sp_18);

    private Bitmap srcBitmap;
    private Bitmap srcpressBitmap;
    private Bitmap bmp;
    private Rect rect = new Rect();
    private Rect srcRect;

    private float circleRadius;

    private Rect viewRect = new Rect();

    private boolean isHasListener = false;

    public ShaderBtnView(Context context) {
        this(context, null);
    }

    public ShaderBtnView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ShaderBtnView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initAttrs(context, attrs);
        imgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        imgPaint.setFilterBitmap(true);

        shaderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        shaderPaint.setColor(solid);
        shaderPaint.setStyle(Paint.Style.FILL);
        shaderPaint.setShadowLayer(radius, shadowDx, shadowDy, shadowColor);

        textPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setTypeface(Typeface.DEFAULT);
        textPaint.setColor(textColor);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setTextSize(textSize);

    }

    private void initAttrs(Context context, AttributeSet attrs) {
        TypedArray styles = context.getTheme().obtainStyledAttributes(attrs, R.styleable.ShaderBtnView, 0, 0);
        try {
            btnType = styles.getInt(R.styleable.ShaderBtnView_btnType, 1);
            srcImg = styles.getResourceId(R.styleable.ShaderBtnView_imgSrc, -1);
            srcPressImg = styles.getResourceId(R.styleable.ShaderBtnView_imgSrc_press, -1);
            text = styles.getString(R.styleable.ShaderBtnView_text);
            radius = styles.getDimension(R.styleable.ShaderBtnView_radius, 0f);
            shadowColor = styles.getColor(R.styleable.ShaderBtnView_shadowColor, Color.parseColor("#1a000000"));
            solid = styles.getColor(R.styleable.ShaderBtnView_solid, Color.WHITE);
            solidPress = styles.getColor(R.styleable.ShaderBtnView_solid, Color.parseColor("#ffa200"));
            shadowDx = styles.getFloat(R.styleable.ShaderBtnView_shadowDx, 0f);
            shadowDy = styles.getFloat(R.styleable.ShaderBtnView_shadowDx, 6f);
            shadowLeft = styles.getDimension(R.styleable.ShaderBtnView_shadowBottom, getResources().getDimension(R.dimen.dp_11));
            shadowRight = styles.getDimension(R.styleable.ShaderBtnView_shadowBottom, getResources().getDimension(R.dimen.dp_11));
            shadowTop = styles.getDimension(R.styleable.ShaderBtnView_shadowBottom, getResources().getDimension(R.dimen.dp_5));
            shadowBottom = styles.getDimension(R.styleable.ShaderBtnView_shadowBottom, getResources().getDimension(R.dimen.dp_17));
            corner = styles.getDimension(R.styleable.ShaderBtnView_corner, getResources().getDimension(R.dimen.dp_4));

            imgWidth = styles.getDimension(R.styleable.ShaderBtnView_imgWidth, getResources().getDimension(R.dimen.dp_11));
            imgHeight = styles.getDimension(R.styleable.ShaderBtnView_imgHeight, getResources().getDimension(R.dimen.dp_11));

            textSize = styles.getDimension(R.styleable.ShaderBtnView_textSize, getResources().getDimension(R.dimen.sp_18));
            textColor = styles.getColor(R.styleable.ShaderBtnView_textColor, Color.BLACK);

        } finally {
            styles.recycle();
        }
        if (srcImg != -1) {
            srcBitmap = BitmapFactory.decodeResource(getResources(), srcImg);
            bmp = srcBitmap;
        }
        if (srcPressImg != -1) {
            srcpressBitmap = BitmapFactory.decodeResource(getResources(), srcPressImg);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onDetachedFromWindow() {
        if (srcBitmap != null) {
            srcBitmap.recycle();
        }
        if (srcpressBitmap != null) {
            srcpressBitmap.recycle();
        }
        super.onDetachedFromWindow();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        viewRect.left = 0;
        viewRect.right = w;
        viewRect.top = 0;
        viewRect.bottom = h;

        btnW = w;
        btnH = h;
        rectF.left = shadowLeft;
        rectF.top = shadowTop;
        rectF.right = w - shadowRight;
        rectF.bottom = h - shadowBottom;
        centerX = (float) w / 2;
        centerY = (float) h / 2;

        rect.left = (int) (centerX - imgWidth / 2);
        rect.right = (int) (centerX + imgWidth / 2);
        rect.top = (int) (centerY - imgHeight / 2);
        rect.bottom = (int) (centerY + imgHeight / 2);

        if (btnType == 1) {
            rect.left = (int) (centerX - shadowRight / 2 + shadowLeft / 2 - imgWidth / 2);
            rect.right = (int) (centerX - shadowRight / 2 + shadowLeft / 2 + imgWidth / 2);
            rect.top = (int) (centerY - shadowBottom / 2 + shadowTop / 2 - imgHeight / 2);
            rect.bottom = (int) (centerY - shadowBottom / 2 + shadowTop / 2 + imgHeight / 2);
        }

        int padding = Math.max(Math.max((int) shadowTop, (int) shadowLeft), Math.max((int) shadowRight, (int) shadowBottom));
        circleRadius = Math.min(btnH / 2 - padding, btnW / 2 - padding);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (btnType == 1) {//方形按钮
            if (corner != 0) {
                canvas.drawRoundRect(rectF, corner, corner, shaderPaint);
            } else {
                canvas.drawRect(rectF, shaderPaint);
            }
        }
        if (btnType == 0) {//圆形按钮
            canvas.drawCircle(centerX, centerY, circleRadius, shaderPaint);
        }
        if (srcImg != -1 && bmp != null) {//中心图片
            canvas.drawBitmap(bmp, null, rect, imgPaint);
        }
        if (!TextUtils.isEmpty(text)) {//文字
            canvas.drawText(text, centerX - shadowRight + shadowLeft, (float) getTextBaseY(viewRect, text), textPaint);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            updatePress(true);
        } else if (event.getAction() == MotionEvent.ACTION_UP) {
            updatePress(false);
        }
        if(!isHasListener)
            return true;
        return super.onTouchEvent(event);
    }

    @Override
    public void setOnClickListener(@Nullable OnClickListener l) {
        isHasListener = true;
        super.setOnClickListener(l);
    }

    /**
     * 更新点击
     *
     * @param down
     */
    private void updatePress(boolean down) {
        shaderPaint.setColor(down ? solidPress : solid);
        textPaint.setColor(down ? Color.WHITE : textColor);
        bmp = down ? srcpressBitmap : srcBitmap;
        invalidate();
    }


    private int getTextBaseY(Rect targetRect, String str) {
        if (btnType == 0) {
            Rect rect = new Rect();
            Paint.FontMetricsInt fmi = textPaint.getFontMetricsInt();
            textPaint.getTextBounds(str, 0, str.length(), rect);
            return (targetRect.bottom + targetRect.top - fmi.bottom - fmi.top) / 2;
        }
        Rect rect = new Rect();
        Paint.FontMetricsInt fmi = textPaint.getFontMetricsInt();
        textPaint.getTextBounds(str, 0, str.length(), rect);
        return (targetRect.bottom - (int) shadowBottom + targetRect.top + (int) shadowTop - fmi.bottom - fmi.top) / 2;
    }

}

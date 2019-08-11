package nl.dionsegijn.steppertouch;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;
import androidx.annotation.Nullable;

public class WindSpeedGroupView extends View {
    private Paint defaultPaint;
    private Paint colorPaint;
    private int btnNum = 4;
    private String[] values = new String[]{"高速", "中速", "低速", "停"};
    private int width;
    private int height;
    private float selectX, selectY;

    public WindSpeedGroupView(Context context) {
        this(context, null);
    }

    public WindSpeedGroupView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public WindSpeedGroupView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initPaint();
    }

    private void initPaint() {
        defaultPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        defaultPaint.setColor(Color.parseColor("#ffededed"));

        colorPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        width = w;
        height = h;
        LinearGradient linearGradient = new LinearGradient(0, height * 1 / 4, 0, height, Color.parseColor("#ffff863a"), Color.parseColor("#ffffc83b"), LinearGradient.TileMode.CLAMP);
        colorPaint.setShader(linearGradient);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawColor(Color.parseColor("#ffededed"));
        canvas.drawRect(0, 0 + height * 1 / 4, width, height, colorPaint);
    }
}

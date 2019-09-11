package nl.dionsegijn.steppertouch;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import androidx.annotation.Nullable;

/**
 * 网格线选择器
 */
public class GridLineView extends View implements MatrixObject{

    private static final int COUNT = 40;
    private Paint linePaint;
    private int width,height;
    private float disW,disH;
    private Matrix mMatrix;

    public GridLineView(Context context) {
        this(context, null);
    }

    public GridLineView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public GridLineView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initPaint();
    }

    private void initPaint() {
        linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        linePaint.setColor(Color.parseColor("#dedddc"));
        linePaint.setStrokeWidth(1f);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        width = w;
        height = h;
        disW = (float)width/COUNT;
        disH = (float)height/COUNT;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.setMatrix(mMatrix);
        for (int i = 0; i <= COUNT; i++) {
            canvas.drawLine(0 + i * disW, 0, 0 + i * disW, height, linePaint);
            canvas.drawLine(0, 0 + i * disH, width, 0 + i * disH, linePaint);
        }
    }

    @Override
    public void setMatrix(Matrix matrix) {
        // collapse null and identity to just null
        if (matrix != null && matrix.isIdentity()) {
            matrix = null;
        }

        // don't invalidate unless we're actually changing our matrix
        if (matrix == null && !mMatrix.isIdentity() ||
                matrix != null && !mMatrix.equals(matrix)) {
            mMatrix.set(matrix);
//            configureBounds();
            invalidate();
        }
    }
}

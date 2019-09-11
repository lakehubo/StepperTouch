package nl.dionsegijn.steppertouch;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.util.AttributeSet;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;

public class ScaleImageView extends AppCompatImageView implements MatrixObject {
    private Matrix mMatrix;
    public ScaleImageView(Context context) {
        super(context);
    }

    public ScaleImageView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public ScaleImageView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.setMatrix(mMatrix);
        super.onDraw(canvas);
    }

    @Override
    public void setMatrix(Matrix matrix) {
        mMatrix = matrix;
        invalidate();
    }
}

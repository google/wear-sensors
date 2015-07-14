package net.waynepiekarski.wearsensors;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

public class GraphView extends View {

    private Paint foreground;
    private Paint background;
    private Paint leader;
    private double mMax;
    private int step;
    private int stepAhead;
    private double array[];
    private Bitmap bitmap;
    private Canvas canvas;
    private Paint paint[];
    private int palette[] = { Color.RED, Color.GREEN, Color.BLUE, Color.CYAN, Color.MAGENTA, Color.YELLOW, Color.GRAY };

    public GraphView(Context context, AttributeSet attrs) {
        super(context, attrs);
        foreground = new Paint();
        foreground.setColor(Color.LTGRAY);
        background = new Paint();
        background.setColor(Color.BLACK);
        leader = new Paint();
        leader.setColor(Color.DKGRAY);
        paint = new Paint[palette.length];
        for (int i = 0; i < palette.length; i++) {
            paint[i] = new Paint();
            paint[i].setColor(palette[i]);
        }
        reset();
        step = 0;
        stepAhead = 1;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec){
        int parentWidth = MeasureSpec.getSize(widthMeasureSpec);
        int parentHeight = MeasureSpec.getSize(heightMeasureSpec);
        this.setMeasuredDimension(parentWidth, parentHeight);
    }

    @Override
    protected void onDraw(Canvas liveCanvas) {
        super.onDraw(canvas);

        if ((bitmap == null) || (bitmap.getWidth() != canvas.getWidth()) || (bitmap.getHeight() != canvas.getHeight())) {
            bitmap = Bitmap.createBitmap(liveCanvas.getWidth(), liveCanvas.getHeight(), Bitmap.Config.ARGB_8888);
            canvas = new Canvas(bitmap);
            canvas.drawColor(background.getColor());
            canvas.drawRect(0,0,canvas.getWidth()-1,0, foreground);
            canvas.drawRect(canvas.getWidth()-1,0,canvas.getWidth()-1,canvas.getHeight()-1, foreground);
            canvas.drawRect(canvas.getWidth()-1,canvas.getHeight()-1,0,canvas.getHeight()-1, foreground);
            canvas.drawRect(0, canvas.getHeight()-1, 0, 0, foreground);
        }

        // Clear out pixels on the current column before we draw here
        canvas.drawLine(step, 0, step, canvas.getHeight(), background);
        canvas.drawLine(stepAhead, 0, stepAhead, canvas.getHeight(), leader);

        // Plot the latest data at the current column
        if (array != null) {
            for (int i = 0; i < array.length; i++) {
                int x = step;
                int y = (int)(canvas.getHeight()/2.0 + array[i]/mMax*canvas.getHeight() / 2.0);

                canvas.drawLine(x, y - 1, x, y + 1, paint[i]);
            }
        }

        liveCanvas.drawBitmap(bitmap, 0, 0, foreground);

        step += 1;
        if (step > canvas.getWidth())
            step = 0;
        stepAhead += 1;
        if (stepAhead > canvas.getWidth())
            stepAhead = 0;
    }

    public void setSize(int length) {
        if ((array == null) || (length != array.length))
            array = new double[length];
    }

    public void setValues(float[] in) {
        if (in.length != array.length)
            Logging.fatal("Mismatch between incoming length " + array.length + " with existing " + in.length);
        for (int i = 0; i < in.length; i++)
            array[i] = in[i];
        invalidate();
    }

    public void setMaximum(double in) {
        if (mMax != in) {
            mMax = in;
            array = null;
            invalidate();
        }
    }

    public void reset() {
        array = null;
        step = 0;
        mMax = 1.0;
        invalidate();
    }
}

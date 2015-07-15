// ---------------------------------------------------------------------
// Copyright 2015 Google Inc. All Rights Reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//    http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//
// ---------------------------------------------------------------------

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
    private int stepNext;
    private int stepPrev;
    private double current[];
    private double prev[];
    private Bitmap bitmap;
    private Canvas canvas;
    private Paint paint[];
    private int palette[] = { Color.RED, Color.GREEN, Color.BLUE, Color.CYAN, Color.MAGENTA, Color.YELLOW };

    public GraphView(Context context, AttributeSet attrs) {
        super(context, attrs);
        foreground = new Paint();
        foreground.setColor(Color.LTGRAY);
        background = new Paint();
        background.setColor(Color.BLACK);
        leader = new Paint();
        leader.setColor(Color.DKGRAY);
        paint = new Paint[palette.length];
        if (palette.length != BaseActivity.MAX_SENSOR_VALUES)
            Logging.fatal("Internal limit palette " + palette.length + " does not match sensors " + BaseActivity.MAX_SENSOR_VALUES);
        for (int i = 0; i < palette.length; i++) {
            paint[i] = new Paint();
            paint[i].setColor(palette[i]);
        }
        reset();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec){
        int parentWidth = MeasureSpec.getSize(widthMeasureSpec);
        int parentHeight = MeasureSpec.getSize(heightMeasureSpec);
        this.setMeasuredDimension(parentWidth, parentHeight);
    }

    private void clearBitmap() {
        if ((bitmap != null) || (canvas != null)) {
            canvas.drawColor(background.getColor());
            canvas.drawRect(0, 0, canvas.getWidth() - 1, 0, foreground);
            canvas.drawRect(canvas.getWidth() - 1, 0, canvas.getWidth() - 1, canvas.getHeight() - 1, foreground);
            canvas.drawRect(canvas.getWidth() - 1, canvas.getHeight() - 1, 0, canvas.getHeight() - 1, foreground);
            canvas.drawRect(0, canvas.getHeight() - 1, 0, 0, foreground);
        }
    }

    @Override
    protected void onDraw(Canvas liveCanvas) {
        super.onDraw(canvas);

        if ((bitmap == null) || (bitmap.getWidth() != canvas.getWidth()) || (bitmap.getHeight() != canvas.getHeight())) {
            bitmap = Bitmap.createBitmap(liveCanvas.getWidth(), liveCanvas.getHeight(), Bitmap.Config.ARGB_8888);
            canvas = new Canvas(bitmap);
            clearBitmap();
        }

        // Clear out pixels on the current column before we draw here
        canvas.drawLine(step, 0, step, canvas.getHeight(), background);
        canvas.drawLine(stepNext, 0, stepNext, canvas.getHeight(), leader);

        // Plot the latest data at the current column
        if (current != null) {
            for (int i = 0; i < current.length; i++) {
                int x1 = stepPrev;
                int y1 = (int)(canvas.getHeight()/2.0 + prev[i]/mMax*canvas.getHeight() / 2.0);
                int x2 = step;
                int y2 = (int)(canvas.getHeight()/2.0 + current[i]/mMax*canvas.getHeight() / 2.0);

                // Only draw if there is no wrap-around
                if (x2 > x1)
                    canvas.drawLine(x1, y1, x2, y2, paint[i]);
            }
        }

        liveCanvas.drawBitmap(bitmap, 0, 0, foreground);

        step += 1;
        if (step > canvas.getWidth())
            step = 0;
        stepNext += 1;
        if (stepNext > canvas.getWidth())
            stepNext = 0;
        stepPrev += 1;
        if (stepPrev > canvas.getWidth())
            stepPrev = 0;

        // Save the current values as previous values for the next run
        double temp[] = prev;
        prev = current;
        current = temp;
    }

    public void setSize(int length) {
        if (length > palette.length)
            length = palette.length;
        if ((current == null) || (length != current.length)) {
            current = new double[length];
            prev = new double[length];
        }
    }

    static public int min(int a, int b) {
        if (a < b)
            return a;
        else
            return b;
    }

    public void setValues(float[] in) {
        if (min(in.length,palette.length) != current.length)
            Logging.fatal("Mismatch between incoming length " + current.length + " with existing " + in.length);
        for (int i = 0; i < min(in.length, palette.length); i++)
            current[i] = in[i];
        invalidate();
    }

    public void resetMaximum(double in) {
        reset();
        mMax = in;
        invalidate();
    }

    public void reset() {
        current = null;
        prev = null;
        step = 0;
        stepNext = step + 1;
        stepPrev = step - 1;
        mMax = 1.0;
        clearBitmap();
    }
}

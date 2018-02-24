package arc.oval.com.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import arc.oval.com.ovalarcscaleview.R;


public class OvalScaleView extends View {

    private Paint paint;
    private int radius;
    private int scaleTextSize = 45; // 刻度字体大小

    int rectX;  // 圆弧所在的矩形区域，右边距的坐标
    int startX; // 圆弧所在矩形区域，左边距的坐标
    int startY; //  上边距
    int rectY;  // 下边距

    float scaleMargin = 1.5f;

    private int scaleCount;     //总刻度数
    private int maxScale = 200; //最大刻度
    private int minScale = 30;  //最小刻度

    private int highScaleLineHeight = 75;   //10的倍数的刻度的高度。 eg:10,20,30
    private int lowScaleLineHeight = 45;    // 普通刻度的高度
    private int sweepAngle;     //圆弧扫过的角度
    private int startAngle;     //圆弧开始的角度
    private int dividerBetweenInnerAndOutOval;  // 内圆弧 和外圆弧 的间隔

    private RectF ovalOut;     // 外部圆弧的矩形区域
    private RectF ovalInner;    // 内部圆形的矩形区域
    private float changeAngle = 0f;     //第一个刻度开始，改变的刻度

    private OnRotateListener rotateListener;    // 监听扫过的刻度
    private int currentWeightScale;     //  当前的数值
    private int lastWeightScale;        //  上一个刻度的数值

    private boolean startToListenScroll; // 是否开始监听滚动的开关，这里我们设置为手动按下才开始监听，否则不监听。


    public OvalScaleView(Context context) {
        super(context);
    }

    public OvalScaleView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        paint = new Paint();
    }

    public int getMaxScale() {
        return maxScale;
    }

    public void setMaxScale(int maxScale) {
        this.maxScale = maxScale;
    }

    public int getMinScale() {
        return minScale;
    }

    public void setMinScale(int minScale) {
        this.minScale = minScale;
    }

    @Override
    protected void onDraw(Canvas canvas) {

        initPaint();

        initCircle();

        drawArc(canvas);

        drawScale(canvas);

        notifyListenerUpdate();
    }


    public void initPaint() {

        paint.setAntiAlias(true);
        paint.setDither(true);
        paint.setColor(getResources().getColor(R.color.paintColor));
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(0);

    }

    private void initCircle() {

        scaleCount = maxScale - minScale;

        lastWeightScale = minScale;

        //超出屏幕的距离，左右两边的距离相等
        int space = 800;

        rectX = getWidth() + space;
        startX = -space;
        startY = 0;

        rectY = rectX - startX;

        radius = (rectX - startX) / 2;

        startAngle = -90;
        sweepAngle = (int) (170 * scaleMargin);

        dividerBetweenInnerAndOutOval = 200;

        ovalOut = new RectF(startX, startY, rectX, rectY);
        ovalInner = new RectF(startX + dividerBetweenInnerAndOutOval, startY + dividerBetweenInnerAndOutOval,
                rectX - dividerBetweenInnerAndOutOval, rectY - dividerBetweenInnerAndOutOval);

    }

    private void drawArc(Canvas canvas) {


        canvas.drawArc(ovalOut, startAngle + changeAngle, sweepAngle, false, paint);

        canvas.drawArc(ovalInner, startAngle + changeAngle, sweepAngle, false, paint);
    }

    private void drawScale(Canvas canvas) {

        canvas.translate(getWidth() / 2, rectY / 2);
        paint.setStrokeWidth(1);
        canvas.rotate(changeAngle);

        for (int i = 0; i <= scaleCount; i++) {

            if (i % 5 == 0) {

                drawHighScaleLine(canvas);

                drawText(canvas, i);
            } else {

                canvas.drawLine(0, lowScaleLineHeight - radius, 0, -radius, paint);
            }

            canvas.rotate(scaleMargin);
        }
    }

    private void notifyListenerUpdate() {

        currentWeightScale = minScale + (int) (-changeAngle / scaleMargin);

        if (rotateListener != null && startToListenScroll) {

            int scale = currentWeightScale;

            if (currentWeightScale < minScale) {

                scale = minScale;
            } else if (currentWeightScale > maxScale) {

                scale = maxScale;
            }

            rotateListener.onRotateScroll(scale);
        }
    }

    private void drawHighScaleLine(Canvas canvas) {

        paint.setStyle(Paint.Style.STROKE);
        canvas.drawLine(0, highScaleLineHeight - radius, 0, -radius, paint);
    }

    private void drawText(Canvas canvas, int current) {

        int scale = minScale + current;
        paint.setTextSize(scaleTextSize);
        paint.setStyle(Paint.Style.FILL);
        Rect textBound = new Rect();

        paint.getTextBounds(scale + "", 0, (scale + "").length(), textBound);
        canvas.drawText(scale + "", -textBound.width() / 2, highScaleLineHeight + textBound.height() - radius, paint);
    }

    int lastXPosition = 0;

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        int x = (int) event.getX();

        switch (event.getAction()) {

            case MotionEvent.ACTION_DOWN:

                startToListenScroll = true;

                lastXPosition = x;

                return true;
            case MotionEvent.ACTION_MOVE:

                int distanceX = lastXPosition - x;

                if (currentWeightScale - lastWeightScale < 0) {

                    if (distanceX <= 0 && currentWeightScale <= minScale) {

                        return super.onTouchEvent(event);
                    }

                } else if (currentWeightScale - lastWeightScale > 0) {

                    if (distanceX >= 0 && currentWeightScale >= maxScale) {

                        return super.onTouchEvent(event);
                    }
                }

                if (Math.abs(distanceX) > 15) {

                    if (distanceX > 0) {

                        changeAngle -= scaleMargin;
                    } else {

                        changeAngle += scaleMargin;
                    }

                    lastXPosition = x;

                    postInvalidate();

                    lastWeightScale = currentWeightScale;
                }
                return true;
            case MotionEvent.ACTION_UP:

                if (currentWeightScale < minScale) {

                    changeAngle = 0;
                    currentWeightScale = minScale;

                } else if (currentWeightScale > maxScale) {

                    changeAngle = scaleCount * scaleMargin;
                    currentWeightScale = maxScale;
                }
                postInvalidate();

                lastXPosition = 0;

                return true;
        }


        return super.onTouchEvent(event);
    }

    public void setRotateListener(OnRotateListener listener) {
        rotateListener = listener;
    }

    public void setCurrentScale(int weight) {

        if (weight < minScale && weight > maxScale) {

            return;
        }

        currentWeightScale = weight;
        changeAngle = (minScale - weight) * scaleMargin;

        postInvalidate();
    }

    public interface OnRotateListener {
        void onRotateScroll(int scale);
    }

}

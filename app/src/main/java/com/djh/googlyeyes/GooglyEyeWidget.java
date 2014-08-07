package com.djh.googlyeyes;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;

/**
 * Created by dillonhodapp on 7/30/14.
 */
public class GooglyEyeWidget extends View implements SensorEventListener{

    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private long lastTime;
    private static final long THRESHOLD_TIME = 16; //milliseconds
    private static final int HANDLE_WIDTH = 60;
    private static final int MIN_WIDTH = 24;

    private Context mContext;
    private Paint mSclera;
    private Paint mPupil;
    private Paint mBoundingHandle;
    private Paint mBoundingBox;
    private Rect mBoundingRect = new Rect();
    private Rect mUpperLeftHandle = new Rect();
    private Rect mUpperRightHandle = new Rect();
    private Rect mLowerLeftHandle = new Rect();
    private Rect mLowerRightHandle = new Rect();
    private float unitX;
    private float unitY;

    private int boxWidth = 100;
    private int boxCornerX = 100;
    private int boxCornerY = 100;
    private Mode mMode;


    public enum Mode {
        DRAGGING,
        RESIZING_UPPER_LEFT,
        RESIZING_UPPER_RIGHT,
        RESIZING_LOWER_LEFT,
        RESIZING_LOWER_RIGHT,
        EDITING,
        PLACED
    }

    public GooglyEyeWidget(Context context) {
        super(context);
        mContext = context;
        init();
    }

    public GooglyEyeWidget(Context context, AttributeSet attrs) {
        super(context, attrs);

        mContext = context;
        init();
    }

    public GooglyEyeWidget(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        init();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int x = boxCornerX + (boxWidth + 2 * HANDLE_WIDTH) / 2;
        int y = boxCornerY + (boxWidth + 2 * HANDLE_WIDTH) / 2;
        int pupilRadius = (boxWidth * 2000) / 6000;

        if (mMode != Mode.PLACED) {
            //draw bounding box
            mBoundingRect.set(boxCornerX + HANDLE_WIDTH / 2, boxCornerY + HANDLE_WIDTH / 2, boxCornerX + HANDLE_WIDTH + boxWidth + HANDLE_WIDTH / 2, boxCornerY + HANDLE_WIDTH + boxWidth + HANDLE_WIDTH / 2);
            canvas.drawRect(mBoundingRect, mBoundingBox);
            //draw handles
            mUpperLeftHandle.set(boxCornerX, boxCornerY, boxCornerX + HANDLE_WIDTH, boxCornerY + HANDLE_WIDTH);
            mUpperRightHandle.set(boxCornerX + boxWidth + HANDLE_WIDTH, boxCornerY, boxCornerX + boxWidth + 2 * HANDLE_WIDTH, boxCornerY + HANDLE_WIDTH);
            mLowerLeftHandle.set(boxCornerX, boxCornerY + boxWidth + HANDLE_WIDTH, boxCornerX + HANDLE_WIDTH, boxCornerY + boxWidth + 2 * HANDLE_WIDTH);
            mLowerRightHandle.set(boxCornerX + boxWidth + HANDLE_WIDTH, boxCornerY + boxWidth + HANDLE_WIDTH, boxCornerX + boxWidth + 2 * HANDLE_WIDTH, boxCornerY + boxWidth + 2 * HANDLE_WIDTH);
            canvas.drawRect(mUpperLeftHandle, mBoundingHandle);
            canvas.drawRect(mUpperRightHandle, mBoundingHandle);
            canvas.drawRect(mLowerLeftHandle, mBoundingHandle);
            canvas.drawRect(mLowerRightHandle, mBoundingHandle);
        }

        //draw eyeball
        canvas.drawCircle(x, y, boxWidth / 2, mSclera);
        canvas.drawCircle(x + (unitX * (boxWidth / 2 - pupilRadius)),  y + (unitY * (boxWidth / 2 - pupilRadius)), pupilRadius, mPupil);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

    }

public boolean isTouchingBoundingBox(float eventX, float eventY) {
    if (eventX >= boxCornerX && eventX <= (boxCornerX + boxWidth + 2 * HANDLE_WIDTH)) {
        if (eventY >= boxCornerY && eventY <= (boxCornerY + boxWidth + 2 * HANDLE_WIDTH)) {
            if (!isTouchingUpperLeft(eventX, eventY) && !isTouchingUpperRight(eventX, eventY) && !isTouchingLowerLeft(eventX, eventY) && !isTouchingLowerRight(eventX,eventY)) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    } else {
        return false;
    }
}

    public boolean isTouchingUpperLeft(float eventX, float eventY) {
        if (eventX >= boxCornerX && eventX <= (boxCornerX + HANDLE_WIDTH)) {
            if (eventY >= boxCornerY && eventY <= (boxCornerY + HANDLE_WIDTH)) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    public boolean isTouchingUpperRight(float eventX, float eventY) {
        if (eventX >= (boxCornerX + boxWidth + HANDLE_WIDTH) && eventX <= (boxCornerX + boxWidth + 2 * HANDLE_WIDTH)) {
            if (eventY >= boxCornerY && eventY <= (boxCornerY + HANDLE_WIDTH)) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    public boolean isTouchingLowerLeft(float eventX, float eventY) {
        if (eventX >= boxCornerX && eventX <= (boxCornerX + HANDLE_WIDTH)) {
            if (eventY >= (boxCornerY + boxWidth + HANDLE_WIDTH) && eventY <= (boxCornerY + boxWidth + 2 * HANDLE_WIDTH)) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    public boolean isTouchingLowerRight(float eventX, float eventY) {
        if (eventX >= (boxCornerX + boxWidth + HANDLE_WIDTH) && eventX <= (boxCornerX + boxWidth + 2 * HANDLE_WIDTH)) {
            if (eventY >= (boxCornerY + boxWidth + HANDLE_WIDTH) && eventY <= (boxCornerY + boxWidth + 2 * HANDLE_WIDTH)) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    public void setMode(Mode mode) {
        mMode = mode;
    }

    public Mode getMode() {
        return mMode;
    }

    public void setDraggingCoords(int x, int y) {
        boxCornerX = boxCornerX + x;
        boxCornerY = boxCornerY + y;
        invalidate();
    }

    public void resizeUpperLeft(int delta) {
        if ((boxWidth - delta) > MIN_WIDTH) {
            boxCornerX = boxCornerX + delta;
            boxCornerY = boxCornerY + delta;
            boxWidth = boxWidth - delta;
        }
        invalidate();
    }

    public void resizeUpperRight(int delta) {
        if ((boxWidth + delta) > MIN_WIDTH) {
            boxWidth = boxWidth + delta;
            boxCornerY = boxCornerY - delta;
        }
        invalidate();
    }

    public void resizeLowerLeft(int delta) {

        if ((boxWidth - delta) > MIN_WIDTH) {
            boxWidth = boxWidth - delta;
            boxCornerX = boxCornerX + delta;
        }
        invalidate();
    }

    public void resizeLowerRight(int delta) {
        if ((boxWidth + delta) > MIN_WIDTH) {
            boxWidth = boxWidth + delta;
        }
        invalidate();
    }

    private void init() {
        mMode = Mode.EDITING;
        mSensorManager = (SensorManager) mContext.getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);

        mSclera = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPupil = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBoundingHandle = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBoundingBox = new Paint(Paint.ANTI_ALIAS_FLAG);

        mSclera.setColor(getResources().getColor(android.R.color.white));
        mPupil.setColor(getResources().getColor(android.R.color.black));
        mSclera.setStyle(Paint.Style.FILL);
        mPupil.setStyle(Paint.Style.FILL);
        mBoundingHandle.setColor(getResources().getColor(R.color.bounding_box_handle));
        mBoundingBox.setColor(getResources().getColor(R.color.bounding_box));
        mBoundingHandle.setStyle(Paint.Style.FILL);
        mBoundingBox.setStyle(Paint.Style.STROKE);


        invalidate();
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        Sensor mySensor = sensorEvent.sensor;

        if (mySensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            double x = sensorEvent.values[0];
            double y = sensorEvent.values[1];

            long currentTime = System.currentTimeMillis();

            if (currentTime - lastTime > THRESHOLD_TIME) {
                //get vector magnitude
                double magnitude = Math.sqrt((x * x) + (y * y));
                //create unit vector components
                x = x / magnitude;
                y  = y / magnitude;
                //scale vectors

                unitX = (float) -x;
                unitY = (float) y;

                lastTime = currentTime;
                invalidate();
            }

        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    public void flingOffScreen() {
        final int distance = 1000;
        ValueAnimator animator = ValueAnimator.ofFloat(0,1);
        animator.setDuration(300);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = ((Float) (animation.getAnimatedValue())).floatValue();
                boxCornerX = boxCornerX + (int)(value * distance);
                invalidate();
            }
        });
        animator.start();
    }

}

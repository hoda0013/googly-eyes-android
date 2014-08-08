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
import android.util.TypedValue;
import android.view.View;

/**
 * Created by dillonhodapp on 7/30/14.
 */
public class GooglyEyeWidget extends View implements SensorEventListener{

    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private long lastTime;
    private static final long THRESHOLD_TIME = 16; //milliseconds
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
    private Rect mDragHandleRight = new Rect();
    private Rect mDragHandleLeft = new Rect();
    private float unitX;
    private float unitY;

    private int boxWidth = 50;
    private int boxCornerX = 50;
    private int boxCornerY = 50;
    private int handleWidth = 24;
    private int dragHandleLength = 40;

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
        units();
    }

    public GooglyEyeWidget(Context context, AttributeSet attrs) {
        super(context, attrs);

        mContext = context;
        init();
        units();
    }

    public GooglyEyeWidget(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        init();
        units();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int x = boxCornerX + (boxWidth + 2 * handleWidth) / 2;
        int y = boxCornerY + (boxWidth + 2 * handleWidth) / 2;
        int pupilRadius = (boxWidth * 2000) / 6000;

        if (mMode != Mode.PLACED) {
            //draw bounding box
            mBoundingRect.set(boxCornerX + handleWidth / 2, boxCornerY + handleWidth / 2, boxCornerX + handleWidth + boxWidth + handleWidth / 2, boxCornerY + handleWidth + boxWidth + handleWidth / 2);
            canvas.drawRect(mBoundingRect, mBoundingBox);
            //draw handles
            mUpperLeftHandle.set(boxCornerX, boxCornerY, boxCornerX + handleWidth, boxCornerY + handleWidth);
            mUpperRightHandle.set(boxCornerX + boxWidth + handleWidth, boxCornerY, boxCornerX + boxWidth + 2 * handleWidth, boxCornerY + handleWidth);
            mLowerLeftHandle.set(boxCornerX, boxCornerY + boxWidth + handleWidth, boxCornerX + handleWidth, boxCornerY + boxWidth + 2 * handleWidth);
            mLowerRightHandle.set(boxCornerX + boxWidth + handleWidth, boxCornerY + boxWidth + handleWidth, boxCornerX + boxWidth + 2 * handleWidth, boxCornerY + boxWidth + 2 * handleWidth);
            canvas.drawRect(mUpperLeftHandle, mBoundingHandle);
            canvas.drawRect(mUpperRightHandle, mBoundingHandle);
            canvas.drawRect(mLowerLeftHandle, mBoundingHandle);
            canvas.drawRect(mLowerRightHandle, mBoundingHandle);
            mDragHandleLeft.set(boxCornerX - dragHandleLength, boxCornerY + ((handleWidth + handleWidth + boxWidth) / 2) - (handleWidth / 2), boxCornerX - dragHandleLength + handleWidth, boxCornerY + ((handleWidth + handleWidth + boxWidth) / 2) - (handleWidth / 2) + handleWidth);
            canvas.drawRect(mDragHandleLeft, mBoundingHandle);
            mDragHandleRight.set(boxCornerX + (2 * handleWidth) + boxWidth + dragHandleLength - handleWidth, boxCornerY + ((handleWidth + handleWidth + boxWidth) / 2) - (handleWidth / 2), boxCornerX + (2 * handleWidth) + boxWidth + dragHandleLength, boxCornerY + ((handleWidth + handleWidth + boxWidth) / 2) - (handleWidth / 2) + handleWidth);
            canvas.drawRect(mDragHandleRight, mBoundingHandle);
        }

        //draw eyeball
        canvas.drawCircle(x, y, boxWidth / 2, mSclera);
        canvas.drawCircle(x + (unitX * (boxWidth / 2 - pupilRadius)),  y + (unitY * (boxWidth / 2 - pupilRadius)), pupilRadius, mPupil);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

    }

    private void units() {
        boxCornerX = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, boxCornerX, getResources().getDisplayMetrics());
        boxCornerY = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, boxCornerY, getResources().getDisplayMetrics());
        boxWidth = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, boxWidth, getResources().getDisplayMetrics());
        handleWidth = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, handleWidth, getResources().getDisplayMetrics());
        dragHandleLength = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dragHandleLength, getResources().getDisplayMetrics());

    }

    public boolean isTouchingBoundingBox(float eventX, float eventY) {
        if (eventX >= boxCornerX && eventX <= (boxCornerX + boxWidth + 2 * handleWidth)) {
            if (eventY >= boxCornerY && eventY <= (boxCornerY + boxWidth + 2 * handleWidth)) {
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

    public boolean isTouchingDragHandle(float eventX, float eventY) {
        if (mDragHandleLeft.contains((int)eventX, (int)eventY) || mDragHandleRight.contains((int)eventX, (int)eventY)) {
            return true;
        }
        return false;
    }

    public boolean isTouchingUpperLeft(float eventX, float eventY) {
        if (eventX >= boxCornerX && eventX <= (boxCornerX + handleWidth)) {
            if (eventY >= boxCornerY && eventY <= (boxCornerY + handleWidth)) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    public boolean isTouchingUpperRight(float eventX, float eventY) {
        if (eventX >= (boxCornerX + boxWidth + handleWidth) && eventX <= (boxCornerX + boxWidth + 2 * handleWidth)) {
            if (eventY >= boxCornerY && eventY <= (boxCornerY + handleWidth)) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    public boolean isTouchingLowerLeft(float eventX, float eventY) {
        if (eventX >= boxCornerX && eventX <= (boxCornerX + handleWidth)) {
            if (eventY >= (boxCornerY + boxWidth + handleWidth) && eventY <= (boxCornerY + boxWidth + 2 * handleWidth)) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    public boolean isTouchingLowerRight(float eventX, float eventY) {
        if (eventX >= (boxCornerX + boxWidth + handleWidth) && eventX <= (boxCornerX + boxWidth + 2 * handleWidth)) {
            if (eventY >= (boxCornerY + boxWidth + handleWidth) && eventY <= (boxCornerY + boxWidth + 2 * handleWidth)) {
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

    public void flingOffScreen(final boolean isFlingLeft, final MainActivity.Listener listener) {
        final int distance = 1000;
        ValueAnimator animator = ValueAnimator.ofFloat(0,1);
        animator.setDuration(300);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = ((Float) (animation.getAnimatedValue())).floatValue();

                if (isFlingLeft) {
                    boxCornerX = boxCornerX - (int) (value * distance);

                } else {
                    boxCornerX = boxCornerX + (int) (value * distance);
                }
                invalidate();
            }
        });
        animator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                Log.e("DONE", "DONE");
                listener.animationFinished();
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        animator.start();
    }

}

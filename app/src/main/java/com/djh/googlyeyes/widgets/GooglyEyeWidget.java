package com.djh.googlyeyes.widgets;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import com.djh.googlyeyes.R;

/**
 * Created by dillonhodapp on 7/30/14.
 */
public class GooglyEyeWidget extends View implements SensorEventListener{

    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private long lastTime;
    private static final long THRESHOLD_TIME = 16; //milliseconds

    private Context mContext;
    private GooglyEyeWidget thisEye = this;
    private Paint mSclera;
    private Paint mPupil;
    private Paint mBoundingHandle;
    private Paint mBoundingBox;
    private Paint mClearPaint;
    private Rect mBoundingRect = new Rect();
    private Rect mLowerRightHandle = new Rect();
    private Path mLowerRightTriangle = new Path();
    private Matrix translateMatrix = new Matrix();
    private float unitX;
    private float unitY;
    private GestureDetector gestureDetector;
    private int id;
    private Listener mListener;

    private int boxWidth = 100;
    private int boxCornerX = 50;
    private int boxCornerY = 50;
    private int handleWidth = 24;
    private int MIN_WIDTH = 0;
    private int scleraRadius;
    private int pupilRadius;
    private Mode mMode;

    public interface Listener {
        public void removeView(GooglyEyeWidget eye);
        public void updateVals(double x, double z);
    }

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
        units();
        init();
    }

    public GooglyEyeWidget(Context context, int nextSize) {
        super(context);
        mContext = context;
        units();
        init();
        boxWidth = nextSize;
    }

    public GooglyEyeWidget(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        units();
        init();
    }

    public GooglyEyeWidget(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        units();
        init();
    }

    public void setListener(Listener listener) {
        mListener = listener;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int x = boxCornerX + (boxWidth / 2);
        int y = boxCornerY + (boxWidth / 2);
        scleraRadius = (boxWidth - (handleWidth)) / 2;
        pupilRadius = (scleraRadius * 2000) / 3000;

        if (mMode != Mode.PLACED) {
            //draw bounding box
            mBoundingRect.set(boxCornerX, boxCornerY, boxCornerX + boxWidth, boxCornerY + boxWidth);
            canvas.drawRect(mBoundingRect, mBoundingBox);
            //draw touch box in lower right corner
            mLowerRightHandle.set(boxCornerX + boxWidth - handleWidth, boxCornerY + boxWidth - handleWidth, boxCornerX + boxWidth, boxCornerY + boxWidth);
            canvas.drawRect(mLowerRightHandle, mClearPaint);
            //draw triangle in lower right corner
            mLowerRightTriangle.reset();
            updatePath();
            canvas.drawPath(mLowerRightTriangle, mBoundingHandle);
        }

        //draw eyeball
        canvas.drawCircle(x, y, scleraRadius, mSclera);
        canvas.drawCircle(x + (unitX * (scleraRadius - pupilRadius)),  y + (unitY * (scleraRadius - pupilRadius)), pupilRadius, mPupil);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    private void updatePath() {

        mLowerRightTriangle.moveTo(boxCornerX + boxWidth, boxCornerY + boxWidth);
        mLowerRightTriangle.lineTo(boxCornerX + boxWidth, boxCornerY + boxWidth - handleWidth);
        mLowerRightTriangle.lineTo(boxCornerX + boxWidth - handleWidth, boxCornerY + boxWidth);
        mLowerRightTriangle.lineTo(boxCornerX + boxWidth, boxCornerY + boxWidth);
        mLowerRightTriangle.close();
    }

    private void units() {
        boxCornerX = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, boxCornerX, getResources().getDisplayMetrics());
        boxCornerY = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, boxCornerY, getResources().getDisplayMetrics());
        boxWidth = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, boxWidth, getResources().getDisplayMetrics());
        handleWidth = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, handleWidth, getResources().getDisplayMetrics());
        MIN_WIDTH = 2 * handleWidth;
    }

    public boolean isTouchingDragPoint(float eventX, float eventY) {
        if (mBoundingRect.contains((int)eventX, (int)eventY) && !mLowerRightHandle.contains((int)eventX, (int)eventY)) {
            return true;
        }
        return false;
    }

    public boolean isTouchingSclera(float eventX, float eventY) {

        if (eventX >= (boxCornerX + (boxWidth / 2) - scleraRadius) && eventX <= (boxCornerX + (boxWidth / 2) + scleraRadius)) {
            if (eventY >= (boxCornerY + (boxWidth / 2) - scleraRadius) && eventY <= (boxCornerY + (boxWidth / 2) + scleraRadius)) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    public boolean isTouchingResizer(float eventX, float eventY) {
        if (mLowerRightHandle.contains((int)eventX, (int)eventY)) {
            return true;
        }
        return false;
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

    public void resizeLowerRight(int delta) {
        if ((boxWidth + delta) >= MIN_WIDTH) {
            boxWidth = boxWidth + delta;
        }
        invalidate();
    }

    float tempX = 0.0f;
    float tempY = 0.0f;

    @Override
    public boolean onTouchEvent(MotionEvent event) {

            Log.e(String.format("BOX: %s", this.getId()), "TRUE");
            float eventX = event.getX();
            float eventY = event.getY();

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    tempX = eventX;
                    tempY = eventY;

                    Log.e("XTOUCH", String.valueOf(eventX));
                    Log.e("YTOUCH", String.valueOf(eventY));

                    if (isTouchingResizer(eventX, eventY) && getMode() != GooglyEyeWidget.Mode.PLACED) {
                        setMode(GooglyEyeWidget.Mode.RESIZING_LOWER_RIGHT);
                        gestureDetector.onTouchEvent(event);
                    } else if (isTouchingDragPoint(eventX, eventY) && getMode() != GooglyEyeWidget.Mode.PLACED) {
                        setMode(GooglyEyeWidget.Mode.DRAGGING);
                        gestureDetector.onTouchEvent(event);
                    } else if (isTouchingSclera(eventX, eventY)) {
                        setMode(GooglyEyeWidget.Mode.DRAGGING);
                        Optometrist.INSTANCE.getFocus(getId());
                        gestureDetector.onTouchEvent(event);
                    } else {
                        //touch event did not hit the eye
                        setMode(GooglyEyeWidget.Mode.PLACED);
                        return false;
                    }
                    return true;

                case MotionEvent.ACTION_MOVE:
                    if (getMode() == GooglyEyeWidget.Mode.DRAGGING) {
                        int deltaX = (int) (eventX - tempX);
                        int deltaY = (int) (eventY - tempY);

                        setDraggingCoords(deltaX, deltaY);

                        tempX = eventX;
                        tempY = eventY;
                        gestureDetector.onTouchEvent(event);
                        return true;
                    } else if (getMode() == GooglyEyeWidget.Mode.RESIZING_LOWER_RIGHT) {
                        int deltaX = (int) (eventX - tempX);
                        int deltaY = (int) (eventY - tempY);
                        int delta = 0;

                        if (Math.abs(deltaX) > Math.abs(deltaY)) {
                            delta = deltaY;
                        } else {
                            delta = deltaX;
                        }
                        resizeLowerRight(delta);
                        tempX = eventX;
                        tempY = eventY;
                        gestureDetector.onTouchEvent(event);
                        return true;
                    } else {
                        return false;
                    }
                case MotionEvent.ACTION_UP:
                    setMode(GooglyEyeWidget.Mode.EDITING);
                    gestureDetector.onTouchEvent(event);
                    return true;

                default:
                    return false;
            }


    }

    class MyGestureDetector extends GestureDetector.SimpleOnGestureListener {
        private static final int SWIPE_MIN_DISTANCE = 90;
        private static final int SWIPE_MAX_OFF_PATH = 100;
        private static final int SWIPE_THRESHOLD_VELOCITY = 2000;

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            try {
                if (Math.abs(e1.getY() - e2.getY()) > SWIPE_MAX_OFF_PATH)
                    return false;
                // right to left swipe
                if (e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                    Toast.makeText(mContext, getResources().getString(R.string.eye_deleted), Toast.LENGTH_SHORT).show();
                    flingOffScreen(true);
                } else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                    Toast.makeText(mContext, getResources().getString(R.string.eye_deleted), Toast.LENGTH_SHORT).show();
                    flingOffScreen(false);
                }
            } catch (Exception e) {

            }
            return false;
        }

        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }
    }

    private void init() {
        id = this.getId();
        gestureDetector = new GestureDetector(mContext, new MyGestureDetector());
        mMode = Mode.EDITING;
        mSensorManager = (SensorManager) mContext.getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);

        mSclera = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPupil = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBoundingHandle = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBoundingBox = new Paint(Paint.ANTI_ALIAS_FLAG);
        mClearPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mClearPaint.setColor(getResources().getColor(R.color.transparent));
        mSclera.setColor(getResources().getColor(android.R.color.white));
        mPupil.setColor(getResources().getColor(android.R.color.black));
        mSclera.setStyle(Paint.Style.FILL);
        mPupil.setStyle(Paint.Style.FILL);
        mBoundingHandle.setColor(getResources().getColor(R.color.bounding_box_handle));
        mBoundingBox.setColor(getResources().getColor(R.color.bounding_box));
        mBoundingHandle.setStyle(Paint.Style.FILL);
        mBoundingBox.setStyle(Paint.Style.FILL_AND_STROKE);

        invalidate();
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        Sensor mySensor = sensorEvent.sensor;

        if (mySensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            double x = sensorEvent.values[0];
            double y = sensorEvent.values[1];
            double z = sensorEvent.values[2];
            z = z - 4;
            mListener.updateVals(x, z);

            long currentTime = System.currentTimeMillis();

            if (currentTime - lastTime > THRESHOLD_TIME) {
                //get vector magnitude
                double magnitude = Math.sqrt((x * x) + (z * z));
                //create unit vector components
                x = x / magnitude;
                z  = z / magnitude;
                //scale vectors

                unitX = (float) -x;
                unitY = (float) -z;
                lastTime = currentTime;
                invalidate();
            }

        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    public void flingOffScreen(final boolean isFlingLeft) {
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
                mListener.removeView(thisEye);
                Optometrist.INSTANCE.removeEye(thisEye);
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

    public int getBoxWidth() {
        return boxWidth;
    }

}

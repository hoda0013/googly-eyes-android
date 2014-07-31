package com.djh.googlyeyes;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by dillonhodapp on 7/30/14.
 */
public class GooglyEyeWidget extends View implements SensorEventListener{

    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private long lastTime;
    private static final long THRESHOLD_TIME = 16; //milliseconds
    private static final float SCLERA_RADIUS = 100;
    private static final float PUPIL_RADIUS = 80;


    private Paint mSclera;
    private Paint mPupil;
    private Context mContext;
    private float unitX;
    private float unitY;

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

        int x = getMeasuredWidth() / 2;
        int y = getMeasuredHeight() / 2;
        int scleraRadius = x;
        int pupilRadius = (scleraRadius * 2000) / 3000;


        canvas.drawCircle(x, y, scleraRadius, mSclera);
        canvas.drawCircle(x + (unitX * (scleraRadius - pupilRadius)),  y + (unitY * (scleraRadius - pupilRadius)), pupilRadius, mPupil);

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

    }

    private void init() {
        mSensorManager = (SensorManager) mContext.getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);

        mSclera = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPupil = new Paint(Paint.ANTI_ALIAS_FLAG);
        mSclera.setColor(getResources().getColor(android.R.color.white));
        mPupil.setColor(getResources().getColor(android.R.color.black));
        mSclera.setStyle(Paint.Style.FILL);
        mPupil.setStyle(Paint.Style.FILL);
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
//                unitX = (float) (-x * (SCLERA_RADIUS - PUPIL_RADIUS + 1));
//                unitY = (float) (y * (SCLERA_RADIUS - PUPIL_RADIUS + 1));

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


}

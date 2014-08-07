package com.djh.googlyeyes;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewManager;
import android.widget.AbsoluteLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends Activity implements View.OnClickListener{

    RelativeLayout mContainer;
    private Context mContext;
    private int eyeCounter = 0;
    private GooglyEyeWidget theEye = null;

    private List<GooglyEyeWidget> listGooglyEyes = new ArrayList<GooglyEyeWidget>();
    private static final int SWIPE_MIN_DISTANCE = 90;
    private static final int SWIPE_MAX_OFF_PATH = 100;
    private static final int SWIPE_THRESHOLD_VELOCITY = 2000;
    private GestureDetector gestureDetector;

    View.OnTouchListener gestureListener = new View.OnTouchListener() {
        public boolean onTouch(View v, MotionEvent event) {

            float eventX = event.getX();
            float eventY = event.getY();

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    tempX = eventX;
                    tempY = eventY;

                    Log.e("XTOUCH", String.valueOf(eventX));
                    Log.e("YTOUCH", String.valueOf(eventY));

                    for (GooglyEyeWidget eye : listGooglyEyes) {
                        if (eye.isTouchingUpperLeft(eventX, eventY)) {
                            theEye = eye;
                            theEye.setMode(GooglyEyeWidget.Mode.RESIZING_UPPER_LEFT);
                            clearOtherEyes(theEye.getId());
                        } else if (eye.isTouchingUpperRight(eventX, eventY)) {
                            theEye = eye;
                            theEye.setMode(GooglyEyeWidget.Mode.RESIZING_UPPER_RIGHT);
                            clearOtherEyes(theEye.getId());
                        } else if(eye.isTouchingLowerLeft(eventX, eventY)) {
                            theEye = eye;
                            theEye.setMode(GooglyEyeWidget.Mode.RESIZING_LOWER_LEFT);
                            clearOtherEyes(theEye.getId());
                        } else if(eye.isTouchingLowerRight(eventX, eventY)) {
                            theEye = eye;
                            theEye.setMode(GooglyEyeWidget.Mode.RESIZING_LOWER_RIGHT);
                            clearOtherEyes(theEye.getId());
                        } else if(eye.isTouchingBoundingBox(eventX, eventY)){
                            theEye = eye;
                            theEye.setMode(GooglyEyeWidget.Mode.DRAGGING);
                            clearOtherEyes(theEye.getId());
                        } else {
                            //touch event did not hit the eye
                            eye.setMode(GooglyEyeWidget.Mode.PLACED);
                            if (theEye != null) {
                                if (eye.getId() == theEye.getId()) {
                                    theEye = null;
                                }
                            }
                        }
                    }

                    break;

                case MotionEvent.ACTION_MOVE:
                    if (theEye != null && theEye.getMode() == GooglyEyeWidget.Mode.DRAGGING) {
                        int deltaX = (int) (eventX - tempX);
                        int deltaY = (int) (eventY - tempY);

                        theEye.setDraggingCoords(deltaX, deltaY);

                        tempX = eventX;
                        tempY = eventY;
                    } else if (theEye != null && theEye.getMode() == GooglyEyeWidget.Mode.RESIZING_UPPER_LEFT) {
                        int deltaX = (int) (eventX - tempX);
                        int deltaY = (int) (eventY - tempY);
                        int delta = 0;

                        if (Math.abs(deltaX) > Math.abs(deltaY)) {
                            delta = deltaY;
                        } else {
                            delta = deltaX;
                        }

                        theEye.resizeUpperLeft(delta);
                        tempX = eventX;
                        tempY = eventY;
                    } else if (theEye != null && theEye.getMode() == GooglyEyeWidget.Mode.RESIZING_UPPER_RIGHT) {
                        int deltaX = (int) (eventX - tempX);
                        int deltaY = (int) (eventY + tempY);
                        int delta = 0;

                        if (Math.abs(deltaX) > Math.abs(deltaY)) {
                            delta = deltaY;
                        } else {
                            delta = deltaX;
                        }

                        theEye.resizeUpperRight(delta);
                        tempX = eventX;
                        tempY = eventY;
                    } else if (theEye != null && theEye.getMode() == GooglyEyeWidget.Mode.RESIZING_LOWER_LEFT) {
                        int deltaX = (int) (eventX - tempX);
                        int deltaY = (int) (eventY + tempY);
                        int delta = 0;

                        if (Math.abs(deltaX) > Math.abs(deltaY)) {
                            delta = deltaY;
                        } else {
                            delta = deltaX;
                        }
                        theEye.resizeLowerLeft(delta);
                        tempX = eventX;
                        tempY = eventY;
                    } else if (theEye != null && theEye.getMode() == GooglyEyeWidget.Mode.RESIZING_LOWER_RIGHT) {
                        int deltaX = (int) (eventX - tempX);
                        int deltaY = (int) (eventY - tempY);
                        int delta = 0;

                        if (Math.abs(deltaX) > Math.abs(deltaY)) {
                            delta = deltaY;
                        } else {
                            delta = deltaX;
                        }
                        theEye.resizeLowerRight(delta);
                        tempX = eventX;
                        tempY = eventY;
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    if (theEye != null) {
                        theEye.setMode(GooglyEyeWidget.Mode.EDITING);
                    }
                    break;
            }
            return gestureDetector.onTouchEvent(event);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContainer = (RelativeLayout) findViewById(R.id.container);
        mContext = this;
        gestureDetector = new GestureDetector(this, new MyGestureDetector());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.plus) {
            //If there's an eye in focus, change it to PLACED
            if (theEye != null) {
                theEye.setMode(GooglyEyeWidget.Mode.PLACED);
            }
            //create new eye and set listeners
            theEye = new GooglyEyeWidget(this);
            theEye.setId(eyeCounter++);
            theEye.setOnClickListener(this);
            theEye.setOnTouchListener(gestureListener);
            listGooglyEyes.add(theEye);
            mContainer.addView(theEye);
            for (int i = 0; i < listGooglyEyes.size() - 1; i++) {
                listGooglyEyes.get(i).setMode(GooglyEyeWidget.Mode.PLACED);
            }
            theEye.setMode(GooglyEyeWidget.Mode.EDITING);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    float tempX = 0.0f;
    float tempY = 0.0f;

    @Override
    public void onClick(View v) {
    }

    class MyGestureDetector extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            try {
                if (Math.abs(e1.getY() - e2.getY()) > SWIPE_MAX_OFF_PATH)
                    return false;
                // right to left swipe
                if(e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                    Toast.makeText(mContext, getString(R.string.eye_deleted), Toast.LENGTH_SHORT).show();
                    theEye.flingOffScreen();
//                    mContainer.removeView(theEye);
//                    listGooglyEyes.remove(theEye);

                }  else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                    Toast.makeText(mContext, getString(R.string.eye_deleted), Toast.LENGTH_SHORT).show();
                    theEye.flingOffScreen();
//                    mContainer.removeView(theEye);
//                    listGooglyEyes.remove(theEye);
                }

            } catch (Exception e) {
                // nothing
            }
            return false;
        }

        @Override
        public boolean onDown(MotionEvent e) {

            return true;
        }
    }

    private void clearOtherEyes(int id) {
        for (GooglyEyeWidget eye : listGooglyEyes) {
            if (eye.getId() != id) {
                eye.setMode(GooglyEyeWidget.Mode.PLACED);
            }
        }
    }
}

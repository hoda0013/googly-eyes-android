package com.djh.googlyeyes.widgets;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

import com.djh.googlyeyes.R;

/**
 * Created by dillonhodapp on 8/13/14.
 */
public class CropWidget extends View {

    private Context mContext;

    private static final int HANDLE_WIDTH = 60;
    private int MIN_WIDTH = 24;

    private Paint mBoundingHandle;
    private Paint mBoundingBox;
    private Rect mBoundingRect = new Rect();
    private Rect mUpperLeftHandle = new Rect();
    private Rect mUpperRightHandle = new Rect();
    private Rect mLowerLeftHandle = new Rect();
    private Rect mLowerRightHandle = new Rect();

    private int mScreenWidth = 0;
    private int mScreenHeight = 0;

    private int boxWidth = 100;
    private int boxHeight = 100;
    private int boxCornerX = 100;
    private int boxCornerY = 100;
    private int handleWidth = 24;


    public CropWidget(Context context) {
        super(context);
        mContext = context;
        units();
        init();
    }

    public CropWidget(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        units();
        init();
    }

    public CropWidget(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        units();
        init();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int x = boxCornerX + (boxWidth + 2 * HANDLE_WIDTH) / 2;
        int y = boxCornerY + (boxWidth + 2 * HANDLE_WIDTH) / 2;

        //draw bounding box
        mBoundingRect.set(boxCornerX, boxCornerY, boxCornerX + boxWidth, boxCornerY + boxHeight);
        canvas.drawRect(mBoundingRect, mBoundingBox);
        //draw handles
        mUpperLeftHandle.set(boxCornerX, boxCornerY, boxCornerX + HANDLE_WIDTH, boxCornerY + HANDLE_WIDTH);
        mUpperRightHandle.set(boxCornerX + boxWidth - HANDLE_WIDTH, boxCornerY, boxCornerX + boxWidth, boxCornerY + HANDLE_WIDTH);
        mLowerLeftHandle.set(boxCornerX, boxCornerY + boxHeight - HANDLE_WIDTH, boxCornerX + HANDLE_WIDTH, boxCornerY + boxHeight);
        mLowerRightHandle.set(boxCornerX + boxWidth - HANDLE_WIDTH, boxCornerY + boxHeight - HANDLE_WIDTH, boxCornerX + boxWidth, boxCornerY + boxHeight);
        canvas.drawRect(mUpperLeftHandle, mBoundingHandle);
        canvas.drawRect(mUpperRightHandle, mBoundingHandle);
        canvas.drawRect(mLowerLeftHandle, mBoundingHandle);
        canvas.drawRect(mLowerRightHandle, mBoundingHandle);
    }

    private void units() {
        boxCornerX = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, boxCornerX, getResources().getDisplayMetrics());
        boxCornerY = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, boxCornerY, getResources().getDisplayMetrics());
        boxWidth = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, boxWidth, getResources().getDisplayMetrics());
        handleWidth = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, handleWidth, getResources().getDisplayMetrics());
        MIN_WIDTH = 2 * handleWidth;
    }

    private void init() {
        mBoundingHandle = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBoundingBox = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBoundingHandle.setColor(getResources().getColor(R.color.bounding_box_handle));
        mBoundingBox.setColor(getResources().getColor(R.color.bounding_box));
        mBoundingHandle.setStyle(Paint.Style.FILL);
        mBoundingBox.setStyle(Paint.Style.STROKE);
        invalidate();
    }

    private void initBox() {
        boxCornerX = 0;
        boxCornerY = 0;
        boxWidth = mScreenWidth;
        boxHeight = mScreenHeight;
        invalidate();
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

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        int width = 0;
        int height = 0;

        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

//        //Measure Width
//        if (widthMode == MeasureSpec.EXACTLY) {
//            //Must be this size
//            width = widthSize;
//        } else if (widthMode == MeasureSpec.AT_MOST) {
//            //Can't be bigger than...
////            width = Math.min(desiredWidth, widthSize);
//        } else {
//            //Be whatever you want
////            width = desiredWidth;
//        }
//
//        //Measure Height
//        if (heightMode == MeasureSpec.EXACTLY) {
//            //Must be this size
//            height = heightSize;
//        } else if (heightMode == MeasureSpec.AT_MOST) {
//            //Can't be bigger than...
////            height = Math.min(desiredHeight, heightSize);
//        } else {
//            //Be whatever you want
////            height = desiredHeight;
//        }

        //MUST CALL THIS
        setMeasuredDimension(width, height);
        mScreenWidth = widthSize;
        mScreenHeight = heightSize;
        initBox();
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
}

package org.amongothers.slidingmenu;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.support.v4.view.MotionEventCompat;
import android.util.AttributeSet;
import android.view.DragEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

/**
 * Created by Administrator on 2014/8/3.
 */
public class SlidingMenuLayout extends RelativeLayout {
  static final int INVALID_POINTER = -1;

  View mContent;
  View mMenu;
  MenuLayout mMenuContainer;

  public SlidingMenuLayout(Context context) {
    super(context);
  }

  public SlidingMenuLayout(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public SlidingMenuLayout(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
  }

  public void init(Activity activity, View menu, int widthOffset) {
    TypedArray a = activity.getTheme().obtainStyledAttributes(new int[]{android.R.attr.windowBackground});
    int background = a.getResourceId(0, 0);
    a.recycle();

    ViewGroup decor = (ViewGroup) activity.getWindow().getDecorView();
    ViewGroup decorChild = (ViewGroup) decor.getChildAt(0);
    decorChild.setBackgroundResource(background);
    decor.removeView(decorChild);
    mContent = decorChild;
    mMenu = menu;
    RelativeLayout.LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
    addView(mContent, params);
    mMenuContainer = new MenuLayout(activity, mMenu, widthOffset);
    mMenuContainer.addView(mMenu, params);
    addView(mMenuContainer, params);
    decor.addView(this);
  }

  public void setOpenPercent(float percent) {
    mMenuContainer.setOpenPercent(percent);
  }

  public void setMenuListener(MenuListener listener) {
    mMenuContainer.setMenuListener(listener);
  }

  public interface MenuListener {
    void onPercentChanged(final float percent);
  }

  @Override
  protected boolean fitSystemWindows(Rect insets) {
    int leftPadding = insets.left;
    int rightPadding = insets.right;
    int topPadding = insets.top;
    int bottomPadding = insets.bottom;
    setPadding(leftPadding, topPadding, rightPadding, bottomPadding);
    return true;
  }

  class MenuLayout extends RelativeLayout implements OnDragListener {
    int mWidthOffset;
    int mWidth;
    int mMenuWidth;
    int mRight;
    float mOpenPercent = 0f;
    View mMenu;
    MenuListener mListener;
    int mTouchSlop;
    float mLastMotionX;
    float mLastMotionY;
    boolean mIsBeingDragged;

    public MenuLayout(Context context, View menu, int widthOffset) {
      super(context);
      mMenu = menu;
      mWidthOffset = widthOffset;
      //a touch event on the edge should be considered to open the menu
      mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop() * 2;
    }


    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
      final int action = ev.getAction() & MotionEventCompat.ACTION_MASK;
      if (action == MotionEvent.ACTION_DOWN) {
        float x = ev.getX();
        if (mMenuContainer.isShowing() && !mMenuContainer.hit(x)) {
          return true;
        } else if (!mMenuContainer.isShowing() && x <= mTouchSlop) {
          setOpenPercent(0.1f);
          return true;
        }
      }
      return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
      final int action = event.getAction() & MotionEventCompat.ACTION_MASK;
      if (action == MotionEvent.ACTION_DOWN) {
        mLastMotionX = MotionEventCompat.getX(event, 0);
        mLastMotionY = MotionEventCompat.getY(event, 0);
        if (!mMenuContainer.isShowing()) {
          if (mLastMotionX > mTouchSlop) {
            return false;
          }
        }
        return true;
      } else if (action == MotionEvent.ACTION_MOVE) {
        if (!mIsBeingDragged) {
          determineDrag(event);
        }
        return true;
      } else if (action == MotionEvent.ACTION_UP) {
        if (mMenuContainer.isShowing() && mMenuContainer.mOpenPercent <= 0.5f) {
          mMenuContainer.close();
        }
      }
      return false;
    }

    void determineDrag(MotionEvent event) {
      final float x = MotionEventCompat.getX(event, 0);
      final float dx = x - mLastMotionX;
      final float xDiff = Math.abs(dx);
      final float y = MotionEventCompat.getY(event, 0);
      final float dy = y - mLastMotionY;
      final float yDiff = Math.abs(dy);
      if (xDiff > mTouchSlop && xDiff > yDiff) {
        mIsBeingDragged = true;
        this.setOnDragListener(mMenuContainer);
        this.startDrag(null, new DragShadowBuilder(), null, 0);
        mLastMotionX = x;
        mLastMotionY = y;
      }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
      if (mWidth == 0) {
        mWidth = r - 1;
        mMenuWidth = mWidth - mWidthOffset;
      }
      int left = (int) (0 - (mMenuWidth) * (1 - mOpenPercent));
      mRight = left + mMenuWidth;
      mMenu.layout(left, 0, mRight, b - t);
    }

    public void setOpenPercent(float percent) {
      mOpenPercent = percent;
      requestLayout();
    }

    public void setMenuListener(MenuListener listener) {
      mListener = listener;
    }

    public boolean isShowing() {
      return mOpenPercent > 0f;
    }

    public boolean hit(float x) {
      return x <= mRight;
    }

    public void show() {
      setOpenPercent(1f);
    }

    public void close() {
      setOpenPercent(0f);
    }

    @Override
    public boolean onDrag(View v, DragEvent event) {
      final int action = event.getAction();
      switch (action) {
        case DragEvent.ACTION_DRAG_STARTED:
          return true;
        case DragEvent.ACTION_DRAG_LOCATION:
          float percent = 0f;
          float x = event.getX();
          if (x >= mMenuWidth) {
            percent = 1f;
          } else {
            percent = x / mMenuWidth;
          }
          setOpenPercent(percent);
          if (mListener != null) {
            mListener.onPercentChanged(percent);
          }
          return true;
        case DragEvent.ACTION_DRAG_ENDED:
          mIsBeingDragged = false;
          if (mOpenPercent >= 0.5f) {
            setOpenPercent(1f);
          } else {
            setOpenPercent(0f);
          }
          return true;
        default:
          return false;
      }
    }

    void animOpen() {

    }

    void animClose() {

    }
  }
}

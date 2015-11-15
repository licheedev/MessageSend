package cn.easydone.messagesendview;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.RelativeLayout;

/**
 * Created by Android Studio
 * User: Ailurus(ailurus@foxmail.com)
 * Date: 2015-11-15
 * Time: 16:31
 */
public class KeyboardStateLayout extends RelativeLayout {
    private static final String TAG = KeyboardStateLayout.class.getSimpleName();
    public static final byte KEYBOARD_STATE_SHOW = -3;
    public static final byte KEYBOARD_STATE_HIDE = -2;
    public static final byte KEYBOARD_STATE_INIT = -1;
    private boolean mHasInit;
    private boolean mHasKeybord;
    private int mHeight;
    private onKybdsChangeListener mListener;

    public KeyboardStateLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public KeyboardStateLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public KeyboardStateLayout(Context context) {
        super(context);
    }

    /**
     * set keyboard state listener
     */
    public void setOnkbdStateListener(onKybdsChangeListener listener) {
        mListener = listener;
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        if (!mHasInit) {
            mHasInit = true;
            mHeight = b;
            if (mListener != null) {
                mListener.onKeyBoardStateChange(KEYBOARD_STATE_INIT);
            }
        } else {
            mHeight = mHeight < b ? b : mHeight;
        }
        if (mHasInit && mHeight > b) {
            mHasKeybord = true;
            if (mListener != null) {
                mListener.onKeyBoardStateChange(KEYBOARD_STATE_SHOW);
            }
            Log.w(TAG, "show keyboard.......");
        }
        if (mHasInit && mHasKeybord && mHeight == b) {
            mHasKeybord = false;
            if (mListener != null) {
                mListener.onKeyBoardStateChange(KEYBOARD_STATE_HIDE);
            }
            Log.w(TAG, "hide keyboard.......");
        }
    }

    public interface onKybdsChangeListener {
        public void onKeyBoardStateChange(int state);
    }
}
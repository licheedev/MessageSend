package cn.easydone.messagesendview;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Handler;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.File;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Android Studio
 * User: Ailurus(ailurus@foxmail.com)
 * Date: 2015-11-15
 * Time: 09:33
 */
public class MessageSendView extends LinearLayout implements View.OnClickListener, Constans {

    private TextView mBtnSend;
    private EditText mTextEditor;
    private ImageView mSwitchModeButton;
    private TextView mRecordVoiceButton;
    private ImageView mIconMore;
    private FrameLayout mExpandViewContainer;
    private boolean mSupportVoice;
    private boolean mIsExpandable;
    private boolean mIsVoiceMode = false;
    private boolean mIsVoiceDown = false;
    private boolean mIsVoiceStopNotify = false;
    private int mVoiceStopDelayTimes = 0;

    /* Record voice */
    private AudioRecorder mAudioRecorder = AudioRecorder.getInstance();
    private String mVoiceFilePath;

    private OnSendClickListener mSendListener;
    private OnExpandListener mExpandListener;
    private OnRecordVoiceListener mRecordVoiceListener;
    private Handler handler = new Handler();

    public void setOnExpandListener(OnExpandListener l) {
        mExpandListener = l;
    }

    public void setOnSendClickListener(OnSendClickListener l) {
        mSendListener = l;
    }

    public void setOnRecordListener(OnRecordVoiceListener l) {
        mRecordVoiceListener = l;
    }

    public interface OnSendClickListener {
        void onSendClick(MessageSendView view);
    }

    public interface OnExpandListener {
        void onExpand();

        void onCollapse();
    }

    public interface OnRecordVoiceListener {
        void onStart(MessageSendView view);

        void onFinish(MessageSendView view, String fileName, boolean cancelled);

        void onInfo(MessageSendView view, int remainSeconds);

        void willCancel(boolean willCancel);
    }

    public MessageSendView(Context context) {
        this(context, null);
    }

    public MessageSendView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    private void updateSendButton() {
        CharSequence s = mTextEditor.getText();
        if (s == null || TextUtils.isEmpty(s.toString().trim())) {
            mBtnSend.setBackgroundResource(R.drawable.btn_disabled_bg);
        } else {
            mBtnSend.setBackgroundResource(R.drawable.btn_neutral_bg);
        }
    }

    private void init(Context context, AttributeSet attrs) {
        if (!isInEditMode()) {
            mVoiceFilePath = StorageUtils.getTempDir() + File.separator + "voice_temp_" + System.currentTimeMillis() + ".amr";
        }
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.MessageSendView);
        mSupportVoice = a.getBoolean(R.styleable.MessageSendView_supportVoice, false);
        mIsExpandable = a.getBoolean(R.styleable.MessageSendView_expandable, false);
        String hint = a.getString(R.styleable.MessageSendView_android_hint);
        String sendText = a.getString(R.styleable.MessageSendView_sendText);
        int maxLines = a.getInt(R.styleable.MessageSendView_android_maxLines, 1);
        int maxLength = a.getInt(R.styleable.MessageSendView_android_maxLength, 1000);
        a.recycle();

        View view = LayoutInflater.from(context).inflate(R.layout.view_message_send, this, true);

        mBtnSend = (TextView) view.findViewById(R.id.editor_send);
        if (!TextUtils.isEmpty(sendText)) {
            mBtnSend.setText(sendText);
        }
        mBtnSend.setOnClickListener(this);

        mExpandViewContainer = (FrameLayout) findViewById(R.id.editor_expand_container);

        mTextEditor = (EditText) view.findViewById(R.id.editor);
        mTextEditor.setHint(hint);
        mTextEditor.setMaxLines(maxLines);
        mTextEditor.setSingleLine(false);
        mTextEditor.setFilters(new InputFilter[]{new InputFilter.LengthFilter(maxLength)});
        mTextEditor.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                updateSendButton();
            }
        });

        mRecordVoiceButton = (TextView) view.findViewById(R.id.voice_record_button);
        mRecordVoiceButton.setOnTouchListener(new OnTouchListener() {
            private float downY;
            private static final float CANCEL_Y_OFFSET = 100;
            private Timer mTimer;
            private int remainSeconds;
            private boolean finished = false;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Log.d("mRecordVoiceButton", String.format("type: %d y: %d", event.getAction(), px2dp(v.getContext(), event.getY())));
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        if (mIsVoiceDown)
                            return true;
                        mIsVoiceDown = true;
                        mIsVoiceStopNotify = false;
                        mVoiceStopDelayTimes = 0;
                        mRecordVoiceButton.setText(getResources().getString(R.string.chat_message_release_to_send));
                        downY = px2dp(v.getContext(), event.getY());
                        if (mRecordVoiceListener != null)
                            mRecordVoiceListener.onStart(MessageSendView.this);

                        mAudioRecorder.startRecording(mVoiceFilePath, MAX_CHAT_VOICE_LENGTH);
                        remainSeconds = MAX_CHAT_VOICE_SECONDS;
                        mTimer = new Timer();
                        mTimer.schedule(new TimerTask() {
                            @Override
                            public void run() {
                                handler.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        mVoiceStopDelayTimes++;
                                        if (mIsVoiceStopNotify && mVoiceStopDelayTimes >= 1) {
                                            mAudioRecorder.stopRecording();
                                            mIsVoiceDown = false;
                                            mTimer.cancel();
                                            if (mVoiceStopDelayTimes < 2) {
                                                return;
                                            }
                                        }

                                        remainSeconds--;
                                        if (remainSeconds == 0) {
                                            mAudioRecorder.stopRecording();
                                        }
                                        if (mRecordVoiceListener != null && !finished) {
                                            if (remainSeconds == 0) {
                                                mRecordVoiceListener.onFinish(MessageSendView.this, mVoiceFilePath, false);
                                            } else {
                                                mRecordVoiceListener.onInfo(MessageSendView.this, remainSeconds);
                                            }
                                        }
                                        if (remainSeconds == 0) {
                                            mTimer.cancel();
                                            finished = true;
                                        }
                                    }
                                }, 500);
                            }
                        }, 1000, 1000);
                        finished = false;
                        if (mRecordVoiceListener != null) {
                            mRecordVoiceListener.onInfo(MessageSendView.this, remainSeconds);
                        }
                        break;
                    case MotionEvent.ACTION_MOVE: {
                        float y = px2dp(v.getContext(), event.getY());
                        if (downY - y > CANCEL_Y_OFFSET) {
                            if (mRecordVoiceListener != null)
                                mRecordVoiceListener.willCancel(true);
                        } else {
                            if (mRecordVoiceListener != null)
                                mRecordVoiceListener.willCancel(false);
                        }
                        break;
                    }
                    case MotionEvent.ACTION_UP: {
                        if (!mIsVoiceDown)
                            return true;

                        mIsVoiceStopNotify = true;
                        mRecordVoiceButton.setText(getResources().getString(R.string.chat_message_pressed_to_talk));
                        float y = px2dp(v.getContext(), event.getY());
                        if (mRecordVoiceListener != null && !finished) {
                            mRecordVoiceListener.onFinish(MessageSendView.this, mVoiceFilePath, downY - y > CANCEL_Y_OFFSET);
                        }
                        finished = true;
                        break;
                    }
                }

                return true;
            }
        });


        mSwitchModeButton = (ImageView) findViewById(R.id.editor_switch_mode);
        mSwitchModeButton.setVisibility(mSupportVoice ? VISIBLE : GONE);
        mSwitchModeButton.setOnClickListener(this);

        mIconMore = (ImageView) findViewById(R.id.editor_more);
        mIconMore.setVisibility(mIsExpandable ? VISIBLE : GONE);
        mIconMore.setOnClickListener(this);

        setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    mTextEditor.requestFocus();
                }
            }
        });

        updateSendButton();
    }

    public void setVoiceEnabled(boolean enabled) {
        if (mSupportVoice != enabled) {
            mSupportVoice = enabled;
            mSwitchModeButton.setVisibility(mSupportVoice ? VISIBLE : GONE);
        }
    }

    public void setExpandView(View view) {
        if (mIsExpandable) {
            mExpandViewContainer.removeAllViews();
        }
        mIsExpandable = (view != null);
        mIconMore.setVisibility(mIsExpandable ? VISIBLE : GONE);

        if (view != null) {
            mExpandViewContainer.addView(view, new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT));
            mExpandViewContainer.setVisibility(GONE);
        }
    }

    public void collapse() {
        mExpandViewContainer.setVisibility(GONE);
        if (mExpandListener != null) {
            mExpandListener.onCollapse();
        }
    }

    public void expand() {
        mExpandViewContainer.setVisibility(VISIBLE);
        if (mExpandListener != null) {
            mExpandListener.onExpand();
        }
    }

    public void toggleExpandView() {
        if (!isExpanded()) {
            mExpandViewContainer.setVisibility(VISIBLE);
            if (mExpandListener != null) {
                mExpandListener.onExpand();
            }
        } else {
            mExpandViewContainer.setVisibility(GONE);
            if (mExpandListener != null) {
                mExpandListener.onCollapse();
            }
        }
    }

    public boolean isExpanded() {
        return mExpandViewContainer.getVisibility() == VISIBLE;
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.editor_send) {
            if (mTextEditor.getText() == null || TextUtils.isEmpty(mTextEditor.getText().toString().trim()))
                return;
            if (mSendListener != null) {
                mSendListener.onSendClick(this);
            }

        } else if (i == R.id.editor_more) {
            if (mIsExpandable) {
                toggleExpandView();
            }

        } else if (i == R.id.editor_switch_mode) {
            mIsVoiceMode = !mIsVoiceMode;
            if (mIsExpandable && isExpanded()) {
                toggleExpandView();
            }

            if (mIsVoiceMode) {
                mSwitchModeButton.setImageResource(R.mipmap.ic_editor_keyboard);
                mTextEditor.setVisibility(GONE);
                mRecordVoiceButton.setVisibility(VISIBLE);
                InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(mTextEditor.getWindowToken(), 0);
            } else {
                mSwitchModeButton.setImageResource(R.mipmap.ic_editor_voice);
                mTextEditor.setVisibility(VISIBLE);
                mRecordVoiceButton.setVisibility(GONE);
                mTextEditor.requestFocus();
                InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(mTextEditor, 0);
            }


        }
    }

    public String getEditText() {
        return mTextEditor.getText().toString();
    }

    public void clearEditText() {
        mTextEditor.setText("");
    }

    public static int px2dp(Context context, float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }
}

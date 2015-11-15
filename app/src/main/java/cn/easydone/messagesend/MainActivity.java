package cn.easydone.messagesend;

import android.graphics.Rect;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.widget.Toast;

import cn.easydone.messagesendview.Constans;
import cn.easydone.messagesendview.KeyboardStateLayout;
import cn.easydone.messagesendview.MediaUtil;
import cn.easydone.messagesendview.MessageSendView;
import cn.easydone.messagesendview.RecordVoiceView;

public class MainActivity extends AppCompatActivity {

    private RecordVoiceView recordVoiceView;
    private MessageSendView messageSendView;
    private long voiceLength;
    private TextView textView;
//    private String voicePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        KeyboardStateLayout rootView = (KeyboardStateLayout) findViewById(R.id.rootView);
        recordVoiceView = (RecordVoiceView) findViewById(R.id.record_state_view);
        messageSendView = (MessageSendView) findViewById(R.id.chat_editor);
        textView = (TextView) findViewById(R.id.text);
        controlKeyboardLayout(rootView, messageSendView);
        rootView.setOnkbdStateListener(new KeyboardStateLayout.onKybdsChangeListener() {
            @Override
            public void onKeyBoardStateChange(int state) {
                switch (state) {
                    case KeyboardStateLayout.KEYBOARD_STATE_HIDE:
                        //TODO
                        break;
                    case KeyboardStateLayout.KEYBOARD_STATE_SHOW:
                        //TODO
                        break;
                }
            }
        });
        setMessageSendView();
    }

    private void setMessageSendView() {
        View explandableView = getLayoutInflater().inflate(R.layout.expand_view, null);
        messageSendView.setExpandView(explandableView);
        messageSendView.setOnSendClickListener(new MessageSendView.OnSendClickListener() {
            @Override
            public void onSendClick(MessageSendView view) {
                String text = view.getEditText();
                //TODO 发送文字消息请求
                messageSendView.clearEditText();
                textView.setVisibility(View.VISIBLE);
                textView.setText(text);
            }
        });

        messageSendView.setOnExpandListener(new MessageSendView.OnExpandListener() {
            @Override
            public void onExpand() {
                hideInputMethod(messageSendView);
            }

            @Override
            public void onCollapse() {
            }
        });

        messageSendView.setOnRecordListener(new MessageSendView.OnRecordVoiceListener() {
            @Override
            public void onStart(MessageSendView view) {
                recordVoiceView.setVisibility(View.VISIBLE);
                recordVoiceView.setState(false);
            }

            @Override
            public void onFinish(MessageSendView view, String fileName, boolean cancelled) {
                recordVoiceView.setVisibility(View.GONE);
                if (cancelled)
                    return;

                long amrDuration = MediaUtil.getAmrDuration(fileName);
                voiceLength = amrDuration / 1000;
                if (voiceLength < Constans.MIN_CHAT_VOICE_SECONDS)
                    return;
//                voicePath = fileName;
                //TODO 发送语音消息请求
                messageSendView.clearEditText();
            }

            @Override
            public void onInfo(MessageSendView view, int remainSeconds) {
                recordVoiceView.setRemainIcons(remainSeconds);
            }

            @Override
            public void willCancel(boolean willCancel) {
                recordVoiceView.setState(willCancel);
            }
        });

        View takePhotoView = explandableView.findViewById(R.id.chat_editor_more_camera);
        takePhotoView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickImageWithCamera();
            }
        });

        View pickImageView = explandableView.findViewById(R.id.chat_editor_more_gallary);
        pickImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickImageSingleFromGallery();
            }
        });
    }

    private void pickImageSingleFromGallery() {
        //TODO 从相册选择照片
        Toast.makeText(MainActivity.this, "choose picture from gallery", Toast.LENGTH_LONG).show();
    }

    private void pickImageWithCamera() {
        //TODO 照相
        Toast.makeText(MainActivity.this, "take photo with camera", Toast.LENGTH_LONG).show();
    }

    public void hideInputMethod(View view) {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    @Override
    public void onBackPressed() {
        if (messageSendView.isExpanded()) {
            messageSendView.collapse();
        } else {
            super.onBackPressed();
        }
    }

    private void controlKeyboardLayout(final View root, final View scrollToView) {
        root.getViewTreeObserver().addOnGlobalLayoutListener( new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                Rect rect = new Rect();
                //获取root在窗体的可视区域
                root.getWindowVisibleDisplayFrame(rect);
                //获取root在窗体的不可视区域高度(被其他View遮挡的区域高度)
                int rootInvisibleHeight = root.getRootView().getHeight() - rect.bottom;
                //若不可视区域高度大于100，则键盘显示
                if (rootInvisibleHeight > 100) {
                    int[] location = new int[2];
                    //获取scrollToView在窗体的坐标
                    scrollToView.getLocationInWindow(location);
                    //计算root滚动高度，使scrollToView在可见区域
                    int srollHeight = (location[1] + scrollToView.getHeight()) - rect.bottom;
                    root.scrollTo(0, srollHeight);
                } else {
                    //键盘隐藏
                    root.scrollTo(0, 0);
                }
            }
        });
    }
}

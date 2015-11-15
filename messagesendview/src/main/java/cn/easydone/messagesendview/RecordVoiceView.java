package cn.easydone.messagesendview;

import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * Created by Android Studio
 * User: Ailurus(ailurus@foxmail.com)
 * Date: 2015-11-15
 * Time: 10:41
 */
public class RecordVoiceView extends RelativeLayout {
    private ImageView mRecordImageView;
    private ImageView mVolumeView;
    private TextView mRecordTextView;
    private View mRecordIconContainer;
    private TextView mRemainSecondsView;

    public RecordVoiceView(Context context, AttributeSet attrs) {
        super(context, attrs);

        View view = LayoutInflater.from(context).inflate(R.layout.view_record_voice_state, this, true);
        mRecordImageView = (ImageView) view.findViewById(R.id.record_state_image);
        mVolumeView = (ImageView) view.findViewById(R.id.record_state_volume);
        mRecordTextView = (TextView) view.findViewById(R.id.record_state_text);

        mRecordIconContainer = view.findViewById(R.id.record_icon_container);
        mRemainSecondsView = (TextView) view.findViewById(R.id.remain_seconds);
    }

    public void setState(boolean willCancel) {
        if (willCancel) {
            mRecordImageView.setImageResource(R.mipmap.ic_voice_cancel);
            mVolumeView.setVisibility(GONE);
            mRecordTextView.setBackgroundResource(R.drawable.chat_record_cancel_tv_bg);
            mRecordTextView.setText(getResources().getString(R.string.chat_message_voice_cancel_state));
        } else {
            mRecordImageView.setImageResource(R.mipmap.ic_voice_recording);
            mVolumeView.setVisibility(VISIBLE);
            mRecordTextView.setBackgroundResource(0);
            mRecordTextView.setText(getResources().getString(R.string.chat_message_slide_up_to_cancel));
            //noinspection ResourceType
            mVolumeView.setBackgroundResource(R.anim.chat_voice_record);
            AnimationDrawable anim = (AnimationDrawable) mVolumeView.getBackground();
            anim.start();
        }
    }

    public void setRemainIcons(int seconds) {
        if (seconds <= 10) {
            mRemainSecondsView.setText(String.valueOf(seconds));
            mRemainSecondsView.setVisibility(VISIBLE);
            mRecordIconContainer.setVisibility(GONE);
        } else {
            mRemainSecondsView.setVisibility(GONE);
            mRecordIconContainer.setVisibility(VISIBLE);
        }
    }
}

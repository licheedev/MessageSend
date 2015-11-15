package cn.easydone.messagesendview;

import android.media.MediaRecorder;
import android.util.Log;

/**
 * Created by Android Studio
 * User: Ailurus(ailurus@foxmail.com)
 * Date: 2015-11-15
 * Time: 09:36
 */
public class AudioRecorder {

    private static final String TAG = AudioRecorder.class.getSimpleName();
    private MediaRecorder mRecorder;
    private static final AudioRecorder mInstance = new AudioRecorder();

    private AudioRecorder() {}

    public static AudioRecorder getInstance() {
        return mInstance;
    }

    public boolean startRecording(String fileName, int maxDurationMs) {
        mRecorder = new MediaRecorder();
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.AMR_NB);
        mRecorder.setOutputFile(fileName);
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        mRecorder.setMaxDuration(maxDurationMs);

        mRecorder.setOnInfoListener(new MediaRecorder.OnInfoListener() {
            @Override
            public void onInfo(MediaRecorder mr, int what, int extra) {
                //TODO: Generated method body.
            }
        });

        try {
            mRecorder.prepare();
            mRecorder.start();
        } catch (Exception e) {
            Log.e(TAG, "prepare() failed");
            return false;
        }

        return true;
    }

    public void stopRecording() {
        if (mRecorder == null)
            return;

        mRecorder.stop();
        mRecorder.release();
        mRecorder = null;
    }

}

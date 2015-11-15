package cn.easydone.messagesendview;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * Created by Android Studio
 * User: Ailurus(ailurus@foxmail.com)
 * Date: 2015-11-15
 * Time: 15:01
 */
public class MediaUtil {

    public static long getAmrDuration(String path) {
        long duration = -1;
        int[] packedSize = {12, 13, 15, 17, 19, 20, 26, 31, 5, 0, 0, 0, 0, 0, 0, 0};
        RandomAccessFile randomAccessFile = null;
        File file;
        try {
            file = new File(path);
            randomAccessFile = new RandomAccessFile(file, "rw");
            long length = file.length();//文件的长度
            int pos = 6;//设置初始位置
            int frameCount = 0;//初始帧数
            int packedPos = -1;
            byte[] datas = new byte[1];//初始数据值
            while (pos <= length) {
                randomAccessFile.seek(pos);
                if (randomAccessFile.read(datas, 0, 1) != 1) {
                    duration = length > 0 ? ((length - 6) / 650) : 0;
                    break;
                }
                packedPos = (datas[0] >> 3) & 0x0F;
                pos += packedSize[packedPos] + 1;
                frameCount++;
            }
            duration += frameCount * 20;//帧数*20
        } catch (IOException ignored) {
        } finally {
            if (randomAccessFile != null) {
                try {
                    randomAccessFile.close();
                } catch (IOException ignored) {
                }
            }
        }
        return duration;
    }

}

package cn.easydone.messagesendview;

import android.content.Context;
import android.os.Environment;

import java.io.File;
import java.io.IOException;

/**
 * Created by Android Studio
 * User: Ailurus(ailurus@foxmail.com)
 * Date: 2015-11-15
 * Time: 09:40
 */
public class StorageUtils {

    private static final String NO_MEDIA = ".nomedia";
    private static final String CACHE_DIR = ".MSV";

    private static final String STORAGE_ROOT = Environment.getExternalStorageDirectory().getAbsolutePath();
    private static final String STORAGE_CACHE_DIR = STORAGE_ROOT+ File.separator + CACHE_DIR;
    private static final String DATA_ROOT = Environment.getDataDirectory().getAbsolutePath();

    private static final String STORAGE_PHOTO_DIR =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getAbsolutePath() + File.separator + "Camera";

    private static final String STORAGE_SAVE = STORAGE_ROOT + File.separator + "HZB";

    private static final String DIR_IMAGE = STORAGE_CACHE_DIR + File.separator + "images";
    private static final String DIR_AUDIO = STORAGE_CACHE_DIR + File.separator + "audios";
    private static final String DIR_TEMP = STORAGE_CACHE_DIR + File.separator + "temp";
    private static final String DIR_CHAT_ICON = STORAGE_CACHE_DIR + File.separator + "gen_icons";

    private static boolean isNoMediaEnsured = false;

    private static MD5FileNameGenerator fileNameGenerator = new MD5FileNameGenerator();

    static {
        ensureCacheDir();
    }

    public static String getInternalFilesDir(Context context) {
        return context.getFilesDir().getAbsolutePath() + File.separator;
    }

    public static String getStorageCacheDir() {
        ensureCacheDir();
        return STORAGE_CACHE_DIR;
    }

    public static String getTempDir() {
        ensureDir(DIR_TEMP);
        return DIR_TEMP;
    }

    public static String getImageDir() {
        ensureDir(DIR_IMAGE);
        return DIR_IMAGE;
    }

    public static String getAudioDir() {
        ensureDir(DIR_AUDIO);
        return DIR_AUDIO;
    }

    public static String getPhotoDir() {
        ensureDir(STORAGE_PHOTO_DIR);
        return STORAGE_PHOTO_DIR;
    }

    private static String getPhotoSaveDir() {
        ensureDir(STORAGE_SAVE);
        return STORAGE_SAVE;
    }

    public static String getChatIconDir() {
        ensureDir(DIR_CHAT_ICON);
        return DIR_CHAT_ICON;
    }

    private static void ensureDir(String path) {
        File f = new File(path);
        if (!f.exists() || !f.isDirectory()) {
            //noinspection ResultOfMethodCallIgnored
            f.mkdirs();
        }
    }

    public static String getAudioFilePath(String url) {
        return StorageUtils.getAudioDir() + fileNameGenerator.generate(url);
    }

    public static String getOriginalFileSavePath(String url) {
        return StorageUtils.getPhotoSaveDir() + File.separator + fileNameGenerator.generate(url) + ".jpeg";
    }

    private static void ensureCacheDir() {
        ensureDir(STORAGE_CACHE_DIR);

        //We don't care much about the existence of .nomedia file.
        if (!isNoMediaEnsured) {
            boolean isClear = true;
            File nomedia = new File(STORAGE_CACHE_DIR, NO_MEDIA);
            if (nomedia.exists()) {
                if (nomedia.isDirectory()) {
                    isClear = nomedia.delete();
                } else {
                    isNoMediaEnsured = true;
                    return;
                }
            }

            if (isClear) {
                try {
                    isNoMediaEnsured = nomedia.createNewFile();
                } catch (IOException ignored) {
                    ignored.printStackTrace();
                    isNoMediaEnsured = false;
                }
            } else {
                isNoMediaEnsured = false;
            }
        }
    }

}

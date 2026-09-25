package com.github.tand0.shogio;

import android.app.Application;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/** メインアプリケーション */
public class MainApplication extends Application {

    /** プロパティの位置 */
    private static final String PROPERTIES = "config.properties";

    /** プロパティ */
    protected Properties properties = new Properties();

    /** プロパティの stream を取る
     *
     * @return Input Stream
     * @throws IOException IO Exception
     */
    protected InputStream getPropertiesStream() throws IOException{
        return this.getAssets().open(PROPERTIES);
    }
    /** コンストラクタ */
    public MainApplication() {
    }
    @Override
    public void onCreate() {
        super.onCreate();
        try (InputStream is = getPropertiesStream()) {
            properties.load(is); // Propertiesに読み込む
        } catch (IOException e) {
            // EMPTY
        }
    }
    /**
     * プロパティの取得
     * @param key 名前
     * @param defaultValue デフォルト値
     * @return 結果
     */
    public String getString(String key, String defaultValue) {
        if (properties == null) {
            return defaultValue;
        }
        return properties.getProperty(key, defaultValue);
    }

    /**
     * プロパティの取得
     * @param key 名前
     * @param defaultValue デフォルト値
     * @return 結果
     */
    public int getInt(String key, int defaultValue) {
        String string =  getString(key, "" + defaultValue);
        int result;
        try {
            result = Integer.parseInt(string);
        } catch (NumberFormatException e) {
            result = defaultValue;
        }
        return result;
    }

}

package com.github.tand0.shogio.tool;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/** プロパティデータの取得 */
public class MyPropertiesAccess {

    private static Properties properties = null;

    /** プロパティの取得
     * @param key プロパティの key
     * @return プロパティの value
     * @throws IOException IOException
     */
    public static String getString(String key) throws IOException {
        if (key == null) {
            return null;
        }
        if (properties == null) {
            properties = new Properties();
            try (InputStream is = new java.io.FileInputStream("app/src/main/assets/config.properties")) {
                // プロパティファイルをロード
                properties.load(is);
            }
        }
        String result =  properties.getProperty(key);
        if (result == null) {
            return key;
        }
        return result;
    }
    /** プロパティの取得
     * @param key プロパティの key
     * @return プロパティの value
     * @throws IOException IOException
     */
    public static int getInt(String key) throws IOException {
        String string = getString(key);
        return Integer.parseInt(string);
    }
}

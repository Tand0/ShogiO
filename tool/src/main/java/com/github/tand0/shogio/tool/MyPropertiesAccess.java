package com.github.tand0.shogio.tool;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

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
    public static Map<String, String> getMap(String key) throws IOException {
        Map<String, String> x = new TreeMap<>();
        int len = key.length();
        for (String name: properties.stringPropertyNames()) {
            if (0 == name.indexOf(key)) {
                String p = name.substring(len);
                x.put(p, properties.get(name).toString());
            }
        }
        return x;
    }

    /** プロパティの取得
     * @param key プロパティの key
     * @param defaultValue デフォルト値
     * @return プロパティの value
     */
    public static int getInt(String key, int defaultValue) throws IOException {
        int intValue;
        try {
            intValue = MyPropertiesAccess.getInt(key);
        } catch (IOException e) {
            return defaultValue;
        }
        return intValue;
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

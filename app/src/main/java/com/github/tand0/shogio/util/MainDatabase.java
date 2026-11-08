package com.github.tand0.shogio.util;

/**
 * データベース(またはモデル)から win, los 値を得るためのインタフェース
 */
public interface MainDatabase {
    /** データベース名のasset側のzipのファイル名 **/
    String DATABASE_ZIP_NAME = "mytable_small.zip";

    /** データベース名 **/
    String DATABASE_NAME = "mytable_small.db";

    /**  テーブル名とカラム名の定義 */
    String TABLE_NAME = "mytable";

    /** 主キーその1 */
    String KEY1 = "key1";

    /** 主キーその2 */
    String KEY2 = "key2";

    /** 主キーその3 */
    String KEY3 = "key3";

    /** 主キーその4 */
    String KEY4 = "key4";

    /** パラメータ(win値) */
    String WIN = "win";

    /** パラメータ(los値) */
    String LOS = "los";

    /**
     * テーブルキーから 評価値を取得する
     * @param key 主キー
     * @return 戻り値(win, los)
     */
    long[] getData(long[] key);
}

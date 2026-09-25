package com.github.tand0.shogio.util;

/**
 * データベース(またはモデル)から win, los 値を得るためのインタフェース
 */
public interface MainDatabase {

    /**
     * テーブルキーから 評価値を取得する
     * @param key 主キー
     * @return 戻り値(win, los)
     */
    long[] getData(long[] key);
}

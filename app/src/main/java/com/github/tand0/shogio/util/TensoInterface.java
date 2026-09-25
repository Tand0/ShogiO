package com.github.tand0.shogio.util;

/** tenso 用のインタフェース */
public interface TensoInterface {
    /** テーブルを入れると InputGen に変換し、TensorFlow を使って勝率を取得する
     * @param table テーブル
     * @return 勝率
     * @throws RuntimeException 取得失敗
     */
    Float runCompiledModel(Table table) throws RuntimeException;
}

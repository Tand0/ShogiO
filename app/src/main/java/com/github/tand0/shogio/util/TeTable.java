package com.github.tand0.shogio.util;


/**
 * 手とテーブルを合体させたもの
 */
public class TeTable {
    /** 手 */
    public final int te;

    /** テーブル */
    public final Table table;

    /**
     * コンストラクタ
     * @param table テーブル
     * @param te 手
     */
    public TeTable(Table table, int te) {
        this.table = table;
        this.te = te;
    }

    /**
     * 表示用の手を取得する
     * @return 表示用の手
     */
    public String display() {
        if (te == TableDefine.LOS) { // 次に指す手がない
            return "-------"; // 定跡エンジンだと指し手がでないのでバーにする
        }
        return TableDefine.changeTeIntToKangiString(te);
    }
    @Override
    public boolean equals(Object x) {
        if (x == this) return true;
        if (x instanceof TeTable teTable) {
            // 手が同じで無くても、局面が同じなら重複扱い
            return this.table.equals(teTable.table);
        }
        if (x instanceof Table) {
            return table.equals(x);
        }
        return false;
    }
}

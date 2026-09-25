package com.github.tand0.shogio.util;


import java.util.Locale;

/** 勝ち負け 手テーブル */
public class EvalTeTable extends TeTable implements Comparable<TeTable> {
    /** 勝率 */
    public final float eval;

    /** 勝ち数 */
    public final long win;
    /** 負け数 */
    public final long los;

    /**
     *  先手勝ち数と、先手負け数から勝率を作る
     * @param win 先手勝数
     * @param los 先手負数
     * @return 勝率
     */
    public static float getWinLosToRate(long win, long los) {
        long sum = win + los;
        if (sum == 0) {
            return 0.5f;
        }
        return ((float) win) / ((float)sum);
    }
    /**
     * コンストラクタ
     * @param table テーブル
     * @param te 手
     * @param win 勝ち数
     * @param los 負け数
     */
    public EvalTeTable(Table table, int te, long win, long los) {
        this(table, te, getWinLosToRate(win,los) , win, los);
    }
    /**
     * コンストラクタ
     * @param table テーブル
     * @param te 手
     * @param eval 勝率
     * @param win 勝ち数
     * @param los 負け数
     */
    public EvalTeTable(Table table, int te, float eval, long win, long los) {
        super(table, te);
        this.eval = eval;
        this.win = win;
        this.los = los;
    }

    @Override
    public String display() {
        return super.display() +
                String.format(Locale.getDefault(),
                " %6.2f%% w:%,d l:%,d",
                eval * 100, win, los);
    }

    @Override
    public boolean equals(Object o) {
        if (super.equals(o)) {
            return true; // 局面が同じなら同じになる
        }
        if (o instanceof EvalTeTable ett) {
            return ett.eval == this.eval;
        } else if (o instanceof ExplorerTable et) {
            return et.getEval() == this.eval;
        }
        return false;
    }
    @Override
    public int compareTo(TeTable o) {
        if (this.equals(o)) {
            return 0; // 局面が同じなら同じになる
        }
        float diff;
        if (o instanceof EvalTeTable ett) {
            diff = ett.eval - this.eval;
        } else if (o instanceof ExplorerTable et) {
            diff = et.getEval() - this.eval;
        } else {
            diff = 0.5f;
        }
        //
        if (diff != 0f) {
            // 手番に応じて符号を反転
            return (this.table.getTeban() == 0)
                    ? (diff > 0 ? -1 : 1)
                    : (diff > 0 ? 1 : -1);
        }
        //
        // eval が完全一致した場合のタイブレーク
        return -1;
    }


    /** 局面を左右反転させる
     *
     * @return 反転したテーブル
     */
    public EvalTeTable flippingHorizontal() {
        return new EvalTeTable(this.table.flippingHorizontal(), this.te, this.win, this.los);
    }
    /** 局面を上下反転させる
     *
     * @return 反転したテーブル
     */
    public EvalTeTable flippingVertical() {
        return new EvalTeTable(this.table.flippingVertical(), this.te, 1 - this.eval, this.los, this.win);
    }
}

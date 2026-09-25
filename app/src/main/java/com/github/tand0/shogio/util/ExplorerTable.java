package com.github.tand0.shogio.util;

import java.util.List;
import java.util.Locale;

/**
 * 手とテーブルを合体させたもの(探索テーブル)
 */
public class ExplorerTable extends TeTable implements Comparable<ExplorerTable> {

    /** 先手詰み評価 */
    public static final float MATE_MAX = 10.0f;

    /** 先手詰まされ評価 */
    public static final float MATE_MIN = -10.0f;

    /** 評価値 */
    private float eval;

    /** wait */
    private List<ExplorerTable> explorerTableList = null;

    /**
     * コンストラクタ
     * @param teTable 手テーブル
     */
    public ExplorerTable(TeTable teTable) {
        super(teTable.table,teTable.te);
        this.eval = EvalTeTable.getWinLosToRate(0,0);
    }

    /** Eval の取得
     *
     * @return eval
     */
    public float getEval() {
        return this.eval;
    }

    /**
     * eval の設定
     * @param eval eval
     */
    public void setEval(float eval) {
        this.eval = eval;
    }

    /**
     * 枝が展開されている場合 True
     * @return True: 展開されている, False: 展開されていない
     */
    public boolean isExpand() {
        return explorerTableList != null;
    }

    /**
     * テーブルリストを追加する
     * @param explorerTableList テーブルリスト
     */
    public void setList(List<ExplorerTable> explorerTableList) {
        this.explorerTableList = explorerTableList;
    }

    /**
     * テーブルリストを取得する
     * @return explorerTableList テーブルリスト
     */
    public List<ExplorerTable>  getList() {
        return this.explorerTableList;
    }

    /**
     * リストをクリアする
     */
    public void clearList() {
        if (isExpand()) {
            this.explorerTableList.clear();
        }
        this.explorerTableList = null;
    }


    @Override
    public String display() {
        return super.display() +
            String.format(Locale.getDefault(), " %6.2f%%",eval * 100);
    }

    @Override
    public boolean equals(Object x) {
        return switch (x) {
            case TeTable teTable -> table.equals(teTable.table);
            case Table ignored -> table.equals(x);
            case null, default -> false;
        };
    }

    @Override
    public int compareTo(ExplorerTable o) {
        if (this.equals(o)) {
            return 0; // 局面が同じなら同じになる
        }
        // 差を出す
        float diff = o.eval - this.eval;
        // 手番に応じて符号を反転
        return (this.table.getTeban() == 0)
                ? (diff > 0 ? -1 : 1)
                : (diff > 0 ? 1 : -1);
    }
}

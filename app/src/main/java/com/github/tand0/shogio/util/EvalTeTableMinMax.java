package com.github.tand0.shogio.util;

import java.util.Locale;

/** min/max用の評価データ */
public class EvalTeTableMinMax extends EvalTeTable {
    /**
     * コンストラクタ
     * @param table テーブル情報
     * @param eval 評価値
     * @param te 手数
     * @param nowLevel 現在の手数
     */
    public EvalTeTableMinMax(Table table, int te, float eval,long nowLevel) {
        super(table, te, eval, nowLevel, 0);
    }

    /**
     * 手数情報
     * @return 手数情報
     */
    public long nowLevel() {
        return win;
    }

    @Override
    public String display() {
        return super.display() +
                String.format(Locale.getDefault(),
                        " %6.2f%% te:%,d",
                        eval * 100, nowLevel());
    }

}

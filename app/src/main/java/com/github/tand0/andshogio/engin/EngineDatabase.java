package com.github.tand0.andshogio.engin;

import com.github.tand0.andshogio.util.EvalTeTable;
import com.github.tand0.andshogio.util.MainDatabase;
import com.github.tand0.andshogio.util.Table;
import com.github.tand0.andshogio.util.TableDefine;
import com.github.tand0.andshogio.util.TableKey;
import com.github.tand0.andshogio.util.TeTable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/** 定跡がある場合、定跡の結果を渡す */
public class EngineDatabase extends Engine {

    /** engine 名を取得する
     * @return engine 名
     */
    @Override
    public String getEngineId() {
        return "engine_database";
    }

    /** ダメなテーブルのセット */
    private final Set<TeTable> topOpBadTeableSet;

    /** 最善手情報 */
    private final MainDatabase db;

    /** コンストラクタ
     * @param db データベース情報
     * @param topOpBadTeableSet ダメなテーブルのセット
     */
    public EngineDatabase(MainDatabase db, Set<TeTable> topOpBadTeableSet) {
        super();
        this.db = db;
        this.topOpBadTeableSet = topOpBadTeableSet;
    }

    @Override
    public void createNow(EngineRequest r) {
        Table table = r.teTableList().getLast().table;
        List<TeTable> nextTeTableList = table.createChild();
        nextTeTableList.removeAll(this.topOpBadTeableSet); // ダメな手は外す
        List<TeTable> displayTeTableList = (r.display()) ?  new ArrayList<>() : null;
        int te;
        float eval = 0.5f;
        int teban = table.getTeban();
        if (nextTeTableList.isEmpty()) {
            te = 0;
        } else {
            te = 0;
            // 単純に指してる手が多い方を選ぶ
            long best = Integer.MIN_VALUE;
            for (TeTable teTable: nextTeTableList) {
                TableKey tableKey = new TableKey(teTable.table);
                long[] key = tableKey.getKey();
                long[] winLos = db.getData(key);
                if (winLos != null) {
                    long win = winLos[0];
                    long los = winLos[1];
                    long now = (teban == 0) ? win : los;
                    if (best < now) {
                        best = now;
                        te = teTable.te;
                        eval = ((win + los) == 0) ?
                                0.5f : (float) win / (float) (win + los);
                    }
                    if (r.display()) {
                        // 表示モードの時はこちら
                        displayTeTableList.add(
                                new EvalTeTable(teTable.table, teTable.te, win, los));
                    }
                }
            }
            if (r.display()) {
                // 表示モードの時はこちら
                displayTeTableList = displayTeTableList.stream()
                        .map(x->(EvalTeTable)x)
                        .sorted(Comparator.comparing(
                                x->Integer.MAX_VALUE - x.win - x.los))
                        .collect(Collectors.toList());
                // 先頭に前局面を乗せる
                displayTeTableList.addFirst(new TeTable(table, TableDefine.LOS));
            }
        }
        this.setNow(new EngineResponse(
                r.turn(), te, EngineDatabase.class,
                displayTeTableList, eval));
    }

}

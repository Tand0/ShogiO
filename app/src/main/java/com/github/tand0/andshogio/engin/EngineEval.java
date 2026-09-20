package com.github.tand0.andshogio.engin;

import com.github.tand0.andshogio.util.ExplorerTable;
import com.github.tand0.andshogio.util.TensoInterface;
import com.github.tand0.andshogio.util.Table;
import com.github.tand0.andshogio.util.TableDefine;
import com.github.tand0.andshogio.util.TeTable;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/** 評価値用エンジン */
public class EngineEval extends Engine {

    /** レイヤインタフェース */
    private final TensoInterface tensoInterface;

    /** ダメなテーブルのセット */
    private final Set<TeTable> topOpBadTeableSet;

    /** engine 名を取得する
     * @return engine 名
     */
    @Override
    public String getEngineId() {
        return "engine_eval";
    }

    /** コンストラクタ
     *
     * @param tensoInterface レイヤーのインタフェース
     * @param topOpBadTeableSet ダメなテーブルのセット
     */
    public EngineEval(TensoInterface tensoInterface, Set<TeTable> topOpBadTeableSet) {
        super();
        this.tensoInterface = tensoInterface;
        this.topOpBadTeableSet = topOpBadTeableSet;
    }

    @Override
    public void createNow(EngineRequest r) {
        Table table = r.teTableList().getLast().table;
        List<TeTable> nextTeTableList = table.createChild();
        nextTeTableList.removeAll(topOpBadTeableSet); // 最悪手を削除する
        Set<ExplorerTable> displayTeTableSet;
        float eval;
        if (r.display()) {
            displayTeTableSet = new TreeSet<>();
        } else {
            displayTeTableSet = null;
        }
        List<TeTable> displayTeTableList = null;
        int te;
        if (nextTeTableList.isEmpty()) {
            te = TableDefine.LOS;
            eval = (table.getTeban() == 0) ? 0 : 1.0f;
        } else {
            //
            te = TableDefine.LOS; // 初期値
            int myTurn = table.getTeban(); // 手番
            eval = (myTurn == 0) ? Float.MIN_VALUE : Float.MAX_VALUE;
            for (int i = 0 ; i < nextTeTableList.size() ; i++) {
                TeTable teTable = nextTeTableList.get(i);
                Float result = this.tensoInterface.runCompiledModel(teTable.table);
                if (result == null) {
                    continue; // 初期化が終わってない
                }
                if (myTurn == 0) { // 先手は数値が大きい方が良い
                    if (eval <= result) {
                        te = teTable.te;
                        eval = result;
                    }
                } else { // 後手は数値が小さい方が良い
                    if (result <= eval) {
                        te = teTable.te;
                        eval = result;
                    }
                }
                if (r.display()) {
                    ExplorerTable eTable = new ExplorerTable(teTable);
                    eTable.setEval(result);
                    displayTeTableSet.add(eTable);
                }
            }
        }
        if (r.display()) {
            // 表示モードの時はこちら
            displayTeTableList = new ArrayList<>(displayTeTableSet);
            // 先頭に前局面を乗せる
            displayTeTableList.addFirst(new TeTable(table,TableDefine.LOS));
        }
        this.setNow(new EngineResponse(
                r.turn(), te, EngineEval.class,
                displayTeTableList, eval));
    }
}

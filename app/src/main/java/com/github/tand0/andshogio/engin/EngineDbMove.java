package com.github.tand0.andshogio.engin;

import com.github.tand0.andshogio.util.EvalTeTable;
import com.github.tand0.andshogio.util.MainDatabase;
import com.github.tand0.andshogio.util.EvalMoveResult;
import com.github.tand0.andshogio.util.TableDefine;
import com.github.tand0.andshogio.util.TableKey;
import com.github.tand0.andshogio.util.TeTable;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/** 定跡がある場合、探索する
  */
public class EngineDbMove extends Engine {

    /** engine 名を取得する
     * @return engine 名
     */
    @Override
    public String getEngineId() {
        return "engine_db_move";
    }


    /** 最善手情報 */
    private final MainDatabase db;

    /** コンストラクタ
     * @param db データベース情報
     */
    public EngineDbMove(MainDatabase db) {
        this.db = db;
    }

    @Override
    public void createNow(EngineRequest r) {
        Set<EvalTeTable> displayTeTableSet;
        if (r.display()) {
            displayTeTableSet = new TreeSet<>();
        } else {
            displayTeTableSet = null;
        }
        float eval = 0.5f;
        for (TeTable teTable : r.teTableList().getLast().table.createChild()) {
            TableKey key = new TableKey(teTable.table);
            long[] result = db.getData(key.getKey());
            if (result == null) {
                return; // DBが取れなかった
            }
            EvalMoveResult moveResult = TableDefine.getEvalMoveList(db, teTable.table, result[0], result[1]);
            if (r.display()) {
                //
                EvalTeTable evalTable = new EvalTeTable(teTable.table, teTable.te, moveResult.win(), moveResult.los());
                displayTeTableSet.add(evalTable);
                eval = evalTable.eval;
                //
            }
        }
        List<TeTable> displayTeTableList;
        if (r.display()) {
            // 表示モードの時はこちら
            displayTeTableList =  new ArrayList<>(displayTeTableSet);
            // 先頭に前局面を乗せる
            displayTeTableList.addFirst(r.teTableList().getLast());
        } else {
            displayTeTableList = null;
        }
        this.setNow(new EngineResponse(
                r.turn(), TableDefine.LOS, EngineDatabase.class,
                displayTeTableList, eval));
    }
}

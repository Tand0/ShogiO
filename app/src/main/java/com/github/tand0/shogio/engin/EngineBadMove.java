package com.github.tand0.shogio.engin;

import com.github.tand0.shogio.util.BadMoveResult;
import com.github.tand0.shogio.util.EvalTeTable;
import com.github.tand0.shogio.util.MainDatabase;
import com.github.tand0.shogio.util.Table;
import com.github.tand0.shogio.util.TableDefine;
import com.github.tand0.shogio.util.TeTable;

import java.util.ArrayList;
import java.util.List;

/** 定跡がある場合、定跡以外の合法手をリストアップする。
  */
public class EngineBadMove extends Engine {

    /** engine 名を取得する
     * @return engine 名
     */
    @Override
    public String getEngineId() {
        return "engine_bad_move";
    }


    /** 最善手情報 */
    private final MainDatabase db;

    /** コンストラクタ
     * @param db データベース情報
     */
    public EngineBadMove(MainDatabase db) {
        this.db = db;
    }

    @Override
    public void createNow(EngineRequest r) {
        Table table = r.teTableList().getLast().table;
        BadMoveResult b = TableDefine.getBadMoveList(db, table);
        List<TeTable> displayTeTableList;
        if (r.display()) {
            displayTeTableList = new ArrayList<>();
            //
            // もとに戻す手を先頭の乗せておく
            displayTeTableList.addFirst(new TeTable(table,TableDefine.LOS));
            //
            if (b != null) {
                for (TeTable teTable : b.teTableList()) {
                    EvalTeTable eTable = new EvalTeTable(teTable.table, teTable.te, b.win(), b.los());
                    displayTeTableList.add(eTable);
                }
            }
        } else {
            // 空を返す
            displayTeTableList = null;
        }
        this.setNow(new EngineResponse(
                r.turn(), TableDefine.LOS, EngineDatabase.class,
                displayTeTableList, 0.5f));
    }
}

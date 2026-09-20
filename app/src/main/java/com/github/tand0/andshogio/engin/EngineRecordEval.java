package com.github.tand0.andshogio.engin;

import com.github.tand0.andshogio.util.EvalTeTable;
import com.github.tand0.andshogio.util.TensoInterface;
import com.github.tand0.andshogio.util.TableDefine;
import com.github.tand0.andshogio.util.TeTable;

import java.util.ArrayList;
import java.util.List;

/** 棋譜上の評価値を表示する */
public class EngineRecordEval extends Engine {
    /** レイヤインタフェース */
    private final TensoInterface tensoInterface;

    /** engine 名を取得する
     * @return engine 名
     */
    @Override
    public String getEngineId() {
        return "engine_turn";
    }

    /** コンストラクタ
     * @param tensoInterface レイヤーのインタフェース
     */
    public EngineRecordEval(TensoInterface tensoInterface) {
        super();
        this.tensoInterface = tensoInterface;
    }

    @Override
    public void createNow(EngineRequest r) {
        //
        if (r == null) {
            return; // r が入ってなければ終了
        }
        //
        if (r.teTableList().isEmpty()) {
            return; // データ取れない
        }
        //
        List<TeTable> displayList = r.display() ? new ArrayList<>() : null;
        if (r.display()) {
            //
            for (int i = 0 ; i < r.teTableList().size() ; i++) {
                TeTable teTable = r.teTableList().get(i);
                Float result = this.tensoInterface.runCompiledModel(teTable.table);
                if (result == null) {
                    continue; // 初期化が終わってない
                }
                long win = (int) (result * 1000f);
                long los = (int) ((1.0f - result) * 1000f);
                EvalTeTable eTable = new EvalTeTable(teTable.table, teTable.te, result, win,los);
                displayList.add(eTable);
            }
        }
        this.setNow(new EngineResponse(
                r.turn(), TableDefine.LOS, EngineDatabase.class,
                displayList, 0.5f));
    }
}

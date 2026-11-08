package com.github.tand0.shogio.engin;

import com.github.tand0.shogio.util.EvalTeTable;
import com.github.tand0.shogio.util.TensoInterface;
import com.github.tand0.shogio.util.Table;
import com.github.tand0.shogio.util.TableDefine;
import com.github.tand0.shogio.util.TeTable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/** 評価値用エンジン */
public class EngineEval extends Engine {

    /** レイヤインタフェース */
    private final TensoInterface tensoInterface;

    /** ダメなテーブルのセット */
    private final Set<Table> topOpBadTeableSet = new HashSet<>();

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
        for (TeTable teTable : topOpBadTeableSet) {
            this.topOpBadTeableSet.add(teTable.table);
        }
    }

    @Override
    public void createNow(EngineRequest r) {
        Table first = r.teTableList().getLast().table;
        //
        //
        List<EvalTeTable> displayList = first.createChild((table,y)-> {
                float result = this.tensoInterface.runCompiledModel(table);
                if (topOpBadTeableSet.contains(table)) {
                    return null;  // すでに存在するので見ない
                }
                // tableが書き換わらないように cloneを取る
                return new EvalTeTable(table.clone(), y, result, 0, 0);
            }).stream() // streamを取る
            .sorted()  // 評価値でソート
            .collect(Collectors.toCollection(ArrayList::new)); // 後で追加できるリストを作る
        if (displayList.isEmpty()) {  // 詰んだ
            return;
        }
        if (TableDefine.checkWinTeTableList(displayList)) {  // 勝利した
            return;
        }
        //
        // eval 値を決定する (先にisEmpty()チェックしているのでnullになることはない)
        EvalTeTable target = displayList.getFirst();
        if (r.display()) {
            // 表示モードの時はこちら
            // 先頭に前局面を乗せる
            displayList.addFirst(new EvalTeTable(first,TableDefine.LOS,0,0));
        } else {
            displayList = null;
        }
        this.setNow(new EngineResponse(
                r.turn(), target.te, EngineEval.class,
                displayList, target.eval));
    }
}

package com.github.tand0.shogio.engin;

import com.github.tand0.shogio.util.Table;
import com.github.tand0.shogio.util.TableDefine;
import com.github.tand0.shogio.util.TeTable;

import java.util.List;
import java.util.Random;

/** 合法手を探してランダムに指す */
public class EngineRandom extends Engine {
    /** engine 名を取得する
     * @return engine 名
     */
    @Override
    public String getEngineId() {
        return "engine_random";
    }

    /** コンストラクタ */
    public EngineRandom() {
        super();
    }

    @Override
    public void createNow(EngineRequest r) {
        Table table = r.teTableList().getLast().table;
        // 合法手の取得
        List<TeTable> nextTeTableList = table.createChild();
        int te;
        if (nextTeTableList.isEmpty()) {
            te = TableDefine.LOS; // 移動できない⇒投了する
        } else {
            // 合法手の中からランダムな手を得る
            Random rand = new Random();
            int index = rand.nextInt(nextTeTableList.size());
            TeTable randomElement = nextTeTableList.get(index);
            //
            te = randomElement.te;
        }
        if (!r.display()) {
            nextTeTableList = null;
        } else {
            // もとに戻す手を先頭の乗せておく
            nextTeTableList.addFirst(new TeTable(table,TableDefine.LOS));
        }
        float eval = 0.5f;
        this.setNow(new EngineResponse(
                r.turn(), te, EngineRandom.class,
                nextTeTableList, eval));
    }
}

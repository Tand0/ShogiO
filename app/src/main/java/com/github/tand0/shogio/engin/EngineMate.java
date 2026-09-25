package com.github.tand0.shogio.engin;

import com.github.tand0.shogio.util.PnDnTable;
import com.github.tand0.shogio.util.TeTable;

import java.util.ArrayList;
import java.util.List;

/** 詰みエンジン */
public class EngineMate extends Engine {

    /** engine 名を取得する
     * @return engine 名
     */
    @Override
    public String getEngineId() {
        return "engine_mate";
    }

    /** コンストラクタ */
    public EngineMate() {
        super();
    }

    @Override
    public void createNow(EngineRequest r) {
        //
        if (r == null) {
            fail(null);
            return; // r が入ってなければ終了
        }
        // テーブルを取得する
        TeTable lastTable = r.teTableList().getLast();
        if (lastTable == null) {
            fail(r);
            return; // データ取れない
        }
        //
        PnDnTable target = emi.createNow(lastTable);
        //
        // displayするか否か設定する
        List<TeTable> displayList;
        if (r.display()) { // 詰み筋を表示ルーチン側に渡す
            displayList = new ArrayList<>();
            if (target != null) {
                displayList.add(target);
            }
        } else {
            // 表示モードで無い場合はクリア
            displayList = null;
        }
        // 結果を出す
        if (target != null) {
            int or = 0;
            float eval;
            if (target.getPn(or) == 0) {
                eval = (lastTable.table.getTeban() == 0) ? 1.0f : 0.0f;
            } else {
                eval = 0.5f;
            }
            this.setNow(new EngineResponse(
                    r.turn(), target.te, EngineMateImpl.class,
                    displayList, eval));
        }
    }

    /** 詰みエンジン実装 */
    private final EngineMateImpl emi = new EngineMateImpl() {
        @Override
        public boolean isEnd() {
            return EngineMate.this.isEnd();
        }
    };
}

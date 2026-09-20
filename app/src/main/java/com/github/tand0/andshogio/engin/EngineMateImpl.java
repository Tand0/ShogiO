package com.github.tand0.andshogio.engin;

import com.github.tand0.andshogio.util.PnDnTable;
import com.github.tand0.andshogio.util.Table;
import com.github.tand0.andshogio.util.TableDefine;
import com.github.tand0.andshogio.util.TeTable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

/** 詰みエンジン */
public abstract class EngineMateImpl {
    /** 学習する最大局面数 */
    private static final int MAX_TABLE = 100000;

    /** 学習を停止する
     * 要するに、キューになんか来たら
     * @return キューが空でないなら true
     */
    public abstract boolean isEnd();

    /**
     * 詰みがあるか確認する
     * @param lastTable テーブル情報
     * @return EvalTeTable
     */
    public PnDnTable createNow(TeTable lastTable) {
        int or = 0; // or node 選択
        PnDnTable next = new PnDnTable(lastTable, 1);
        PnDnTable target = null;
        //
        // 同一局面外し用の HashSet
        HashMap<Table, PnDnTable> factory = new HashMap<>();
        //
        // 同一手回避用の HashSet
        HashSet<PnDnTable> stack = new HashSet<>();
        //
        stack.add(next);
        try {
            while ((factory.size() < MAX_TABLE)
                    && (next.getPn(or) != 0)
                    && (next.getDn(or) != 0)
                    && (!isEnd())) {
                calculateNode(or, next, factory, stack);
            }
            if (next.getPn(or) == 0) {
                target = next.getFirstList(); // 詰ませた
            }
        } finally {
            for (PnDnTable work : factory.values()) {
                work.clearList();
            }
            factory.clear();
            stack.clear();
        }
        //
        // 結果を出す
        return target;
    }

    /** PNDNの計算実際部分
     *
     * @param or 0: OR ノード、 1: AND ノード
     * @param t 現局面
     * @param factory 工場。同一局面は同一の PnDnを使う
     * @param stack スタック。同一局面を処理するのを防ぐ。
     */
    public void calculateNode(
            int or, PnDnTable t,
            HashMap<Table, PnDnTable> factory, HashSet<PnDnTable> stack) {
        if ((MAX_TABLE <= factory.size())
                || (t.getPn(or) == 0)
                || (t.getDn(or) == 0)
                || isEnd()) {
            return; // これ以上の計算不要
        }
        //
        if (! t.isExpand()) { // 展開されていない
            List<TeTable> teTableList = t.table.createChild();
            if (teTableList.isEmpty()) {
                // target が null ということは、値が一つもない＝お前はもう詰んでいる
                t.setDn(or, 0); // or条件:自局が詰んでいる(不詰み)、and条件:詰み
                t.setPn(or, Integer.MAX_VALUE);
                return;
            }
            if (TableDefine.checkWinTeTableList(teTableList)) {
                t.setPn(or, 0); // or条件:自局が勝ち(詰ませている), and条件:不詰み
                t.setDn(or, Integer.MAX_VALUE);
                //
                // 追加
                List<PnDnTable> list = new ArrayList<>();
                PnDnTable winTable = new PnDnTable(teTableList.getFirst(), TableDefine.WIN);
                winTable.setPn(or, 0); // or条件:自局が勝ち(詰ませている), and条件:不詰み
                winTable.setDn(or, Integer.MAX_VALUE);
                list.add(winTable);
                t.setList(list);
                return;
            }
            //
            List<PnDnTable> resultList = new ArrayList<>();
            for (TeTable teTable : teTableList) {
                if ((or == 0) && (!teTable.table.isOute(teTable.table.getTeban()))) {
                    // Or条件の場合は相手への王手でなければならないので、
                    // Or条件の王手でなければ繰り返す
                    continue;
                }
                PnDnTable pnDnTable = factory.get(teTable.table);
                if (pnDnTable == null) {
                    // factory にいなければ生成
                    pnDnTable = new PnDnTable(teTable, (stack.size() / 5) + 1);
                    // factory にいなければ追加
                    factory.put(teTable.table, pnDnTable);
                }
                resultList.add(pnDnTable);
            }
            // collect 部分
            t.setList(resultList);
            return;
        }
        //
        // 実装された
        // Dn==0なら削除する
        t.removeDnZeroList(or);
        //
        // 最小のターゲットえを得る
        PnDnTable target = t.getMinPnList(or);
        //
        if (target != null) {
            stack.add(target); // スタックを詰む
            calculateNode(1 - or, target, factory, stack); // and/or条件を反転させて子供を計算
            stack.remove(target); // スタックを詰む
            //
            if (target.getDn(or) == 0) {
                // Dn==0なら削除する
                t.removeDnZeroList(or);
                //
            } else if (target.getPn(or) == 0) {
                // Pn==0なら
                target.setDn(or, Integer.MAX_VALUE);
                t.setPn(or, 0);
                t.setDn(or, Integer.MAX_VALUE);
                //
                // ターゲットを残して前消し
                t.clearList(target);
                return;
            }
        } else {
            // target が null ということは、値が一つもない＝お前はもう詰んでいる
            t.setPn(or, Integer.MAX_VALUE);
            t.setDn(or, 0); // or条件:自局が詰んでいる(不詰み)、and条件:詰み
        }
        //
        t.setPnMin(or); // Or条件: Pnは最小値を得る, And条件: Dnは最小値を得る
        t.setDnSum(or); // Or条件: Dnは合計値を得る, And条件: Pnは合計値を得る
    }

}

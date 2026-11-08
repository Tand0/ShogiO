package com.github.tand0.shogio.engin;

import com.github.tand0.shogio.util.AllBitboard;import com.github.tand0.shogio.util.PnDnTable;
import com.github.tand0.shogio.util.Table;
import com.github.tand0.shogio.util.TableDefine;
import com.github.tand0.shogio.util.TeTable;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.List;

/** 詰みエンジン */
public abstract class EngineMateImpl {
    /** 学習する最大局面数 */
    private static final int MAX_TABLE = 100000;

    /** 読み深さ */
    private static final int MAX_LEVEL = 30;

    /** 学習を停止する
     * 要するに、キューになんか来たら
     * @return キューが空でないなら true
     */
    public abstract boolean isEnd();

    /**
     * 同一局面回避用
     */
    private final ArrayDeque<PnDnTable> stack = new ArrayDeque<>(MAX_LEVEL);

    /** 工場
     */
    private final HashMap<Table, PnDnTable> factory = new HashMap<>();

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
        try {
            stack.add(next);
            PnDnTable calculateResult = null;
            while ((factory.size() < MAX_TABLE)
                    && (next.getPn(or) != 0)
                    && (next.getDn(or) != 0)
                    && (!isEnd())) {
                calculateResult = calculateNode(or, next);
            }
            if (next.getPn(or) == 0) {
                target = calculateResult; // 詰ませた
            }
        } finally {
            factory.clear();
            stack.clear();
        }
        //
        // 結果を出す
        return target;
    }

    /** PnDn の計算実際部分
     *
     * @param or 0: OR ノード、 1: AND ノード
     * @param t 現局面
     */
    public PnDnTable calculateNode(
            int or, PnDnTable t) {
        if ((MAX_TABLE <= factory.size())
                || (MAX_LEVEL <= stack.size())
                || isEnd()) {
            // 強制で終わらせる
            t.setDn(0, 0);  // or条件:不詰み
            t.setPn(0, Integer.MAX_VALUE);
            t.setDn(1, Integer.MAX_VALUE);  // and条件:不詰み
            t.setPn(1, 0);
            return null; // これ以上の計算不要
        }
        if ((t.getPn(or) == 0)|| (t.getDn(or) == 0)) {
            return null;
        }
        //
        List<PnDnTable> resultList = t.table.createChild((
                (x,te)-> findTable(or, t, x, te)));
        if (resultList.isEmpty()) {
            // target が null ということは、指せる手(orの時は王手を掛けられる手)が一つもない
            t.setDn(or, 0); // or条件:詰み局面がない(不詰み)、and条件:詰み
            t.setPn(or, Integer.MAX_VALUE);
            return null;
        }
        if (TableDefine.checkWinTeTableList(resultList)) {
            return resultList.getFirst();
        }
        // collect 部分
        if (! t.isExpand()) {
            t.setExpand();
            if (!resultList.isEmpty()) {
                return resultList.getFirst();
            }
            return null;
        }
        //
        // すでに展開されている
        // Dn==0なら削除する
        resultList.removeIf(obj -> obj.getDn(or) == 0);
        //
        // 最小のターゲットえを得る
        PnDnTable target = null;
        while (true) {
            if (resultList.isEmpty()) break;  // empty なら終了
            target = this.getMinPnList(resultList, or);  // 最小の手を取る
            if (target == null) break;  // 得られなかったら終了
            if (! stack.contains(target)) break;  // 同じ手が無ければ終了
            resultList.remove(target);  // 同じ手はループするので考慮しない
        }
        //
        if (target != null) {
            stack.add(target); // スタックを詰む
            calculateNode(1 - or, target); // and/or条件を反転させて子供を計算
            stack.remove(target); // スタックを詰む
            //
            if (target.getDn(or) == 0) {
                // Dn==0なら削除する
                resultList.removeIf(obj -> obj.getDn(or) == 0);
                //
            } else if (target.getPn(or) == 0) {
                // Pn==0なら
                target.setDn(or, Integer.MAX_VALUE);
                t.setPn(or, 0);
                t.setDn(or, Integer.MAX_VALUE);
                //
                // ターゲットを残して前消し
                resultList.clear();
                return target;
            }
        } else {
            // target が null ということは、値が一つもない＝お前はもう詰んでいる
            t.setPn(or, Integer.MAX_VALUE);
            t.setDn(or, 0); // or条件:自局が詰んでいる(不詰み)、and条件:詰み
        }
        //
        t.setPnMin(resultList, or); // Or条件: Pnは最小値を得る, And条件: Dnは最小値を得る
        t.setDnSum(resultList, or); // Or条件: Dnは合計値を得る, And条件: Pnは合計値を得る
        return target;
    }

    /**
     * リストから最も小さい Pn を取得する
     * @param or  0: Or ノード、1: And ノード
     * @return 最も小さい Pn
     */
	public PnDnTable getMinPnList(List<PnDnTable> pnDnTableList, int or) {
		PnDnTable best = null;
		int bestValue = Integer.MAX_VALUE;
        //
		for (PnDnTable x : pnDnTableList) {
			int value = x.getPn(or);
			if (value < bestValue) {
				bestValue = value;
				best = x;
				if (bestValue == 0) {
					break; // or条件:自局が勝ち(詰ませている), and条件:不詰み
				}
			}
		}
		return best;
	}

    /**
     * テーブルを生成する
     * @param or 0: OR ノード、 1: AND ノード
     * @param t 現局面
     * @param x 番目
     * @param te 手
     * @return PnDnTable PnDnTableを返す。不要な場合はnullを返す
     */
    protected PnDnTable findTable(int or, PnDnTable t, Table x, int te) {
        AllBitboard all = new AllBitboard(t.table);
        if (te == TableDefine.WIN) {
            t.setPn(or, 0);  // or条件:自局が勝ち(詰ませている), and条件:不詰み
            t.setDn(or, Integer.MAX_VALUE);
            // cloneを使って変更されないようにコピーを取る
            PnDnTable winTable = new PnDnTable(new TeTable(x.clone(), te), te);
            winTable.setPn(or, 0);  // or条件:自局が勝ち(詰ませている), and条件:不詰み
            winTable.setDn(or, Integer.MAX_VALUE);
            return winTable;
        }
        if (or == 0) {
            all.clear(x);
            if (!x.checkSelfMate(x.getSide(), all)) {
                // Or条件の場合は相手への王手でなければならないので、
                // Or条件の王手でなければ生成しない。
                // 生成しないので clone は取らなない
                return null;
            }
        }
        PnDnTable pnDnTable = factory.get(x);
        if (pnDnTable == null) {
            // factory にいなければ生成 (factoryにいるならcloneは取ら無くて良い)
            // cloneを使って変更されないようにコピーを取る
            pnDnTable = new PnDnTable(
                    new TeTable(x.clone(), te), (stack.size() / 5) + 1);
            // factory にいなければ追加
            factory.put(pnDnTable.table, pnDnTable);
        }
        return pnDnTable;
    }
}

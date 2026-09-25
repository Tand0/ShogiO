package com.github.tand0.shogio.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** PnDn 値を保持する */
public class PnDnTable extends TeTable {
	/** Pn dn 値 */
	private final int[] pndn = new int[2];

	/** wait */
	private List<PnDnTable> pnDnTableList = null;

	/**
	 * コンストラクタ
	 * @param teTable 手テーブル
	 * @param wait 重みづけ
	 */
	public PnDnTable(TeTable teTable, int wait) {
		super(teTable.table,teTable.te);
		this.pndn[0] = wait; // 重みづけ
		this.pndn[1] = wait; // 重みづけ
	}

	/** Pn の取得。
	 * Pn値が 0 のとき、or条件:詰み, and条件:不詰み。
	 * Pn値が Integer.MAX_VALUE のとき、or条件:不詰み, and条件:詰み。
	 * @param or  0: Or ノード、1: And ノード
	 * @return Pn
	 */
	public int getPn(int or) {
		return this.pndn[or];
	}

	/**
	 * Dn の取得。
	 * Dn値が 0 のとき、or条件:不詰み, and条件:詰み。
	 * Dn値が Integer.MAX_VALUE のとき、or条件:詰み, and条件:不詰み。
	 * @param or  0: Or ノード、1: And ノード
	 * @return Dn
	 */
	public int getDn(int or) {
		return this.pndn[1 - or];
	}

	/**
	 * Pn の設定。
	 * Pn値が 0 のとき、or条件:詰み, and条件:不詰み。
	 * Pn値が Integer.MAX_VALUE のとき、or条件:不詰み, and条件:詰み。
	 * @param or  0: Or ノード、1: And ノード
	 * @param pn Pn
	 */
	public void setPn(int or, int pn) {
		this.pndn[or] = pn;
	}

	/**
	 * Dn の設定。
	 * Dn値が 0 のとき、or条件:不詰み, and条件:詰み。
	 * Dn値が Integer.MAX_VALUE のとき、or条件:詰み, and条件:不詰み。
	 * @param or  0: Or ノード、1: And ノード
	 * @param dn Dn
	 */
	public void setDn(int or, int dn) {
		this.pndn[1 - or] = dn;
	}

	/**
	 * 枝が展開されている場合 True
	 * @return True: 展開されている, False: 展開されていない
	 */
	public boolean isExpand() {
		return pnDnTableList != null;
	}

	/**
	 * テーブルリストを追加する
	 * @param pnDnTableList テーブルリスト
	 */
	public void setList(List<PnDnTable> pnDnTableList) {
		this.pnDnTableList = pnDnTableList;
	}

	/**
	 * リストの最初のテーブルを取得する
	 * @return テーブル
	 */
	public PnDnTable getFirstList() {
		if (! isExpand()) {
			return null;
		}
		return this.pnDnTableList.getFirst();
	}

	/**
	 * リストから最も小さい Pn を取得する
	 * @param or  0: Or ノード、1: And ノード
	 * @return 最も小さい Pn
	 */
	public PnDnTable getMinPnList(int or) {
		if (! isExpand()) {
			return null;
		}
		PnDnTable best = null;
		int bestValue = Integer.MAX_VALUE;

		for (PnDnTable x : this.pnDnTableList) {
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
	 * リストから Dn 値がゼロになるテーブルを削除する
	 * @param or  0: Or ノード、1: And ノード
	 */
	public void removeDnZeroList(int or) {
		if (! isExpand()) {
			return;
		}
		this.pnDnTableList.removeIf(obj -> obj.getDn(or) == 0);
	}

	/**
	 * リストをクリアする
	 */
	public void clearList() {
		if (isExpand()) {
			this.pnDnTableList.clear();
		}
		this.pnDnTableList = null;
	}

	/** 一つを除きテーブルをクリアする
	 * @param target テーブル
	 */
	public void clearList(PnDnTable target) {
		if (isExpand()) {
			this.pnDnTableList.clear();
		} else {
			this.pnDnTableList = new ArrayList<>();
		}
		this.pnDnTableList.add(target);
	}

	/**
	 * 最小の Pn を設定する
	 * @param or  0: Or ノード、1: And ノード
	 */
	public void setPnMin(int or) {
		int minValue = Integer.MAX_VALUE;
		for (PnDnTable x : this.pnDnTableList) {
			int value = x.getPn(or);
			if (value < minValue) {
				minValue = value;
				if (minValue == 0) {
					break;
				}
			}
		}
		this.setPn(or, minValue);
	}

	/**
	 * 最大の Dn を設定する
	 * @param or  0: Or ノード、1: And ノード
	 */
	public void setDnSum(int or) {
		int sum = 0;
		for (PnDnTable x : this.pnDnTableList) {
			int dn = x.getDn(or);
			if (dn == Integer.MAX_VALUE) {
				sum = Integer.MAX_VALUE;
				break;
			}
			sum += dn;
		}
		this.setDn(or, sum);
	}

	@Override
	public boolean equals(Object x) {
        return switch (x) {
            case TeTable teTable -> table.equals(teTable.table);
            case Table ignored -> table.equals(x);
            case null, default -> false;
        };
    }

	@Override
	public String display() {
		return super.display() +
				String.format(Locale.getDefault(),
						" pn[0]:%d dn[1]:%d", pndn[0], pndn[1]);
	}
}
package com.github.tand0.shogio.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/** PnDn 値を保持する */
public class PnDnTable extends TeTable {
	/** Pn dn 値 */
	private final int[] pnDn = new int[2];

	/** 拡張フラグ */
	public boolean expandFlag = false;

	/**
	 * コンストラクタ
	 * @param teTable 手テーブル
	 * @param wait 重みづけ
	 */
	public PnDnTable(TeTable teTable, int wait) {
		super(teTable.table,teTable.te);
		this.pnDn[0] = wait; // 重みづけ
		this.pnDn[1] = wait; // 重みづけ
	}

	/** Pn の取得。
	 * Pn値が 0 のとき、or条件:詰み, and条件:不詰み。
	 * Pn値が Integer.MAX_VALUE のとき、or条件:不詰み, and条件:詰み。
	 * @param or  0: Or ノード、1: And ノード
	 * @return Pn
	 */
	public int getPn(int or) {
		return this.pnDn[or];
	}

	/**
	 * Dn の取得。
	 * Dn値が 0 のとき、or条件:不詰み, and条件:詰み。
	 * Dn値が Integer.MAX_VALUE のとき、or条件:詰み, and条件:不詰み。
	 * @param or  0: Or ノード、1: And ノード
	 * @return Dn
	 */
	public int getDn(int or) {
		return this.pnDn[1 - or];
	}

	/**
	 * Pn の設定。
	 * Pn値が 0 のとき、or条件:詰み, and条件:不詰み。
	 * Pn値が Integer.MAX_VALUE のとき、or条件:不詰み, and条件:詰み。
	 * @param or  0: Or ノード、1: And ノード
	 * @param pn Pn
	 */
	public void setPn(int or, int pn) {
		this.pnDn[or] = pn;
	}

	/**
	 * Dn の設定。
	 * Dn値が 0 のとき、or条件:不詰み, and条件:詰み。
	 * Dn値が Integer.MAX_VALUE のとき、or条件:詰み, and条件:不詰み。
	 * @param or  0: Or ノード、1: And ノード
	 * @param dn Dn
	 */
	public void setDn(int or, int dn) {
		this.pnDn[1 - or] = dn;
	}

	/**
	 * 枝が展開されている場合 True
	 * @return True: 展開されている, False: 展開されていない
	 */
	public boolean isExpand() {
		return expandFlag;
		//return pnDnTableList != null;
	}

	/**
	 * 開いたことを示す
	 */
	public void setExpand() {
		expandFlag = true;
	}

	/**
	 * 最小の Pn を設定する
	 * @param or  0: Or ノード、1: And ノード
	 */
	public void setPnMin(List<PnDnTable> pnDnTableList, int or) {
		int minValue = Integer.MAX_VALUE;
		for (PnDnTable x : pnDnTableList) {
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
	public void setDnSum(List<PnDnTable> pnDnTableList, int or) {
		int sum = 0;
		for (PnDnTable x : pnDnTableList) {
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
						" pn[0]:%d dn[1]:%d", pnDn[0], pnDn[1]);
	}
}
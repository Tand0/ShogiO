package com.github.tand0.shogio.util;

/** keyを作成するときのポインターの設定 */
public class Pointer {
	/** 配列のどこを取るか */
	public int pos;	
    
    /** 出現した歩の数 */
    int pKSum = 0;

    /** 出現した香の数 */
    int pPSum = 0;

    /** 出現した桂の数 */
    int pNSum = 0;

    /** 出現した桂の数 */
    int pLSum = 0;

    /** 出現した銀の数 */
    int pSSum = 0;

    /** 出現した金の数 */
    int pGSum = 0;

    /** 出現した飛の数 */
    int pRSum = 0;
    
    /** 出現した角の数 */
    int pBSum = 0;
    
	/** コンストラクタ */
	public Pointer() {
	    pos = 0;
	}
	/** クリアする */
	public void clear() {
		pos = 0;
	}
    /** 持ちコマ超過チェック */
    public void checkSum() {
    	boolean flag =  (pPSum <= 18) && (pLSum <= 4) && (pNSum <= 4) && (pSSum <= 4) && (pGSum <= 4)
    			&& (pRSum <= 2) && (pBSum <= 2) && (pKSum <= 2);
    	if (! flag) {
    		throw new UnsupportedOperationException(
    			"NG p=" + pPSum + " l=" + pLSum + " n=" + pNSum +
    			" s=" + pSSum + " g=" + pGSum + " r=" + pRSum + " b=" + pBSum + " k=" + pKSum);
    	}
    }
    /**
     * すべてのコマを打ったチェック
     * @return 全てのコマを打ったらtrue
     */
    public boolean okSum() {
    	return (pPSum == 18) && (pLSum == 4) && (pNSum == 4) && (pSSum == 4) && (pGSum == 4)
    			&& (pRSum == 2) && (pBSum == 2) && (pKSum == 2);
    }
}
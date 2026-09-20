package com.github.tand0.andshogio.util;

/**
 * x,y,flagの情報を得る
 */
public class XYFlag {
    /** 移動先 x */
    private final int x;
    /** 移動先 y */
    private final int y;
    /** 複数移動する場合true (香,角,飛が対応) */
    private final boolean flag;
    /** コンストラクタ
     * @param x 移動先X
     * @param y 移動先Y
     * @param flag 複数移動するか？
     */
    public XYFlag(int x, int y, boolean flag) {
        this.x = x;
        this.y = y;
        this.flag = flag;
    }
    /**
     * 移動先x の取得
     * @return 移動先x
     */
    public int x() {
        return this.x;
    }
    /**
     * 移動先y
     * @param teban 手番(後手はプラスマイナスが反転する)
     * @return 移動先y
     */
    public int y(int teban) {
        return (teban == 0) ? y : -y;
    }
    /**
     * 飛、竜、角、馬、香車のように複数移動するならtrue
     * @return 複数移動するなら true
     */
    public boolean flag() {
        return flag;
    }
}

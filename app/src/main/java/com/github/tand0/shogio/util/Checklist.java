package com.github.tand0.shogio.util;


/**
 * 成りや打ちを管理する
 *
 * @param forcedNari 成ることが強制される位置。敵なら 8-xする(桂,香,銀,以外)
 * @param beatKinshi  成ることが強制される位置。敵なら 8-xする(桂,香,以外)
 * @param narazuOK    成れる場合、ならない手を合法にするか？ (桂,香,以外)
 * @param narenai     成れるコマか？ (金,王,以外)
 */
public record Checklist(int forcedNari, int beatKinshi, boolean narazuOK, boolean narenai) {
    /**
     * 合法手チェック用
     */
    public static final Checklist[] CHECKLIST = {
            new Checklist( 8,  0, false, true), //pNull 空白
            new Checklist(+1, +1, false, false ), // pP 歩
            new Checklist(+2, +1, true,  false ), // pL 香
            new Checklist(+2, +2, true,  false ), // pN 桂
            new Checklist( 0,  0, true,  false ), // pS 銀
            new Checklist( 8,  0, false, true  ), // pG 金
            new Checklist( 0,  0, false, false ), // pB 角
            new Checklist( 0,  0, false, false ), // pR 飛
            new Checklist( 8,  0, false, true  ),  //pK  王
    };
    /**
     * 成りを強制する
     *
     * @param side 手番
     * @param y     位置
     * @return 成りを強制する必要がある場合はtrue
     */
    public boolean getForcedNari(int side, int y) {
        return (side == 0) ? (y < forcedNari) : ((8 - forcedNari) < y);
    }

    /**
     * 敵陣ならばtrue
     *
     * @param side 手番 (0: 先手、1: 後手)
     * @param y     位置
     * @return 敵陣ならば true
     */
    public boolean isTekijin(int side, int y) {
        return (side == 0) ? (y <= 2) : (6 <= y);
    }

    /**
     * 打ち込み禁止位置に打ち込んでいないか？
     *
     * @param side 手番 (0: 先手、1: 後手)
     * @param y     位置
     * @return 打ち込めないならtrue
     */
    public boolean getUchiKinshi(int side, int y) {
        if (beatKinshi == 0) {
            return false; // 0ならどこでも打てる
        }
        return (side == 0) ? (y < beatKinshi) : ((8 - beatKinshi) < y);
    }

    /**
     * 成らずの考慮も必要か？／桂馬／銀／香車などは成らずを検討しないといけない。
     *
     * @return 成っているならtrue
     */
    @Override
    public boolean narazuOK() {
        return narazuOK;
    }

    /**
     * 成らない情報
     *
     * @return 成ることができないならtrue
     */
    @Override
    public boolean narenai() {
        return narenai;
    }
}
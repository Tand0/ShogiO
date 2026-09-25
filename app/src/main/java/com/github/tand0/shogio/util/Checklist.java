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
     * 成りを強制する
     *
     * @param teban 手番
     * @param y     位置
     * @return 成りを強制する必要がある場合はtrue
     */
    public boolean getForcedNari(int teban, int y) {
        return (teban == 0) ? (y < forcedNari) : ((8 - forcedNari) < y);
    }

    /**
     * 敵陣ならばtrue
     *
     * @param teban 手番
     * @param y     位置
     * @return 敵陣ならば true
     */
    public boolean isTekijin(int teban, int y) {
        return (teban == 0) ? (y <= 2) : (6 <= y);
    }

    /**
     * 打ち込み禁止位置に打ち込んでいないか？
     *
     * @param teban 手番
     * @param y     位置
     * @return 打ち込めないならtrue
     */
    public boolean getUchiKinshi(int teban, int y) {
        if (beatKinshi == 0) {
            return false; // 0ならどこでも打てる
        }
        return (teban == 0) ? (y < beatKinshi) : ((8 - beatKinshi) < y);
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
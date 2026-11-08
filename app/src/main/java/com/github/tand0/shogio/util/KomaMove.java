package com.github.tand0.shogio.util;

import static com.github.tand0.shogio.util.TableDefine.*;
import static com.github.tand0.shogio.util.Table.addNariKoma;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** コマの利きをチェックするクラス */
public class KomaMove {
    /** x,y,flagの情報 (x軸, y軸, 飛車のように先も見るならflagはtrue) */
    public final XYFlag xYFlag;
    /** コマの種類 */
    public final int[] koma;

    /** 合法手を作るときに使う配列(上左右に移動できるコマ) */
    public final static int[] CHECKLIST_up =   {
            pG, pS, addNariKoma(pS), addNariKoma(pL), addNariKoma(pN), addNariKoma(pP), addNariKoma(pR), pK };

    /** 合法手を作るときに使う配列(真上に移動できるコマ) */
    public final static int[] CHECKLIST_upUp = {
            pG, pS, addNariKoma(pS), addNariKoma(pL), addNariKoma(pN), addNariKoma(pP), addNariKoma(pB), pK, pP };

    /** 合法手を作るときに使う配列(横に移動できるコマ) */
    public final static int[] CHECKLIST_yoko = {
            pG, addNariKoma(pS), addNariKoma(pL), addNariKoma(pN), addNariKoma(pP), addNariKoma(pB), pK };

    /** 合法手を作るときに使う配列(下横に移動できるコマ) */
    public final static int[] CHECKLIST_downYoko = { pS, pK, addNariKoma(pR) };
    /** 合法手を作るときに使う配列(桂馬) */
    public final static int[] CHECKLIST_kei = { pN };
    /** 合法手を作るときに使う配列(上に複数移動できるコマ) */
    public final static int[] CHECKLIST_kyo = { pL, pR, addNariKoma(pR) };
    /** 合法手を作るときに使う配列(飛車) */
    public final static int[] CHECKLIST_rook = { pR, addNariKoma(pR) };
    /** 合法手を作るときに使う配列(角) */
    public final static int[] CHECKLIST_kaku = { pB, addNariKoma(pB) };

    /** 移動先が合法手である手を探す用(8方向用)
     * 飛角香車は別でやるので、成ったときだけ入れておく
     */
    public final static List<KomaMove> moversFlagFalse = Arrays.asList(
            // 8方向
            new KomaMove( 1, 1, CHECKLIST_downYoko,false),
            new KomaMove(-1, 1, CHECKLIST_downYoko,false),
            new KomaMove( 1,-1, CHECKLIST_up,false),
            new KomaMove(-1,-1, CHECKLIST_up,false),
            new KomaMove( 0,-1, CHECKLIST_upUp,false),
            new KomaMove( 1, 0, CHECKLIST_yoko,false),
            new KomaMove(-1, 0, CHECKLIST_yoko,false),
            new KomaMove( 0, 1, CHECKLIST_yoko,false),
            //
            // 桂馬チェック
            new KomaMove( 1,-2, CHECKLIST_kei,false),
            new KomaMove(-1,-2, CHECKLIST_kei,false));

    /** 移動先が合法手である手を探す用(8方向用)
     * 飛角香車は別でやるので、成ったときだけ入れておく
     */
    public final static List<KomaMove> moversFlagTrue = Arrays.asList(
            // 複数移動チェック(香角飛)
            new KomaMove( 0,-1, CHECKLIST_kyo,true),
            new KomaMove( 0, 1, CHECKLIST_rook,true),
            new KomaMove( 1, 0, CHECKLIST_rook,true),
            new KomaMove(-1, 0, CHECKLIST_rook,true),
            new KomaMove( 1,-1, CHECKLIST_kaku,true),
            new KomaMove( 1, 1, CHECKLIST_kaku,true),
            new KomaMove(-1,-1, CHECKLIST_kaku,true),
            new KomaMove(-1, 1, CHECKLIST_kaku,true)
    );

    /** 移動先が合法手である手を探す用(8方向用)
     * 飛角香車は別でやるので、成ったときだけ入れておく
     */
    public final static List<KomaMove> movers = new ArrayList<>();
    static {
        movers.addAll(moversFlagFalse);
        movers.addAll(moversFlagTrue);
    }

    /**
     * コマがあるか検索する
     * @param targetKoma 標的となるコマ
     * @return true:コマがある
     */
    public boolean findKoma(int targetKoma) {
        for (int x : koma) {
            if (x == (targetKoma & 0x1FL)) {
                return true;
            }
        }
        return false;
    }

    /** コンストラクタ
     * @param x 移動先のx位置
     * @param y 移動先のy位置
     * @param koma コマの種類
     * @param flag 複数移動する場合true (香,角,飛が対応)
     */
    public KomaMove(int x,int y,int[] koma,boolean flag) {
        this.xYFlag = new XYFlag(x,y,flag);
        this.koma = koma;
    }
}

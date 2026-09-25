package com.github.tand0.shogio.util;

import static com.github.tand0.shogio.util.TableDefine.*;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * 局面情報検索
 * <pre>
 * +-------------------------------+-------------------------------+-------------------------------+-------------------------------+
 * +---------------+---------------+---------------+---------------+---------------+---------------+---------------+---------------+
 * +-------+-------+-------+-------+-------+-------+-------+-------+-------+-------+-------+-------+-------+-------+-------+-------+
 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 * |t| XYZ | ----P---- | --koma9-- | --koma8-- | --koma7-- | --koma6-- | --koma5-- | --koma4-- | --koma3-- | --koma2-- | --koma1-- |0
 * +-+-----+-----------+-----------+-----------+-----------+-----------+-----------+-----------+-----------+-----------+-----------+
 * |sen OuX| ----p---- | --koma9-- | --koma8-- | --koma7-- | --koma6-- | --koma5-- | --koma4-- | --koma3-- | --koma2-- | --koma1-- |1
 * +-------+-----+-----+-----------+-----------+-----------+-----------+-----------+-----------+-----------+-----------+-----------+
 * |sen OuY| -l- | -L- | --koma9-- | --koma8-- | --koma7-- | --koma6-- | --koma5-- | --koma4-- | --koma3-- | --koma2-- | --koma1-- |2
 * +-------+-----+-----+-----------+-----------+-----------+-----------+-----------+-----------+-----------+-----------+-----------+
 * |got OuX| -n- | -N- | --koma9-- | --koma8-- | --koma7-- | --koma6-- | --koma5-- | --koma4-- | --koma3-- | --koma2-- | --koma1-- |3
 * +-------+-----+-----+-----------+-----------+-----------+-----------+-----------+-----------+-----------+-----------+-----------+
 * |got OuY| -s- | -S- | --koma9-- | --koma8-- | --koma7-- | --koma6-- | --koma5-- | --koma4-- | --koma3-- | --koma2-- | --koma1-- |4
 * +-------+-----+-----+-----------+-----------+-----------+-----------+-----------+-----------+-----------+-----------+-----------+
 * |------ | -g- | -G- | --koma9-- | --koma8-- | --koma7-- | --koma6-- | --koma5-- | --koma4-- | --koma3-- | --koma2-- | --koma1-- |5
 * |------ +-----+-----+-----------+-----------+-----------+-----------+-----------+-----------+-----------+-----------+-----------+
 * |------ | -b- | -B- | --koma9-- | --koma8-- | --koma7-- | --koma6-- | --koma5-- | --koma4-- | --koma3-- | --koma2-- | --koma1-- |6
 * |------ +-----+-----+-----------+-----------+-----------+-----------+-----------+-----------+-----------+-----------+-----------+
 * |------ | -r- | -R- | --koma9-- | --koma8-- | --koma7-- | --koma6-- | --koma5-- | --koma4-- | --koma3-- | --koma2-- | --koma1-- |7
 * |------ +-----+-----+-----------+-----------+-----------+-----------+-----------+-----------+-----------+-----------+-----------+
 * |------ | -k- | -K- | --koma9-- | --koma8-- | --koma7-- | --koma6-- | --koma5-- | --koma4-- | --koma3-- | --koma2-- | --koma1-- |8
 * +------ +-----+-----+-----------+-----------+-----------+-----------+-----------+-----------+-----------+-----------+-----------+
 *
 * komaX
 * +-+-+-------+
 * |e|n| koma1 |
 * +-+-+-------+
 *
 * te
 * +-------------------------------+-------------------------------+
 * +---------------+---------------+---------------+---------------+
 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 * |   | ---koma-- | ---newX-- | ---newY-- | ---oldX-- | ---oldY-- |
 * +---+-----------+-----------+-----------+-----------+-----------+
 * if oldY = 9 then from tegoma.
 *
 *
 * XYZ
 *   +-+-+-+
 *   |X Y Z|
 *   +-----+
 * X= sente ou is tumi
 * Y= gote  ou is tumi
 * Z= ???
 *
 * </pre>
 */
public class Table implements Comparable<Table> {
    /** 0x3FL スク */
    private static final long[] CLR_MASK_0x3FL = new long[64];
    /** 0x7L マスク */
    private static final long[] CLR_MASK_0x7L = new long[64];

    static {
        // マスクの初期値を設定する
        for (int i = 0; i < CLR_MASK_0x3FL.length; i++) {
            CLR_MASK_0x3FL[i] = ~(0x3FL << i);
        }
        for (int i = 0; i < CLR_MASK_0x3FL.length; i++) {
            CLR_MASK_0x7L[i] = ~(0x7L << i);
        }
    }

    /** 局面情報 */
    private final long[] vanmen = new long[B_MAX];

    /** 初期化しないテーブルを特別に作る
      * @return 初期化されたテーブル
     */
    static Table createNullTable() {
        return new Table();
    }

    /** 次の手番(中身が空なのでコピーしないといけない)
     */
    private Table() {
    }
    /** 次の手番
     * @param old 一手前の局面
     * @param te 指す手
     */
    public Table(Table old, int te) {
        if (old == null) {
            this.setTeban(0);// 次が先手なので後手にしておく
            //
            // 後手
            setKoma(pl, 0, 0);
            setKoma(pn,2 - 1, 0);
            setKoma(ps,3 - 1, 0);
            setKoma(pg,4 - 1, 0);
            setKoma(pk,5 - 1, 0);
            setKoma(pg,6 - 1, 0);
            setKoma(ps,7 - 1, 0);
            setKoma(pn,8 - 1, 0);
            setKoma(pl,9 - 1, 0);
            //
            setKoma(pb,2 - 1, 2 - 1);
            setKoma(pr,8 - 1, 2 - 1);
            //;
            setKoma(pp, 0, 3 - 1);
            setKoma(pp,2 - 1, 3 - 1);
            setKoma(pp,3 - 1, 3 - 1);
            setKoma(pp,4 - 1, 3 - 1);
            setKoma(pp,5 - 1, 3 - 1);
            setKoma(pp,6 - 1, 3 - 1);
            setKoma(pp,7 - 1, 3 - 1);
            setKoma(pp,8 - 1, 3 - 1);
            setKoma(pp,9 - 1, 3 - 1);
            //
            //
            // 先手
            setKoma(pL, 0, 9 - 1);
            setKoma(pN,2 - 1, 9 - 1);
            setKoma(pS,3 - 1, 9 - 1);
            setKoma(pG,4 - 1, 9 - 1);
            setKoma(pK,5 - 1, 9 - 1);
            setKoma(pG,6 - 1, 9 - 1);
            setKoma(pS,7 - 1, 9 - 1);
            setKoma(pN,8 - 1, 9 - 1);
            setKoma(pL,9 - 1, 9 - 1);
            //
            setKoma(pB,8 - 1, 8 - 1);
            setKoma(pR,2 - 1, 8 - 1);
            //
            setKoma(pP, 0, 7 - 1);
            setKoma(pP,2 - 1, 7 - 1);
            setKoma(pP,3 - 1, 7 - 1);
            setKoma(pP,4 - 1, 7 - 1);
            setKoma(pP,5 - 1, 7 - 1);
            setKoma(pP,6 - 1, 7 - 1);
            setKoma(pP,7 - 1, 7 - 1);
            setKoma(pP,8 - 1, 7 - 1);
            setKoma(pP,9 - 1, 7 - 1);
        } else {
            System.arraycopy(old.vanmen, 0, vanmen, 0, vanmen.length);
            if ((te != TableDefine.WIN) && (te != TableDefine.LOS)) {
                // 既に決着ついていたらここの更新はしない
                int oldY = (int) (te & 0x1FL);
                int oldX = (int) ((te >> 6) & 0x1FL);
                int newY = (int) ((te >> (6 * 2)) & 0x1FL);
                int newX = (int) ((te >> (6 * 3)) & 0x1FL);
                byte koma = (byte) ((te >> (6 * 4)) & 0x3FL);
                this.moveKoma(koma, oldX, oldY, newX, newY);
            }
        }
    }

    /**
     * sfen形式の文字列を Table に変える
     * sfen lnsgk1snl/1r4gb1/p1ppppppp/9/1p5P1/2P6/PP1PPPP1P/1B5R1/LNSGKGSNL b - 7
     * @param sfn  sfen形式の文字列
     */
    public Table(String sfn) {
        String[] split = sfn.split(" ");
        if ((split.length < 4) || (!split[0].equals("sfen"))) {
            return; // 不明なので初期局面を入れる
        }
        // 開始：全てのコマを後手番の持ちコマに含める
        this.clearForCSAProtocol();
        //
        if (split[2].equalsIgnoreCase("b")) {
            this.setTeban(0); // 先手
        } else {
            this.setTeban(1); // 後手
        }
        String target = split[1]; // 局面
        int pos = 0;
        for (int y = 0 ; y < TableDefine.B_MAX ; y++) {
            byte nari = 0;
            for (int x = TableDefine.B_MAX - 1 ; 0 <= x ; x--) {
                if (target.length() <= pos) {
                    break;
                }
                char at = target.charAt(pos);
                pos++;
                if (('0' <= at) && (at <= '9')) {
                    int z = (at - '0');
                    x = x +1 - z; // for文補正
                    continue;
                } else if ('/' == at) {
                    x = x +1; // for文補正
                    continue;
                } else if ('+' == at) {
                    nari = TableDefine.NARI;
                    x = x +1; // for補正
                    continue;
                }
                byte koma = (byte) (TableDefine.getUsiKomaToKoma(at) | nari);
                nari = 0;
                // コマを配置する
                this.setKoma(koma, x, y);
                //  後手の持ちコマを１つ減らす
                this.setTegoma(koma, 1, Math.max(0, this.getTegoma(koma, 1) - 1));
            }
        }
        target = split[3]; // 持ちコマ
        if (!(target.equals("-") || target.equals("none"))) {
            for (pos = 0 ; pos < target.length(); pos++) {
                char at = target.charAt(pos);
                int sum = 1;
                if (('0' <= at) && (at <= '9')) {
                    sum = at - '0';
                    pos++;
                    if (pos < target.length()) {
                        //
                        // 2桁目の数字が続くパターン
                        at = target.charAt(pos);
                        if (('0' <= at) && (at <= '9')) {
                            sum = (sum*10) + (at - '0');
                            pos++;
                        }
                    }
                }
                if (target.length() <= pos) {
                    continue; // 最後が数字だった
                }
                at = target.charAt(pos);
                byte koma = TableDefine.getUsiKomaToKoma(at);
                if ((koma == TableDefine.pNull) || (koma & TableDefine.ENEMY) != 0) {
                    continue;
                }
                // 先手の持ちコマを増やす
                this.setTegoma(koma, 0, Math.min(18, this.getTegoma(koma, 0) + sum));
                // 後手の持ちコマを１つ減らす
                this.setTegoma(koma, 1, Math.max(0, this.getTegoma(koma, 1) - sum));
            }
        }
        // 王がいない場合は生やす
        this.endForCSAProtocol();
    }


    /** 同じ局面か？(評価値と差し手は無視する
     */
    @Override
    public boolean equals(Object anObject) {
        return (anObject instanceof Table table)
                && (this.vanmen[0] == table.vanmen[0])
                && (this.vanmen[1] == table.vanmen[1])
                && (this.vanmen[2] == table.vanmen[2])
                && (this.vanmen[3] == table.vanmen[3])
                && (this.vanmen[4] == table.vanmen[4])
                && (this.vanmen[5] == table.vanmen[5])
                && (this.vanmen[6] == table.vanmen[6])
                && (this.vanmen[7] == table.vanmen[7])
                && (this.vanmen[8] == table.vanmen[8]);
    }

    @Override
    public int compareTo(Table o) {
        if (this.equals(o)) return 0;
        for (int i = 0; i < 3; i++) {
            long diff = Long.compare(this.vanmen[i], o.vanmen[i]);
            if (diff != 0) return (int) diff;
        }
        return 0;
    }


    /** クローンを取得する */
    @SuppressWarnings("CloneDoesntCallSuperClone")
    @Override
    public @NotNull Table clone() {
        Table tableOnly = new Table();
        System.arraycopy(this.vanmen, 0, tableOnly.vanmen, 0, tableOnly.vanmen.length);
        return tableOnly;
    }
    /**
     * 手番を設定する
     *
     * @param teban 先手=0, 後手=1
     */
    public void setTeban(int teban) {
        vanmen[0] = vanmen[0] & (~0x8000000000000000L);
        vanmen[0] = vanmen[0] | ( 0x8000000000000000L * teban);
    }

    /**
     * 手番を取得する
     *
     * @return 先手=0, 後手=1
     */
    public int getTeban() {
        return (vanmen[0] & 0x8000000000000000L) == 0 ? 0 : 1;
    }

    /** x,yの位置にコマを移動する
     *
     * @param koma コマ
     * @param oldX 打つ前の位置、手ゴマから出す場合は BEAT
     * @param oldY 打つ前の位置、手ゴマから出す場合は BEAT
     * @param newX 打つ先の位置
     * @param newY 打つ先の位置
     */
    private void moveKoma(byte koma, int oldX, int oldY, int newX, int newY) {
        int myTurn = this.getTeban();
        if (oldX == BEAT) {
            // 打った
            int now = getTegoma(koma, myTurn) ;
            this.setTegoma(koma, myTurn, Math.max(0, now - 1));
            koma = (byte) (koma | (ENEMY * myTurn));
        } else {
            // 単なる移動だ
            this.setKoma(pNull,oldX, oldY);
            byte newKoma = (byte) (getKoma(newX, newY) & 0xF); // 成っていたらいたら戻す
            if (newKoma != pNull) {
                int now = getTegoma(newKoma, myTurn);
                this.setTegoma(newKoma, myTurn, now + 1);
            }
        }
        this.setKoma(koma,newX, newY);
        this.setTeban(1 - myTurn);
    }

    /** x,yの位置にコマを配置する
     *
     * @param koma コマ
     * @param x 配置する先
     * @param y 配置する先
     */
    public void setKoma(byte koma, int x, int y) {
        if (((koma & 0x30 ) != 0) && ((koma & 0xF)==0)) {
            return; // error!
        }
        int pos = 6 * x;
        vanmen[y] = (vanmen[y] & CLR_MASK_0x3FL[pos]) | (((long) koma) << pos);
        //
        // 移動元が王ならば王のフラグを更新する
        if (koma == TableDefine.pK) { // 先手王
            vanmen[1] = 0x0FFFFFFFFFFFFFFFL & vanmen[1];
            vanmen[1] = (((long)x) << 60) | vanmen[1];
            vanmen[2] = 0x0FFFFFFFFFFFFFFFL & vanmen[2];
            vanmen[2] = (((long)y) << 60) | vanmen[2];
        } else if (koma == TableDefine.pk) { // 後手王
            vanmen[3] = 0x0FFFFFFFFFFFFFFFL & vanmen[3];
            vanmen[3] = (((long)x) << 60) | vanmen[3];
            vanmen[4] = 0x0FFFFFFFFFFFFFFFL & vanmen[4];
            vanmen[4] = (((long)y) << 60) | vanmen[4];
        }
    }
    /**
     *  自分の手の王 X座標
     * @return 先手王x
     */
    public int getMyOuX() {
        return getMyOuX(this.getTeban());
    }
    /**
     *  自分の手の王 X座標
     * @param teban 0:先手、0以外:後手
     * @return 先手王x
     */
    public int getMyOuX(int teban) {
        return (teban == 0) ? getSenteOuX() : getGoteOuX();
    }
    /**
     *  先手の手の王 Y座標
     * @return 先手王x
     */
    public int getMyOuY() {
        return getMyOuY(this.getTeban());
    }
    /**
     *  先手の手の王 Y座標
     * @param teban 0:先手、0以外:後手
     * @return 先手王x
     */
    public int getMyOuY(int teban) {
        return (teban == 0) ? getSenteOuY() : getGoteOuY();
    }
    /**
     *  先手王x の取得
     * @return 先手王x
     */
    public int getSenteOuX() {
        return (int)(vanmen[1] >>> 60);
    }
    /**
     * 先手王y の取得
     * @return 先手王y
     */
    public int getSenteOuY() {
        return (int)(vanmen[2] >>> 60);
    }
    /**
     * 後手王xの取得
     * @return 後手王x
     */
    public int getGoteOuX() {
        return (int)(vanmen[3] >>> 60);
    }
    /**
     * 後手王yの取得
     * @return 後手王y
     */
    public int getGoteOuY() {
        return (int)(vanmen[4] >>> 60);
    }

    /** x,yの位置にあるコマを確認する
     *
     * @param x 配置元の位置
     * @param y 配置元の位置
     * @return コマ
     */
    public byte getKoma(int x, int y) {
        return (byte) ((vanmen[y] >> (6 * x)) & 0x3FL);
    }

    /** 手コマの合計を数える
     * @param teban 手番
     * @return 手コマの数
     */
    public int getTegomaNum(int teban) {
        int sum = 0;
        for (byte i = pP ; i <= pR; i++) {
            sum += getTegoma(i,teban);
        }
        return sum;
    }
    /** 手ゴマの数を取得する
     *
     * @param koma コマ
     * @param teban 手番
     * @return 手ゴマの数
     */
    public int getTegoma(byte koma, int teban) {
        koma = (byte) (koma & 0xF);
        long now = (koma == pP) ? ((vanmen[teban] >>> 54) & 0x3FL) : ((vanmen[koma] >>> (54 + (teban * 3))) & 0b111L);
        return (int)now;
    }

    /** 手ゴマをセットする
     *
     * @param koma コマ
     * @param teban 手番
     * @param now 手ゴマの数
     */
    public void setTegoma(byte koma, int teban, int now) {
        koma = (byte) (koma & 0xF);
        if (koma == pP) {
            vanmen[teban] = (vanmen[teban] & CLR_MASK_0x3FL[54])
                    | ((now & 0x3FL) << 54L);
        } else {
            vanmen[koma ] = (vanmen[koma ] & CLR_MASK_0x7L[54 + (teban * 3)])
                    | ((now & 0x7L) << (54 + (teban * 3)));
        }
    }

    /** デバッグ用 */
    @Override
    public @NotNull String toString() {
        StringBuilder buffer = new StringBuilder();
        int teban = this.getTeban();
        for (int y = 0; y < vanmen.length; y++) {
            buffer.append('P');
            buffer.append((y+1));
            for (int x = vanmen.length - 1; 0 <= x; x--) {
                int koma = getKoma(x,y);
                if (koma == pNull) {
                    buffer.append(' ');
                } else if ((koma & ENEMY) == 0) {
                    buffer.append('+');
                } else {
                    buffer.append('-');
                }
                buffer.append(getKomaToString(koma));
            }
            buffer.append(String.format(" 0x%016x", vanmen[y]));
            buffer.append("\n");
        }
        for (int i = 0; i < 2; i++) {
            buffer.append((i == 0) ? "P+" : "P-");
            for (byte koma = pP; koma <= pK; koma++) {
                int num = getTegoma(koma, i);
                for (int j = 0 ; j < num ; j++) {
                    buffer.append("00");
                    buffer.append(getKomaToString(koma));
                }
            }
            buffer.append("\n");
        }
        buffer.append(teban == 0 ? "+" : "-").append("\n");
        return buffer.toString();
    }

    /** 合法手のリストの取得
     * @return 合法手のリスト
     */
    public List<TeTable> createChild() {
        //
        //
        // 自分の王の位置を特定する
        int teban = this.getTeban();
        int myOuX = this.getMyOuX();
        int myOuY = this.getMyOuY();
        int enemyOuX = this.getMyOuX(1 - teban);
        int enemyOuY = this.getMyOuY(1 - teban);
        //
        List<TeTable> list = new ArrayList<>();
        if (this.checkSelfMate(1 - teban, enemyOuX, enemyOuY)) {
            //
            // すでに詰ましている or 入玉勝ちした
            list.add(new TeTable(this,TableDefine.WIN));
            return list;
        }
        int ans = this.isKingWin(teban, myOuX, myOuY);
        if (0 < ans) {
            // 勝った
            list.add(new TeTable(this,TableDefine.WIN));
            return list;
        }
        if (ans < 0) {
            // 負けた
            return list;
        }

        // 合法手の生成
        for (int i = 0; i < B_MAX * B_MAX ; i++) {
            int x = i % B_MAX;
            int y = i / B_MAX;
            final byte koma = this.getKoma(x, y);
            //
            if (koma == pNull) {
                // 打つ
                getChildXYUchi(x, y, myOuX, myOuY, teban, list);
                continue;
            }
            //
            if ((teban == 0) == ((koma & ENEMY) != 0)) {
                continue; // 相手のコマなら何もしない
            }
            // 自分のコマなら移動する
            getChildXYMove(x, y, myOuX, myOuY, koma, teban, list);
        }
        //
        return list;
    }

    /**
     * 入玉勝ちチェック
     * @return 入玉勝ちならtrue
     */
    public int isKingWin() {
        int teban = this.getTeban();
        int x = this.getMyOuX();
        int y = this.getMyOuY();
        return isKingWin(teban, x, y);
    }

    /**
     * 入玉勝ちチェック
     * @param teban 手番
     * @param myOuX 自分の王x
     * @param myOuY 自分の王y
     * @return -1:先手は 24 点以上あったら投了する、0:関係なし、1:後手は 24 点以上あったら勝ちにする
     */
    public int isKingWin(int teban, int myOuX,int myOuY) {
        int range0;
        if ((teban == 0) && (myOuY <= 2)) {
            range0 = 0;
        } else if ((teban != 0) && (6 <= myOuY)) {
            range0 = 6;
        } else {
            return 0; // 入玉していない
        }
        if (this.checkSelfMate(teban,myOuX,myOuY)) {
            return 0;// 王手を掛けられていない
        }

        //
        // 局面上の敵陣のコマを数える
        int sum = 0;
        int value = 0;
        for (int x = 0 ; x < TableDefine.B_MAX ; x++) {
            for (int y = range0 ; y <= (range0 +2) ; y++) {
                byte koma = this.getKoma(x, y);
                if (koma == TableDefine.pNull) {
                    continue;
                }
                if (((koma & TableDefine.ENEMY)/TableDefine.ENEMY) == teban) {
                    sum++;
                    if (((koma & 0xF) == TableDefine.pB)
                            || ((koma & 0xF) == TableDefine.pR)){
                        value += 5; // 大コマは５点
                    } else if ((koma & 0xF) != TableDefine.pK) { // 王は除く
                        value += 1; // 小コマは１点
                    }
                }
            }
        }
        if (sum < 10) {
            return 0; // 王を除く自コマが10枚より少ない
        }
        // 手持ちの小コマの数を数える
        for (byte i = TableDefine.pP ; i < TableDefine.pB ; i++) {
            value += this.getTegoma(i, teban);
        }
        // 手持ちの大コマの数を数える
        for (byte i = pB ; i <= TableDefine.pR ; i++) {
            value += this.getTegoma(i, teban) * 5;
        }
        if (teban == 0) {
            return (28 <= value) ?  -1 : 0; // 先手は 24 点以上あったら投了する
        }
        return (24 <= value) ? 1 : 0; // 後手は 24 点以上あったら勝ちにする
    }
    /** 打ち込み処理
     * @param x x位置
     * @param y y位置
     * @param myOuX 自分の王の位置x
     * @param myOuY 自分の王の位置y
     * @param teban 手番
     * @param list 合法手のリスト
     */
    private void getChildXYUchi(
            int x, int y,
            int myOuX,int myOuY,
            int teban,
            List<TeTable> list) {
        for (int tegomaKey = pP ; tegomaKey <= pR ; tegomaKey++) {
            if (this.getTegoma((byte)tegomaKey, teban) <= 0) {
                continue; // 手ゴマがない
            }
            if (CHECKLIST[0xF & tegomaKey].getUchiKinshi(teban, y)) {
                continue; // 置いてはいけないところに置いていないかチェック
            }
            byte tegomaHave = (byte) (tegomaKey | (ENEMY * teban));
            if (tegomaKey == pP) {
                // ２歩チェック
                boolean flag = true;
                for (int yy = 0; yy < B_MAX; yy++) {
                    if (tegomaHave == this.getKoma(x, yy)) {
                        flag = false;
                        break;
                    }
                }
                if (! flag) {
                    continue;
                }
            }
            setNext((byte) (tegomaKey | (ENEMY * teban)),
                    BEAT, BEAT ,
                    x,y,
                    myOuX,myOuY,list);
        }
    }
    /** 移動処理
     * @param x x位置
     * @param y y位置
     * @param myOuX 自分の王の位置x
     * @param myOuY 自分の王の位置y
     * @param koma コマ
     * @param teban 手番
     * @param list 合法手のリスト
     */
    private void getChildXYMove(
            int x, int y,
            int myOuX,int myOuY,
            byte koma,
            int teban,
            List<TeTable> list) {
        //
        // チェック用クラス
        Checklist low = CHECKLIST[0xF & koma];
        //
        // 成りコマの値を用意しておく
        final byte naryKey = (byte) (koma | NARI);

        MoveKoma moverKoma = MoveKoma.moverKomaMap.get(koma & 0x1F);
        if (moverKoma == null) {
            return;
        }
        for (XYFlag xYFlag : moverKoma.getXYFlag()) {
            final int dx = xYFlag.x();
            final int dy = xYFlag.y(teban);
            int xx = x + dx;
            int yy = y + dy;
            for (; (0 <= xx) && (0 <= yy) && (xx < B_MAX) && (yy < B_MAX); xx += dx, yy += dy) {
                //
                if (((koma & NARI)!=0) || low.narenai()) {
                    // 既に成っている or 成れません
                    setNext(koma, x, y, xx, yy,myOuX,myOuY, list);
                } else {
                    // 成ることのできるコマでまだ成っていない、または、成れない
                    //
                    if (low.getForcedNari(teban, y) || low.getForcedNari(teban, yy)) {
                        //NG 成ることが強制される位置
                        setNext(naryKey, x, y, xx, yy,myOuX,myOuY, list);
                    } else if (low.isTekijin(teban,y) || low.isTekijin(teban, yy)) {
                        // 敵陣に入った or 敵陣から出た
                        // 成る
                        setNext(naryKey, x, y, xx, yy,myOuX,myOuY, list);
                        // 成らずもできる
                        if (low.narazuOK()) {
                            setNext(koma, x, y, xx, yy,myOuX,myOuY, list);
                        }
                    } else { //
                        // 成れません
                        setNext(koma, x, y, xx, yy,myOuX,myOuY, list);
                    }
                }
                //
                if ((!xYFlag.flag())
                        || (pNull != this.getKoma(xx, yy))) {
                    break;//8方向チェックか、移動先にコマがいたらそこまで
                }
            }
        }
    }

    /**
     * 移動する（移動元と移動先が決まっているパターン)
     * @param oldX 移動元のx位置
     * @param oldY 移動元のy位置
     * @param newX 移動先のx位置
     * @param newY 移動先のy位置
     * @param myOuX 自分の王の位置x
     * @param myOuY 自分の王の位置y
     * @param koma コマ
     * @param list 合法手のリスト
     */
    private void setNext(
            byte koma,
            int oldX, int oldY,
            int newX, int newY,
            int myOuX,int myOuY,
            List<TeTable> list) {
        if ((newX < 0) || (B_MAX <= newX) || (newY < 0) || (B_MAX <= newY)) {
            return; // はみ出る
        }
        //
        boolean tebanNow = this.getTeban() != 0; //手番が反対になっている
        byte targetKoma = this.getKoma(newX, newY);
        boolean targetTeban = (targetKoma & ENEMY) == 0;
        if ((targetKoma != pNull) &&
                ((targetTeban != tebanNow))) {
            return; // 移動先が空でなくて、かつ、自身のコマならば移動できない
        }
        if ((targetKoma & 0b1111) == TableDefine.pK) {
            return; // 王は取ってはいけない
        }
        //
        int te = changeTeToInt(koma, oldX, oldY, newX, newY);
        //
        if ((koma & 0b1111) == TableDefine.pK) {
            // 移動するコマが王なら移動先が王の位置だ
            myOuX = newX;
            myOuY = newY;
        }
        int teban = this.getTeban();
        //
        // 新しい局面を作る
        Table newTable = new Table(this, te);
        //
        if (newTable.checkSelfMate(teban, myOuX, myOuY)) {
            return; // 空き王手ならばNG
        }
        // 打ち歩詰めチェック
        if ((oldX == BEAT) && ((koma & 0xF) == pP) && newTable.checkFuMate()) {
            // もしも、打ちっていて、それが歩で、王手が入っている場合
            return; // 打ち歩詰めチェック
        }
        //
        // 手を追加する
        list.add(new TeTable(newTable, te));
    }

    /**
     * 打ち歩詰めチェック
     * @return true:打ち歩詰め, false:打ち歩詰めではない
     */
    public boolean checkFuMate() {
        int myOuX = this.getMyOuX();
        int myOuY = this.getMyOuY();
        int teban = this.getTeban();
        if ((teban == 0) && (myOuY == 0)) {
            return false; // 先手で王が一番上なら歩が打てないので打ち歩詰めは起きない
        } else if ((teban != 0) && (myOuY == B_MAX -1)) {
            return false; // 後手で王が一番下なら歩が打てないので打ち歩詰めは起きない
        }
        //
        // 玉を動かす
        byte koma = this.getKoma(myOuX, myOuY);
        List<TeTable> list = new ArrayList<>();
        this.getChildXYMove(myOuX, myOuY, myOuX, myOuY, koma, this.getTeban(), list);
        if (! list.isEmpty()) {
            return false; // 移動できる
        }
        // 歩の位置の８方向チェック
        // 飛角香の利き対応
        int enemyFuY =  myOuY + ((teban == 0) ? -1 : 1); // 歩の位置。X軸は変わらない
        for (KomaMove mover : KomaMove.movers) {
            final int dx = mover.xYFlag.x();
            final int dy = mover.xYFlag.y(1 - teban);
            int xx = myOuX + dx;
            int yy = enemyFuY + dy; // 歩の位置へ移動
            if ((xx == myOuX) && (yy == myOuY)) {
                continue; // 王の位置の場合は無視して良い(既にチェック済)
            }
            final int[] moverKoma = mover.koma;
            final boolean isLongRange = mover.xYFlag.flag(); // 継続フラグ
            for (; (0 <= xx) && (0 <= yy) && (xx < B_MAX) && (yy < B_MAX); xx += dx, yy += dy) {
                //
                byte targetKoma = this.getKoma(xx, yy); // 移動先のコマを取得
                //
                if (targetKoma != pNull) {
                    // ターゲットのマスが空でない
                    if ((teban == 0) == ((targetKoma & ENEMY) == 0)) {
                        for (int sKoma : moverKoma) {
                            if (sKoma == (0x1F & targetKoma)) {
                                // 新しい局面を作る
                                int te = changeTeToInt(targetKoma, xx, yy, myOuX, enemyFuY);
                                Table newOnly;
                                newOnly = new Table(this, te);
                                //
                                // 新しい局面が王手か調べる
                                boolean flag = newOnly.checkSelfMate(teban, myOuX, myOuY);
                                if (! flag) { // 王手が掛かってないので移動できる
                                    return false;
                                }
                                // 同一局面に同じコマなんてあるはずないのだからbreakして良い
                                break;
                            }
                        }
                    }
                    break; // 駒にぶつかったら、その方向の探索は無条件で終了
                }
                if (!isLongRange) {
                    // 継続フラグが付いていない
                    break;
                }
            }
        }
        return true; // 移動できない＝詰みである
    }

    /** 王手のチェック
     * @param teban 手番 先手なら0、後手なら1
     * @param x 自分の王の位置X
     * @param y 自分の王の位置Y
     * @return 空き王手ならtrue
     */
    public boolean checkSelfMate(int teban, int x, int y) {
        // ８方向チェック
        // 飛角香の利き対応
        for (KomaMove mover : KomaMove.movers) {
            final int dx = mover.xYFlag.x();
            final int dy = mover.xYFlag.y(teban);
            int xx = x + dx;
            int yy = y + dy;
            final int[] moverKoma = mover.koma;
            final boolean isLongRange = mover.xYFlag.flag(); // 継続フラグ
            for (; (0 <= xx) && (0 <= yy) && (xx < B_MAX) && (yy < B_MAX); xx += dx, yy += dy) {
                //
                byte targetKoma = this.getKoma(xx, yy); // 移動先のコマを取得
                int nariKoma = 0x1F & targetKoma; // 成り情報付きのコマ
                //
                if (targetKoma != pNull) {
                    // ターゲットのマスが空でない
                    if ((teban == 0) != ((targetKoma & ENEMY) == 0)) {
                        //ターゲットのマスが空でなく、かつ、敵のコマなら本気だす
                        for (int sKoma : moverKoma) {
                            if (sKoma == nariKoma) {
                                return true;
                            }
                        }
                    }
                    // 駒にぶつかったら、その方向の探索は無条件で終了
                    break;
                }
                if (!isLongRange) {
                    // 継続フラグが付いていない
                    break;
                }
            }
        }
        return false;
    }

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
            new Checklist( 8,  0, false, true  )  //pK  王
    };

    /** 自分が王手か？
     * @return 自分が王手ならtrue
     */
    public boolean isMyOute() {
        // 相手に王手をかけているかチェック
        int teban = this.getTeban();
        return this.isOute(teban);
    }
    /** 相手が王手か？
     * @param teban 手番
     * @return 自分が王手ならtrue
     */
    public boolean isOute(int teban) {
        // 相手に王手をかけているかチェック
        int enemyOuX = this.getMyOuX(teban);
        int enemyOuY = this.getMyOuY(teban);
        //
        // 相手に王手をかけているかチェック
        return this.checkSelfMate(teban, enemyOuX, enemyOuY);
    }
    /** 局面を初期化する
     * 初期局面を局面上のデータをクリアして先手番なら後手の持ちコマ、
     * 後手番なら先手の持ちコマにする
     */
    public void clearForCSAProtocol() {
        for (int y = 0 ; y < TableDefine.B_MAX ; y++) {
            for (int x = 0 ; x < TableDefine.B_MAX ; x++) {
                // 局面から消す
                this.setKoma(TableDefine.pNull, x, y);
            }
        }
        // 後手にコマを寄せる
        this.setTegoma(TableDefine.pP, 0, 0);
        this.setTegoma(TableDefine.pL, 0, 0);
        this.setTegoma(TableDefine.pN, 0, 0);
        this.setTegoma(TableDefine.pS, 0, 0);
        this.setTegoma(TableDefine.pG, 0, 0);
        this.setTegoma(TableDefine.pB, 0, 0);
        this.setTegoma(TableDefine.pR, 0, 0);
        //
        this.setTegoma(TableDefine.pP, 1, 18);
        this.setTegoma(TableDefine.pL, 1, 4);
        this.setTegoma(TableDefine.pN, 1, 4);
        this.setTegoma(TableDefine.pS, 1, 4);
        this.setTegoma(TableDefine.pG, 1, 4);
        this.setTegoma(TableDefine.pB, 1, 2);
        this.setTegoma(TableDefine.pR, 1, 2);
    }

    /**
     * CSA 状態にする。
     * @param message CSA プロトコルからのメッセージ
     * @return trueなら初期化終わり
     */
    public boolean setForCSAProtocol(String message) {
        if (message.equals("+")) {
            this.setTeban(0);//先手
            endForCSAProtocol();
            return true;
        } else if (message.equals("-")) {
            this.setTeban(1);//先手
            endForCSAProtocol();
            return true;
        } else if (0 == message.indexOf("P+")) {
            String[] splits = message.substring(2).split("00");
            for (String split : splits) {
                if (split.isEmpty()) {
                    continue;
                }
                byte koma = TableDefine.getStringToKoma(split);
                // 先手のコマを増やす
                this.setTegoma(koma, 0, this.getTegoma(koma, 0) + 1);
                // 後手のコマを減らす
                this.setTegoma(koma, 1, Math.max(0, this.getTegoma(koma, 1) - 1));
            }
            return true;
        } else if (0 == message.indexOf("P-")) {
            // EMPTY(後手詰めは考えない)
            return true;
        } else if (0 == message.indexOf("P")) {
            if (message.contains("P1")) {
                clearForCSAProtocol(); // 局面上のものを消す
            }
            message = message + "        "; // エラー防止
            int y = Math.clamp(message.charAt(1) - '1', 0, 8);
            for (int x = 0 ; ((x*3 + 2) < message.length()) && (x < TableDefine.B_MAX) ; x++) {
                //System.out.println(koma + " x=" + x + " x=" + (x*3+2));
                int teban = message.charAt(x*3 + 2) == '+' ? 0 : 1;
                String komaString = message.substring(x*3 + 3,x*3 + 5);
                byte koma = TableDefine.getStringToKoma(komaString);
                if (koma != TableDefine.pNull) {
                    koma = (teban != 0) ? (byte)(TableDefine.ENEMY | koma) : koma;
                    this.setKoma(koma, 8 - x, y);
                    this.setTegoma(koma, 1, Math.max(0, this.getTegoma(koma, 1) - 1));
                }
            }
            return true;
        }
        return false;
    }
    /** CSA 状態にする（最後に先手後手が局面に乗っていなかったら適当に乗せる） */
    public void endForCSAProtocol() {
        //
        // 先手後手の持ち物にあったらまずいので消す
        this.setTegoma(TableDefine.pK, 0, 0);
        this.setTegoma(TableDefine.pK, 1, 0);
        //
        // 先手後手の王がいるかどうか探す
        boolean senteOu = false;
        boolean goteOu = false;
        for (int y = 0; y < TableDefine.B_MAX; y++) {
            for (int x = 0; x < TableDefine.B_MAX; x++) {
                byte ou = this.getKoma(x, y);
                if (ou == TableDefine.pK) {
                    senteOu = true;
                } else if (ou == TableDefine.pk) {
                    goteOu = true;
                }
            }
        }

        // 局面上に空があったらそこに置いておく
        for (int y = 0; y < TableDefine.B_MAX; y++) {
            if (senteOu) {
                break;
            }
            for (int x = 0; x <= TableDefine.B_MAX; x++) {
                if (senteOu) {
                    break;
                }
                byte koma = this.getKoma(x, y);
                if (koma == TableDefine.pNull) {
                    senteOu = true;
                    this.setKoma(TableDefine.pK, x, y);
                }
            }
        }

        // 局面上に空があったらそこに置いておく
        for (int y = TableDefine.B_MAX - 1; 0 <= y; y--) {
            if (goteOu) {
                break;
            }
            for (int x = TableDefine.B_MAX - 1; 0 <= x; x--) {
                if (goteOu) {
                    break;
                }
                byte koma = this.getKoma(x, y);
                if (koma == TableDefine.pNull) {
                    goteOu = true;
                    this.setKoma(TableDefine.pk, x, y);
                }
            }
        }
    }
    /**
     * USI形式の手をintの手に変換する。パターンとしては以下がある
     * <ul>
     *   <li>4e5c  : 手の移動</li>
     *   <li>4e5c+ : 手の移動と成り</li>
     *   <li>P*5d : コマを打つ</li>
     * </ul>
     * @param sfn  sfen形式の文字列
     * @return 局面情報
     */
    public int changeUsiTeToInt(String sfn) {
        Table only = this.clone();
        if (sfn.length() < 4) {
            return 0; // わからん
        }
        // 手番を反転する
        int myTurn = only.getTeban();
        only.setTeban(1 - myTurn);
        //
        int newX = Math.clamp(((byte) sfn.charAt(2)) - '1', 0, TableDefine.B_MAX - 1);
        int newY = Math.clamp(((byte) sfn.charAt(3)) - 'a', 0, TableDefine.B_MAX - 1);
        int oldX;
        int oldY;
        byte koma = TableDefine.getUsiKomaToKoma(sfn.charAt(0));
        if (koma == TableDefine.pNull) {
            oldX = Math.clamp(((byte) sfn.charAt(0)) - '1', 0, TableDefine.B_MAX - 1);
            oldY = Math.clamp(((byte) sfn.charAt(1)) - 'a', 0, TableDefine.B_MAX - 1);
            //
            koma = only.getKoma(oldX, oldY);
            if ((5 <= sfn.length()) && (sfn.charAt(4) == '+')) {
                // 成りがある
                koma = (byte) (TableDefine.NARI | koma);
            }
        } else {
            // 打つ
            oldX = BEAT;
            oldY = BEAT;
        }
        return changeTeToInt(koma, oldX, oldY, newX, newY);
    }

    /** 局面を左右反転させる
     *
     * @return 反転したテーブル
     */
    public Table flippingHorizontal() {
        Table newTable = new Table();
        //
        newTable.setTeban(this.getTeban());
        //
        for (int x = 0 ; x < B_MAX ; x++) {
            for (int y = 0 ; y < B_MAX ; y++) {
                int flipX = B_MAX - x - 1;
                byte koma = this.getKoma(x, y);
                newTable.setKoma(koma, flipX, y);
            }
        }
        for (int teban = 0 ; teban <= 1 ; teban++) {
            for (byte koma = pP; koma < pK; koma++) {
                newTable.setTegoma(koma, teban, this.getTegoma(koma, teban));
            }
        }
        return newTable;
    }
    /** 局面を上下反転させる
     *
     * @return 反転したテーブル
     */
    public Table flippingVertical() {
        Table newTable = new Table();
        //
        // 上下なので手番も反転
        newTable.setTeban(1 - this.getTeban());
        //
        for (int x = 0 ; x < B_MAX ; x++) {
            for (int y = 0 ; y < B_MAX ; y++) {
                int flipY = B_MAX - y - 1;
                byte koma = this.getKoma(x, y);
                if (koma != 0) {
                    koma = (byte) (koma ^ ENEMY); // xor で反転
                }
                newTable.setKoma(koma, x, flipY);
            }
        }
        for (int teban = 0 ; teban <= 1 ; teban++) {
            for (byte koma = pP; koma < pK; koma++) {
                // 反転して投入
                newTable.setTegoma(koma, 1 - teban, this.getTegoma(koma, teban));
            }
        }
        return newTable;
    }
}

package com.github.tand0.shogio.util;

import static com.github.tand0.shogio.util.TableDefine.*;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import kotlin.jvm.functions.Function2;

/**
 * 局面情報検索
 * <pre>
 * ■変更前
 * +-------------------------------+-------------------------------+-------------------------------+-------------------------------+
 * +---------------+---------------+---------------+---------------+---------------+---------------+---------------+---------------+
 * +-------+-------+-------+-------+-------+-------+-------+-------+-------+-------+-------+-------+-------+-------+-------+-------+
 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 * |t|     | ----P---- | --koma9-- | --koma8-- | --koma7-- | --koma6-- | --koma5-- | --koma4-- | --koma3-- | --koma2-- | --koma1-- |0
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
 * komaX
 * +-+-+-------+
 * |e|n| koma1 |
 * +-+-+-------+
 *
 *
 *
 * ■変更後
 * +-------------------------------+-------------------------------+-------------------------------+-------------------------------+
 * +---------------+---------------+---------------+---------------+---------------+---------------+---------------+---------------+
 * +-------+-------+-------+-------+-------+-------+-------+-------+-------+-------+-------+-------+-------+-------+-------+-------+
 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 *0| 15koma| 14koma| 13koma| 12koma| 11koma| 10koma| 09koma| 08koma| 07koma| 06koma| 05koma| 04koma| 03koma| 02koma| 01koma| 00koma|
 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 *1| 31koma| 30koma| 29koma| 28koma| 27koma| 26koma| 25koma| 24koma| 23koma| 22koma| 21koma| 20koma| 19koma| 18koma| 17koma| 16koma|
 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 *2| 47koma| 46koma| 45koma| 44koma| 43koma| 42koma| 41koma| 40koma| 39koma| 38koma| 37koma| 36koma| 35koma| 34koma| 33koma| 32koma|
 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 *3| 63koma| 62koma| 61koma| 60koma| 59koma| 58koma| 57koma| 56koma| 55koma| 54koma| 53koma| 52koma| 51koma| 50koma| 49koma| 48koma|
 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 *4| 79koma| 78koma| 77koma| 76koma| 75koma| 74koma| 73koma| 72koma| 71koma| 70koma| 69koma| 68koma| 67koma| 66koma| 65koma| 64koma|
 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 *5|t|-|senOuX |senOuX |goteOuX|goteOuY|           |- l -|- L -|- n -|- N -|- s -|- S -|- g -|- G -|- b -|- B -|- r -|- R -| 80koma|
 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 *6| sen  bit board 63--0                                                                                                          |
 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 *7| --- p --- | --- p --- |                               | gote  bit board 80--64        | sen  bit board 80--64                 |
 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 *8| gote bit board 63--0                                                                                                          |
 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 *
 * koma
 * +-+-+-+-+ 0=pNull
 * | koma  | 1=p , 1=l , 3=n , 4=s , 5=g , 6=b , 7=r ,8=k,
 * |       | 9=p+,10=l+,11=n+,12=s+,13=g+,14=b+,15=r+
 * +-+-+-+-+
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
 *
 * </pre>
 */
public class Table implements Comparable<Table> {

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
            this.setSide(0);// 次が先手なので後手にしておく
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
            this.setSide(0); // 先手
        } else {
            this.setSide(1); // 後手
        }
        String target = split[1]; // 局面
        int pos = 0;
        for (int y = 0 ; y < TableDefine.B_MAX ; y++) {
            byte nariFlag = 0;
            for (int x = TableDefine.B_MAX - 1 ; 0 <= x ; x--) {
                if (target.length() <= pos) {
                    break;
                }
                char at = target.charAt(pos);
                pos++;
                if (('0' <= at) && (at <= '9')) {
                    int z = (at - '0');
                    x = x +1 - z; // 補正
                    continue;
                } else if ('/' == at) {
                    x = x +1; // 補正
                    continue;
                } else if ('+' == at) {
                    nariFlag = 1;
                    x = x +1; // 補正
                    continue;
                }
                byte koma = addNariKoma(TableDefine.getUsiKomaToKoma(at), nariFlag);
                nariFlag = 0;
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
                if ((koma == TableDefine.pNull) || (isSideKoma(koma) != 0)) {
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

    /** クローンを作る
     * @return Tableのデータ
     */
    public long[] createUndo() {
        final long[] undo = new long[B_MAX];
        System.arraycopy(this.vanmen, 0, undo, 0, this.vanmen.length);
        return undo;
    }

    /** クローンを作る
     * @param undo テーブルのデータ
     */
    public void setUndo(long[] undo) {
        System.arraycopy(undo, 0, this.vanmen, 0, this.vanmen.length);
    }

    /** コンストラクタ(テーブルで復元)
     * @param undo テーブルのデータ
     */
    public Table(long[] undo) {
        setUndo(undo);
    }

    /** 持ち駒を除いた盤面は同じか？
     * 先手後手も同一チェックする
     * @param anObject 比較対象
     * @return true:同じ、 false:同じでない
     */
    public boolean equalsTableOnly(Object anObject) {
        return (anObject instanceof Table table)
                && (((this.vanmen[0] ^ table.vanmen[0])) == 0)
                && (((this.vanmen[1] ^ table.vanmen[1])) == 0)
                && (((this.vanmen[2] ^ table.vanmen[2])) == 0)
                && (((this.vanmen[3] ^ table.vanmen[3])) == 0)
                && (((this.vanmen[4] ^ table.vanmen[4])) == 0)
                && (((this.vanmen[5] ^ table.vanmen[5]) & 0x800000000000000FL) == 0)
                && (((this.vanmen[6] ^ table.vanmen[6])) == 0)
                && (((this.vanmen[7] ^ table.vanmen[7]) & 0x000FFFFFFFFFFFFFL) == 0)
                && (((this.vanmen[8] ^ table.vanmen[8])) == 0);
    }

    @Override
    public int compareTo(Table o) {
        return Arrays.compare(this.vanmen, o.vanmen);
    }

    /** クローンを取得する */
    @SuppressWarnings("CloneDoesntCallSuperClone")
    @Override
    public @NotNull Table clone() {
        return new Table(createUndo());
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

    /**ハッシュコード
     * @return ハッシュコード
     */
    @Override
    public int hashCode() {
        return Arrays.hashCode(vanmen);
    }

    /**
     * 手番を設定する
     *
     * @param side 先手=0, 後手=1
     */
    public void setSide(int side) {
        vanmen[5] = (vanmen[5] & (~0x8000000000000000L)) | ( ((long)side) << 63);
    }

    /**
     * 手番を取得する
     *
     * @return 先手=0, 後手=1
     */
    public int getSide() {
        return (vanmen[5] & 0x8000000000000000L) == 0 ? 0 : 1;
    }

    /** x,yの位置にコマを移動する
     *
     * @param koma コマ
     * @param oldX 打つ前の位置、手ゴマから出す場合は BEAT
     * @param oldY 打つ前の位置、手ゴマから出す場合は BEAT
     * @param newX 打つ先の位置
     * @param newY 打つ先の位置
     */
    protected void moveKoma(byte koma, int oldX, int oldY, int newX, int newY) {
        int side = this.getSide();
        if (oldX == BEAT) {
            // 打った
            int now = getTegoma(koma, side) ;
            this.setTegoma(koma, side, Math.max(0, now - 1));
        } else {
            // 単なる移動だ
            this.setKoma(pNull,oldX, oldY);
            byte newKomaOnly = getKomaOnly(getKoma(newX, newY)); // 成っていたらいたら戻す
            if (newKomaOnly != pNull) {
                int now = getTegoma(newKomaOnly, side);
                this.setTegoma(newKomaOnly, side, now + 1);
            }
        }
        koma = addSideKoma(koma, side);
        this.setKoma(koma, newX, newY);
        this.setSide(1 - side);
    }

    /** 成り状態とどちらが持っているか？の両方を削除してコマだけ取り出す
     *
     * @param koma 成りとどちらの持ちコマかのフラグを持ったコマ
     * @return 純粋なコマ種別のみ
     */
    public static byte getKomaOnly(byte koma) {
        byte type = (byte) (koma & 0xF);
        return pK < type ? (byte) (type - pK) : type;
    }

    /**
     * コマに成り属性があるかチェックする
     * @param koma コマ
     * @return 成ったコマ
     */
    public static boolean isNariKoma(byte koma) {
        return pK < (koma & 0x1F);
    }

    /**
     * コマの先手／後手をチェックする
     * @param koma コマ
     * @return side (0:先手, 1:後手)
     */
    public static int isSideKoma(byte koma) {
        return (koma / 0x20) & 0x1;
    }

    /**
     * コマを設定する
     * @param koma コマ
     * @return 0:先手, 1:後手
     */
    public static byte addSideKoma(byte koma, int side) {
        return (byte) (side == 0? removeSideKoma(koma) : (0x20 | koma));
    }

    /**
     * コマから先手／後手のみ削除し、成り情報を残したコマを得る
     * @param koma コマ
     * @return 0:先手, 1:後手
     */
    public static byte removeSideKoma(byte koma) {
        return (byte) (koma & 0x1F);
    }

    /**
     * コマに成り属性を付け加える。または消す
     * @param koma コマ
     * @param flag 0:成り属性を付けない。1: 成り属性を付ける
     * @return 成ったコマ
     */
    public static byte addNariKoma(byte koma, int flag) {
        if (flag == 0) {
            // フラグが落ちていたら、成りを消す処理が必要です！
            int side = isSideKoma(koma);
            koma = isNariKoma(koma) ? (byte) (getKomaOnly(koma) - pK) : koma;
            return addSideKoma(koma, side);
        }
        return addNariKoma(koma);
    }

    /**
     * コマに成り属性を付け加える
     * @param koma コマ
     * @return 成ったコマ
     */
    public static byte addNariKoma(byte koma) {
        int side = isSideKoma(koma);
        return addSideKoma((byte) (getKomaOnly(koma) + pK), side);
    }

    /** x,yの位置にコマを配置する
     *
     * @param koma コマ
     * @param x 配置する先
     * @param y 配置する先
     */
    public void setKoma(byte koma, int x, int y) {
        int pos = (y * B_MAX) + x;
        int side = (koma >>> 5) & 1;
        long newKoma = koma & 0x1F;
        //
        // コマ種別の配置
        int shift = (4 * (pos & 0xF));
        vanmen[pos >> 4] = (vanmen[pos >> 4] & ~(0b1111L << shift))
                | (newKoma << shift);
        //
        // bitboard 更新
        long b = (koma != 0 && side == 0) ? 1L : 0L;
        long w = (koma != 0 && side == 1) ? 1L : 0L;
        //
        if (pos < 64) {
            vanmen[6] = (vanmen[6] & ~(1L << pos)) | (b << pos);
            vanmen[8] = (vanmen[8] & ~(1L << pos)) | (w << pos);
        } else {
            int p = pos - 64;
            vanmen[7] = (vanmen[7] & ~(1L << p)) | (b << p);
            vanmen[7] = (vanmen[7] & ~(1L << (p + 18))) | (w << (p + 18));
        }
        // 王の位置更新
        if (newKoma == TableDefine.pK) {
            int mv = (side == 0) ? 54 : 46;
            vanmen[5] = (vanmen[5] & ~(0xFFL << mv)) | ((long)pos << mv);
        }
    }

    /** x,yの位置にあるコマを確認する
     *
     * @param x 配置元の位置
     * @param y 配置元の位置
     * @return コマ
     */
    public byte getKoma(int x, int y) {
        // コマの種類を確認する
        final int pos = y * B_MAX + x;
        //
        // 位置の4bitを取り出す（乗算削減）
        final int idx = pos >> 4;          // pos / 16
        final int sh  = (pos & 15) << 2;   // (pos % 16) * 4
        //
        int raw = (int)((vanmen[idx] >>> sh) & 0xF);
        int mask;
        //
        // 敵味方判定 bitを立てる
        mask = (pos - 64) >> 31;      // pos < 64 → -1、pos >= 64 → 0
        final int idx8 = (int)((vanmen[8] >>> pos) & 1L);
        final int idx7 = (int)((vanmen[7] >>> (pos - 64 + 18)) & 1L);
        final long sideBit = (idx8 & mask) | (idx7 & ~mask);
        //
        return (byte)(raw | (sideBit << 5));
    }


    /** コマを CSA文字に変換
     *
     * @param koma コマ
     * @return CSA文字
     */
    public static String getKomaToString(byte koma) {
        koma = getKomaOnly(koma);
        return switch (koma) {
            case pP -> "FU";  // 歩
            case pL -> "KY";  // 香
            case pN -> "KE";  // 桂
            case pS -> "GI";  // 銀
            case pG -> "KI";  // 金
            case pB -> "KA";  // 角
            case pR -> "HI";  // 飛
            case pK -> "OU";  // 玉
            case (pK + pP) -> "TO";  // と
            case (pK + pL) -> "NY";  // 香成
            case (pK + pN) -> "NK";  // 桂成
            case (pK + pS) -> "NG";  // 銀成
            case (pK + pB) -> "UM";  // 馬
            case (pK + pR)  -> "RY";// 竜
            case pNull -> "* ";  // 空白
            default -> "? ";  // 不明
        };
    }

    /** コマを 漢字に変換
     *
     * @param koma コマ
     * @return CSA文字
     */
    public static String getKomaToKanji(byte koma) {
        koma = getKomaOnly(koma);
        return switch (koma) {
            case pP -> "歩";// 歩
            case pL -> "香";// 香
            case pN -> "桂";// 桂
            case pS -> "銀";// 銀
            case pG -> "金";// 金
            case pB -> "角";// 角
            case pR -> "飛";// 飛
            case pK -> "王";// 玉
            case (pK + pP) -> "と";// と
            case (pK + pL) -> "香成";// 香成
            case (pK + pN) -> "桂成";// 桂成
            case (pK + pS) -> "銀成";// 銀成
            case (pK + pB) -> "馬";// 馬
            case (pK + pR) -> "竜";// 竜
            case pNull -> "＊";  // 空白
            default -> "? ";  // 不明
        };
    }

    /** CSA文字をコマに変換
     *
     * @param komaString CSA文字
     * @return コマ
     */
    public static byte changeStringToKoma(String komaString) {
        return switch (komaString.toUpperCase()) {
            case "FU" -> pP;
            case "KY" -> pL;// 香
            case "KE" -> pN;// 桂
            case "GI" -> pS;// 銀
            case "KI" -> pG;// 金
            case "KA" -> pB;// 角
            case "HI" -> pR;// 飛
            case "OU" -> pK;// 玉
            case "TO" -> Table.addNariKoma(pP);// と
            case "NY" -> Table.addNariKoma(pL);// 香成
            case "NK" -> Table.addNariKoma(pN);// 桂成
            case "NG" -> Table.addNariKoma(pS);// 銀成
            case "UM" -> Table.addNariKoma(pB);// 馬
            case "RY" -> Table.addNariKoma(pR);// 竜
            default -> pNull;
        };
    }


    /** 変更する
     *
     * @param te 手
     * @return CSA文字
     */
    public static String changeTeIntToString(int te) {
        if (te == TableDefine.LOS) { // 次に指す手がない
            return STRING_LOS;
        } else if (te == TableDefine.WIN) { // 入玉勝ち
            return STRING_WIN; // 24点法を採用
        }
        StringBuilder buff = new StringBuilder();
        byte koma = Table.changeTeToKoma(te);
        int oldY = Table.changeTeToOldY(te);
        int oldX = Table.changeTeToOldX(te);
        int newY = Table.changeTeToNewY(te);
        int newX = Table.changeTeToNewX(te);
        buff.append((isSideKoma(koma) == 0) ? '+' : '-');
        if (oldY == BEAT) { // 打った
            buff.append('0');
            buff.append('0');
        } else {
            buff.append((char) ('1' + oldX));
            buff.append((char) ('1' + oldY));
        }
        buff.append((char) ('1' + newX));
        buff.append((char) ('1' + newY));
        buff.append(Table.getKomaToString(koma));
        return buff.toString();
    }

    /** 変更する
     *
     * @param te 手
     * @return CSA文字
     */
    public static String changeTeIntToKangiString(int te) {
        if (te == TableDefine.LOS) { // 次に指す手がない
            return "%TORYO";
        } else if (te == TableDefine.WIN) { // 入玉勝ち
            return "%KACHI";
        }
        int index;
        StringBuilder buff = new StringBuilder();
        byte koma = changeTeToKoma(te);
        buff.append((isSideKoma(koma) == 0) ? "先手▲" : "後手△");
        int oldY = changeTeToOldY(te);
        int newY = changeTeToNewY(te);
        int newX = changeTeToNewX(te);
        final String[] newXString = {"１","２","３","４","５","６","７","８","９","Ⅹ"};
        index = Math.min(newX, newXString.length - 1);
        buff.append(newXString[index]);
        final String[] newYString = {"一","二","三","四","五","六","七","八","九","Ⅹ"};
        index = Math.min(newY, newYString.length - 1);
        buff.append(newYString[index]);
        buff.append(Table.getKomaToKanji(koma));
        if (oldY == BEAT) { // 打った
            buff.append("打");
        } else {
            buff.append("＿");
        }
        return buff.toString();
    }
    /** 変更する
     *
     * @param csaTe CSA文字
     * @return 手
     */
    public static int changeTeStringToInt(String csaTe) {
        if (csaTe.length() < 7) {
            return 0;
        }
        int side = csaTe.charAt(0) == '+' ? 0 : 1;
        String komaString = csaTe.substring(5, 7);
        int oldX = csaTe.charAt(1);
        int oldY = csaTe.charAt(2);
        if (oldY == '0') {
            oldX = BEAT;
            oldY = BEAT;
        } else {
            oldX = oldX - '1';
            oldY = oldY - '1';
        }
        int newX = csaTe.charAt(3) - '1';
        int newY = csaTe.charAt(4) - '1';
        byte koma = Table.changeStringToKoma(komaString);
        byte nextKoma = addSideKoma(koma, side);
        return changeTeToInt(nextKoma, oldX, oldY, newX, newY);
    }
    /** 変更する。
     * 入力を -3333FU,T12 としたとき、12秒になる。
     * @param csaTe CSA文字
     * @return 経過時間
     */
    public static int changeTeStringToTime(String csaTe) {
        final Pattern pattern = Pattern.compile(",T(\\d+)");
        Matcher matcher = pattern.matcher(csaTe);
        if (! matcher.find()) {
            return 0;
        }
        String timeString = matcher.group(1);
        if (timeString == null) {
            return 0;
        }
        int time;
        try {
            time = Integer.parseInt(timeString);
        } catch(NumberFormatException e) {
            time = 0;
        }
        return time;
    }

    /** 変更する
     *
     * @param koma コマ
     * @param oldX 移動前x、打つ場合は BEAT
     * @param oldY 移動前y
     * @param newX 移動後x
     * @param newY 移動後y
     * @return 手
     */
    public static int changeTeToInt(byte koma, int oldX, int oldY, int newX, int newY) {
        return oldY | (oldX << 6) | (newY << (6 * 2)) | (newX << (6 * 3)) | (koma << (6 * 4));
    }

    /**
     * 手から oldY を取得する
     * @param te 手
     * @return old Y
     */
    public static int changeTeToOldY(int te) {
        return te & 0x1F;
    }

    /**
     * 手から oldX を取得する
     * @param te 手
     * @return old X
     */
    public static int changeTeToOldX(int te) {
        return (te >>> 6) & 0x1F;
    }

    /**
     * 手から newY を取得する
     * @param te 手
     * @return new Y
     */
    public static int changeTeToNewY(int te) {
        return (te >>> (6 * 2)) & 0x1F;
    }

    /**
     * 手から newX を取得する
     * @param te 手
     * @return new X
     */
    public static int changeTeToNewX(int te) {
        return (te >>> (6 * 3)) & 0x1F;
    }

    /**
     * 手からコマを取得する
     * @param te 手
     * @return コマ
     */
    public static byte changeTeToKoma(int te) {
        return (byte) ((te >>> (6 * 4)) & 0x3F);
    }

    /** 手コマの合計を数える
     * @param side 手番
     * @return 手コマの数
     */
    public int getTegomaNum(int side) {
        return getTegoma(pP, side)
                + getTegoma(pL, side)
                + getTegoma(pN, side)
                + getTegoma(pS, side)
                + getTegoma(pG, side)
                + getTegoma(pB, side)
                + getTegoma(pR, side);
    }

    /** 手ゴマの数を取得する
     *
     * @param koma コマ
     * @param side 手番
     * @return 手ゴマの数
     */
    public int getTegoma(byte koma, int side) {
        koma = getKomaOnly(koma);
        return (int) ((koma == pP) ?
                (vanmen[7] >>> (52 + (side*6))) & 0x3FL:
                ((vanmen[5] >>> (4 + ((koma - 2)*2 + side) * 3))) & 0x7L);
    }

    /** 手ゴマをセットする
     *
     * @param koma コマ
     * @param side 手番
     * @param now 手ゴマの数
     */
    public void setTegoma(byte koma, int side, int now) {
        koma = getKomaOnly(koma);
        if (koma == pP) {
            int pos = 52 + (side*6);
            vanmen[7] = (vanmen[7] & ~(0x3FL << pos)) | ((now & 0x3FL) << pos);
        } else {
            int pos = (4 + ((koma -2)*2 + side)*3);
            vanmen[5] = (vanmen[5] & ~(0x7L << pos)) | ((now & 0x7L)<< pos);
        }
    }

    /** 先手のビットボードを取得する
     * @return 先手のビットボード
     */
    public long[] getBitboardBlack() {
        long[] board = new long[2];
        board[0] = vanmen[6];
        board[1] = vanmen[7] & 0x3FFFFL;
        return board;
    }

    /** 後手のビットボードを取得する
     * @return 後手のビットボード
     */
    public long[] getBitboardWhite() {
        long[] board = new long[2];
        board[0] = vanmen[8];
        board[1] = (vanmen[7] >>> 18) & 0x3FFFFL;
        return board;
    }
    /**
     *  先手王x の取得
     * @return 先手王x
     */
    public int getSenteOu() {
        return (int) ((vanmen[5] >>> 54) & 0b11111111L);
    }
    /**
     * 後手王yの取得
     * @return 後手王y
     */
    public int getGoteOu() {
        return (int) ((vanmen[5] >>> 46) & 0b11111111L);
    }

    /**
     *  自分の手の王 X座標
     * @param side 0:先手、0以外:後手
     * @return 先手王x
     */
    public int getMyOu(int side) {
        return ((side == 0) ? getSenteOu() : getGoteOu());
    }

    /** デバッグ用 */
    @Override
    public @NotNull String toString() {
        StringBuilder buffer = new StringBuilder();
        int side = this.getSide();
        for (int y = 0; y < vanmen.length; y++) {
            buffer.append('P');
            buffer.append((y+1));
            for (int x = vanmen.length - 1; 0 <= x; x--) {
                byte koma = getKoma(x,y);
                if (koma == pNull) {
                    buffer.append(' ');
                } else if (isSideKoma(koma) == 0) {
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
        buffer.append(side == 0 ? "+" : "-").append("\n");
        return buffer.toString();
    }

    /** 合法手のリストの取得
     * @return 合法手のリスト
     */
    public List<TeTable> createChild() {
        return createChild((table,te) -> {
            return new TeTable(table.clone(), te); // cloneを使って変更されないようにコピーを取る
        });
    }
    /** 合法手のリストの取得
     * @return 合法手のリスト
     */
    public  <T extends TeTable> List<T> createChild(Function2<Table, Integer, T> creator) {
        List<T> list = new ArrayList<>();
        int te = LOS; // 初期値を設定する
        final long[] undo = this.createUndo(); // cloneを作って戻せるようにする
        AllBitboard all = new AllBitboard(this);
        Table target = this.clone(); // 壊れても良いように元情報からcloneを作る
        while (true) {
            te = target.iterate(te, undo, all);
            if (te == LOS) {
                break;
            } else if (te == WIN) {
                // new TeTable(this,TableDefine.WIN) を型変換のために↓に変更した
                list.add(creator.invoke(target, TableDefine.WIN));
                break;
            }
            // new TeTable(target,te) を型変換のために↓に変更した
            T createResult = creator.invoke(target,te);
            if (createResult != null) {  // 不要でなければ追加する
                list.add(createResult);
            }
            //
            // whileから戻ってくると指した後に手になっているので戻す
            target.setUndo(undo);
        }
        return list;
    }

    /** 次の手を生成する
     *
     * @param te 現在の手
     * @param undo 失敗時に戻す用
     * @param all ビットボード
     * @return 次の手
     */
    public int iterate(final int te, long[] undo, AllBitboard all) {
        //
        this.setUndo(undo);  // 上書きする
        //
        // 自分の王の位置を特定する
        final int side = this.getSide();
        //
        byte koma;
        int newX;
        int newY;
        int oldX;
        int oldY;
        if ((te == LOS) || (te == WIN)) {
            if (this.checkSelfMate(1 - side, all)) {
                return WIN;  // すでに詰ましている
            }
            int ans = this.isKingWin(side, all);
            if (0 < ans) {
                return WIN;  // 勝った
            }
            if (ans < 0) {
                return LOS; // 負けた
            }
            koma = pNull;
            oldX = 0;
            oldY = 0;
            newX = -1;
            newY = -1;
        } else {
            koma = Table.changeTeToKoma(te);
            oldX = Table.changeTeToOldX(te);
            oldY = Table.changeTeToOldY(te);
            newX = Table.changeTeToNewX(te);
            newY = Table.changeTeToNewY(te);
            if (oldX == BEAT) {
                oldX = newX;
                oldY = newY;
                newX = BEAT;
                newY = BEAT;
            }
        }
        int result = LOS;
        if (this.checkSelfMate(side, all)) {
            // 王手用の処理
            return iterateOute(koma, side, oldX, oldY, newX, newY, undo, all);
        }
        // 王手でない時の処理
        long[] board = new long[2];
        AllBitboard.cpBit(board, all.getBitboard(1 - this.getSide()));  // 後手コピー
        AllBitboard.notBit(board);  // 反転するので空白は先手になる
        int pos =  (oldY * B_MAX) + oldX -1;
        while (true) {
            pos = AllBitboard.getBitBoardNext(board, pos);
            if (B_MAX * B_MAX <= pos) {
                break; // 最後まで検索したのでおわり
            }
            oldX = pos % B_MAX;
            oldY = pos / B_MAX;
            byte tableKoma = this.getKoma(oldX, oldY);
            if (tableKoma == TableDefine.pNull) {
                result = iterateBeat(koma, side, oldX, oldY, undo);
            } else {
                result = iterateNotBeat(koma, side, oldX, oldY, newX, newY, undo, all);
            }
            if (result != LOS) {
                break;
            }
            koma = pNull;  // 該当マスに合法手がなったのでやりなおし
        }
        if (B_MAX * B_MAX <= pos) {
            return LOS; // 最後まで検索したのでおわり
        }
        return result;
    }

    /**
     * 王手の時のループ処理
     * @param koma コマ
     * @param oldX 位置
     * @param oldY 位置
     * @param newX 位置
     * @param newY 位置
     * @param side 先手か後手か
     * @param undo 元に戻す用
     * @param all bitboard用
     */
    public int iterateOute(
            byte koma, int side, int oldX, int oldY, int newX, int newY,
                           long[] undo, AllBitboard all) {
        //
        int pos = this.getMyOu(this.getSide());
        if (koma == pNull) {
            int myOuX = pos % B_MAX;
            int myOuY = pos / B_MAX;
            koma = pK; // 先行して王手処理をする
            oldX = myOuX;
            oldY = myOuY;
        }
        if ((koma == pK) || (koma == pk)) {
            int result = iterateOuteResolve(koma, side, oldX, oldY, newX, newY, all);
            if (result != LOS) {
                return result;
            }
            koma = pNull;
            oldX = -1;
            //
        }
        if (2 <= all.getEffectNum(1 - side)) {
            return LOS; // 両王手のときは以降は不要
        }
        //
        // 移動元をチェック
        long[] space = all.getSpaceBitboard();
        long[] myBoard = new long[2];
        AllBitboard.cpBit(myBoard, all.getEffectOute(1- side));  // 敵の王の効き
        AllBitboard.andBit(myBoard, space); // 敵の効きがあって、打ち込みまだは移動できる場所
        AllBitboard.orBit(myBoard, all.getBitboard(side)); // 自分のコマ
        AllBitboard.clearBit(myBoard, pos);  // 王はすでにチェック済なので確認しない
        //
        // 自分のボードから拾った自分のコマがいける筋をチェックする
        int oldPos = (oldX == -1) ? -1 : oldY *B_MAX + oldX;
        int newPos = (newX == -1) ? -1 : newY *B_MAX + newX;
        int result = LOS;
        while (true) {
            if ((oldPos == -1) || (newPos == -1)) {
                // newPosが決まっていなければ次のマスへするためにここを動かす
                // 決まっている場合はここのマスを動かしたらダメじゃん
                oldPos = AllBitboard.getBitBoardNext(myBoard, oldPos);
            }
            if (B_MAX * B_MAX <= oldPos) {
                break; // 最後まで検索したのでおわり
            }
            // 空白チェック
            oldX = oldPos % B_MAX; // 移動元のコマの位置
            oldY = oldPos / B_MAX;
            if (AllBitboard.getBit(all.getBitboard(side),oldPos) == 0L) {
                // 手持ちのコマの空白への打ち込み
                result = this.iterateBeat(koma, side, oldX, oldY, undo);
            } else {
                // 自コマの移動チェック
                byte nextKoma = this.getKoma(oldX, oldY);
                //
                // 王手を妨げられるマスでマスク
                long[] newEffect = all.createKomaBitboard(space, side, nextKoma, oldX, oldY);
                AllBitboard.andBit(newEffect, all.getEffectOute(1- side));
                //
                newPos = AllBitboard.getBitBoardNext(newEffect, newPos);
                if (newPos < B_MAX * B_MAX) {
                    //
                    // 移動する
                    this.moveKoma(nextKoma, oldX, oldY, newPos % B_MAX, newPos / B_MAX);
                    AllBitboard all2 = new AllBitboard(this); // 移動したのでbitboardを更新する
                    if (this.checkSelfMate(side, all2)) {
                        // 移動したら、そのコマの移動によって王手が掛かった(空き王手になった？)
                        this.setUndo(undo); // hitしたら元に戻す
                        // 次のマスへ――
                        // 移動したら王手掛かったのであれば、どこに移動しても空き王手変わらないので、
                        // AllBitboard.getBitBoardNext(newEffect, newPos)を使った次の移動は見無くて良い
                    } else {
                        // ヒットしたのでこれで終了――
                        // 結果を返す
                        result = Table.changeTeToInt(
                                nextKoma, oldX, oldY, newPos % B_MAX, newPos / B_MAX);
                    }
                }
            }
            if (result != LOS) {
                break;
            }
            koma = pNull; // クリアしておく
            newPos = -1; // クリアしておく
        }
        return result;
    }

    /**
     * 王が移動できるかチェックする
     * @param koma 王の情報
     * @param oldX 王の元の位置x
     * @param oldY 王の元の位置y
     * @param newX 王の先の位置x
     * @param newY 王の先の位置y
     * @param all bitboard用
     * @return 移動した手の情報
     */
    public int iterateOuteResolve(
            byte koma, int side, int oldX,int oldY,int newX, int newY, AllBitboard all) {
        //
        // 王が移動できるマスクを作る
        long[] space = all.getSpaceBitboard();
        long[] kingBitboard = all.createKomaBitboard(space,this.getSide(),koma,oldX, oldY);
        AllBitboard.notAndBit(kingBitboard, all.getEffect(1 - this.getSide())); // 敵の効きを排除
        //
        // ここより前は検索不要
        int i = (newX == -1) ? -1: (newY * B_MAX + newX);
        i = AllBitboard.getBitBoardNext(kingBitboard, i);
        if (i < B_MAX * B_MAX) {
            // 実際に移動する
            byte nextKoma = addSideKoma(koma, side);  // 先手後手フラグを立てる
            this.moveKoma(nextKoma, oldX, oldY, i % B_MAX, i/B_MAX); // 移動して
            return Table.changeTeToInt(
                    nextKoma, oldX, oldY, i % B_MAX, i/B_MAX);
        }
        return LOS;
    }

    /**
     * 該当マスからの移動に合法手があるか？
     * @param side 手番
     * @param koma コマ(不明の場合は pNull )
     * @param oldX 該当マス
     * @param oldY 該当マス
     * @param undo 復旧用
     * @return 次に指す手
     */
    public int iterateBeat(
            byte koma, int side, int oldX, int oldY,
            long[] undo) {
        // 打つ
        int myOuX = this.getMyOu(side) % B_MAX; // 移動後の王の位置(打ち込みで王の位置変わらない)
        int myOuY = this.getMyOu(side) / B_MAX;
        for (byte komaOnly = (byte) ((0XF & koma) + 1) ; // 次の手
             komaOnly <= pR;
             komaOnly++) {
            if (this.getTegoma(komaOnly, side) <= 0) {
                continue; // 手ゴマがない
            }
            if (Checklist.CHECKLIST[getKomaOnly(komaOnly)].getUchiKinshi(side, oldY)) {
                continue; // 置いてはいけないところに置いていないかチェック
            }
            byte tegomaHave = addSideKoma(komaOnly, side);
            if (komaOnly == pP) {  // ２歩チェック
                boolean flag = true;
                for (int yy = 0; yy < B_MAX; yy++) {
                    if (tegomaHave == this.getKoma(oldX, yy)) {
                        flag = false;
                        break;
                    }
                }
                if (!flag) {
                    continue;  // 2歩なので次へ
                }
            }
            //
            //
            //
            // 既に王手のチェックはしている場合で王手の場合、
            // 　⇒打ち込んでいることで回避できるので自王手チェック(空き王手チェック)は不要
            // 既に王手のチェックはしている場合で王手出ない場合、
            //  ⇒新たに打ち込むことで王手になることはないので自王手チェック(空き王手チェック)は不要
            //
            // コマ移動自体は歩以外の場合でもしないといけない
            byte nextKoma = addSideKoma(komaOnly, side);  // 先手後手フラグを立てる
            this.moveKoma(nextKoma, BEAT, BEAT, oldX, oldY);
            if (((nextKoma == pP) || (nextKoma == pp))
                    && (myOuX == oldX)
                    && (myOuY + (this.getSide()==0?-1:+1) == oldY)) {
                AllBitboard all2 = new AllBitboard(this); // 移動したのでbitboardを更新する
                if (this.checkFuMate(undo, all2)) {  // 打ち歩詰めチェック
                    this.setUndo(undo); // hitしたら元に戻す
                    continue; // 打ち歩詰めなので次の持ちコマへ
                }
            }
            return Table.changeTeToInt(nextKoma, BEAT, BEAT, oldX, oldY);
        }
        return LOS;  // もう打てるコマがないので次へ
    }

    /**
     * 該当マスからの移動に合法手があるか？
     * @param side 手番
     * @param koma コマ(不明の場合は pNull )
     * @param oldX 該当マス
     * @param oldY 該当マス
     * @param newX 移動先 (不明の場合は -1)
     * @param newY 移動先 (不明の場合は -1)
     * @param undo 元に戻す用
     * @return 次に指す手
     */
    public int iterateNotBeat(
            byte koma, int side, int oldX, int oldY, int newX, int newY,
            long[] undo, AllBitboard all) {
        final byte baseComa = this.getKoma(oldX, oldY);
        if (koma == pNull) {
            koma = baseComa; // 自分のコマなので打てるか確認する
            newX = -1;
            newY = -1;
        }
        Checklist low = Checklist.CHECKLIST[getKomaOnly(koma)];
        boolean upgrade = !low.narenai();  // 成れるコマである
        //
        // 成れるかをチェックする
        if (upgrade // なれるコマである
                && (newX != -1)  // 最初でない
                && (koma == baseComa)  // 移動前と移動後が同じ
                && (!isNariKoma(baseComa)) // 移動前が成っていない
                && (low.isTekijin(side,oldY) // 敵陣から出た
                || low.isTekijin(side, newY))) { // 敵陣に入った
            byte nextKoma = addSideKoma(addNariKoma(koma), side);  // 先手後手フラグを立てる
            this.moveKoma(nextKoma, oldX, oldY, newX, newY);
            return Table.changeTeToInt(nextKoma, oldX, oldY, newX, newY);
        }
        koma = baseComa; // 移動前のコマに戻す
        //
        //
        long[] space = all.getSpaceBitboard();
        long[] komaBitboard = all.createKomaBitboard(space, side, koma, oldX, oldY);
        int pos = (newX == -1)? -1 : newY * B_MAX + newX;
        //
        // 次の手を指す
        int te = LOS;
        while (true) {
            pos = AllBitboard.getBitBoardNext(komaBitboard, pos);
            newX = pos % B_MAX;
            newY = pos / B_MAX;
            if (B_MAX * B_MAX <= pos) {
                break;  // このコマはもう動かせない ⇒ 隣のマスをチェックする
            }
            //
            // newX,newYが確定したので強制成りが必要か見る
            byte nextKoma =
                    upgrade // 成れないなら考慮不要
                            && (low.getForcedNari(side, newY) // 強制なりなら確定
                            || ((!low.narazuOK()) // 成らずOKでなくて敵陣に出たり入ったりした
                            && (low.isTekijin(side, oldY)
                            || low.isTekijin(side, newY))))
                            ?  addNariKoma(koma) : koma;
            nextKoma = addSideKoma(nextKoma, side);  // 先手後手フラグを立てる
            moveKoma(nextKoma, oldX, oldY, newX, newY); // 新しい局面を作る
            AllBitboard all2 = new AllBitboard(this); // 移動したのでbitboardを更新する
            if (this.checkSelfMate(side, all2)) {
                this.setUndo(undo); // 戻す
                continue; // 空き王手ならばやりなおし
            }
            te = changeTeToInt(nextKoma, oldX, oldY, newX, newY);  // 手を追加する
            break;
        }
        return te;  // もう打てるコマがないので次へ
    }

    /**
     * 入玉勝ちチェック
     * @param side 手番
     * @param all AllBitboard
     * @return -1:先手は 24 点以上あったら投了する、0:関係なし、1:後手は 24 点以上あったら勝ちにする
     */
    public int isKingWin(int side, AllBitboard all) {
        //
        // 入玉チェック
        int myOuY = this.getMyOu(side) / B_MAX;
        if (!((side == 0 && myOuY <= 2) || (side != 0 && myOuY >= 6))) return 0;
        if (this.checkSelfMate(side, all)) {
            return 0;// 王手を掛けられていない
        }
        //
        // 局面上の敵陣のコマを数える
        long[] board = new long[2];
        AllBitboard.cpBit(board, all.getBitboard(side));  // 自サイドのコマ
        AllBitboard.setTekijin(board, side);  // 自サイドのコマで敵陣にあるものだけ抽出
        int sum = Long.bitCount(board[0]) + Long.bitCount(board[1]);  // 数の計測
        int i = -1;
        int value = 0;
        while (true) {
            i = AllBitboard.getBitBoardNext(board, i);
            if (B_MAX * B_MAX <= i) {
                break; // 最後まで検索したのでおわり
            }
            byte koma = this.getKoma(i % B_MAX, i / B_MAX);
            byte komaOnly = Table.getKomaOnly(koma);
            if ((komaOnly == TableDefine.pB)
                    || (komaOnly == TableDefine.pR)){
                value += 5; // 大コマは５点
            } else if (komaOnly != TableDefine.pK) { // 王は除く
                value += 1; // 小コマは１点
            }
        }
        if (sum < 10) {
            return 0; // 王を除く自コマが10枚より少ない
        }
        // 手持ちの小コマの数を数える
        value += this.getTegoma(TableDefine.pP, side);
        value += this.getTegoma(TableDefine.pL, side);
        value += this.getTegoma(TableDefine.pN, side);
        value += this.getTegoma(TableDefine.pS, side);
        value += this.getTegoma(TableDefine.pG, side);
        //
        // 手持ちの大コマの数を数える
        value += this.getTegoma(TableDefine.pB, side) * 5;
        value += this.getTegoma(TableDefine.pR, side) * 5;
        //
        if (side == 0) {
            return (28 <= value) ?  -1 : 0; // 先手は 24 点以上あったら投了する
        }
        return (24 <= value) ? 1 : 0; // 後手は 24 点以上あったら勝ちにする
    }

    /**
     * 打ち歩詰めチェック(王が動かなかったら前に歩があるので王手確定)
     * @param undo 戻し用データ
     * @param all bitboard
     * @return true:打ち歩詰め, false:打ち歩詰めではない
     */
    public boolean checkFuMate(long[] undo, AllBitboard all) {
        int result = iterateOute(
                pNull, this.getSide(), -1, -1, -1, -1,
                undo, all);
        if (result != LOS) {
            this.setUndo(undo);  // 詰みでないなら移動しているので戻す
            return false;  // 歩詰み
        }
        return true;
    }

    /** 王手のチェック
     * @param side 手番 先手なら0、後手なら1
     * @param all ベースとなるビットボード
     * @return 空き王手ならtrue
     */
    public boolean checkSelfMate(int side, AllBitboard all) {
        int pos = this.getMyOu(side);
        return (all.getEffect(1 - side)[pos >> 6] & (1L << (pos & 63))) != 0;
    }

    /** 局面を初期化する
     * 初期局面を局面上のデータをクリアして先手番なら後手の持ちコマ、
     * 後手番なら先手の持ちコマにする
     */
    public void clearForCSAProtocol() {
        AllBitboard all = new AllBitboard(this);
        long[] board = new long[2];
        AllBitboard.cpBit(board, all.getBitboard(0));  // 先手コピー
        AllBitboard.orBit(board, all.getBitboard(1));  // 後手加算
        int pos = -1;
        while (true) {
            pos = AllBitboard.getBitBoardNext(board, pos);
            if (B_MAX * B_MAX <= pos) {
                break;
            }
            // 局面から消す
            this.setKoma(TableDefine.pNull, pos % B_MAX, pos / B_MAX);
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
            this.setSide(0);//先手
            endForCSAProtocol();
            return true;
        } else if (message.equals("-")) {
            this.setSide(1);//先手
            endForCSAProtocol();
            return true;
        } else if (0 == message.indexOf("P+")) {
            String[] splits = message.substring(2).split("00");
            for (String split : splits) {
                if (split.isEmpty()) {
                    continue;
                }
                byte koma = Table.changeStringToKoma(split);
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
                int side = message.charAt(x*3 + 2) == '+' ? 0 : 1;
                String komaString = message.substring(x*3 + 3,x*3 + 5);
                byte koma = Table.changeStringToKoma(komaString);
                if (koma != TableDefine.pNull) {
                    koma = addSideKoma(koma, side);
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
        AllBitboard all = new AllBitboard(this);
        long[] board = new long[2];
        AllBitboard.cpBit(board, all.getBitboard(0));  // 先手コピー
        AllBitboard.orBit(board, all.getBitboard(1));  // 後手加算
        int pos = -1;
        while (true) {
            pos = AllBitboard.getBitBoardNext(board, pos);
            if (B_MAX * B_MAX <= pos) {
                break;
            }
            byte ou = this.getKoma(pos % B_MAX, pos / B_MAX);
            if (ou == TableDefine.pK) {
                senteOu = true;
            } else if (ou == TableDefine.pk) {
                goteOu = true;
            }
        }
        //
        AllBitboard.notBit(board); // ボードを反転させる
        pos = -1;
        if (! senteOu) {
            // 局面上に空があったらそこに置いておく
            pos = AllBitboard.getBitBoardNext(board, pos);
            this.setKoma(TableDefine.pK, pos % B_MAX, pos / B_MAX);
        }
        // 局面上に空があったらそこに置いておく(逆から置くのでbitboardが使えない)
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
        only.setSide(1 - only.getSide());
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
                koma = Table.addNariKoma(koma);
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
        Table newTable = Table.createNullTable();
        //
        newTable.setSide(this.getSide());
        //
        AllBitboard all = new AllBitboard(this);
        long[] board = new long[2];
        AllBitboard.cpBit(board, all.getBitboard(0));  // 先手コピー
        AllBitboard.orBit(board, all.getBitboard(1));  // 後手加算
        int pos = -1;
        while (true) {
            pos = AllBitboard.getBitBoardNext(board, pos);
            if (B_MAX * B_MAX <= pos) {
                break;
            }
            int flipX = B_MAX - (pos % B_MAX) - 1;
            byte koma = this.getKoma(pos % B_MAX, pos / B_MAX);
            newTable.setKoma(koma, flipX, pos / B_MAX);
        }
        //
        newTable.setTegoma(pP, 0, this.getTegoma(pP, 0));
        newTable.setTegoma(pL, 0, this.getTegoma(pL, 0));
        newTable.setTegoma(pN, 0, this.getTegoma(pN, 0));
        newTable.setTegoma(pS, 0, this.getTegoma(pS, 0));
        newTable.setTegoma(pG, 0, this.getTegoma(pG, 0));
        newTable.setTegoma(pB, 0, this.getTegoma(pB, 0));
        newTable.setTegoma(pR, 0, this.getTegoma(pR, 0));
        //
        newTable.setTegoma(pP, 1, this.getTegoma(pP, 1));
        newTable.setTegoma(pL, 1, this.getTegoma(pL, 1));
        newTable.setTegoma(pN, 1, this.getTegoma(pN, 1));
        newTable.setTegoma(pS, 1, this.getTegoma(pS, 1));
        newTable.setTegoma(pG, 1, this.getTegoma(pG, 1));
        newTable.setTegoma(pB, 1, this.getTegoma(pB, 1));
        newTable.setTegoma(pR, 1, this.getTegoma(pR, 1));
        //
        return newTable;
    }
    /** 局面を上下反転させる
     *
     * @return 反転したテーブル
     */
    public Table flippingVertical() {
        Table newTable = Table.createNullTable();
        //
        // 上下なので手番も反転
        newTable.setSide(1 - this.getSide());
        //
        AllBitboard all = new AllBitboard(this);
        long[] board = new long[2];
        AllBitboard.cpBit(board, all.getBitboard(0));  // 先手コピー
        AllBitboard.orBit(board, all.getBitboard(1));  // 後手加算
        int pos = -1;
        while (true) {
            pos = AllBitboard.getBitBoardNext(board, pos);
            if (B_MAX * B_MAX <= pos) {
                break;
            }
            int flipY = B_MAX - (pos / B_MAX) - 1;
            byte koma = this.getKoma(pos % B_MAX, pos / B_MAX);
            if (koma != 0) {
                koma = addSideKoma(koma,1 - isSideKoma(koma)); // xor で反転
            }
            newTable.setKoma(koma, pos % B_MAX, flipY);
        }
        //
        newTable.setTegoma(pP, 0, this.getTegoma(pP, 1));
        newTable.setTegoma(pL, 0, this.getTegoma(pL, 1));
        newTable.setTegoma(pN, 0, this.getTegoma(pN, 1));
        newTable.setTegoma(pS, 0, this.getTegoma(pS, 1));
        newTable.setTegoma(pG, 0, this.getTegoma(pG, 1));
        newTable.setTegoma(pB, 0, this.getTegoma(pB, 1));
        newTable.setTegoma(pR, 0, this.getTegoma(pR, 1));
        //
        newTable.setTegoma(pP, 1, this.getTegoma(pP, 0));
        newTable.setTegoma(pL, 1, this.getTegoma(pL, 0));
        newTable.setTegoma(pN, 1, this.getTegoma(pN, 0));
        newTable.setTegoma(pS, 1, this.getTegoma(pS, 0));
        newTable.setTegoma(pG, 1, this.getTegoma(pG, 0));
        newTable.setTegoma(pB, 1, this.getTegoma(pB, 0));
        newTable.setTegoma(pR, 1, this.getTegoma(pR, 0));
        //
        return newTable;
    }

}

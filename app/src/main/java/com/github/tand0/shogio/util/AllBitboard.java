package com.github.tand0.shogio.util;

import static com.github.tand0.shogio.util.TableDefine.B_MAX;
import org.jetbrains.annotations.NotNull;

/** 全てのビットテーブル */
public class AllBitboard {

    /** コマ用のビットボード */
    private final static long[][][] komaBitboard = new long[32][][];

    /** 横移動時の端消し用のbitboard */
    private final static long[][] edgeBitboard = new long[9][2];

    /** テーブル */
    private Table table;

    /** コマの位置 */
    private final long[][] bitboard = new long[2][];

    /** 全てのコマの効き */
    private final long[][] effectBitboard = new long[2][];

    /** 王手をしたコマの効き */
    private final long[][] effectOuteBitboard = new long[2][];

    /** 王手をしたコマの位置 */
    private final long[][] effectOuteKoma = new long[2][];

    /** 横移動時の端消し用のbitboard */
    private long[] spaceBitboard = null;

    /** コンストラクタ */
    public AllBitboard(Table table) {
        clear(table);
    }

    /** クリアする */
    public void clear(Table table) {
        this.table = table;
        this.bitboard[0] = null;
        this.bitboard[1] = null;
        this.effectBitboard[0] = null;
        this.effectBitboard[1] = null;
        this.effectOuteBitboard[0] = null;
        this.effectOuteBitboard[1] = null;
        this.effectOuteKoma[0] = null;
        this.effectOuteKoma[1] = null;
    }

    /** bitboard を取得する
     * @param side 0:先手、1:後手
     * @return bitboard
     */
    public long[] getBitboard(int side) {
        if (bitboard[side] != null) return bitboard[side];
        if (side == 0) {
            bitboard[side] = table.getBitboardBlack();
        } else {
            bitboard[side] = table.getBitboardWhite();
        }
        return bitboard[side];
    }

    /**
     * 効きの bitboard
     * @param side 0:先手、1:後手
     * @return bitboard
     */
    public long[] getEffect(int side) {
        if (effectBitboard[side] != null) return effectBitboard[side];
        long[] usedBoard = getBitboard(side);
        //
        effectBitboard[side] = new long[2];
        effectOuteBitboard[side] = new long[2];
        effectOuteKoma[side] = new long[2];
        int enemyOu = table.getMyOu(1 - side);
        //
        int i = -1;
        long[] spaceX = new long[2];
        AllBitboard.cpBit(spaceX,getSpaceBitboard());
        setBit(spaceX,table.getMyOu(1 - side));  // 自玉は見ない
        long[] kingBitboard = new long[2];
        setBit(kingBitboard,table.getMyOu(1 - side));  // 相手の王のチェック用
        //
        //
        while (true) {
            i = AllBitboard.getBitBoardNext(usedBoard, i);
            if (B_MAX * B_MAX <= i) {
                break; // 最後まで検索したのでおわり
            }
            int x = i % B_MAX;
            int y = i / B_MAX;
            byte koma = table.getKoma(x, y);
            int type = koma & 0x1F;
            //
            long[] komaEffect = createKomaBitboardEffect(spaceX, side, koma, x, y);
            AllBitboard.orBit(effectBitboard[side], komaEffect);
            if (((komaEffect[0] & kingBitboard[0]) != 0)
               || ((komaEffect[1] & kingBitboard[1]) != 0)) {
                //
                // 王手が掛かった！
                // 王手をしたコマのbitを立てる
                setBit(effectOuteKoma[side], i);
                //
                // 王手をしたコマの効きを設定する
                if ((type != TableDefine.pL)
                    && (type != TableDefine.pR)
                        && (type != TableDefine.pB)
                        && (type != Table.addNariKoma(TableDefine.pR))
                        && (type != Table.addNariKoma(TableDefine.pB))) {
                    // 飛ばないコマの時の王手をしたコマの効きを設定する
                    setBit(effectOuteBitboard[side],i);
                } else {
                    // 飛びコマのとき(戻っていく)の王手をしたコマの効きを設定する
                    for (int ddx = x, ddy = y,
                         dx = Integer.signum((enemyOu % B_MAX) - x),
                         dy = Integer.signum((enemyOu / B_MAX) - y);
                         (ddy * B_MAX + ddx) != enemyOu;
                         ddx += dx, ddy += dy) {
                        setBit(effectOuteBitboard[side],ddy * B_MAX + ddx);
                    }
                }
            }
        }
        return effectBitboard[side];
    }

    /**
     * 王手の効きを乗せる
     * @return 王手の効きのインスタンス
     */
    public long[] getEffectOute(int side) {
        if (bitboard[side] == null) {
            getEffect(side);  // 生成する
        }
        return effectOuteBitboard[side];
    }

    /**
     * 王手が掛かっているコマの数の取得
     * @param side 0:先手、1:後手
     * @return 王手が掛かっているコマの数
     */
    public long getEffectNum(int side) {
        if (effectOuteKoma[side] == null) {
            getEffect(side);  // 生成する
        }
        return Long.bitCount(effectOuteKoma[side][0])
                + Long.bitCount(effectOuteKoma[side][1]);
    }

    /** 81bit bitboard のビットを立てる。
     * JITコンパイラでインライン化されるのを期待してstaticを付けた。
     * @param bb bitboard
     * @param pos ビットを取得する位置(130を超えるとバグになる例を修正済版)
     * @return ビット値
     */
    protected static long getBit(long[] bb, int pos) {
        int mask  = 0x3F & ~((pos & ~80) >> 31); // ((pos < 0) || (81 <= pos))
        return 1L & mask & (bb[1 & (pos >>> 6)] >>> (mask & pos));
    }

    /** 81bit bitboard のビットを立てる。
     * JITコンパイラでインライン化されるのを期待してstaticを付けた。
     * @param bb bitboard
     * @param pos ビットを立てる位置
     */
    protected static void setBit(long[] bb, int pos) {
        int mask = 1 & ~((pos & ~80) >> 31); // ((pos < 0) || (81 <= pos))
        bb[1 & (pos >>> 6)] |= ((long)mask) << (pos & 63);
    }

    /** 81bit bitboard のビットをorで立てる。
     * JITコンパイラでインライン化されるのを期待してstaticを付けた。
     * @param bb bitboard
     * @param pos ビットを立てる位置
     * @param or 1:加算、0:なにもしない、その他:バグります
     */
    protected static void setOrBit(long[] bb, int pos, long or) {
        int mask = 1 & ~((pos & ~80) >> 31); // ((pos < 0) || (81 <= pos))
        bb[1 & (pos >>> 6)] |= (mask & or) << (pos & 63);
    }

    /** 81bit bitboard のビットを落とす。
     * JITコンパイラでインライン化されるのを期待してstaticを付けた。
     * @param bb bitboard
     * @param pos ビットを立てる位置
     */
    protected static void clearBit(long[] bb, int pos) {
        int mask = ~((pos & ~80) >> 31);
        bb[1 & (pos >>> 6)] &= ~(1L << (pos & 63 & mask));
    }
    /** 81bit bitboard を copy する。
     * JITコンパイラでインライン化されるのを期待してstaticを付けた。
     * @param to 更新先
     * @param from 更新元
     */
    protected static void cpBit(long[] to, long[] from) {
        to[0] = from[0];
        to[1] = from[1];
    }
    /** 81bit bitboard を or する。
     * JITコンパイラでインライン化されるのを期待してstaticを付けた。
     * @param to 更新先
     * @param from 更新元
     */
    protected static void andBit(long[] to, long[] from) {
        to[0] &= from[0];
        to[1] &= from[1];
    }
    /** 81bit bitboard を not and する。
     * JITコンパイラでインライン化されるのを期待してstaticを付けた。
     * @param to 更新先
     * @param from 更新元
     */
    protected static void notAndBit(long[] to, long[] from) {
        to[0] &= ~from[0];
        to[1] &= ~from[1];
    }
    /** 81bit bitboard を or する。
     * JITコンパイラでインライン化されるのを期待してstaticを付けた。
     * @param to 更新先
     * @param from 更新元
     */
    protected static void orBit(long[] to, long[] from) {
        to[0] |= from[0];
        to[1] |= from[1];
    }
    /** 81bit bitboard を bit 反転する。
     * JITコンパイラでインライン化されるのを期待してstaticを付けた。
     * @param to 更新先
     */
    protected static void notBit(long[] to) {
        to[0] = ~to[0];
        to[1] = ~to[1];
    }
    /** bit シフトする。
     * JITコンパイラでインライン化されるのを期待してstaticを付けた。
     * @param bb bitboard
     * @param len シフトする長さ。0ならなにもしない。正なら左シフト、負なら右シフト
     */
    protected static void shiftBit(long[] bb, int len) {
        //
        long hi = bb[1];
        long lo = bb[0];
        //
        // マスク生成（条件を -1 or 0 に変換）
        long rs64 = ((len + 64 -1) >> 31);   // len <= -64
        long rs   = (len >> 31) & ~rs64;     // len < 0 && len > -64
        long ls64 = ~((len - 64) >> 31);     // len >= 64
        long ls   = (~(len >> 31)) & ~ls64;  // 0 <= len < 64
        //
        bb[0] = ((hi >>> (-len - 64)) & rs64) // 右シフト 64bit 超え
            | (((hi << (64 + len)) | (lo >>> -len)) & rs)  // 右シフト
            | ((lo << len) & ls);  // 左シフト
        bb[1] = (hi >>> -len) & rs  // 右シフト
            | ((hi << len) | (lo >>> (64 - len))) & ls  // 左シフト
            | (lo << (len - 64)) & ls64;  // 左シフト 64bit 超え
        //
        // 17bit マスク（盤外をゼロに）
        bb[1] &= 0x1FFFF;  // 81ビット枠より先をクリアする
    }

    /** ビットボードを0があるまで右シフトする。
     * JITコンパイラでインライン化されるのを期待してstaticを付けた。
     * @param b ビットボード
     * @param pos 現在のポイント
     * @return 現在の index 値
     */
    public static int getBitBoardNext(long[] b, int pos) {
        pos++; // 次をチェックするために +1 加算する(-1始まりだと0からチェック開始)
        // pos < 64 → maskLo = -1、pos >= 64 → maskLo = 0
        int maskLo = (pos - 64) >> 31;
        // lo = ntz(b[0] >>> pos)
        int lo = Long.numberOfTrailingZeros(b[0] >>> pos);
        // lo < 64 → maskHit = -1、lo >= 64 → maskHit = 0
        int maskHit = (lo - 64) >> 31;
        // pos<64 && lo<64 のときだけ pos+lo を採用
        int resLo = (pos + lo) & maskLo & maskHit;
        int hi0 = Long.numberOfTrailingZeros(b[1]);
        // pos<64 && lo>=64 のときだけ 64 + ntz(b[1]) を採用
        int resHi0 = (64 + hi0) & maskLo & ~maskHit;
        int hi1 = Long.numberOfTrailingZeros(b[1] >>> (pos - 64));
        // pos>=64 のときだけ pos + ntz(b[1] >>> (pos-64)) を採用
        int resHi1 = (pos + hi1) & ~maskLo;
        return Math.min(B_MAX * B_MAX, resLo | resHi0 | resHi1);
    }

    /** 敵陣のマスクをする。
     * JITコンパイラでインライン化されるのを期待してstaticを付けた。
     * @param bitboard マスク先
     * @param side 0:先手、1:後手
     */
    public static void setTekijin(long[] bitboard, int side) {
        // side == 0 → mask0 = -1、side == 1 → mask0 = 0
        int mask0 = (side - 1) >> 31;
        int mask1 = ~mask0;
        long m0_lo = 0b111111111111111111111111111L;  // 先手用マスク（0〜26）
        long m1_lo = (0b111111111L << (B_MAX * 6));
        long m1_hi = 0b11111111111111111L;   // 後手用マスク（54〜80）
        long maskLo = (m0_lo & mask0) | (m1_lo & mask1);  // side に応じてマスクを選択
        long maskHi = m1_hi & mask1;
        bitboard[0] &= maskLo;  // 適用
        bitboard[1] &= maskHi;
    }

    /** 盤面上のスペースのbitboardを生成する */
    public long[] getSpaceBitboard() {
        if (spaceBitboard != null) return spaceBitboard;
        //
        spaceBitboard = new long[2];
        spaceBitboard[0] = ~(this.getBitboard(0)[0] | this.getBitboard(1)[0]);
        spaceBitboard[1] = ~(this.getBitboard(0)[1] | this.getBitboard(1)[1]);
        spaceBitboard[1] = spaceBitboard[1] & 0x1FFFFL;  // 空白マスを作る
        return spaceBitboard;
    }
    /** koma 用の bitboardを返す
     * param spacexX 空白のコマの位置を渡す
     * @param side 先手、後手種類
     * @param koma コマ種類
     * @param x コマのx位置
     * @param y コマのy位置
     * @return bitboard
     */
    public long[] createKomaBitboard(
            long[] space, int side, byte koma, int x, int y) {
        long[] result = createKomaBitboardEffect(space, side, koma, x, y);
        //
        result[0] &= ~this.getBitboard(side)[0];  // 味方のコマのある場所へは移動不可
        result[1] &= ~this.getBitboard(side)[1];  // 味方のコマのある場所へは移動不可
        //
        return result;
    }
    /** koma 用の bitboard の効きを返す
     * param spacexX 空白のコマの位置を渡す
     * @param side 先手、後手種類
     * @param koma コマ種類
     * @param x コマのx位置
     * @param y コマのy位置
     * @return bitboard
     */
    public long[] createKomaBitboardEffect(
            long[] space, int side, byte koma, int x, int y) {
        koma = (byte) (koma & 0x1F); // komaのsideを消す
        final long[] result = new long[2];
        if (komaBitboard[koma] != null) {  // 香車等は別で確認するのでnullが入るときがある
            // 一マス隣(または桂馬)のコマ移動チェック。複数コマ移動移動はここではチェックしない
            AllBitboard.cpBit(result, komaBitboard[koma][side]);
            AllBitboard.andBit(result, edgeBitboard[x]);  // 端をマスクする
            shiftBit(result, (y * B_MAX + x) - (4 * B_MAX + 4)); // 中央から左右ビットずらす
        }
        //
        if ((koma == TableDefine.pL)
                || (koma == TableDefine.pR)
                || (koma == Table.addNariKoma(TableDefine.pR))) {
            // 縦走りを確認する
            createJumping(result, space, x, y,  0, (side*2) - 1);
        }
        if ((koma == TableDefine.pR)
                || (koma == Table.addNariKoma(TableDefine.pR))) {
            // 縦走りを確認する
            createJumping(result, space, x, y,  0, 1 - (side*2));
            createJumping(result, space, x, y, +1, 0);
            createJumping(result, space, x, y, -1, 0);
        } else if ((koma == TableDefine.pB)
                || (koma == Table.addNariKoma(TableDefine.pB))) {
            // 斜め走りを確認する
            createJumping(result, space, x, y, -1, -1);
            createJumping(result, space, x, y, -1, +1);
            createJumping(result, space, x, y, +1, -1);
            createJumping(result, space, x, y, +1, +1);
        }
        //
        //
        return result;
    }

    /**
     * ２ます以上移動するbitboardを生成する
     * @param result 結果
     * @param space 空白のマス
     * @param x 移動元のx軸
     * @param y 移動先のx軸
     * @param dx 差分x
     * @param dy 差分y
     */
    protected void createJumping(
            long[] result, long[] space, int x, int y, int dx, int dy) {
        if (((x == 0) && (dx == -1))
                || ((x == 8) && (dx ==  1))
                || ((y == 0) && (dy == -1))
                || ((y == 8) && (dy ==  1)) ) {
            return; // 端なのでもう移動することはない
        }
        //
        // 最大長さを取る
        final int len = Math.min(
                dx == 0 ? Integer.MAX_VALUE : (dx < 0 ? x - 1 : B_MAX - x - 2),
                dy == 0 ? Integer.MAX_VALUE : (dy < 0 ? y - 1 : B_MAX - y - 2)
        );
        //
        final int pos = y * B_MAX + x;
        final int dPos = dy * B_MAX + dx;
        //
        // 調査線上のbitの有無を並べる(一部無駄が出るが絶対にforは使いたくない)
        long line = getBit(space , pos + dPos)
              | (getBit(space , pos + dPos*2) << 1)
              | (getBit(space , pos + dPos*3) << 2)
              | (getBit(space , pos + dPos*4) << 3)
              | (getBit(space , pos + dPos*5) << 4)
              | (getBit(space , pos + dPos*6) << 5)
              | (getBit(space , pos + dPos*7) << 6);
        //
        line &= ~(-1L << len); // 最大長さ以上は盤面の外なので以降を全部ゼロで埋める
        final int targetLen = Long.numberOfTrailingZeros(~line); // 1で埋まっているところまでを取得
        line |= 1L << targetLen; // 敵か空白なのでビットを立てる
        line &= ~(-1L << (targetLen + 1)); // 先端のコマ以降を全部ゼロで埋める
        //
        // ライン上のbitをbitboardに加算する
        setOrBit(result, pos + dPos, line & 1L);
        setOrBit(result, pos + dPos * 2, (line >>> 1) & 1L);
        setOrBit(result, pos + dPos * 3, (line >>> 2) & 1L);
        setOrBit(result, pos + dPos * 4, (line >>> 3) & 1L);
        setOrBit(result, pos + dPos * 5, (line >>> 4) & 1L);
        setOrBit(result, pos + dPos * 6, (line >>> 5) & 1L);
        setOrBit(result, pos + dPos * 7, (line >>> 6) & 1L);
        setOrBit(result, pos + dPos * 8, (line >>> 7) & 1L);
    }

    /* ビットボードを生成する */
    static {
        komaBitboard[TableDefine.pP] = createKomaBitboard(TableDefine.pP);
        komaBitboard[TableDefine.pL] = null; // 香車はジャンプで確認する
        komaBitboard[TableDefine.pN] = createKomaBitboard(TableDefine.pN);
        komaBitboard[TableDefine.pS] = createKomaBitboard(TableDefine.pS);
        komaBitboard[TableDefine.pG] = createKomaBitboard(TableDefine.pG);
        komaBitboard[TableDefine.pK] = createKomaBitboard(TableDefine.pK);
        komaBitboard[TableDefine.pB] = null; // 角はジャンプで確認する
        komaBitboard[TableDefine.pR] = null; // 飛はジャンプで確認する
        //
        komaBitboard[Table.addNariKoma(TableDefine.pP)] = komaBitboard[TableDefine.pG]; // 金と同じ
        komaBitboard[Table.addNariKoma(TableDefine.pL)] = komaBitboard[TableDefine.pG]; // 金と同じ
        komaBitboard[Table.addNariKoma(TableDefine.pN)] = komaBitboard[TableDefine.pG]; // 金と同じ
        komaBitboard[Table.addNariKoma(TableDefine.pS)] = komaBitboard[TableDefine.pG]; // 金と同じ
        komaBitboard[Table.addNariKoma(TableDefine.pB)] = createKomaBitboard(TableDefine.pR); // 一つ横縦を見る
        komaBitboard[Table.addNariKoma(TableDefine.pR)] = createKomaBitboard(TableDefine.pB); // 一つ斜めを見る
        //
        long[] boardX = new long[2];
        boardX[0] = -1L;
        boardX[1] = 0x1FFFF;
        //
        // エッジボードを生成する
        for (int i = 3 ; 0 <= i ; i--) { // 右シフト
            cpBit(edgeBitboard[i], boardX);
            for (int y = 0 ; y <= B_MAX ; y++) {
                clearBit(edgeBitboard[i], y * B_MAX + 3 - i);
            }
            andBit(boardX, edgeBitboard[i]);
        }
        edgeBitboard[4][0] = -1L;
        edgeBitboard[4][1] = -1L;
        boardX[0] = -1;
        boardX[1] = 0x1FFFF;
        for (int i = 5 ; i < B_MAX ; i++) { // 左シフト
            cpBit(edgeBitboard[i], boardX);
            for (int y = 0 ; y <= B_MAX ; y++) {
                clearBit(edgeBitboard[i], y * B_MAX - i + 4);
            }
            andBit(boardX, edgeBitboard[i]);
        }
    }

    /**
     * コマのビットボードの生成
     * @param koma koma
     * @return koma の bitboard
     */
    private static long[][] createKomaBitboard(byte koma) {
        long[][] target = new long[2][2];
        MoveKoma komaMove = MoveKoma.moverKomaMap.get(((int)koma));
        if (komaMove == null) {
            return null;
        }
        final int basePos = 4*B_MAX + 4;
        for (XYFlag xy : komaMove.getXYFlag()) {
            setBit(target[0], basePos + (xy.y(0) * B_MAX) + xy.x());
            setBit(target[1], basePos + (xy.y(1) * B_MAX) + xy.x());
        }
        return target;
    }

    @Override
    @NotNull
    public String toString() {
        return table.toString()
                + toString("bitboard      [0]=", this.bitboard[0])
                + toString("effectBitboard[0]=", this.effectBitboard[0])
                + toString("effectOuteKoma[0]=", this.effectOuteKoma[0])
                + toString("effectOuteBit [0]=", this.effectOuteBitboard[0])
                + toString("bitboard      [1]=", this.bitboard[1])
                + toString("effectBitboard[1]=", this.effectBitboard[1])
                + toString("effectOuteKoma[1]=", this.effectOuteKoma[1])
                + toString("effectOuteBit [1]=", this.effectOuteBitboard[1]);
    }

    /** toStringの一部を作成する
     * @param key 目標となる文字列
     * @param value 値
     * @return 結果
     */
    public String toString(String key, long[] value) {
        if (value == null) return "";
        return key
                + String.format("%17s",
                Long.toBinaryString(value[1])).replace(' ', '0')
                + String.format("%64s\n",
                Long.toBinaryString(value[0])).replace(' ', '0');
    }
}

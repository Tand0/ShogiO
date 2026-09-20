package com.github.tand0.andshogio.util;


import org.jetbrains.annotations.NotNull;

/** 局面をハフマンで圧縮したkeyを作る */
public class TableKey implements Comparable<TableKey> {
    /** 主キー値 */
    protected final long[] key = new long[4];

    /** 主キー値の取得
     * @return 主キー
     */
    public long[] getKey() {
        return key;
    }

    @Override
    public @NotNull String toString() {
        StringBuilder b = new StringBuilder();
        //
        for (long x : key) {
            b.append(String.format("%016x", x));
        }
        return  b.toString();
    }
    
    /**
     * コンストラクタ
     * @param moji 64文字のkey情報
     */
    public TableKey(String moji) {
        if (moji.length() != 64) {
            throw new UnsupportedOperationException();
        }
        // indexごとに分割してループ
        for (int i = 0 ; i < 4 ; i++) {
            // 負の数bitで変な動作になるのを恐れて２つに分けました
            String cut1 = moji.substring(i*16, i*16+8);
            long cut116 = Long.parseLong(cut1,16);
            String cut2 = moji.substring(i*16+8, i*16+16);
            long cut216 = Long.parseLong(cut2,16);
            key[i] =(cut116 << 32) | cut216;
        }
    }

    /** 先手後手どちらが持っているかのチェックビット位置
     * 後手の場合1、先手なら0になる
     */
    public static final int[] SENTE_BIT_POS = {
            0,             // 空
            0,             // 歩
            18,            // 香
            18 + 4,        // 桂
            18 +(4*2),     // 銀
            18 +(4*3) + 4, // 金
            18 +(4*3),     // 飛
            18 +(4*3) + 2, // 角
    };
    
    /** 成りかどうかのチェックビット位置
     * 手のコマか、成りの場合は1、成らずなら0になる
     */
    public static final int NARI_BIT_BASE = 18 + (4*4) + 4;

    /** 局面上の処理
     * 駒 数 Hafuman   持成 必要
     * 先  1 ------ -  - -  1 myTurn (0=先手,1=後手)
     * 王  2 ------ -  - - 13 pK
     * 空 41 0XXXXX 1  - - 41 pNull
     * 歩 18 10XXXX 2  1 1 72 pP
     * 香  4 1100XX 3  1 1 24 pL
     * 桂  4 1101XX 3  1 1 24 pN
     * 銀  4 1110XX 4  1 1 24 pS
     * 金  4 11110x 5  1 - 20 pG
     * 飛  2 111110 6  1 1 16 pR
     * 角  2 111111 6  1 1 16 pB
     * 合計 251
     * @param table 局面
     */
    public TableKey(Table table) {
        if (table == null) { // null を渡されたら初期局面にする
            table = new Table(null, 0);
        }
        final Pointer p = new Pointer();
        //
        // 王角飛の位置を決める
        int ou0 = (table.getSenteOuX() * 9) + table.getSenteOuY();
        int ou1 = (table.getGoteOuX()  * 9) + table.getGoteOuY();
        //
        setData(p, 1, table.getTeban()); // 手番
        //
        setData(p, 1, table.isMyOute() ? 1 : 0); // 王手が掛かっているか？
        setData(p, 13, (ou0 * 82L) + ou1); // 王の位置
        //
        setData(p, NARI_BIT_BASE, 0L); // 成りのコマか？bitを飛ばす
        setData(p, NARI_BIT_BASE - 4, 0L); // 成りコマbitを飛ばす(金は成らない)
        //
        // 局面上の処理をする
        // 持ちコマの処理
        for (int y = 0; (y < TableDefine.B_MAX) && !p.okSum(); y++) {
            for (int x = 0 ;  (x < TableDefine.B_MAX) && !p.okSum(); x++) {
                // 一つでも超過があったらそれはおかしいので終了
                p.checkSum();
                //
                int koma = table.getKoma(x, y);
                int komaOnly = 0xF & koma;
                if (komaOnly == TableDefine.pK) {
                    p.pKSum++;
                    continue;
                }
                //
                int komaEnemy = (koma & TableDefine.ENEMY) == 0 ? 0 : 1;
                int komaNari  = (koma & TableDefine.NARI) == 0 ? 0 : 1;
                if (komaOnly == TableDefine.pNull) {
                    setData(p,1,0);
                    continue;
                }
                // コマあり
                setData(p, 1, 1);
                switch (komaOnly) {
                case TableDefine.pP:
                    setData(p, 1, 0b0);
                    setKomaEnemyBit(komaOnly,komaEnemy,p.pPSum);
                    setKomaNariBit( komaOnly,komaNari ,p.pPSum);
                    p.pPSum++;
                    break;
                case TableDefine.pL:
                    setData(p, 3, 0b100);
                    setKomaEnemyBit(komaOnly,komaEnemy,p.pLSum);
                    setKomaNariBit( komaOnly,komaNari ,p.pLSum);
                    p.pLSum++;
                    break;
                case TableDefine.pN:
                    setData(p, 3, 0b101);
                    setKomaEnemyBit(komaOnly,komaEnemy,p.pNSum);
                    setKomaNariBit( komaOnly,komaNari ,p.pNSum);
                    p.pNSum++;
                    break;
                case TableDefine.pS:
                    setData(p, 3, 0b110);
                    setKomaEnemyBit(komaOnly,komaEnemy,p.pSSum);
                    setKomaNariBit( komaOnly,komaNari ,p.pSSum);
                    p.pSSum++;
                    break;
                case TableDefine.pG:
                    setData(p, 4, 0b1110);
                    setKomaEnemyBit(komaOnly,komaEnemy,p.pGSum);
                    //金は成れません
                    p.pGSum++;
                    break;
                case TableDefine.pR:
                    setData(p, 5, 0b11110);
                    setKomaEnemyBit(komaOnly,komaEnemy,p.pRSum);
                    setKomaNariBit( komaOnly,komaNari ,p.pRSum);
                    p.pRSum++;
                    break;
                case TableDefine.pB:
                default:
                    setData(p, 5, 0b11111);
                    setKomaEnemyBit(komaOnly,komaEnemy,p.pBSum);
                    setKomaNariBit( komaOnly,komaNari ,p.pBSum);
                    p.pBSum++;
                    break;
                }
            }
        }
        for (int myTurn = 0 ; myTurn < 2 ; myTurn++) { // 手番処理
            if (p.okSum()) {
                break; // すべてのコマが揃ったらループ不要
            }
            long sum = table.getTegoma(TableDefine.pP, myTurn);
            for (int i = 0; i < sum ; i++) {
                setData(p, 1, 0b0);
                setKomaEnemyBit(TableDefine.pP, myTurn, p.pPSum);
                p.pPSum++;
            }
            sum = table.getTegoma(TableDefine.pL, myTurn);
            for (int i = 0; i < sum ; i++) {
                setData(p, 3, 0b100);
                setKomaEnemyBit(TableDefine.pL, myTurn, p.pLSum);
                p.pLSum++;
            }
            sum = table.getTegoma(TableDefine.pN, myTurn);
            for (int i = 0; i < sum ; i++) {
                setData(p, 3, 0b101);
                setKomaEnemyBit(TableDefine.pN, myTurn, p.pNSum);
                p.pNSum++;
            }
            sum = table.getTegoma(TableDefine.pS, myTurn);
            for (int i = 0; i < sum ; i++) {
                setData(p, 3, 0b110);
                setKomaEnemyBit(TableDefine.pS, myTurn, p.pSSum);
                p.pSSum++;
            }
            sum = table.getTegoma(TableDefine.pG, myTurn);
            for (int i = 0; i < sum ; i++) {
                setData(p, 4, 0b1110);
                setKomaEnemyBit(TableDefine.pG, myTurn, p.pGSum);
                p.pGSum++;
            }
            sum = table.getTegoma(TableDefine.pR, myTurn);
            for (int i = 0; i < sum ; i++) {
                setData(p, 5, 0b11110);
                setKomaEnemyBit(TableDefine.pR, myTurn, p.pRSum);
                p.pRSum++;
            }
            sum = table.getTegoma(TableDefine.pB, myTurn);
            for (int i = 0; i < sum ; i++) {
                setData(p, 5, 0b11111);
                setKomaEnemyBit(TableDefine.pB, myTurn, p.pBSum);
                p.pBSum++;
            }
        }
        p.checkSum();
    }

    /** コンストラクタ
     * @param key キー値
     */
    public TableKey(long[] key) {
        this.key[0] = key[0];
        this.key[1] = key[1];
        this.key[2] = key[2];
        this.key[3] = key[3];
    }
    /**
     * 後手bitを立てる
     * @param komaOnly コマの種類
     * @param komaEnemy 先手0, 後手1 
     * @param sum コマの種類の中で何枚目のコマか？
     */
    public void setKomaEnemyBit(int komaOnly, int komaEnemy, int sum) {
        if (komaEnemy != 0) setBit(1 + 7 + 7 + SENTE_BIT_POS[komaOnly] + sum);

    }
    /**
     * 後手bitを立てる
     * @param komaOnly コマの種類
     * @param komaNari 先手0, 後手1 
     * @param sum コマの種類の中で何枚目のコマか？
     */
    public void setKomaNariBit(int komaOnly, int komaNari, int sum) {
        if (komaNari  != 0) setBit(1 + 7 + 7 + SENTE_BIT_POS[komaOnly] + sum + NARI_BIT_BASE);
    }
    /**
     * 後手bitが立っているか取得する
     * @param komaOnly コマの種類
     * @param sum コマの種類の中で何枚目のコマか？
     * @return 先手0,後手0x20
     */
    public byte getKomaEnemyBit(int komaOnly, int sum) {
        return (getBit(1 + 7 + 7 + SENTE_BIT_POS[komaOnly] + sum) != 0) ? TableDefine.ENEMY : 0;
    }
    /**
     * 成りbitが立っているか取得する
     * @param komaOnly コマの種類
     * @param sum コマの種類の中で何枚目のコマか？
     * @return 先手0,後手0x10
     */
    public byte getKomaNariBit(int komaOnly, int sum) {
        return (getBit(1 + 7 + 7 + SENTE_BIT_POS[komaOnly] + sum + NARI_BIT_BASE) != 0) ? TableDefine.NARI : 0;
    }


    @Override
    public int compareTo(TableKey o) {
        if (key[0] != o.key[0]) {
            return (int) (key[0] - o.key[0]);
        } else if (key[1] != o.key[1]) {
            return (int) (key[1] - o.key[1]);
        } else if (key[2] != o.key[2]) {
            return (int) (key[2] - o.key[2]);
        }
        return (int) (key[3] - o.key[3]);
    }
    @Override
    public boolean equals(Object o) {
        if (o instanceof TableKey target) {
            return (key[0] == target.key[0]) &&
                    (key[1] == target.key[1]) &&
                    (key[2] == target.key[2]) &&
                    (key[3] == target.key[3]);
        }
        return false;
    }

    /**
     * hash値を得る
     * @return hash
     */
    public long hashCodeLong() {
        return key[0] ^ key[1] ^ key[2] ^ key[3];
    }

    @Override
    public int hashCode() {
        long x = hashCodeLong();
        return (int)((0xFFFFFFFFL & x) ^ (x >>> 32));
    }

    /**
     * key値から局面を作る
     * @return 局面
     */
    public Table createTable() {
        Table table = Table.createNullTable();
        //
        Pointer p = new Pointer();
        //
        // 手番を設定する
        table.setTeban((int)getData(p,1));
        //
        // 王角飛車の設定の取得
        //
        // 値を投入する
        getData(p, 1); // 王手フラグを飛ばす
        long sum = getData(p, 13);
        long ou1 = sum % (9*9 + 1);
        long ou0 = sum / (9*9 + 1);
        table.setKoma(TableDefine.pK, (int)ou0/9, (int)ou0%9);
        table.setKoma(TableDefine.pk, (int)ou1/9, (int)ou1%9);
        //
        getData(p, NARI_BIT_BASE);     // どとらのコマか？bitを飛ばす
        getData(p, NARI_BIT_BASE - 4); // 成りコマbitを飛ばす(金は成らない)
        //
        // ハフマン処理ここから
        for (int y = 0 ;  (y < TableDefine.B_MAX) && !p.okSum() ; y++) {
            for (int x = 0 ;  x < TableDefine.B_MAX && !p.okSum() ; x++) {
                //
                // 超過チェック
                p.checkSum();
                //
                if ( ((x == ou0/9) && (y == ou0%9)) ||  ((x == ou1/9) && (y == ou1%9)) ) {
                    p.pKSum++;
                    continue; // すでにコマが置かれている
                }
                //
                // 空かどうかチェックする
                long check = getData(p,1);
                if (check == 0) {
                    continue; // 空だ
                }
                //
                // 中身のデータ
                check = getData(p,1);
                byte koma;
                long enemy;
                long nari = 0;
                if (check == 0) { // 00XXX
                    // 歩である
                    koma = TableDefine.pP;
                    enemy = getKomaEnemyBit(koma,p.pPSum);
                    nari = getKomaNariBit(koma,p.pPSum);
                    p.pPSum++;
                } else { // 1XXXX
                    check = getData(p,2);
                    if (check == 0b0) {
                        koma = TableDefine.pL;
                        enemy = getKomaEnemyBit(koma,p.pLSum);
                        nari = getKomaNariBit(koma,p.pLSum);
                        p.pLSum++;
                    } else if (check == 0b1) {
                        koma = TableDefine.pN;
                        enemy = getKomaEnemyBit(koma,p.pNSum);
                        nari = getKomaNariBit(koma,p.pNSum);
                        p.pNSum++;
                    } else if (check == 0b10) {
                        koma = TableDefine.pS;
                        enemy = getKomaEnemyBit(koma,p.pSSum);
                        nari = getKomaNariBit(koma,p.pSSum);
                        p.pSSum++;
                    } else { // 0b11
                        check = getData(p,1);
                        if (check == 0) {
                            koma = TableDefine.pG;
                            enemy = getKomaEnemyBit(koma,p.pGSum);
                            p.pGSum++;
                            // 金に成りはない
                        } else {
                            check = getData(p,1);
                            if (check == 0) {
                                koma = TableDefine.pR;
                                enemy = getKomaEnemyBit(koma,p.pRSum);
                                nari = getKomaNariBit(koma,p.pRSum);
                                p.pRSum++;
                            } else {
                                koma = TableDefine.pB;
                                enemy = getKomaEnemyBit(koma,p.pBSum);
                                nari = getKomaNariBit(koma,p.pBSum);
                                p.pBSum++;
                            }
                        }
                    }
                }
                koma = (byte) (enemy | nari | koma);
                table.setKoma(koma, x, y);
            }
        }
        //
        // 持ちコマチェック
        while (! p.okSum()) {
            //
            // 超過チェック
            p.checkSum();
            //
            byte koma;
            int enemy;
            long check = peakData(p,1);
            if (check == 0b0) { //0b0
                getData(p,1);
                koma = TableDefine.pP;
                enemy = getKomaEnemyBit(koma,p.pPSum);
                p.pPSum++;// 歩である
                p.checkSum();
            } else { // 0b1
                check = peakData(p,3);
                if (check == 0b100) {
                    getData(p,3);
                    koma = TableDefine.pL;
                    enemy = getKomaEnemyBit(koma,p.pLSum);
                    p.pLSum++;
                    p.checkSum();
                } else if (check == 0b101) {
                    getData(p,3);
                    koma = TableDefine.pN;                     
                    enemy = getKomaEnemyBit(koma,p.pNSum);
                    p.pNSum++;
                    p.checkSum();
                } else if (check == 0b110) {
                    getData(p,3);
                    koma = TableDefine.pS;                     
                    enemy = getKomaEnemyBit(koma,p.pSSum);
                    p.pSSum++;
                    p.checkSum();
                } else { //0b111
                    check = peakData(p,4);
                    if (check == 0b1110) {
                        getData(p,4);
                        koma = TableDefine.pG;
                        enemy = getKomaEnemyBit(koma,p.pGSum);
                        p.pGSum++;
                        p.checkSum();
                    } else { // 0b1111
                        check = peakData(p,5);
                        getData(p,5);
                        if (check == 0b11110) {
                            koma = TableDefine.pR;
                            enemy = getKomaEnemyBit(koma,p.pRSum);
                            p.pRSum++;
                            p.checkSum();
                        } else { // 0b11111
                            koma = TableDefine.pB;
                            enemy = getKomaEnemyBit(koma,p.pBSum);
                            p.pBSum++;
                            p.checkSum();
                        }
                    }
                }
            }
            // enemyはTableDefine.ENEMYが入っているので、先手0,後手1にする
            enemy = (enemy == 0) ? 0 : 1;
            table.setTegoma(koma, enemy,table.getTegoma(koma, enemy) + 1);
        }
        //
        return table;
    }

    /** 手番を習得する。
     * @return 手番。 0:先手、1:後手
     */
    public int getTeban() {
        // 先頭ビットに手番情報が入っているので取れる
        return (key[0] & 0x8000000000000000L) == 0 ? 0 : 1;
    }

    /** ビット情報を設定する
     * @param pos ビット位置
     * @param len 長さ
     * @param data データ
     */
    protected void setBitLen(int pos, int len, long data) {
        int end = pos + len;
        for (int p = end - 1; p >= pos; p--) {
            boolean bit = (data & 1L) != 0;
            setBit(p, bit);
            data >>>= 1;
        }
    }
    /** ビット情報を取得する
     * @param pos ビット位置
     * @param len 長さ
     * @return ビット情報
     */
    protected long getBitLen(int pos, int len) {
        long result = 0;
        for (int i = 0; i < len; i++) {
            result = (result << 1) | getBit(pos + i);
        }
        return result;
    }
    /** ビット設定に1を立てる
     * @param len ビット位置
     * @param isBit が立っている
     */
    protected void setBit(int len, boolean isBit) {
        if (len >= 256) {
            throw new UnsupportedOperationException();
        }
        //
        int idx = len >>> 6; // /64
        int shift = 63 - (len & 63);
        //
        long mask = 1L << shift;
        if (isBit) {
            key[idx] |= mask;
        } else {
            key[idx] &= ~mask;
        }
    }
    /** ビット設定に1を立てる
     * @param len ビット位置
     */
    protected void setBit(int len) {
        if (256 <= len) {
            throw new UnsupportedOperationException("setData Exception(1)!");            
        }
        int idx = len >>> 6;
        int shift = 63 - (len & 63);
        key[idx] |= (1L << shift);
    }
    /**
     * ビット情報を得る
     * @param len ビットの位置
     * @return ビット
     */
    protected long getBit(int len) {
        if (256 <= len) {
            throw new UnsupportedOperationException("setData Exception(1)!");            
        }
        int idx = len >>> 6;
        int shift = 63 - (len & 63);
        return (key[idx] >>> shift) & 1L;
    }

    /**
     * データを一つ書き込む
     * @param p ポインタ
     * @param len ビットの長さ
     * @param data データ
     */
    protected void setData(Pointer p,int len, long data) {
        setBitLen(p.pos,len,data);
        p.pos = p.pos + len;
    }
    
    /**
     * データを一つ取得する
     * @param p ポインタ
     * @param len 長さ
     * @return データ
     */
    protected long getData(Pointer p,int len) {
        long result = getBitLen(p.pos,len);
        p.pos = p.pos + len;
        return result;
    }
    
    /**
     * データを一つ取得する(ポインタをずらさない)
     * @param p ポインタ
     * @param len 長さ
     * @return データ
     */
    protected long peakData(Pointer p,int len) {
        return getBitLen(p.pos,len);
    }

}
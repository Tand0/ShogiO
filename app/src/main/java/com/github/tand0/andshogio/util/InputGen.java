package com.github.tand0.andshogio.util;


import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

/** Input 変換を行う。
 * Tableを変換して３次元のデータにする
 * 0面目は成り情報
 * 1面目～８面目は歩、香、桂、銀、金、角、飛、王
 * ９面目は先手後手情報
 * 10面目は先手後手情報
 * 11面目は先手持ちコマ情報＋手番情報
 * 12面目は後手持ちコマ情報＋手番情報
 * 13面目は先手効き情報
 * 14面目は後手効き情報
 */
public class InputGen {
    /** 成りチャネルの位置 */
    public static final int NARI_CHANNEL = 0;

    /** 先後チャネルの位置 */
    public static final int SENGO_CHANNEL = 9;

    /** 手コマチャネルの位置 */
    public static final int TEGOMA_CHANNEL = 11;

    /** 利きコマチャネルの位置 */
    public static final int DANGER_CHANNEL = 13;

    /** 全チャネル数 */
    public static final int MAX_CHANNEL = 16;

    /** データそのもの */
    private final float[] result = new float[TableDefine.B_MAX * TableDefine.B_MAX * MAX_CHANNEL];

    /** コンストラクタ */
    public InputGen() {
    }

    /**
     * テーブルキーから Layer が生成するテーブルを取得する。
     * 1. 後手番の場合、先手番になる
     * 2. 反転している可能性がある
     * @param key 元となるテーブルキー
     * @return InputGen が生成するテーブルキー
     */
    public TableKey createInputGenKey(TableKey key) {
        Table table = key.createTable();
        this.updateTable(table);
        table = this.get3DToTable();
        return new TableKey(table);
    }

    /** resultに値を設定する
     * @param x x値
     * @param y y値
     * @param channel チャネル値
     * @param value 値
     */
    public void set(int x, int y, int channel, float value) {
        result[((x * TableDefine.B_MAX) + y) * MAX_CHANNEL + channel] = value;
    }

    /** resultから値を取得する
     * @param x x値
     * @param y y値
     * @param channel チャネル値
     * @return 値
     */
    public float get(int x, int y, int channel) {
        return result[((x * TableDefine.B_MAX) + y) * MAX_CHANNEL + channel];
    }
    /**
     * インプットを取得する
     * @return インプット
     */
    public synchronized float[] getInput() {
        return result;
    }

    /**
     * テーブルをレイヤ―に変換する
     * @param paramTable テーブル
     */
    public synchronized void updateTable(Table paramTable) {
        // 全てをゼロクリアする
        Arrays.fill(result, 0f);
        //
        // 元のテーブルが後手番の場合、上下反転して先手番に替える
        final Table fv =(0 == paramTable.getTeban()) ?
                paramTable : paramTable.flippingVertical();
        //
        // 左右反転しても評価値は同じだが、より小さい方を選ぶ
        final Table fh = fv.flippingHorizontal();
        final Table table = (fv.compareTo(fh) <= 0) ? fv : fh;
        //
        // コマ数分のループ
        for (int y = 0; y < TableDefine.B_MAX; y++) {
            for (int x = 0; x < TableDefine.B_MAX ; x++) {
                //
                // 最終面に all 1 を入れることで盤面境界が分かるようにする
                set(x, y , MAX_CHANNEL - 1, 1f);
                //
                // 局面上のコマの取得
                byte koma = table.getKoma(x, y);
                //
                // 空白の場合、無視する
                if ((koma & 0xF) == TableDefine.pNull) {
                    continue;
                }
                // 成りコマがある場合は0レイヤに1に1を入れる
                if ((koma & TableDefine.NARI) != 0) {
                    set(x, y, NARI_CHANNEL, 1.0f);
                }
                // コマがおかれているところは1で埋める
                for (int channel = 0 ; channel < TEGOMA_CHANNEL ; channel++) {
                    set(x, y, channel, -1.0f);
                }
                // 先手か後手かを配置する 0:先手、1:後手
                int teban = (koma & TableDefine.ENEMY) == 0 ? 0 : 1;
                set(x, y, SENGO_CHANNEL + teban,  1.0f);
                // コマを配置する
                set(x, y, koma & 0xF, 1.0f);
                //
                // ここから利きの指定
                MoveKoma moverKoma = MoveKoma.moverKomaMap.get(koma & 0x1F);
                if (moverKoma != null) {
                    for (XYFlag xYFlag : moverKoma.getXYFlag()) {
                        final int dx = xYFlag.x();
                        final int dy = xYFlag.y(teban);
                        int xx = x + dx;
                        int yy = y + dy;
                        for (; (0 <= xx) && (0 <= yy) && (xx < TableDefine.B_MAX) && (yy < TableDefine.B_MAX); xx += dx, yy += dy) {
                            // 利きの場所に DANGER_LAYER_VALUE を加算する
                            float k = get(xx, yy,DANGER_CHANNEL + teban);
                            set(xx, yy,DANGER_CHANNEL + teban, k + 1.0f);
                            //
                            if ((!xYFlag.flag())
                                    || (TableDefine.pNull != table.getKoma(xx, yy))) {
                                break;//8方向チェックか、移動先にコマがいたらそこまで
                            }
                        }
                    }
                }
            }
        }
        for (int myTurn = 0 ; myTurn <= 1 ; myTurn++) {
            //
            // 持ちコマ
            // 歩の場合９～18枚目を追加
            int sum = table.getTegoma(TableDefine.pP, myTurn);
            // 歩1枚目～18枚目
            for (int y = 1; (y <= 2) && (0 < sum); y++) {
                for (int x = 0; (x < TableDefine.B_MAX) && (0 < sum); x++) {
                    set(x, y, TEGOMA_CHANNEL + myTurn, 1.0f);
                    sum--;
                }
            }
            // 香車～飛車までの持ちコマ
            for (byte koma = TableDefine.pL ; koma <= TableDefine.pR ; koma++) {
                sum = table.getTegoma(koma,myTurn);
                for (int x = 0; (x < TableDefine.B_MAX) && (0 < sum); x++) {
                    set(x, 3 + koma - TableDefine.pL,
                            TEGOMA_CHANNEL + myTurn, 1.0f);
                    sum--;
                }
            }
        }
    }
    /** レイヤ―をテーブルにもどす
     * @return テーブル
     */
    public Table get3DToTable() {
        //
        Table table = new Table(null, 0);
        this.changeTable1(table); // 盤面
        this.changeTable2(table); // 持ち駒
        //
        return table;
    }

    /** テーブル取得／盤面の処理
     * @param table テーブル
     */
    private void changeTable1(Table table) {
        table.endForCSAProtocol();
        //
        // 局面上の確認処理
        for (int x = 0; x < TableDefine.B_MAX ; x++) {
            for (int y = 0; y < TableDefine.B_MAX ; y++) {
                //
                // 成りレイヤ―にフラグがってたら 成りビットを立てる
                byte nari = get(x, y, NARI_CHANNEL) == 1.0f ?
                        TableDefine.NARI : 0;
                //
                // 後手レイヤ―にフラグがってたら 後手ビットを立てる
                byte sengo = get(x, y, SENGO_CHANNEL + 1) == 1.0f ?
                        TableDefine.ENEMY : 0;
                //
                // 歩から王までのループ
                table.setKoma(TableDefine.pNull,x,y); // 空入力
                for (byte koma = TableDefine.pP ; koma <= TableDefine.pK ; koma++) {
                    if (get(x, y, koma) != 1) {
                        continue; // 空入力
                    }
                    table.setKoma((byte) (sengo | nari | koma), x, y);
                }
            }
        }
        // 手番の設定 (必ず先手)
        table.setTeban(0);
    }

    /**
     * テーブル取得／持ち駒の処理
     * @param table テーブル
     */
    private void changeTable2(Table table) {
        //
        for (int myTurn = 0 ; myTurn <= 1 ; myTurn++) {
            // 歩1枚目～18枚目
            int sum = 0;
            boolean flag = true;
            for (int y = 1; (y <= 2) && flag; y++) {
                for (int x = 0; x < TableDefine.B_MAX; x++) {
                    if (0 < get(x, y,TEGOMA_CHANNEL + myTurn)) {
                        sum++;
                    } else {
                        flag = false;
                        break;
                    }
                }
            }
            table.setTegoma(TableDefine.pP, myTurn, sum);
            //
            // 香車～飛車までの持ちコマ
            for (byte koma = TableDefine.pL; koma <= TableDefine.pR; koma++) {
                sum = 0;
                for (int x = 0; x < 4; x++) { // コマの数は最大で4枚
                    if (0 < get(x, 3 + koma - TableDefine.pL,
                            TEGOMA_CHANNEL + myTurn)) {
                        sum++;
                    } else {
                        break;
                    }
                }
                table.setTegoma(koma, myTurn, sum);
            }
        }
    }

    @NotNull
    @Override
    public String toString() {
        StringBuilder buff = new StringBuilder();
        for (int channel = 0; channel < InputGen.MAX_CHANNEL ; channel++) {
            buff.append("Channel=").append(channel).append("\n");
            for (int y = 0 ; y < TableDefine.B_MAX ; y++) {
                for (int x = 0 ; x < TableDefine.B_MAX ; x++) {
                    buff.append(String.format("%2d", (int)this.get(x, y, channel)));
                }
                buff.append("\n");
            }
        }
        return buff.toString();
    }
}

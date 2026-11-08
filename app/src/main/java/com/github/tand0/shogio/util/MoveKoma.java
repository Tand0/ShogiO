package com.github.tand0.shogio.util;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;


/** コマの移動に使うクラス（コマの移動先を得る） */
public class MoveKoma {
    /** indexTarget */
    private final int[][] indexMoveKoma = new int[3][4];

    /** コマ */
    private final int koma;

    /** 移動先情報のリスト */
    private final ArrayList<XYFlag> xYFlag = new ArrayList<>();

    /**
     * 逆引きでMoveKomaのインデックスを取得する
     * @param index インデックス (0 以上)
     * @param x x値(-1 ～ +0)
     * @param y y値(-2 ～ +1)
     */
    private void setIndex(int index, int x, int y) {
        indexMoveKoma[x + 1][y + 2] = index;
    }

    /**
     * 逆引きでMoveKomaのインデックスを取得する
     * @param x x値(-1 ～ +0)
     * @param y y値(-2 ～ +1)
     * @return インデックス (-1の場合は対応するものがない)
     */
    public int getIndex(int x, int y) {
        return indexMoveKoma[x + 1][y + 2];
    }

    /** コンストラクタ
     *
     * @param koma コマ
     */
    public MoveKoma(int koma) {
        this.koma = koma;
    }

    /**
     * 移動先情報リストの取得
     * @return 移動先情報リスト
     */
    public ArrayList<XYFlag> getXYFlag() {
        return this.xYFlag;
    }

    /**
     * コマの種類からどこに移動するかのリストを得る。
     */
    public static final Map<Integer,MoveKoma> moverKomaMap =
            KomaMove.movers //movers のリスト
                    .stream() // ストリームに変換
                    .flatMap(mover->Arrays.stream(mover.koma).boxed()) // コマのリスト
                    .distinct() // コマのリストから重複を排除
                    .map(MoveKoma::new) // コマの枠からコマ用クラスに変換
                    .peek(moveKoma-> { //中間処理を実行
                        //
                        // コマに移動先情報を追加
                        KomaMove.movers.forEach(mover-> {
                            for (Integer targetKoma : mover.koma) {
                                if (targetKoma != moveKoma.koma) {
                                    continue; // コマ標のコマではない
                                }
                                moveKoma.xYFlag.add(mover.xYFlag);
                            }
                        });
                        for (int x = 0 ; x < moveKoma.indexMoveKoma.length ; x++) {
                            for (int y = 0; y < moveKoma.indexMoveKoma[x].length ; y++) {
                                // index 異常値をいれておく
                                moveKoma.indexMoveKoma[x][y] = moveKoma.xYFlag.size();
                            }
                        }
                        for (int index = 0; index < moveKoma.xYFlag.size(); index++) {
                            // 正常な個所は上書きする
                            XYFlag xYFlag = moveKoma.getXYFlag().get(index);
                            moveKoma.setIndex(index, xYFlag.x(), xYFlag.y(0));
                        }
                    })
                    .collect(Collectors.toMap(
                            moveKoma->moveKoma.koma, moveKoma-> moveKoma));

}

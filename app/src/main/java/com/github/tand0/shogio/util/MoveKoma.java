package com.github.tand0.shogio.util;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;


/** コマの移動に使うクラス（コマの移動先を得る） */
public class MoveKoma {
    /** コマ */
    private final int koma;

    /** 移動先情報のリスト */
    private final ArrayList<XYFlag> xYFlag = new ArrayList<>();

    /** コンストラクタ
     *
     * @param koma コマ
     */
    public MoveKoma(int koma) {
        this.koma = koma;
    }
    /**
     * 移動先情報の追加
     * @param xYFlag 移動先情報
     */
    private void add(XYFlag xYFlag) {
        this.xYFlag.add(xYFlag);
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
                    .peek(moveKoma->{ //中間処理を実行
                        //
                        // コマに移動先情報を追加
                        KomaMove.movers.forEach(mover-> {
                            for (Integer targetKoma : mover.koma) {
                                if (targetKoma != moveKoma.koma) {
                                    continue; // コマ標のコマではない
                                }
                                moveKoma.add(mover.xYFlag);
                            }
                        });
                    })
                    .collect(Collectors.toMap(moveKoma->moveKoma.koma, moveKoma-> moveKoma));

}

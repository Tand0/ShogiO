package com.github.tand0.andshogio.engin;

import com.github.tand0.andshogio.util.TeTable;

import java.util.List;

/**
 * 将棋エンジンの戻り値
 *
 * @param te          手
 *                    TableDefine.LOS の時は詰みだが、エンジン上手がないときに選択される。
 *                    例えば定跡エンジンで定跡がなくなった場合は TableDefine.LOS を返す
 * @param turn        ターン数
 * @param cls         自身のクラス
 * @param displayList デバッグリスト。
 *                    null: 将棋サーバとの対戦処理の場合
 *                    non-null: GUIからの指示で要求された場合
 * @param eval        先手勝率。0.5f が中立。1.0f が先手勝率 100%、0.0f が先手負け率 100%
 */
public record EngineResponse(
        int turn, int te, Class<?> cls,
        List<TeTable> displayList, float eval) {

}

package com.github.tand0.shogio.engin;

import com.github.tand0.shogio.util.TeTable;

import java.util.List;

/**
 * 将棋エンジンへのリクエスト値
 *
 * @param forResult   処理が終わった時の打ち返し先
 * @param turn        手数
 * @param teTableList 手順
 * @param display     表示モードの場合 true
 */
public record EngineRequest(SendListener forResult,
                            int turn,
                            List<TeTable> teTableList,
                            boolean display) {
}

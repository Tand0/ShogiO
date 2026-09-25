package com.github.tand0.shogio.engin;

import com.github.tand0.shogio.util.TeTable;

/**
 * マニュアル設定
 * @param teTable テーブル名
 * @param id      エンジンの名称
 */
public record MessageManualRequest(TeTable teTable, String id) {
}

package com.github.tand0.shogio.engin;

import com.github.tand0.shogio.util.TeTable;

import java.util.List;

/**
 * TeTable応答
 * @param teTableList 手テーブル
 */
public record MessageDisplayResponse(List<TeTable> teTableList) {

}

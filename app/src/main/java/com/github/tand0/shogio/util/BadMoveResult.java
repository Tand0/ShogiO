package com.github.tand0.shogio.util;

import java.util.List;

/** リザルト用
 * @param teTableList teTableList
 * @param win win 値
 * @param los los 値
 */
public record BadMoveResult(List<TeTable> teTableList, long win, long los) {}
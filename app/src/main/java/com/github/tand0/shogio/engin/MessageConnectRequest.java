package com.github.tand0.shogio.engin;

/**
 * 将棋サーバへの接続要求
 *
 * @param ip         接続先 IP 情報
 * @param port       接続先 IP port 情報
 * @param user       ユーザ名
 * @param pass       ユーザ識別子
 * @param manualMode true のときマニュアルモードが有効
 * @param extensionMode trueのときCSA拡張モード、 falseのときCSAモード
 * @param gameName   ゲーム名
 * @param senGo      ゲーム名で指定する先後情報
 * @param loopingMode true のとき連続実行
 * @param maxTableFactory max table factory
 * @param maxNoChange max no change
 * @param maxLevel max level
 * @param minSon min son
 */
public record MessageConnectRequest(String ip, int port,
                                    String user, String pass,
                                    boolean manualMode, boolean extensionMode,
                                    String gameName, String senGo,
                                    boolean loopingMode,
                                    int maxTableFactory,
                                    int maxNoChange,
                                    int maxLevel,
                                    int minSon) {
}

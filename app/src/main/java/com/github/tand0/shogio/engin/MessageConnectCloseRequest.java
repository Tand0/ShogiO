package com.github.tand0.shogio.engin;

/** 将棋サーバへの切断要求 */
public class MessageConnectCloseRequest extends MessageRequest {
    @Override
    public void run(StatusManager manager) {
        // 終了を呼ぶ
        manager.stopSocket();
        // 空を入れる
        manager.connectData(null);
        //
    }
}

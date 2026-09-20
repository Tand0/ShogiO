package com.github.tand0.andshogio.engin;

/**
 * スレッドに処理を行わせるためのリスナー
 */
public interface SendListener {
    /** スレッドへの要求を送る。
     * @param x 要求された処理内容
     * @throws InterruptedException 処理できないときの例外
     */
    void send(Object x) throws InterruptedException;
}

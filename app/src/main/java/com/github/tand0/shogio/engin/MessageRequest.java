package com.github.tand0.shogio.engin;


/**
 * 汎用のリクエストの要求
 */
public abstract class MessageRequest {
    /** 要求する処理の中身
     * @param manager 状態管理
     * @throws InterruptedException Interrupted Exception
     */
    public abstract void run(StatusManager manager) throws InterruptedException;
}

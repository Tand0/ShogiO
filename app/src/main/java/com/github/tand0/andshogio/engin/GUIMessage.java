package com.github.tand0.andshogio.engin;

/**
 * GUI送信用メッセージ
 * @param message
 */
public record GUIMessage(String message) {
    @Override
    public boolean equals(Object object) {
        if (object instanceof GUIMessage) {
            return this.message().equals(((GUIMessage)object).message());
        }
        return false;
    }
}

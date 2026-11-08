package com.github.tand0.shogio.linux;

import com.github.tand0.shogio.engin.EngineRequest;
import com.github.tand0.shogio.engin.EngineStop;
import com.github.tand0.shogio.engin.MessageConnectRequest;
import com.github.tand0.shogio.engin.MessageDisplayResponse;
import com.github.tand0.shogio.engin.MessageRequest;
import com.github.tand0.shogio.engin.SendListener;
import com.github.tand0.shogio.engin.StatusManager;
import com.github.tand0.shogio.util.MainDatabase;
import com.github.tand0.shogio.util.TeTable;
import com.github.tand0.shogio.util.TensoInterface;

import java.util.LinkedList;

/**
 * 全てをいっきにやる
 */
public class Main {
    /** ステータスマネージャ */
    private final StatusManager statusManager = new StatusManager();

    /** SQL に接続する用 */
    private final MainDatabase helper = new MainDatabaseLinux();

    /** 評価値用 */
    private final TensoInterface tFLiteManager = new TFLiteManagerLinux();

    /** メイン部分
     * @param argc コマンド引数
     */
    public static void main(String[] argc) {
        System.out.println("hello world");
        Main main = new Main();
        main.run();
    }

    /** 実働部分 */
    public void run() {
        //
        statusManager.forGUI(forGUI);
        statusManager.setDatabase(helper);
        statusManager.setTFLiteManager(tFLiteManager);
        //
        // スレッドとして起動する
        Thread t1 = new Thread(statusManager, "engine_StatusManager");
        t1.start();
        //
        String ip = "localhost";
        int port = 4081;
        String user = "tand0shogio";
        String pass = "tand0shogio";
        boolean manualMode = false;
        boolean extensionMode = false;
        String gameName = "default-1500-0";
        String senGo = "*";
        boolean loopingMode = false;
        int maxTableFactory = 100000;
        int maxNoChange = 100000;
        int maxLevel = 5;
        int minSon = 10;
        //起動する
        new MessageConnectRequest(
                ip,
                port,
                user,
                pass,
                manualMode,
                extensionMode,
                gameName,
                senGo,
                loopingMode,
                maxTableFactory,
                maxNoChange,
                maxLevel,
                minSon);
    }

    /** 外部から処理を要求されたとき用 */
    private final SendListener forGUI = x -> {
        // ここからGUIスレッドに入る
        switch (x) {
            case String s -> System.out.println(s);
            case Exception exception -> {
                System.out.println("Exception");
                System.out.println(exception.getMessage());
            }
            case EngineRequest engineRequest -> {
                System.out.println("EngineRequest");
                System.out.println(engineRequest);
            }
            case EngineStop ignored -> System.out.println("EngineStop");
            case TeTable table -> {
                System.out.println("TeTable");
                System.out.println(table);
            }
            case LinkedList<?> list -> {
                System.out.println("LinkedList<?>");
                System.out.println(list.size());
            }
            case MessageRequest messageRequest -> {
                System.out.println("MessageRequest");
                System.out.println(messageRequest);
            }
            case MessageDisplayResponse res -> {
                System.out.println("MessageDisplayResponse");
                System.out.println(res);
            }
            default -> System.out.println("Got a " + x.getClass().getName());
        }
    };
}

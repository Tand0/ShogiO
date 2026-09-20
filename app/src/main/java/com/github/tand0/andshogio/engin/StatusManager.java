package com.github.tand0.andshogio.engin;

import android.util.ArraySet;

import androidx.annotation.Nullable;

import com.github.tand0.andshogio.util.EvalTeTable;
import com.github.tand0.andshogio.util.TensoInterface;
import com.github.tand0.andshogio.util.MainDatabase;
import com.github.tand0.andshogio.util.Table;
import com.github.tand0.andshogio.util.TableDefine;
import com.github.tand0.andshogio.util.TeTable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/** 将棋サーバと接続するための状態管理用クラス
 */
public class StatusManager implements SendListener, Runnable {
    /** データベース */
    private MainDatabase database;

    /** 評価値用 */
    private TensoInterface lInterface;

    /** 待ちキュー */
    private final BlockingQueue<Object> queue = new ArrayBlockingQueue<>(100);

    /** 将棋サーバ用のnio処理 */
    private SendListener forGUI = null;

    /** 将棋サーバ用のソケット */
    private Socket socket  = null;

    /** 将棋サーバとの状態 */
    private ST serverState = ST.START;

    /** 指し手の情報 */
    private final List<TeTable> sumTeTableList = new LinkedList<>();

    /** 最高にダメな手のリスト */
    private final Set<TeTable> topObBadTeTableSet = new ArraySet<>();

    /** エンジンの情報 */
    private final List<EngineStatus> engineStatusList = new LinkedList<>();

    /** エンジンの情報 */
    private final List<Engine> engineEtcList = new LinkedList<>();

    /** 棋譜情報を eval化するためのエンジン */
    private EngineRecordEval engineRecordEval;

    /** 探索用のエンジン */
    private EngineExplorer explorer;

    /** エンジンのターン */
    private int engineTurn = -1;

    /** 自分のターン 先手:0 後手: 1 */
    private int yourTurn = 0;

    /** ターゲットとなる時間 */
    private long targetTime;

    /** 初期時間 */
    private long totalTime = 0;

    /** 秒読み時間 */
    private long byoyomiTime = 0;

    /** 遅延時間 */
    private long delayTime = 0;

    /** 加算時間 */
    private long incrementTime = 0;

    /** 経過時間 */
    private long sumTime = 0;

    /** 接続用データ */
    private MessageConnectRequest connectData;

    /** エンジンをまとめて合議するための時間。
     * 単位は msec
     */
    private final static long AFTER_TRANSACTION_TIME = 100L;

    /** 探索用のエンジンの取得
     * @return 探索用のエンジン
     */
    public EngineExplorer explorer() {
        return this.explorer;
    }

    /** 最大沈黙時間
     *
     * @return 最大沈黙時間(秒)
     */
    public long getSleepTime() {
        long time;
        long max = 10;
        // Total_Time 記述と Increment 加算時間の両方の記述が省略された場合、
        // 対局全体を通じた時間制限は存在しない。→最大 max 秒にする。
        if ((totalTime == 0) && (incrementTime == 0)) {
            time = max;
        } else {
            time = totalTime // 経過時間
                    + ((sumTeTableList.size() - 1) * incrementTime) // 加算時間
                    + byoyomiTime // 秒読み時間
                    + delayTime // 遅延時間
                    - this.sumTime; // 経過時間
        }
        // Total_Timeから経過時間が max 秒より大きい場合、max 秒とする
        time = Math.min(max, time);
        // 時間が2秒を切った場合、２秒とする。
        time = Math.max(time, 2);
        //
        return time;
    }

    /** コンストラクタ
     */
    public StatusManager() {
    }

    /** 接続用データの設置
     * @param connectData 接続用データ
     */
    public void connectData(MessageConnectRequest connectData) {
        this.connectData = connectData;
    }

    /** 最高にダメな手のリストの取得
     * @return 最高にダメな手のリスト
     */
    public Set<TeTable> topObBadTeTableSet() {
        return topObBadTeTableSet;
    }

    /** GUI にアクセスするためのインタフェースの取得
     * @param forGUI GUI にアクセスするためのインタフェース
     */
    public void forGUI(SendListener forGUI) {
        this.forGUI = forGUI;
    }
    /** GUI にアクセスするためのインタフェースの取得
     * @return GUI にアクセスするためのインタフェース
     */
    public SendListener forGUI() {
        return this.forGUI;
    }
    /**
     * データベースのアクセスするためのインタフェースの取得
     * @param database データベースのアクセスするためのインタフェース
     */
    public void setDatabase(MainDatabase database) {
        this.database = database;
    }

    /**
     * TensorFlow へのアクセスするためのインタフェースの取得
     * @param lInterface TensorFlow へのアクセスするためのインタフェース
     */
    public void setTFLiteManager(TensoInterface lInterface) {
        this.lInterface = lInterface;
    }

    /** 受信処理 */
    public void run() {
        //
        // エンジンをあらかじめ起動する(上にある方が先に処理される)
        Engine engine = new EngineMate();
        this.engineStatusList.addLast(new EngineStatus(engine,true));
        new Thread(engine, engine.getEngineId()).start();
        //
        engine = new EngineManual();
        this.engineStatusList.addLast(new EngineStatus(engine,true));
        new Thread(engine, engine.getEngineId()).start();
        //
        EngineDatabase engineDatabase = new EngineDatabase(database, topObBadTeTableSet);
        this.engineStatusList.addLast(new EngineStatus(engineDatabase,true));
        new Thread(engineDatabase, engineDatabase.getEngineId()).start();
        //
        this.explorer = new EngineExplorer(lInterface, topObBadTeTableSet);
        this.engineStatusList.addLast(new EngineStatus(explorer,false));
        new Thread(this.explorer, this.explorer.getEngineId()).start();
        //
        engine = new EngineRandom();
        this.engineStatusList.addLast(new EngineStatus(new EngineRandom(),false));
        new Thread(engine, engine.getEngineId()).start();
        //
        // 以降はetcListに追加されスレッド化されない
        EngineEval engineEval = new EngineEval(lInterface, topObBadTeTableSet);
        this.engineEtcList.addLast(engineEval);
        this.engineRecordEval = new EngineRecordEval(lInterface);
        this.engineEtcList.addLast(engineRecordEval);
        this.engineEtcList.addLast(new EngineBadMove(database));
        this.engineEtcList.addLast(new EngineDbMove(database));
        //
        while (true) {
            //
            try {
                try {
                    Object command;
                    if (targetTime == 0) { // 無限待ち状態
                        // キューが空ならデータが入るまで待機する
                        command = this.queue.take();
                    } else {
                        long sleep = targetTime - System.currentTimeMillis();
                        if (sleep <= 0) {
                            targetTime = 0;
                            // timeout発生
                            forGUI.send("queue timeout");
                            invokeEngine(null, true);
                            continue;
                        } else {
                            long pollWait = 2000;
                            command = this.queue.poll(pollWait, TimeUnit.MILLISECONDS);
                            if (command == null) {
                                forGUI.send("t=" + (sleep / 1000) + " s=" + sumTime);
                            }
                        }
                    }
                    switch (command) {
                        case null -> {
                        }
                        case Exception ignored ->
                            // 将棋サーバからの例外情報をGUIに渡す
                            forGUI.send(command);
                        case GUIMessage s -> forGUI.send(s.message()); // メッセージを送る
                        case String s -> {
                            // 将棋サーバからのメッセージ
                            try {
                                invokeMessageData(s);
                            } catch (IOException e) {
                                // ここは普通にエラーでるので catch を入れる
                                forGUI.send(e);
                                //
                                // 終了を呼ぶ
                                stopSocket();
                            }
                        }
                        case MessageRequest request -> request.run(this);
                        case MessageConnectRequest messageConnectRequest ->
                            // 将棋サーバへの接続要求
                            invokeMessageConnect(messageConnectRequest);
                        case EngineResponse res -> {
                            // エンジンからの応答処理
                            if (res.displayList() == null) {
                                invokeEngine(res, false);
                            } else {
                                // 表示モード
                                invokeDisplay(res);
                            }
                        }
                        case MessageManualRequest messageManualRequest ->
                                requestManualEngine(messageManualRequest);
                        default ->
                            // 分からん
                            forGUI.send("Unknown! Got a " + command.getClass().getName());
                    }
                } catch (IOException e) {
                    // ここはネットワークが切断で普通にエラーでるので catch を入れる
                    forGUI.send(e);
                    //
                    // 終了を呼ぶが、break はしない
                    stopSocket();
                }
            } catch (InterruptedException e) {
                // 割り込みが来た＝終了する
                try {
                    forGUI.send(e);
                } catch (InterruptedException ex) {
                    //EMPTY
                }
                break;
            }
        }
        // 無限待ち whileが終了したら
        stopSocket();  // 終了宣言
    }

    /**
     * このクラスの入口
     * @param x 送信内容
     */
    @Override
    public void send(Object x) throws InterruptedException {
        this.queue.put(x);
    }

    /** GUIからのサーバ接続要求
     * @param data 将棋情報
     */
    protected void invokeMessageConnect(MessageConnectRequest data) {
        explorer.maxTableFactory(data.maxTableFactory());
        explorer.maxNoChange(data.maxNoChange());
        explorer.maxLevel(data.maxLevel());
        explorer.minSon(data.minSon());
        if (this.socket != null) {
            try {
                this.forGUI.send("already connected!");
            } catch(InterruptedException e) {
                // EMPTY
            }
            return; // 既に起動中なので無視する
        }
        this.connectData = data; // データ配置
        //
        // サーバーからのメッセージを受信するスレッドを起動
        Thread receiveThread = new Thread(() -> {
            try {
                this.forGUI.send(
                        "connect IP=" + data.ip() + " " + "port=" + data.port());
                this.socket = new Socket();
                SocketAddress socketAddress = new InetSocketAddress(
                        data.ip(), data.port());
                int timeoutMs = 10000; // 接続タイマー
                this.socket.connect(socketAddress, timeoutMs);
                String login = "LOGIN " + data.user() + " " + connectData.pass();
                if (data.extensionMode()) {
                    // CSA拡張モードのとき x1 を追加する
                    login = login + " x1";
                }
                this.sendSocket(login + "\r\n");
                final Socket socket = StatusManager.this.socket;
                if (socket == null) {
                    forGUI.send("Read socket is null");
                    return;
                }
                try (InputStream is = socket.getInputStream();
                     InputStreamReader isr = new InputStreamReader(is);
                     BufferedReader in = new BufferedReader(isr)) {
                    while (true) {
                        // ここで切断されるまで無限ループ
                        String serverMsg = in.readLine();
                        if (serverMsg == null) {
                            break;
                        }
                        StatusManager.this.send(serverMsg);
                    }
                }
            } catch (IOException | InterruptedException e) {
                try {
                    forGUI.send("Read end:" + e.getMessage());
                } catch (InterruptedException ex) {
                    // EMPTY
                }
            } finally {
                try {
                    //
                    stopSocket(); // 終了する
                    //
                    // 終わったメッセージ
                    StatusManager.this.forGUI.send("close invokeMessageConnect()");
                    //
                    // 待ったの生成
                    this.createTopOpBad();
                    //
                    // クラスフィールドのコネクトを呼ぶ。
                    // 停止が呼ばれた場合、null になっているのを確認できる
                    final MessageConnectRequest nowData = this.connectData;
                    if ((nowData != null) && nowData.loopingMode()) {
                        StatusManager.this.forGUI.send("reconnect invokeMessageConnect()");
                        StatusManager.this.send(nowData); // 繰り返しモードなら実行
                    }
                } catch (InterruptedException ex) {
                    // EMPTY
                }
            }
            //
        }, "engine_connect");
        receiveThread.start();
    }
    /** 将棋サーバを停止する */
    public void stopSocket() {
        this.serverState = ST.START;
        if (socket != null) {
            try {
                socket.close();
            } catch (IOException e) {
                // EMPTY
            }
            socket = null;
        }
    }

    /** 将棋サーバにメッセージを送信する
     *
     * @param message 送信するメッセージ
     * @throws InterruptedException Interrupted Exception
     * @throws IOException IO Exception
     */
    public void sendSocket(String message) throws InterruptedException, IOException {
        if (socket == null) {
            forGUI.send("socket == null");
            return;
        }
        forGUI.send(">" + message.trim());
        socket.getOutputStream().write(message.getBytes());
    }

    /**
     * データ処理をするもの
     * @param data 将棋サーバからの文字列
     */
    protected void invokeMessageData(String data) throws InterruptedException, IOException {
        final String TOTAL_TIME = "Total_Time:";//300
        final String BYOYOMI = "Byoyomi:";//10
        final String DELAY = "Delay:";//3
        final String INCREMENT = "Increment:";//5

        // ログに展開する
        forGUI.send("<" + data);
        //
        if (serverState == ST.START) {
            if (0 == data.indexOf("LOGIN:")) {
                // ログイン
                String[] split = data.split(" ");
                if ((split.length != 2) || (!split[1].equals("OK"))) {
                    // ログイン失敗を検知した
                    stopSocket();
                }
                if (this.connectData.extensionMode()) {
                    // CSA拡張モードのとき %%GAME をコールする
                    sendSocket("%%GAME " + this.connectData.gameName() + " " + this.connectData.senGo() + "\r\n");
                }
                //
                this.sumTime = 0L; // 初期時間を設定する
                //
            } else if (0 == data.indexOf("Your_Turn:+")) {
                yourTurn = 0; // 先手
            } else if (0 == data.indexOf("Your_Turn:-")) {
                yourTurn = 1; // 後手
            } else if (0 == data.indexOf(TOTAL_TIME)) {
                totalTime = Integer.parseInt(data.substring(TOTAL_TIME.length()));
            } else if (0 == data.indexOf(BYOYOMI)) {
                byoyomiTime = Integer.parseInt(data.substring(BYOYOMI.length()));
            } else if (0 == data.indexOf(DELAY)) {
                delayTime = Integer.parseInt(data.substring(DELAY.length()));
            } else if (0 == data.indexOf(INCREMENT)) {
                incrementTime = Integer.parseInt(data.substring(INCREMENT.length()));
            } else if (data.equals("END Game_Summary")) {
                //
                sumTeTableList.clear(); // クリアする
                sumTeTableList.addLast(new TeTable(new Table(null, 0), 0 ));
                forGUI.send(sumTeTableList);
                //
                // 準備OKを渡す
                sendSocket("AGREE\r\n");
                serverState = ST.FIGHT;
            }
        } else if (serverState == ST.FIGHT) {
            if ((0 == data.indexOf("%CHUDAN"))
                    || (0 == data.indexOf("%TIME_UP"))
                    || (0 == data.indexOf("#WIN"))
                    || (0 == data.indexOf("#HIKIWAKE"))
                    || (0 == data.indexOf("#LOSE"))
                    || (0 == data.indexOf("#DRAW"))) {
                // 終わった
                sendSocket("LOGOUT\r\n");
                stopSocket();
            } else if (0 == data.indexOf("LOGOUT:completed")) {
                // 終了する LOGOUT は出さなくてよい
                forGUI.send("Game over");
                stopSocket();
            } else if (0 == data.indexOf("START")) {
                // 初手。自分が先手であれば指す
                if (yourTurn == 0) {
                    String teString = "+2726FU";
                    sendSocket(teString + "\r\n");
                }
            } else if (0 == data.indexOf("+")) {
                // 先手が指した(dataは指し手)。後手が次に指す
                if (yourTurn == 0) {
                    invokeYourTurn(data);
                } else {
                    invokeMyTurn(data);
                }
            } else if (0 == data.indexOf("-")) {
                // 後手が指した(dataは指し手)。先手が次に指す
                if (yourTurn == 0) {
                    invokeMyTurn(data);
                } else {
                    invokeYourTurn(data);
                }
            }
        }
    }

    /** 対戦相手の手を処理する
     * @param teString CSA サーバプロトコルの指し手情報
     * @throws InterruptedException Interrupted Exception
     */
    public void invokeYourTurn(String teString) throws InterruptedException {
        TeTable lastTable = sumTeTableList.getLast();
        int te = TableDefine.changeTeStringToInt(teString);
        Table newTable = new Table(lastTable.table, te);
        this.sumTime += TableDefine.changeTeStringToTime(teString);
        sumTeTableList.add(new TeTable(newTable,te));
        forGUI.send(sumTeTableList);
        //
        // 待ち時間を決める
        targetTime = 0; // 無限待ち
    }

    /** 自分の手を指す
     *
     * @param teString CSA サーバプロトコルの指し手情報
     * @throws InterruptedException Interrupted Exception
     */
    public void invokeMyTurn(String teString) throws InterruptedException {
        // 指し手を反映する
        invokeYourTurn(teString);
        //
        for (EngineStatus engineStatus: engineStatusList) {
            // エンジンフラグを落とす
            engineStatus.setEngineResponse(null);
        }
        // 待ち時間を決める (AFTER_TRANSACTION_TIME はエンジン以外の処理として見積もる)
        targetTime = System.currentTimeMillis() + (1000L * this.getSleepTime()) - AFTER_TRANSACTION_TIME;
        forGUI.send("targetTime t=" + this.getSleepTime());
        //
        // リクエスト要求を作る
        engineTurn = sumTeTableList.size();
        EngineRequest re = new EngineRequest(this, engineTurn, sumTeTableList, false);
        //
        // エンジンを叩き起こす
        for (EngineStatus engineStatus: engineStatusList) {
            engineStatus.engine.send(re);
        }
    }

    /**
     * エンジンからの戻り値
     * @param response エンジンからの戻り値
     * @param timeout タイムアウトなら true
     * @throws InterruptedException Interrupted Exception
     * @throws IOException IO Exception
     */
    public void invokeEngine(EngineResponse response, boolean timeout) throws InterruptedException, IOException {
        if (response != null) {
            if (response.turn() == sumTeTableList.size()) {
                // フラグ立て
                for (EngineStatus status : engineStatusList) {
                    if (response.cls().equals(status.engine.getClass())) {
                        status.setEngineResponse(response);
                        if (status.decide && (response.te() != TableDefine.LOS)) {
                            // 決定でかつ詰んでなければtimeoutと同じ効果がある
                            timeout = true;
                        }
                    }
                }
            }
        }
        //
        // 他のエンジンの終了を待つか確認する
        boolean flag = true; // 全て応答があるなら True
        for (EngineStatus status : engineStatusList) {
            if ((this.connectData != null)
                    && (!this.connectData.manualMode())
                    && (status.engine instanceof EngineManual)) {
                    // マニュアルモードが false のときでかつマニュアルエンジンのとき
                continue; // マニュアルエンジンは無視する
            }
            if (status.getEngineResponse() == null) {
                flag = false; // 応答が無いものがある
                break;
            }
        }
        if (flag) {
            // 全てのエンジンから回答が来たら timeoutと同じである
            timeout = true;
        }
        if (!timeout) {
            return;
        }
        //
        // timeout発生
        targetTime = 0; // 待ちを無限に替える
        engineTurn = -1; // 同じ手数で応答後にやってきたエンジンを無視させる
        //
        for (EngineStatus status : engineStatusList) {
            EngineResponse res = status.getEngineResponse();
            if (res == null) {
                // エンジンの現在の値を返す
                res = status.engine.getNow();
                status.engine.send(new EngineStop());
                if (res == null) {
                    // それでも resがなければ
                    continue;
                }
            }
            if (res.te() != TableDefine.LOS) {
                // 詰んでなければ指し手を送信する
                this.forGUI.send("engine=" + status.engine.getEngineId());
                String teString = TableDefine.changeTeIntToString(res.te());
                if (res.te() == TableDefine.WIN) {
                    Table table = sumTeTableList.getLast().table;
                    if (0 != table.isKingWin()) {
                        // 24点法なので引き分け宣言する
                        teString = TableDefine.STRING_DRAW;
                    }
                }
                sendSocket(teString + "\r\n");
                return;
            }
        }
        //
        // 詰んだ
        sendSocket("%TORYO\r\n");
        sendSocket("LOGOUT\r\n");
        stopSocket();
        //
    }

    /** マニュアルエンジンからの指示要求
     * @param manual マニュアルエンジン
     */
    public void requestManualEngine(MessageManualRequest manual) {
        TeTable teTable = manual.teTable();
        String id = manual.id();
        //
        Engine engine = null;
        for (EngineStatus target : engineStatusList) {
            if (target.engine.getEngineId().equals(id)) {
                engine = target.engine;
                break;
            }
        }
        if (engine == null) {
            for (Engine target : engineEtcList) {
                if (target.getEngineId().equals(id)) {
                    engine = target;
                    break;
                }
            }
        }
        if (engine == null) {
            try {
                forGUI.send("No id id=" + id);
            } catch (InterruptedException e) {
                // EMPTY
            }
            return;
        }
        List<TeTable> teTableList = List.of(teTable);
        EngineRequest req = new EngineRequest(this, -1, teTableList, true);
        // 手動でcreateを呼ぶ
        engine.createNow(req);
        try {
            // 結果を手動で返す
            req.forResult().send(engine.getNow());
        } catch(InterruptedException e) {
            // EMPTY
        }
    }

    /**
     * エンジンからの戻り処理(画面表示用)
     * @param response エンジンからの戻り値
     * @throws InterruptedException Interrupted Exception
     * @throws IOException IO Exception
     */
    public void invokeDisplay(EngineResponse response) throws InterruptedException, IOException {
        MessageDisplayResponse res = new MessageDisplayResponse(response.displayList());
        forGUI.send(res);
    }

    /** 手のリストの中から最高にダメな手を探して topObBadTeTableList に追加する。
     * 要するに「まった」を掛けて、ダメな手を指さないように学習する
     */
    private void createTopOpBad() {
        if (sumTeTableList.isEmpty()) {
            return; // 空なら処理しない
        }
        Table lastTable = sumTeTableList.getLast().table;
        if (! lastTable.createChild().isEmpty()) {
            return; // 詰んでないなら処理しない
        }
        int losTeban = lastTable.getTeban();
        //
        // 評価値を取得する
        EngineRequest req = new EngineRequest(null, -1, sumTeTableList, true);
        engineRecordEval.createNow(req);
        EngineResponse res = engineRecordEval.getNow();
        List<TeTable> teTableList = res.displayList();
        //
        // ここから再悪手を探す
        // 先手の場合は、最も評価の落差が大きい手を探す
        // 後手の場合は、最も評価が大きくなった手を探す
        TeTable oldTeTable = getTeTable(teTableList, losTeban);
        if (oldTeTable != null) {
            // 待ったする手を決定する
            topObBadTeTableSet.add(oldTeTable);
        }
        try {
            invokeDisplay(res); // 結果を表示する
        } catch (InterruptedException|IOException e) {
            // EMPTY
        }
    }

    /** 再悪手を取得する
     *
     * @param teTableList 評価値付き棋譜リスト
     * @param losTeban 負けた手番情報
     * @return 最も悪手なテーブル
     */
    @Nullable
    private TeTable getTeTable(List<TeTable> teTableList, int losTeban) {
        float oldSub = 0;
        float oldValue = 0.5f;
        TeTable oldTable = null;
        //
        for (TeTable teTable : teTableList) {
            if (! (teTable instanceof EvalTeTable evalTeTable)) {
                continue;
            }
            //
            if (losTeban == evalTeTable.table.getTeban()) {
                continue; // 負けた局面だけチェックする
            }
            if (oldTable == null) {
                oldTable = evalTeTable;
                oldValue = evalTeTable.eval;
                continue;
            }
            float nowSub = 100.0F + (evalTeTable.eval - oldValue);
            nowSub = nowSub * nowSub; // 2乗してマイナスを消す
            oldValue = evalTeTable.eval;
            if (oldSub <= nowSub) { // 差分が大きい方を採用
                oldSub = nowSub;
                oldTable = evalTeTable;
            }
        }
        return oldTable;
    }
}

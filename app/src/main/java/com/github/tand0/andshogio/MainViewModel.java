package com.github.tand0.andshogio;

import android.os.Handler;
import android.os.Looper;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.github.tand0.andshogio.engin.SendListener;
import com.github.tand0.andshogio.util.Table;
import com.github.tand0.andshogio.util.TeTable;

import java.util.List;

/**
 * メインのビューモデル
 */
public class MainViewModel extends ViewModel {
    /**
     * log area
     */
    private final MutableLiveData<String> log = new MutableLiveData<>();


    /**
     * 学習結果の手テーブルのリスト
     */
    private final MutableLiveData<List<TeTable>> evalTeTableList = new MutableLiveData<>();

    /** キャンバス用の手テーブル */
    private final MutableLiveData<TeTable> canvasTeTable = new MutableLiveData<>();

    /**
     * 手テーブルのリスト
     */
    private final MutableLiveData<List<TeTable>> teTableList = new MutableLiveData<>();

    /** ステータス */
    private SendListener status;

    /**
     * コンストラクタ
     */
    public MainViewModel() {
        this.setCanvasTeTable(new TeTable(new Table(null, 0),0));
    }
    /**
     * ログを追加する。スレッド考慮しない
     * @param message message
     */
    public void appendLog(String message) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            // UIスレッドの内から呼ばれた場合
            appendLogForInner(message);
        } else {
            // UIスレッドの外から呼ばれた場合
            Handler mainHandler = new Handler(Looper.getMainLooper());
            // UIスレッドに入れて無理やり呼ぶ
            mainHandler.post(() -> appendLogForInner(message));
        }
    }

    /**
     * UIスレッド内でログを追加する
     * @param message メッセージ
     */
    private void appendLogForInner(String message) {
        final int len = 20000;

        String work = this.log.getValue();
        if (work == null) {
            work = "";
        }
        work = work + "\n" + message;
        if (len < work.length()) {
            work = work.substring(work.length() - len);
        }
        this.log.setValue(work);
    }


    /** 学習結果の手テーブルのリストの取得
     * @return 手テーブルのリスト
     */
    public LiveData<List<TeTable>> getEvalTeTableList() {
        return this.evalTeTableList;
    }

    /**
     * ログの取得
     * @return ログ
     */
    public LiveData<String> getLog() {
        return this.log;
    }

    /** 手テーブルのリストの取得
     * @return 手テーブルの知ると
     */
    public LiveData<List<TeTable>> getTeTableList() {
        return this.teTableList;
    }

    /** 手テーブルのリストの更新
     *
     * @param teTableList 手テーブルのリスト
     */
    public void updateTeTableList(List<TeTable> teTableList) {
        this.teTableList.postValue(teTableList);
    }

    /** 学習結果の手テーブルのリストの更新
     *
     * @param teTableList 手テーブルのリスト
     */
    public void updateEvalTeTableList(List<TeTable> teTableList) {
        this.evalTeTableList.postValue(teTableList);
    }

    /**
     * キャンバス用の手テーブルの取得
     * @return 手テーブル
     */
    public LiveData<TeTable> getCanvasTeTable() {
        return this.canvasTeTable;
    }

    /**
     * キャンバス用の手テーブルの設定
     * @param canvasTeTable 手テーブル
     */
    public void setCanvasTeTable(TeTable canvasTeTable) {
        this.canvasTeTable.postValue(canvasTeTable);
    }

    /** ステータスの設定
     * @param status ステータス
     */
    public void setStatus(SendListener status) {
        this.status = status;
    }

    /** ステータスの取得
     * @return ステータス
     */
    public SendListener getStatus() {
        return this.status;
    }
}

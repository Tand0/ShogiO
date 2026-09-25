package com.github.tand0.shogio;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.OpenableColumns;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.widget.ViewPager2;

import com.github.tand0.shogio.engin.EngineRequest;
import com.github.tand0.shogio.engin.EngineStop;
import com.github.tand0.shogio.engin.MessageManualRequest;
import com.github.tand0.shogio.engin.MessageDisplayResponse;
import com.github.tand0.shogio.engin.MessageRequest;
import com.github.tand0.shogio.engin.SendListener;
import com.github.tand0.shogio.engin.StatusManager;
import com.github.tand0.shogio.litert.TFLiteManager;
import com.github.tand0.shogio.tab.AndCanvasView;
import com.github.tand0.shogio.tab.AndFragmentAdapter;
import com.github.tand0.shogio.util.EvalTeTable;
import com.github.tand0.shogio.util.PropertyDefine;
import com.github.tand0.shogio.util.Table;
import com.github.tand0.shogio.util.TableDefine;
import com.github.tand0.shogio.util.TeTable;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;


/**
 * メインとなるアクティビティ
 */
public class MainActivity extends AppCompatActivity {
    /** main thread */
    private Thread t1;

    /** view model */
    private MainViewModel viewModel;

    /** ページャ */
    private ViewPager2 viewPager;

    /** キャンパスビュー */
    private AndCanvasView canvasView;

    /** SQL に接続する用 */
    private MainDatabaseHelper helper;

    /** 評価値用 */
    private TFLiteManager tFLiteManager = null;

    /** ステータスマネージャ */
    private final StatusManager statusManager = new StatusManager();

    /** タブレイアウト用のメディエーター */
    private TabLayoutMediator mediator = null;

    /** CSA 取得 ボタン押した処理ののランチャー */
    private ActivityResultLauncher<Intent> filePickerLauncher;

    /**
     * @param savedInstanceState If the activity is being re-initialized after
     *     previously being shut down then this Bundle contains the data it most
     *     recently supplied in {@link #onSaveInstanceState}.  <b><i>Note: Otherwise it is null.</i></b>
     *
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //
        DisplayMetrics dm = getResources().getDisplayMetrics();
        int layout = (dm.widthPixels < dm.heightPixels) ? R.layout.activity_main : R.layout.activity_main_land;
        //
        EdgeToEdge.enable(this);
        setContentView(layout);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        // システムUIの高さ分だけ、Viewに余白を設定する
        View mainView = findViewById(R.id.main);
        ViewCompat.setOnApplyWindowInsetsListener(mainView, (v, windowInsets) -> {
            Insets systemBars = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return windowInsets;
        });
        //
        //
        final FragmentManager fragmentManager = getSupportFragmentManager();
        final AndFragmentAdapter adapter = new AndFragmentAdapter(fragmentManager, getLifecycle());
        viewPager = findViewById(R.id.view_pager);
        viewPager.setAdapter(adapter);
        viewPager.setUserInputEnabled(true);
        //
        TabLayout tabs = findViewById(R.id.tab_layout);
        // 新しく登録しなおす
        final TabLayoutMediator.TabConfigurationStrategy str =
                (tab, position) ->
                        tab.setText(this.getResources().getString(adapter.getText(position)));
        mediator = new TabLayoutMediator(tabs, viewPager, str);
        mediator.attach();
        //
        //
        if (viewModel == null) {
            viewModel = new ViewModelProvider(this).get(MainViewModel.class);
        }
        //
        if (tFLiteManager == null) {
            // 推論エンジンをロードする
            // 画面を回転指せリセットすると２回呼ばれるので２回目はパス
            AssetManager am = this.getApplicationContext().getAssets();
            tFLiteManager = new TFLiteManager(am);
        }
        //
        if (helper == null) {
            // データベースを作成する
            // 画面を回転指せリセットすると２回呼ばれるので２回目はパス
            helper = new MainDatabaseHelper(this, getForGui());
        }
        //
        if (t1 == null) {
            // 状態管理を上げる
            t1 = new Thread(statusManager, "engine_StatusManager");
            t1.start();
        }
        // 再設定
        statusManager.forGUI(getForGui());
        statusManager.setDatabase(helper);
        statusManager.setTFLiteManager(tFLiteManager);
        viewModel.setStatus(statusManager);
        //
        // 局面の更新があったときの処理
        canvasView = mainView.findViewById(R.id.topView);
        //
        try {
            // 起動バージョンを表示する
            String name = this.getPackageName();
            PackageManager pm = this.getPackageManager();
            PackageInfo info = pm.getPackageInfo(name, PackageManager.GET_META_DATA);
            viewModel.appendLog("Version:" + info.versionName);
        } catch (PackageManager.NameNotFoundException e) {
            viewModel.appendLog(e.getMessage());
        }
        // アプリケーションが動くかチェック
        MainApplication mainApp = (MainApplication) getApplication();
        int border = mainApp.getInt(PropertyDefine.APP_BORDER, 0);
        viewModel.appendLog("border=" + border);
        //
        // 独自実装
        filePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        getFilePickerLauncherResult(result.getData()); // 独自実装
                    }
                }
        );
    }
    /**
     * on pause
     */
    @Override
    public void onPause() {
        super.onPause();
    }

    /** 終了処理 */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediator != null) {
            mediator.detach();
            mediator = null;
        }
        if (t1 != null) {
            t1.interrupt(); // キューを終わらせる
            t1 = null; // スレッド情報を消す
        }
        if (helper != null) {
            try {
                helper.close();
            } catch (Exception e) {
                // EMPTY
            }
            helper = null; // ヘルパーを消す
        }
        if (tFLiteManager != null) {
            tFLiteManager.close();
            tFLiteManager = null;
        }
    }

    /**
     * GUIに対する要求を渡すためのリスナの取得
     * @return GUIに対する要求を渡すためのリスナ
     */
    public SendListener getForGui() {
        return this.forGUI;
    }

    /** 外部から処理を要求されたとき用 */
    private final SendListener forGUI = new SendListener() {
        @Override
        public void send(Object x) {
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(() -> {
                // ここからGUIスレッドに入る
                switch (x) {
                    case String s -> viewModel.appendLog(s);
                    case Exception exception -> viewModel.appendLog(exception.getMessage());
                    case EngineRequest engineRequest ->
                            canvasView.updateEngineRequest(engineRequest);
                    case EngineStop ignored -> canvasView.updateEngineRequest(null); // 削除
                    case TeTable table -> viewModel.setCanvasTeTable(table);
                    case LinkedList<?> list -> {
                        //
                        // 移し替える
                        List<TeTable> teTableList =
                                list.stream().map(y->(TeTable)y).toList();
                        //
                        TeTable teTable = teTableList.getLast();
                        viewModel.setCanvasTeTable(teTable);
                        //
                        viewModel.updateTeTableList(teTableList);
                        //
                    }
                    case MessageRequest messageRequest -> {
                        try {
                            viewModel.getStatus().send(messageRequest);
                        } catch (InterruptedException e) {
                            viewModel.appendLog(
                                    "(MessageRequest) Got a " + e.getClass().getName());
                        }
                    }
                    case MessageDisplayResponse res -> {
                        viewModel.updateEvalTeTableList(res.teTableList());
                        viewPager.setCurrentItem(4);
                    }
                    default -> viewModel.appendLog("Got a " + x.getClass().getName());
                }
            });
        }
    };

    /**
     * フラグメントから呼び出してページを切り替えるためのメソッド
     * @param pageIndex 遷移先のページ番号（0から始まるインデックス）
     */
    public void navigateToPage(int pageIndex) {
        if (viewPager != null) {
            // 第2引数を true にするとスライドのアニメーションが付きます。不要なら false にします。
            viewPager.setCurrentItem(pageIndex, true);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem menuButton){
        int buttonId = menuButton.getItemId();
        return this.action(buttonId) || super.onOptionsItemSelected(menuButton);
    }

    /**
     * ボタンクリックの共通処理
     * @param id button id
     * @return if used then true
     */
    public boolean action(int id) {
        String idString = getIdString(id);
        viewModel.appendLog(idString);
        if (id == R.id.engine_top_of_bad) {
            // 最高に悪い手の表示
            SendListener status = viewModel.getStatus();
            MessageRequest req = new MessageRequest() {
                @Override
                public void run(StatusManager manager) throws InterruptedException {
                    // ダメだった手の表示
                    MessageDisplayResponse x =
                            new MessageDisplayResponse(
                                    manager.topObBadTeTableSet().stream().toList());
                    manager.forGUI().
                    send(x);
                }
            };
            try {
                status.send(req);
            } catch (InterruptedException e) {
                viewModel.appendLog("action failed e=" + e.getMessage());
                return false;
            }
        } else if (id == R.id.engine_clean) {
            try {
                this.getForGui().send(new TeTable(new Table(null, 0), 0));
                return true;
            } catch (InterruptedException e) {
                viewModel.appendLog("Got the error myAction e=" + e.getMessage());
                return false;
            }
        } else if (id == R.id.engine_read_csa) {
            return this.readCsa();
        } else if (id == R.id.action_policy) {
            return jumpUrl(viewModel, PropertyDefine.URL_POLICY);
        } else if (id == R.id.action_report) {
            return jumpUrl(viewModel, PropertyDefine.URL_REPORT);
        } else if (id == R.id.action_show_thread) {
            showThreadStatus();
            viewPager.setCurrentItem(0); // logへ
            return true;
        } else if (id == R.id.action_flipping_horizontal) {
            try {
                TeTable teTable = viewModel.getCanvasTeTable().getValue();
                if (teTable != null) {
                    teTable = new TeTable(teTable.table.flippingHorizontal(), TableDefine.LOS);
                    this.getForGui().send(teTable);
                } else {
                    return false;
                }
                return true;
            } catch (InterruptedException e) {
                viewModel.appendLog("Got the error myAction e=" + e.getMessage());
                return false;
            }
        } else if (id == R.id.action_flipping_vertical) {
            try {
                TeTable teTable = viewModel.getCanvasTeTable().getValue();
                if (teTable != null) {
                    teTable = new TeTable(teTable.table.flippingVertical(), TableDefine.LOS);
                    this.getForGui().send(teTable);
                } else {
                    return false;
                }
                return true;
            } catch (InterruptedException e) {
                viewModel.appendLog("Got the error myAction e=" + e.getMessage());
                return false;
            }
        } else if (id == android.R.id.home) {
            finish(); // ひとつ前に戻る
            return true;
        }
        //
        // デフォルトはエンジンを起動する
        viewModel.updateEvalTeTableList(new ArrayList<>()); // テーブルクリア
        //
        SendListener status = viewModel.getStatus();
        TeTable teTable = viewModel.getCanvasTeTable().getValue();
        MessageManualRequest req = new MessageManualRequest(teTable, idString);
        try {
            status.send(req);
        } catch (InterruptedException e) {
            viewModel.appendLog("action failed e=" + e.getMessage());
            return false;
        }
        return true;
    }

    private String getIdString(int id) {
        String idString;
        try {
            idString = this.getResources().getResourceName(id);
            int index = idString.indexOf('/');
            if (0 <= index) {
                idString = idString.substring(index + 1);
            }
        } catch(android.content.res.Resources.NotFoundException e) {
            return e.getMessage();
        }
        return idString;
    }

    /**
     * URL によるジャンプ
     * @param viewModel viewModel
     * @param id string id
     * @return if used then true
     */
    public boolean jumpUrl(MainViewModel viewModel, String id) {
        MainApplication mainApp = (MainApplication) getApplication();
        String url = mainApp.getString(id, "http://localhost:8080/");
        viewModel.appendLog("url=" + url);
        //
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        try {
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            // ブラウザが開けない場合の代替処理
            viewModel.appendLog(e.getMessage());
            return false;
        }
        return true;
    }

    /** CSAファイルの読み込み
     * @return if used then true
     */
    protected boolean readCsa() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        //
        filePickerLauncher.launch(intent);
        return true;
    }
    /**
     * 別スレッドでUriからデータを読み込む
     */
    protected void readFromUriAsync(String fileName, Uri uri) {
        try (ExecutorService es = Executors.newSingleThreadExecutor()) {
            es.execute(() -> {
                // ContentResolver経由でInputStreamを取得（SAFの必須手順）
                String extension = TableDefine.getExtension(fileName);
                if ((!TableDefine.EXTENSION_CSA.equals(extension))
                        && (!TableDefine.EXTENSION_DB.equals(extension))) {
                    viewModel.appendLog("extension error uri=" + uri + " ext=" + extension);
                    return; // 想定した拡張子ちゃうんちゃう
                }
                try (InputStream is = MainActivity.this.getContentResolver().openInputStream(uri)) {
                    List<EvalTeTable> eval = new ArrayList<>();
                    if (TableDefine.EXTENSION_CSA.equals(extension)) {
                        TableDefine.runFileCsaStream(eval, false, is, 0);
                    } else {
                        TableDefine.runFileDbStream(is, eval);
                    }
                    //
                    List<TeTable> teTable = eval.stream().map(x->(TeTable)x).toList();
                    viewModel.updateTeTableList(teTable); // 指し手側に出す
                    viewModel.appendLog("Load OK size=" + teTable.size());
                } catch (Exception e) {
                    viewModel.appendLog("Load NG e=" +e.getMessage());
                }
            });
        }
    }
    /**
     * URIからファイル名（拡張子含む）を取得するメソッド
     *
     * @param context コンテキスト（ActivityやFragmentなど）
     * @param uri 対象のファイルURI (content://...)
     * @return ファイル名（取得失敗時はnull）
     */
    protected String getFileNameFromUri(Context context, Uri uri) {
        String fileName = null;

        // URIのスキームが content の場合のみ処理を行う
        if ("content".equals(uri.getScheme())) {

            // try-with-resources を使うことで、Cursor を自動的にクローズ（解放）します
            try (Cursor cursor = context.getContentResolver().query(uri, null, null, null, null)) {

                // カーソルが正常に取得でき、かつ最初のデータに移動できた場合
                if (cursor != null && cursor.moveToFirst()) {

                    // OpenableColumns.DISPLAY_NAME を指定してファイル名が格納されている列のインデックスを取得
                    int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);

                    if (nameIndex != -1) {
                        // 列から文字列（ファイル名）を取り出す
                        fileName = cursor.getString(nameIndex);
                    }
                }
            } catch (Exception e) {
                viewModel.appendLog("getFileNameFromUri error=" + e.getMessage());
            }
        }
        // スキームが file:// の場合のフォールバック（念のため）
        else if ("file".equals(uri.getScheme())) {
            fileName = uri.getLastPathSegment();
        }

        return fileName;
    }

    /**
     * ランチャーがOKだった時の処理を行う
     * @param intent  Intent
     */
    protected void getFilePickerLauncherResult(Intent intent) {
        Uri uri = intent.getData();
        if (uri == null) {
            return;
        }
        String fileName = getFileNameFromUri(this, uri);
        // バックグラウンドで非同期読み込みを実行
        readFromUriAsync(fileName, uri);
    }

    /** メモリやスレッドの状態を表示する */
    protected void showThreadStatus() {
        try {
            Runtime runtime = Runtime.getRuntime();
            // 使用中のメモリ量 (バイト)
            long usedMemory = runtime.totalMemory() - runtime.freeMemory();
            // MBに変換する場合
            long usedMemoryMb = usedMemory / 1024 / 1024;
            // JVMが使用可能な最大メモリ量 (これを超えるとOOMが発生)
            long maxMemory = runtime.maxMemory() / 1024 / 1024;
            // 現時点でJVMに割り当てられている総メモリ量
            long totalMemory = runtime.totalMemory() / 1024 / 1024;
            //
            String x = "usedMemory=" + usedMemoryMb + "M\n"
                    + "maxMemory=" + maxMemory + "M\n"
                    + "totalMemory=" + totalMemory + "M";
            forGUI.send(x);
            //
            String string = Thread.getAllStackTraces()
                    .keySet()
                    .stream()
                    .map(t ->
                            String.format(
                                    Locale.JAPAN,
                                    "TName=%s\n",
                                    t.getName()))
                    .sorted() // ソート
                    .collect(Collectors.joining());
            forGUI.send(string);
        } catch (InterruptedException e) {
            // TMPTY
        }
    }
}
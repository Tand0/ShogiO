package com.github.tand0.shogio.tab;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.github.tand0.shogio.MainActivity;
import com.github.tand0.shogio.MainViewModel;
import com.github.tand0.shogio.R;
import com.github.tand0.shogio.engin.MessageConnectRequest;
import com.github.tand0.shogio.engin.MessageConnectCloseRequest;
import com.github.tand0.shogio.engin.MessageRequest;
import com.github.tand0.shogio.engin.StatusManager;
import com.google.android.material.slider.Slider;


/** 設定用のフラグメント
 */
public class AndSettingFragment extends Fragment {
    /** パラメータ保存用の固有文字指定
     * context.getSharedPreferences()で使う
     */
    public static final String APP_SETTING = "app_settings";

    /** ビューモデル */
    private MainViewModel viewModel;

    /** 将棋サーバの IP address */
    private EditText settingIp;

    /** 将棋サーバの IP port */
    private EditText settingPort;

    /** CSA プロトコルのユーザ名を入れる View */
    private EditText settingUser;

    /** CSA プロトコルのパスワードを入れる View */
    private EditText settingPass;

    /** 手差しを許すか用のチェックを入れる View */
    private SwitchCompat settingManual;

    /** CSA拡張モードを使うかチェック View */
    private SwitchCompat settingExtension;

    /** 連続実行を行うためんぼ View */
    private SwitchCompat settingLooping;

    /** max table factory */
    private Slider explorerMaxTableFactory;

    /** max no change */
    private Slider explorerMaxNoChange;

    /** max level */
    private Slider explorerMaxLevel;

    /** min son */
    private Slider explorerMinSon;

    /** IP address のデフォルト値 */
    private static final String IP_DEFAULT = "10.0.2.2";

    /** ゲーム名のデフォルト */
    private static final String GAME_NAME_DEFAULT = "default-1500-0";

    /** ゲーム名のデフォルト (floodgate用) */
    private static final String GAME_NAME_FLOOD_DEFAULT = "floodgate-300-10F";


    /** ゲーム名の View */
    private EditText settingGameName;

    /** 先後の view */
    private Spinner settingSpinner;

    /**
     * コンストラクタ
     */
    public AndSettingFragment() {
        super(R.layout.fragment_setting);
    }
    /**
     * on create
     * @param savedInstanceState If the activity is being re-initialized after
     *     previously being shut down then this Bundle contains the data it most
     *     recently supplied in {@link #onSaveInstanceState}.  <b><i>Note: Otherwise it is null.</i></b>
     *
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    /**
     * ログを作る
     * @param view The View returned.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     * from a previous saved state as given here.
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        viewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);
        //
        addButton(view);
        settingIp = view.findViewById(R.id.setting_ip);
        settingPort = view.findViewById(R.id.setting_port);
        settingUser = view.findViewById(R.id.setting_user);
        settingPass = view.findViewById(R.id.setting_pass);
        settingManual = view.findViewById(R.id.setting_manual);
        settingExtension = view.findViewById(R.id.setting_used_csa_extension);
        settingGameName = view.findViewById(R.id.setting_gamename);
        settingSpinner = view.findViewById(R.id.setting_spinner);
        settingLooping = view.findViewById(R.id.setting_looping);
        explorerMaxTableFactory = view.findViewById(R.id.explorer_max_table_factory);
        explorerMaxTableFactory.addOnChangeListener((x,y,z)->changeExplorer());
        explorerMaxNoChange = view.findViewById(R.id.explorer_max_no_change);
        explorerMaxNoChange.addOnChangeListener((x,y,z)->changeExplorer());
        explorerMaxLevel = view.findViewById(R.id.explorer_max_level);
        explorerMaxLevel.addOnChangeListener((x,y,z)->changeExplorer());
        explorerMinSon = view.findViewById(R.id.explorer_min_son);
        explorerMinSon.addOnChangeListener((x,y,z)->changeExplorer());
        //
        load();
    }

    /** 探索用に変化があるときに呼び出される */
    private void changeExplorer()  {
        int maxTableFactory = (int) this.explorerMaxTableFactory.getValue();
        int maxNoChange = (int) this.explorerMaxNoChange.getValue();
        int maxLevel = (int) this.explorerMaxLevel.getValue();
        int minSon = (int) this.explorerMinSon.getValue();
        try {
            viewModel.getStatus().send(
                    new MessageRequest() {
                        @Override
                        public void run(StatusManager manager) {
                            manager.explorer().maxTableFactory(maxTableFactory);
                            manager.explorer().maxNoChange(maxNoChange);
                            manager.explorer().maxLevel(maxLevel);
                            manager.explorer().minSon(minSon);
                        }
                    });
        } catch(InterruptedException e) {
            // EMPTY
        }
    }
    /**
     * ボタンの追加
     * @param view ビュー
     */
    public void addButton(View view) {
        if (view instanceof Button) {
            view.setOnClickListener(this::myAction);
        } else if (view instanceof ViewGroup vg) {
            for (int i = 0 ; i < vg.getChildCount() ; i++) {
                addButton(vg.getChildAt(i));
            }
        }
    }

    /**
     * click event for button
     * @param v view
     */
    public void myAction(View v) {
        if (v instanceof Button) {
            String idString = getIdString(v);
            viewModel.appendLog("Action: " + idString);
            switch (idString) {
                case "setting_save":
                    save();
                    break;
                case "ip_10_0_2_2":
                    if (this.settingIp != null) {
                        String ip = this.getResources().getString(R.string.ip_10_0_2_2);
                        this.settingIp.setText(ip);
                        this.settingGameName.setText(GAME_NAME_DEFAULT);
                    }
                    break;
                case "ip_gserver_denryu_sen_jp":
                    if (this.settingIp != null) {
                        String ip = this.getResources().getString(R.string.ip_gserver_denryu_sen_jp);
                        this.settingIp.setText(ip);
                    }
                    break;
                case "ip_wgserver_computer_shogi_org":
                    if (this.settingIp != null) {
                        String ip = this.getResources().getString(R.string.ip_wgserver_computer_shogi_org);
                        this.settingIp.setText(ip);
                    }
                    break;
                case "ip_wdoor_c_u_tokyo_ac_jp":
                    if (this.settingIp != null) {
                        String ip = this.getResources().getString(R.string.ip_wdoor_c_u_tokyo_ac_jp);
                        this.settingIp.setText(ip);
                        this.settingGameName.setText(GAME_NAME_FLOOD_DEFAULT);
                    }
                    break;
                case "setting_connect":
                    Toast.makeText(requireContext(), idString, Toast.LENGTH_SHORT).show();
                    connect();
                    break;
                case "setting_stop":
                    Toast.makeText(requireContext(), idString, Toast.LENGTH_SHORT).show();
                    stop();
                    break;
                case "setting_manual":
                    break; // なにもしない
                default:
                    Toast.makeText(requireContext(), idString, Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    }
    /**
     * on resume
     */
    @Override
    public void onResume() {
        super.onResume();
    }

    /** ロードする */
    public void load() {
        //
        View viewGroup = getView();
        if (viewGroup == null) {
            return;
        }
        SharedPreferences preferences = getSharedPreferences();
        if (preferences == null) {
            return;
        }
        loadApply(preferences, viewGroup);
        //
    }

    /** 再帰処理
     *
     * @param preferences 保存用クラス
     * @param view ビュー
     */
    public void loadApply(SharedPreferences preferences, View view) {
        if (view instanceof EditText editText) {
            String idString = getIdString(view);
            if (idString == null) {
                return;
            }
            String text = editText.getText().toString();
            int inputType = editText.getInputType();
            try {
                text = preferences.getString(idString, text);
                editText.setText(text);
                if ((inputType & InputType.TYPE_TEXT_VARIATION_PASSWORD) != 0) {
                    text = "***";
                }
                viewModel.appendLog( "load:" + idString + ": " + text);
            } catch(ClassCastException e) {
                viewModel.appendLog( e.getMessage());
            }
        } else if (view instanceof SwitchCompat sm) {
            String idString = getIdString(view);
            if (idString == null) {
                return;
            }
            boolean checked = sm.isChecked();
            try {
                checked = preferences.getBoolean(idString, checked);
                sm.setChecked(checked);
                viewModel.appendLog( "load:" + idString + ": " + checked);
            } catch(ClassCastException e) {
                viewModel.appendLog( e.getMessage());
            }
        } else if (view instanceof Spinner sm) {
            String idString = getIdString(view);
            if (idString == null) {
                return;
            }
            Object item = sm.getSelectedItem();
            SpinnerAdapter sa = sm.getAdapter();
            if ((item == null) || (sa == null)) {
                return;
            }
            String preferencesText = preferences.getString(idString, item.toString());
            try {
                for (int i = 0 ; i < sa.getCount() ; i++) {
                    Object target = sa.getItem(i);
                    if (preferencesText.equals(target.toString())) {
                        sm.setSelection(i);
                        viewModel.appendLog( "load:" + idString + ": " + preferencesText);
                        break;
                    }
                }
            } catch(ClassCastException e) {
                viewModel.appendLog( e.getMessage());
            }
        } else if (view instanceof ViewGroup vg) {
            for (int i = 0 ; i < vg.getChildCount() ; i++) {
                loadApply(preferences, vg.getChildAt(i));
            }
        }
    }
    /**
     * on pause
     */
    @Override
    public void onPause() {
        super.onPause();
    }
    protected SharedPreferences getSharedPreferences() {
        Context context = this.getContext();
        if (context == null) {
            viewModel.appendLog("context == null");
            return null;
        }
        return context.getSharedPreferences(APP_SETTING, Context.MODE_PRIVATE);
    }

    /** 保存する */
    public void save() {
        View viewGroup = getView();
        if (viewGroup == null) {
            return;
        }
        SharedPreferences preferences = getSharedPreferences();
        if (preferences == null) {
            return;
        }
        final SharedPreferences.Editor editor = preferences.edit();
        saveApply(editor, viewGroup);
        editor.apply();
    }

    /** 保存する
     *
     * @param editor エディタ
     * @param view ビュー
     */
    public void saveApply(SharedPreferences.Editor editor, View view) {
        if (view instanceof EditText editText) {
            String idString = getIdString(view);
            if (idString == null) {
                return;
            }
            int inputType = editText.getInputType();
            String text = editText.getText().toString();
            editor.putString(idString, text);
            if ((inputType & InputType.TYPE_TEXT_VARIATION_PASSWORD) != 0) {
                text = "***";
            }
            viewModel.appendLog( "save:" + idString + ": " + text);
        } else if (view instanceof SwitchCompat sm) {
            String idString = getIdString(view);
            if (idString == null) {
                return;
            }
            boolean checked = sm.isChecked();
            editor.putBoolean(idString, checked);
            viewModel.appendLog("save:" + idString + ": " + checked);
        } else if (view instanceof Spinner sm) {
            String idString = getIdString(view);
            if (idString == null) {
                return;
            }
            Object item = sm.getSelectedItem();
            if (item == null) {
                return;
            }
            editor.putString(idString, item.toString());
            viewModel.appendLog("save:" + idString + ": " + item);
        } else if (view instanceof ViewGroup vg) {
            for (int i = 0 ; i < vg.getChildCount() ; i++) {
                saveApply(editor, vg.getChildAt(i));
            }
        }
    }

    /**
     * View から View の ID の文字列を取得する
     * @param view View
     * @return View の ID
     */
    private String getIdString(View view) {
        String idString = null;
        try {
            idString = view.getResources().getResourceName(view.getId());
            int index = idString.indexOf('/');
            if (0 <= index) {
                idString = idString.substring(index + 1);
            }
        } catch(android.content.res.Resources.NotFoundException e) {
            viewModel.appendLog(e.getMessage());
        }
        return idString;
    }

    /** 状態管理へ接続を要求する */
    private void connect() {
        SharedPreferences preferences = getSharedPreferences();
        if (preferences == null) {
            return;
        }
        final String aNShogiO = "ANShogiO";
        String ip = this.settingIp.getText().toString();
        ip = ip.isEmpty() ? IP_DEFAULT : ip;
        String portString = this.settingPort.getText().toString();
        portString = portString.isEmpty() ? "4081" : portString;
        int port = Integer.parseInt(portString);
        String user = this.settingUser.getText().toString();
        user = user.isEmpty() ? aNShogiO : user;
        String pass = this.settingPass.getText().toString();
        pass = pass.isEmpty() ? aNShogiO : pass;
        boolean manualMode = this.settingManual.isChecked();
        boolean extensionMode = this.settingExtension.isChecked();
        boolean loopingMode = this.settingLooping.isChecked();
        int maxTableFactory = (int) this.explorerMaxTableFactory.getValue();
        int maxNoChange = (int) this.explorerMaxNoChange.getValue();
        int maxLevel = (int) this.explorerMaxLevel.getValue();
        int minSon = (int) this.explorerMinSon.getValue();
        String gameName = this.settingGameName.getText().toString();
        if (gameName.isEmpty()) {
            if (ip.equals(this.getResources().getString(R.string.ip_wdoor_c_u_tokyo_ac_jp))) {
                gameName = GAME_NAME_FLOOD_DEFAULT;
            } else {
                gameName = GAME_NAME_DEFAULT;
            }
        }
        String spinner;
        Object item = settingSpinner.getSelectedItem();
        if (item != null) {
            spinner = item.toString();
            if (spinner.isEmpty()) {
                spinner = "*";
            }
        } else {
            spinner = "*";
        }
        // スペースを取らないとエラーになる
        user = user.replaceAll("\\s", "");
        pass = pass.replaceAll("\\s", "");
        try {
            viewModel.getStatus().send(
                    new MessageConnectRequest(
                        ip,
                        port,
                        user,
                        pass,
                        manualMode,
                        extensionMode,
                        gameName,
                        spinner,
                        loopingMode,
                        maxTableFactory,
                        maxNoChange,
                        maxLevel,
                        minSon));
        } catch (InterruptedException e) {
            viewModel.appendLog(e.getMessage());
        }
        //
        // 親Activityを取得
        if (getActivity() instanceof MainActivity mainActivity) {

            // Log表示へ遷移させる
            mainActivity.navigateToPage(0);
        }
    }

    /** 状態管理へ接続を要求する */
    private void stop() {
        try {
            viewModel.getStatus().send(
                    new MessageConnectCloseRequest());
        } catch (InterruptedException e) {
            viewModel.appendLog(e.getMessage());
        }
        // 親Activityを取得
        if (getActivity() instanceof MainActivity mainActivity) {
            // Log表示へ遷移させる
            mainActivity.navigateToPage(0);
        }
    }
}
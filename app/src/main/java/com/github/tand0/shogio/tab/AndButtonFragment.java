package com.github.tand0.shogio.tab;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.github.tand0.shogio.MainActivity;
import com.github.tand0.shogio.R;

/** ボタン設定用フラグメント
 */
public class AndButtonFragment extends Fragment {

    /** コンストラクタ */
    public AndButtonFragment() {
        super(R.layout.fragment_button);
    }

    /**
     *
     * @param inflater The LayoutInflater object that can be used to inflate
     * any views in the fragment,
     * @param container If non-null, this is the parent view that the fragment's
     * UI should be attached to.  The fragment should not add the view itself,
     * but this can be used to generate the LayoutParams of the view.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     * from a previous saved state as given here.
     *
     * @return view
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
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
        //
        addButton(view);
        //
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
        if (!(v instanceof Button)) {
            return;
        }
        MainActivity act = (MainActivity) getActivity();
        if (act == null) {
            return;
        }
        int buttonId = v.getId();
        act.action(buttonId);
    }
    /**
     * on resume
     */
    @Override
    public void onResume() {
        super.onResume();
    }

    /**
     * on pause
     */
    @Override
    public void onPause() {
        super.onPause();
    }

}
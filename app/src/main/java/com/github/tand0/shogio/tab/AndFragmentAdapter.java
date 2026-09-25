package com.github.tand0.shogio.tab;


import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.github.tand0.shogio.R;

/**
 * フラグメントアダプタ
 */
public class AndFragmentAdapter extends FragmentStateAdapter {

    /**
     * コンストラクタ
     * @param fragmentManager fragment manager
     * @param lifecycle life cycle
     */
    public AndFragmentAdapter(FragmentManager fragmentManager, Lifecycle lifecycle) {
        super(fragmentManager, lifecycle);
    }
    /**
     * create fragment
     * @param position fragment position
     * @return fragment
     */
    @NonNull
    @Override
    public Fragment createFragment(int position) {
        if (position == 0) {
            return new AndLogFragment();
        } else if (position == 1) {
            return new AndSettingFragment();
        } else if (position == 2) {
            return new AndTurnFragment();
        } else if (position == 3) {
            return new AndButtonFragment();
        } else if (position == 4) {
            return new AndEvalFragment();
        }
        throw new java.lang.UnsupportedOperationException("createFragment");
    }

    /** タブに出力する文字のID
     *
     * @param position fragment position
     * @return string id
     */
    public int getText(int position) {
        if (position == 0) {
            return R.string.tab_log;
        } else if (position == 1) {
            return R.string.setting_login;
        } else if (position == 2) {
            return R.string.setting_turn;
        } else if (position == 3) {
            return R.string.select;
        } else {
            return R.string.eval;
        }
    }
    /**
     * get item count
     * @return item count
     */
    @Override
    public int getItemCount() {
        return 5;
    }

}
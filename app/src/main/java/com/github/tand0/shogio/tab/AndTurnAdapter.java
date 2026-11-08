package com.github.tand0.shogio.tab;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.github.tand0.shogio.R;
import com.github.tand0.shogio.engin.MessageConnectCloseRequest;
import com.github.tand0.shogio.engin.SendListener;
import com.github.tand0.shogio.util.PnDnTable;
import com.github.tand0.shogio.util.TeTable;

import java.util.ArrayList;
import java.util.List;

/**
 * 棋譜を表示用のアダプタ
 */
public class AndTurnAdapter extends ArrayAdapter<TeTable> {
    /** リソース ID */
    private final int mResource;

    /** リストビューの要素 */
    private final List<TeTable> tops = new ArrayList<>();

    /** GUI部品のレイアウト情報 */
    private final LayoutInflater mInflater;

    /** ログなどの出力用 */
    private SendListener forGui;

    /** 親のリストビュー */
    private ListView listView;

    /**
     * コンストラクタ
     * @param context コンテキスト
     * @param resource リソース ID
     */
    public AndTurnAdapter(
            Context context, int resource) {
        super(context, resource);
        this.mResource = resource;
        //
        this.mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        View view;
        //
        if (convertView != null) {
            view = convertView;
        } else {
            view = mInflater.inflate(mResource, null);
        }
        //
        // リストビューに表示する要素を取得
        final TeTable item = ((0 <= position) && (position < this.getCount()))
                ? this.getItem(position) : null;
        if (item == null) {
            return view;
        }
        //
        TextView folderView = view.findViewById(R.id.cheese_folder);
        folderView.setText("-");
        TextView fileView = view.findViewById(R.id.cheese_text);
        if (fileView != null) {
            fileView.setText(item.display());
            boolean expand = (item instanceof PnDnTable e) && (! e.isExpand());
            int id;
            if (expand) {
                id = R.drawable.box_folder_open;
            } else {
                id = R.drawable.box_folder_close;
            }
            fileView.setCompoundDrawablesWithIntrinsicBounds(id, 0, 0, 0);
            fileView.setOnClickListener(
                    (v) -> {
                        //
                        try {
                            // 指していたら止める
                            forGui.send(new MessageConnectCloseRequest());
                            forGui.send(item); // 画面に表示する
                        } catch (InterruptedException e) {
                            // EMPTY
                        }
                    });
        }
        //
        //
        return view;
    }

    /**
     * 行のまとめて更新
     * @param items リストビューの要素
     * @param forGui ログなどの出力用
     * @param listView 表示用のリストビュー
     */
    public void update(List<? extends TeTable> items, SendListener forGui, ListView listView) {
        this.listView = listView;
        this.forGui = forGui;
        this.tops.clear();
        this.tops.addAll(items);
        //
        updateTree();
    }
    /**
     * update Tree
     */
    public void updateTree() {
        if (this.listView == null) {
            return;
        }
        int position = listView.getSelectedItemPosition();  // 位置確認
        this.clear();  // データのクリア
        this.addAll(this.tops);  // ここで追加する
        this.notifyDataSetChanged();  // 状態変更通知
        int max = listView.getChildCount();
        if (0 <= position) {
            // 位置合わせ
            position = Math.min(position, max - 1);
            listView.setSelection(position);
        }
    }
}

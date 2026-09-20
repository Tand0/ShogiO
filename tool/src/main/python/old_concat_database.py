import tensorflow as tf
from pathlib import Path
from prop import to_base_pass


def main(dataset_dir, concat_name):
    print(f"dataset_dir={dataset_dir}")
    print(f"concat_name={concat_name}")
    if (dataset_dir is None):
        print("dataset_dir name is None")
        return
    if (concat_name is None):
        print("concat_name name is None")
        return
    #
    dataset = None
    for path in Path(dataset_dir).iterdir():
        if not path.is_dir():
            # ディレクトリでなければ除外する
            # print(f"file={path.name}")
            continue
        if not path.name.startswith("dataset"):
            # 先頭がdatasetで始まるファイルでなければ無視する
            # print(f"skip={path.name}")
            continue
        #
        #
        # データセットのロード
        name = f"{dataset_dir}/{path.name}"
        print(f"base dataset={name}")
        dataset_new = tf.data.Dataset.load(
                name,
                compression="GZIP"  # 必ず保存時と同じ形式を指定
            )
        if dataset is None:
            dataset = dataset_new
        else:
            dataset = dataset.concatenate(dataset_new)
    #
    # データの形状（Shape）を確認
    for x, y in dataset.take(1):
        print("入力データの形状 (バッチサイズ, 次元1, 次元2, 次元3):", x.shape)
        print("教師信号の形状 (バッチサイズ, 次元1):", y.shape)
    #
    print(f"Dataset を GZIP 圧縮して {concat_name} に保存開始します。")
    tf.data.Dataset.save(dataset, concat_name, compression="GZIP")
    print(f"Dataset を GZIP 圧縮して {concat_name} に保存しました。")
    #


if __name__ == "__main__":
    dataset_dir = to_base_pass("app.db.eval")
    concat_name = f"{dataset_dir}/concat_database"
    main(dataset_dir, concat_name)
#

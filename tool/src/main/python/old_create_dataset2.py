import numpy as np
import os
import tensorflow as tf
from tqdm import tqdm
from prop import get_prop
from prop import to_dataset_pass
from prop import to_bin_pass
import re
import random
import zipfile


def zip_list_generator(channel_max):
    """zipファイルの一覧を返すジェネレータ"""
    #
    # ベースのフォルダを決める
    app_name = "app.db.total"
    #
    if isinstance(app_name, bytes):
        app_name = app_name.decode('utf-8')
    #
    bin_name = to_bin_pass(app_name)
    #
    file_keys = {}
    for f in os.listdir(bin_name):
        match = re.search("inp_(\\d+.zip)", f)
        if match:
            inp_name = f"{bin_name}/inp_{match.group(1)}"
            out_name = f"{bin_name}/out_{match.group(1)}"
            file_keys[inp_name] = out_name
    #
    # zip の順番をシャッフル
    keys = list(file_keys.items())
    random.shuffle(keys)
    #
    pbar = tqdm(keys, desc="Reading")
    for inp_name, out_name in pbar:
        pbar.set_description(f"{inp_name}")
        #
        with zipfile.ZipFile(inp_name, 'r') as f:
            b_data = f.read('raw_data.bin')
            data_inp = np.frombuffer(b_data, dtype=np.float32)
            data_inp = data_inp.reshape(-1, 9, 9, channel_max)

        with zipfile.ZipFile(out_name, 'r') as f:
            b_data = f.read('raw_data.bin')
            label = np.frombuffer(b_data, dtype=np.float32)
            label = label.reshape(-1, 1)
        #
        # zip 内シャッフル
        n = data_inp.shape[0]
        idx = np.arange(n)
        np.random.shuffle(idx)
        #
        data_inp = data_inp[idx]
        label = label[idx]
        #
        for i in range(n):
            yield data_inp[i], label[i]


def create_database(dataset_name):
    """ dataset を 順番に生成する """
    #
    # channel_max 値を得る
    channel_max = int(get_prop("app.channel.max"))
    print(f"main channel_max={channel_max}")
    #
    # 名前単位の tf.data.Dataset の作成
    dataset = tf.data.Dataset.from_generator(
        zip_list_generator,
        args=[channel_max],
        output_signature=(
            tf.TensorSpec(shape=(9, 9, channel_max), dtype=tf.float32),
            tf.TensorSpec(shape=(1,), dtype=tf.float32)
        )
    )
    #
    # データの形状（Shape）を確認
    for x, y in dataset.take(1):
        print("入力データの形状 (バッチサイズ, 次元1, 次元2, 次元3):", x.shape)
        print("教師信号の形状 (バッチサイズ, 次元1):", y.shape)
    #
    print(f"Dataset を GZIP 圧縮して {dataset_name} に保存開始します。")
    tf.data.Dataset.save(dataset, dataset_name, compression="GZIP")
    print(f"Dataset を GZIP 圧縮して {dataset_name} に保存しました。")
    #
    return dataset


if __name__ == "__main__":
    dataset_name = to_dataset_pass("app.db.total")
    create_database(dataset_name)

#

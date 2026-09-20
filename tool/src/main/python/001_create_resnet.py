import json
import numpy as np
import os
import pathlib
from prop import get_prop
from prop import to_dataset_pass
from prop import to_base_pass
from prop import to_bin_pass
import re
import random
import tensorflow as tf
from tensorflow.keras import layers
from tensorflow.keras import models
import time
from tqdm import tqdm
import zipfile

def zip_list_generator(app_name: str, channel_max: int):
    """zipファイルの一覧を返すジェネレータ"""
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


def create_database(app_name: str):
    """  dataset が存在しない場合は作成して保存する  """
    #
    # channel max 値を得る
    channel_max = int(get_prop("app.channel.max"))
    print(f"main channel_max={channel_max}")
    #
    # 名前単位の tf.data.Dataset の作成
    dataset = tf.data.Dataset.from_generator(
        zip_list_generator,
        args=[app_name, channel_max],
        output_signature=(
            tf.TensorSpec(shape=(9, 9, channel_max), dtype=tf.float32),
            tf.TensorSpec(shape=(1,), dtype=tf.float32)
        )
    )
    #
    # データの形状（Shape）を確認
    for x, y in dataset.take(1):
        print("output の形状 (バッチサイズ, 次元1, 次元2, 次元3):", x.shape)
        print("output のdtype", x.dtype)
        print("label の形状 (バッチサイズ, 次元1):", y.shape)
        print("label のdtype", y.dtype)
    #
    dataset_name = to_dataset_pass(app_name)
    print(f"save {dataset_name} start")
    tf.data.Dataset.save(dataset, dataset_name, compression="GZIP")
    print(f"save {dataset_name} end")
    #


def load_database(dataset_name: str) -> tf.data.Dataset:
    """ dataset がすでに存在する場合はロードする """
    #
    # channel max値を得る
    channel_max = int(get_prop("app.channel.max"))
    print(f"main channel_max={channel_max}")
    #
    # データセットのロード
    print(f"load dataset={dataset_name} start")
    dataset = tf.data.Dataset.load(
            dataset_name,
            compression="GZIP",  # 必ず保存時と同じ形式を指定
            element_spec=(
                tf.TensorSpec(shape=(9, 9, channel_max), dtype=tf.float32),
                tf.TensorSpec(shape=(1,), dtype=tf.float32)))
    print(f"load dataset={dataset_name} end")
    return dataset


class TimeBasedCheckpoint(tf.keras.callbacks.Callback):
    """ 特定時間が経過したときに保存をする """
    def __init__(self, save_dir, interval_minutes=120):
        """ コンストラクタ """
        super().__init__()
        self.save_dir = save_dir
        self.interval_seconds = interval_minutes * 60
        self.last_saved_time = None
        #
        # 保存先ディレクトリの作成
        os.makedirs(save_dir, exist_ok=True)

    def on_train_begin(self, logs=None):
        """ トレーニングの開始 """
        # 訓練開始時を最初の基準時間にする
        self.last_saved_time = time.time()

    def on_epoch_end(self, epoch, logs=None):
        """ epoch の最後 """
        current_time = time.time()
        elapsed_since_last_save = current_time - self.last_saved_time

        # 設定した時間（秒）が経過しているかチェック
        if elapsed_since_last_save >= self.interval_seconds:
            # 経過分数を計算
            total_elapsed_minutes = int((current_time - self.last_saved_time) / 60)
            #
            # ファイル名にエポック数を含めて保存
            filepath = os.path.join(
                self.save_dir,
                f"model_time_checkpoint_epoch_{epoch + 1:02d}.keras")
            #
            # model の保存を実行
            self.model.save(filepath)
            print(f"\n[時間経過保存] 約 {total_elapsed_minutes} 分が経過した: {filepath}")
            #
            # 基準時間を更新
            self.last_saved_time = current_time


def residual_block(
        x: tf.keras.layers.Layer,
        filters: int,
        kernel_size: tuple[int, int]) -> tf.keras.layers.Layer:
    """基本となる残差ブロック (サイズを変更しない)"""
    # 1つ目の畳み込み
    y = layers.Conv2D(
        # フィルタ大きさ
        filters,
        # カーネルのサイズ。３マスづつの塊で評価する
        kernel_size,
        # 画像の周囲に 0 （ゼロパディング）を追加し、
        # 入力画像と出力画像のサイズが同じになるように調整
        # validを使うとパディングを行わない（外枠を埋めない）ため、
        # 次のtf.keras.layersの入力サイズが小さくなります。
        padding='same',
        use_bias=False
    )(x)
    #
    # BatchNormalization
    y = layers.BatchNormalization()(y)
    #
    # 活性化関数の意味
    #   'sigmoid': 値を0.0～1.0に強制する
    #   'gelu' : 近年のLLM等で主流の、非常に滑らかな関数です。
    #            特徴: x が負の領域でもわずかに負の値を返し、
    #            0付近の変化が非常に滑らかです。
    #   'softsign': sigmoid に最も形が近く、より緩やかに変化します。
    #   'swish' (または 'silu'): Googleが発見した、
    #            シグモイド派生の滑らかな関数です。
    #            特徴: 0付近での変化がシグモイドよりも緩やかで、
    #            勾配消失が起きにくいです。
    y = layers.Activation('swish')(y)
    #
    # 2つ目の畳み込み
    y = layers.Conv2D(
        # フィルタ大きさ
        filters,
        # カーネルのサイズ。３マスづつの塊で評価する
        kernel_size,
        # False: バイアス項を追加しない(対BatchNormalization対策)
        padding='same',
        use_bias=False
    )(y)
    #
    # BatchNormalization
    y = layers.BatchNormalization()(y)
    #
    # スキップ接続（入力を足し合わせる）
    out = layers.add([x, y])
    #
    # 活性化関数
    out = layers.Activation('swish')(out)
    return out


def create_custom_resnet(
        input_shape: tuple[int, int, int],
        num_blocks: int,
        filters: int,
        kernel_size: tuple[int, int],
        num_classes: int) -> models.Model:
    """(9, 9, channel_max)の入力を受け取るカスタムResNet"""
    #
    # channel max値を得る
    channel_max = int(get_prop("app.channel.max"))
    print(f"main channel_max={channel_max}")
    #
    # 入力層
    inputs = layers.Input(shape=input_shape, dtype=tf.float32)
    #
    # Output Shapeを(None,9,9,15)から(None,9,9,filters)にする
    x = layers.Conv2D(
        filters,
        kernel_size=(1, 1),
        strides=(1, 1),
        padding='same',
        use_bias=False
    )(inputs)
    #
    # ショートカット側に Conv2D を入れた場合は、
    # その直後に必ず BatchNormalization を配置するのが鉄則らしい
    x = layers.BatchNormalization()(x)
    #
    # 残差ブロックを積み重ねる
    for _ in range(num_blocks):
        x = residual_block(x, filters, kernel_size)
    #
    # 過学習を防ぐため適度に Dropout する
    x = layers.Dropout(0.15)(x)
    #
    # 出力層（タスクに合わせて平坦化）
    x = layers.GlobalAveragePooling2D()(x)
    # sigmoid で結果を 0.0～1.0 にする
    x = layers.Dense(
        num_classes,
        activation='sigmoid',
        kernel_initializer='glorot_uniform')(x)
    #
    # model の定義
    model = models.Model(inputs=inputs, outputs=x)
    return model


def main():
    """ メイン処理 """
    #
    # ベースのフォルダを決める
    dataset_estimate_app_name = "app.db.estimate"
    dataset_app_name = "app.db.total"
    #
    channel_max = int(get_prop("app.channel.max"))
    base_dir = to_base_pass("app.db.eval")
    model_name = f"{base_dir}/model.keras"
    check_point = f"{base_dir}/time_checkpoints"
    android_model_name = f"{base_dir}/model.tflite"
    history_name = f"{base_dir}/history.json"
    #
    if not pathlib.Path(to_dataset_pass(dataset_app_name)).exists():
        # データセットの生成
        create_database(dataset_app_name)
    #
    # データセットのロード
    # ⇒ 一旦セーブしたものを使うことでメモリの大量消費を防ぐ
    loaded_dataset = load_database(to_dataset_pass(dataset_app_name))
    #
    print(f"読み込んだdatasetの型:{loaded_dataset.element_spec}")
    #
    # データの個数の取得
    size = loaded_dataset.cardinality().numpy()
    val_size = int(0.05 * size)  # 検証用の割合を設定
    if val_size == 0:
        val_size = 1
    print(f"データ件数:{size} val_size={val_size}")
    #
    # take と skip で分割
    val_dataset = loaded_dataset.take(val_size)
    train_dataset = loaded_dataset.skip(val_size)
    #
    # BATCH_SIZE は元が1024単位でデータを構成しているので
    # 1024の倍数は避けてシャッフル率を上げる
    BATCH_SIZE  = 1500
    #
    # BUFFER_SIZE は BATCH_SIZE より
    # 大きくとらないとシャッフルされない
    # ※ sql での出力時に
    # ※ ORDER BY RANDOM()でシャッフルしているから
    # ※ これ以上いらなくない？  ということで消した
    # BUFFER_SIZE = 100000
    #
    # shuffle ＆ AUTOTUNE にする
    # val_dataset = val_dataset.shuffle(BUFFER_SIZE)
    val_dataset = val_dataset.batch(BATCH_SIZE)
    val_dataset = val_dataset.prefetch(tf.data.AUTOTUNE)
    #
    # shuffle ＆ AUTOTUNE にする
    # train_dataset = train_dataset.shuffle(BUFFER_SIZE)
    train_dataset = train_dataset.batch(BATCH_SIZE)
    train_dataset = train_dataset.prefetch(tf.data.AUTOTUNE)
    #
    print(f"変化したdatasetの型:{train_dataset.element_spec}")
    #
    # データが正しいか確認する
    for x_batch, y_batch in train_dataset:
        loop_pos = 0
        # バッチ内の各要素を1件ずつ表示
        for x, y in zip(x_batch, y_batch):
            print("teacher:", y.numpy().item())  # 正解ラベル
            print_batch_data(channel_max, x)     # 盤面
            print("")
            loop_pos = loop_pos + 1
            if (3 <= loop_pos):
                # 3件表示したら終了
                break
        # バッチ１個したら終了
        break
    #
    # 混合精度ポリシーを有効化
    #   TensorFlowでは、model を構築する前にグローバルなポリシー
    #   を設定するだけで、内部のレイヤーが自動的に mixed_float16
    #   で実行されるようになります。
    #   float16の場合 約6.10 × 10^-5
    #   float32の場合 約1.18 * 10^-38
    # ※ LiteRT は FP16 を嫌うため、以下は削除(float32にする)
    # policy = tf.keras.mixed_precision.Policy('mixed_float16')
    # tf.keras.mixed_precision.set_global_policy(policy)
    tf.keras.mixed_precision.set_global_policy('float32')
    #
    #
    model_3d = None
    if pathlib.Path(model_name).exists():
        #
        # model のファイルが存在するなら、それを使う
        model_3d = tf.keras.models.load_model(
            model_name, safe_mode=False)
    else:
        #
        # model の作成
        model_3d = create_custom_resnet(
            # 入力サイズ: [縦9, 横9, channel_max チャンネル]
            input_shape = (9, 9, channel_max),
            #
            # ブロック数: 5がデフォルト
            num_blocks = 34,
            #
            # 畳み込みフィルタサイズ: 32がデフォルト
            filters = 32,
            #
            # 残差ブロックの kernel_size
            kernel_size = (3, 3),
            #
            # 出力サイズ
            num_classes=1)
    #
    # 構造の確認
    model_3d.summary()
    #
    # x分に１回自動保存します
    interval_minutes = 60 * 3
    #
    # 最大繰り返し数
    epochs = 1000
    #
    # 変化がなくなったら精度を上げる
    learning_rate = 1e-3        # 学習率(def: 0.001)
    weight_decay = 1e-4         # 重み減衰
    learning_rate_last = 1e-6   # 学習率の最低値
    #
    # 最適化アルゴリズムの指定
    #    Adam: Adaptive Moment Estimation
    #    AdamW: + Weight Decay
    #    SGD: 確率的勾配降下法
    opt = tf.keras.optimizers.AdamW(
        learning_rate=learning_rate, # 学習率の指定
        weight_decay=weight_decay)   # 重み減衰
        # clipnorm=1.0) # グラディエント・クリッピング
    #
    # model の訓練方法の指定
    model_3d.compile(
        #
        # 最適化アルゴリズム
        optimizer=opt,
        #
        # 損失関数
        #   binary_crossentropy,
        #   categorical_crossentropy,
        #   accuracy: 正解率
        #   mae: 平均絶対誤差
        #   mse: 平均二乗誤差
        #   losses.Huber: 外れ値の影響を受けにくい、
        #     扱いやすい損失関数外れ値が多い場合
        #     delta=5〜10 が最も安定する
        loss=tf.keras.losses.Huber(delta=10.0),
        # 評価指標: 損失関数と同じ
        metrics=['mse', 'mae']
    )
    #
    # 時間による自動保存 コールバックの定義
    time_checkpoint_cb = TimeBasedCheckpoint(
        save_dir=check_point,    # チェックポイントのセーブ先
        interval_minutes=interval_minutes  # x分に１回自動保存します
    )
    #
    # 学習しなくなったら止めるコールバックの定義
    early_stopping = tf.keras.callbacks.EarlyStopping(
        monitor='val_loss',        # 監視する指標(検証データの損失)
        patience=2,                # 変化がなくなってから何エポック待つか
        mode='min',                # 損失なので、値が「減少」しなくなった時を検知
        verbose=1,                 # 早期終了したタイミングでのみメッセージ表示
        restore_best_weights=False # True なら最も成績が良かった時のみ反映
    )
    #
    # 学習しなくなったら学習率を下げるコールバック定義
    reduce_lr = tf.keras.callbacks.ReduceLROnPlateau(
        monitor='val_loss', # 監視する評価値
        factor=0.7,     # 学習率を掛ける値。0.5で半減、0.1で1割
        patience=1,     # 変化がなくなってから何エポック待つか
        verbose=1,      # 学習しなくなったタイミングでのみメッセージ表示
        min_lr=learning_rate_last # 学習率の最小値
    )

    # モデルが存在 する 場合でかつandroid用のmodelが存在 する 場合 ⇒ 学習しない
    # モデルが存在しない場合でかつandroid用のmodelが存在 する 場合 ⇒ 学習
    # モデルが存在 する 場合でかつandroid用のmodelが存在しない場合 ⇒ 学習
    # モデルが存在しない場合でかつandroid用のmodelが存在しない場合 ⇒ 学習
    if not pathlib.Path(model_name).exists() or not pathlib.Path(android_model_name).exists():
        #
        # model の訓練
        history = model_3d.fit(
            train_dataset,  # 学習用データ
            validation_data=val_dataset,  # 検証用データ
            epochs=epochs,    # 最大繰り返し数
            callbacks=[
                time_checkpoint_cb, # 時間による自動保存
                early_stopping,     # 学習しなくなったら止める
                reduce_lr])         # 学習が進まないなときに学習率を下げる
        #
        # 実行結果の保存
        with open(history_name, 'w') as f:
            json.dump(history.history, f)
        print(f"history を {history_name} に保存しました。")
        #
        # 保存
        model_3d.save(model_name)
        print(f"model を {model_name} に保存しました。")
    #
    #
    # コンバーターの初期化（SavedModelやKeras model から）
    converter = tf.lite.TFLiteConverter.from_keras_model(model_3d)
    #
    # サポートする演算子のセットにSELECT_TF_OPSを追加。
    # TFLiteでサポートされていないTensorFlow演算子を
    # model に含めたい場合、変換設定に
    # tf.lite.OpsSet.SELECT_TF_OPS を追加します。
    # これにより、AndroidやiOSなどの環境で
    # TensorFlowの演算子を実行できるようになります
    # エラーが出ない限り変更しないこと。
    converter.target_spec.supported_ops = [
        tf.lite.OpsSet.TFLITE_BUILTINS
        # tf.lite.OpsSet.SELECT_TF_OPS  # エラーが出たら停止
    ]
    #
    # 量子化は使わない（float32 のまま）
    converter.optimizations = []
    #
    # FP16 を禁止（LiteRT は FP16 を嫌う）
    converter.target_spec.supported_types = [tf.float32]
    #
    # カスタムオプは使わない
    converter.allow_custom_ops = False
    #
    # 動的 shape を禁止
    converter.experimental_new_converter = True
    converter.experimental_enable_resource_variables = False
    #
    # 変換処理の本題
    tflite_model = converter.convert()
    #
    # android用に .tflite ファイルとして保存
    with open(android_model_name, 'wb') as f:
        f.write(tflite_model)
    print(f"Android用を {android_model_name} に保存")
    #
    #
    # シグネチャが付いているかを確認する
    interpreter = tf.lite.Interpreter(model_path=android_model_name)
    print("signatures を確認する / interpreter.get_signature_list()")
    print(interpreter.get_signature_list())
    print("入力 shape を確認する / interpreter.get_input_details()")
    print(interpreter.get_input_details())
    print("出力 shape を確認する / interpreter.get_output_details()")
    print(interpreter.get_output_details())
    #
    # 動作確認
    #
    if not pathlib.Path(to_dataset_pass(dataset_estimate_app_name)).exists():
        # 結果確認用のデータセットの生成
        create_database(dataset_estimate_app_name)
        #
    # 推論用のデータセットのロード
    loaded_dataset = load_database(to_dataset_pass(dataset_estimate_app_name))
    #
    # バッチ用に１次元追加する
    loaded_dataset = loaded_dataset.batch(BATCH_SIZE)
    #
    print("入力・教師信号・推論を交互に表示する")
    for x_batch, y_batch in loaded_dataset:
        # バッチごとに推論
        pred_batch = model_3d.predict(x_batch)
        # バッチ内の各要素を1件ずつ表示
        for x, y, p in zip(x_batch, y_batch, pred_batch):
            print("predict:", p.item())          # モデル出力
            print("teacher:", y.numpy().item())  # 正解ラベル
            print_batch_data(channel_max, x)     # 盤面
            print("")

def print_batch_data(channel_max, data_inp):
    for ch in range(channel_max):
        print(f"\tlayer={ch}")
        for y in range(9):
            print("\t\t", end="")
            for x in range(9):
                print(f" {int(data_inp[x, y, ch]):+1.0f}".replace("+", " "), end="")
            print()

if __name__ == "__main__":
    main()
#

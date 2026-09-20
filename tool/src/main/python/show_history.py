import json
import matplotlib.pyplot as plt
from prop import to_base_pass

def main():
    base_dir = to_base_pass("app.db.eval")
    history_name = f"{base_dir}/history.json"
    history_png = f"{base_dir}/history.png"
    lr = "learning_rate"
    #
    with open(history_name, 'r') as f: history_dict = json.load(f)
    #
    # グラフの描画
    #  dpi=100 の場合、figsize=(10, 5) は 1000ピクセル × 500ピクセル
    fig = plt.figure(dpi=100, figsize=(10, 5))
    if lr in history_dict:
        ax1 = fig.add_subplot(1, 1, 1)
    # 
    styles = ['bo-', 'ro-', 'co-', 'mo-', 'yo-', 'ko-', 'wo-']
    i = 0
    # 損失関数を表示
    for k,v in history_dict.items():
        if k == lr:
            continue
        epochs = range(1, len(v) + 1)
        plt.plot(epochs, v, styles[i], label=k)
        i = (i + 1) % len(styles)
    plt.legend()
    plt.xlabel('Epochs')
    plt.ylabel('Loss')


    # learning_rate があれば表示
    k = lr
    v = history_dict[k]
    if v is not None:
        # 2つ目のY軸を作成（X軸を共有し、目盛りを右側にする）
        ax2 = ax1.twinx()
        #
        epochs = range(1, len(v) + 1)
        ax2.plot(epochs, v, 'go-', label=k)
        i = (i + 1) % len(styles)
        ax2.legend()

    plt.savefig(history_png)

if __name__ == "__main__":
    main()
#

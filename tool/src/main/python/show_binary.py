import os
from prop import to_bin_pass
from prop import get_prop
from tqdm import tqdm
import re
import numpy as np
import zipfile


def main():
    prop_name = "app.db.estimate"
    bin_name = to_bin_pass(prop_name)
    print(f"bin_name ={bin_name}")
    channel_max = int(get_prop("app.channel.max"))
    print(f"channel_max={channel_max}")
    #
    for f in os.listdir(bin_name):
        match = re.search("inp_(\\d+.zip)", f)
        if match:
            inp_name = f"{bin_name}/inp_{match.group(1)}"
            out_name = f"{bin_name}/out_{match.group(1)}"
            print(f"inp_name={inp_name}")
            #
            data_inp = None
            with zipfile.ZipFile(inp_name, 'r') as f:
                b_data = f.read('raw_data.bin')
                b_data = np.frombuffer(b_data, dtype=np.float32)
                data_inp = b_data.reshape(-1, 9, 9, channel_max)
            #
            label = None
            with zipfile.ZipFile(out_name, 'r') as f:
                b_data = f.read('raw_data.bin')
                b_data = np.frombuffer(b_data, dtype=np.float32)
                label = b_data.reshape(-1,1)
            print()
            print("data_inp type")
            print(data_inp.dtype)
            print("label type")
            print(label.dtype)
            with tqdm(total=data_inp.shape[0], desc=inp_name) as pbar:
                for batch in range(data_inp.shape[0]):
                    #
                    # 経過表示の更新
                    pbar.update(1)
                    #
                    # １つづつ取り出す
                    out = label[batch]
                    print(f"\nout={out.item():0.2f}")
                    print_batch_data(channel_max, batch, data_inp)

def print_batch_data(channel_max, batch, data_inp):
    batch_data = data_inp[batch]
    for ch in range(channel_max):
        print(f"\tchannel={batch} layer={ch}")
        for y in range(9):
            print("\t\t", end="")
            for x in range(9):
                print(f" {int(batch_data[x, y, ch]):+1.0f}".replace("+", " "), end="")
            print()

if __name__ == "__main__":
    main()
#

import sys
import time
from tqdm import tqdm

# file=sys.stdout を指定し、動的な更新（\r）をAndroid Studioに認識させやすくする
for i in tqdm(range(100), file=sys.stdout, desc="Processing"):
    time.sleep(0.1)

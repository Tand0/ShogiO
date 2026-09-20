import os
# oneDNNのカスタム演算を無効化する
os.environ['TF_ENABLE_ONEDNN_OPTS'] = '0'


import tensorflow as tf
# print(f"cuda  version={tf.sysconfig.get_build_info()['cuda_version']}")
# print(f"cudnn version={tf.sysconfig.get_build_info()['cudnn_version']}")
print(f"tf    version={tf.__version__}")
print("Num GPUs Available: ", len(tf.config.list_physical_devices('GPU')))

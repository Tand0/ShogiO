from jproperties import Properties
from pathlib import Path
import re

CONFIG_FILE = "../../../../app/src/main/assets/config.properties"

configs = None

def get_prop(property_name):
    """ プロパティを取得する """
    global configs
    if configs is None:
        configs = Properties()
        with open(CONFIG_FILE, "rb") as config_file:
            configs.load(config_file)
    result = configs.get(property_name)
    if result is None:
        return None
    return configs.get(property_name).data


def to_wsl_path(property_name):
    """ プロパティを取得し d:// を /mnt/c/ に変換する """
    win_path = get_prop(property_name)
    # 最初にある「ドライブレター + : + スラッシュ/バックスラッシュ（1つ以上）」を「/mnt/小文字ドライブレター/」に置換
    linux_path = re.sub(r'^([a-zA-Z]):[/\\]+', lambda m: f"/mnt/{m.group(1).lower()}/", win_path)
    
    # 残りのバックスラッシュ（\）をすべてスラッシュ（/）に統一
    return linux_path.replace('\\', '/')



def to_bin_pass(property_name):
    """ DB用ファイル名をbin用のファイル名に変換する """
    parent = to_base_pass(property_name)
    return f"{parent}/bin_{Path(to_wsl_path(property_name)).stem}"

def to_dataset_pass(property_name):
    """ DB用ファイル名をdataset用のファイル名に変換する """
    parent = to_base_pass(property_name)
    return f"{parent}/dataset_{Path(to_wsl_path(property_name)).stem}"


def to_base_pass(property_name):
    """ ファイルから親のフォルダだけ取り出す """
    return f"{Path(to_wsl_path(property_name)).parent}"


if __name__ == "__main__":
    print(get_prop("app.db.all"))
    print(to_wsl_path("app.db.all"))
    print(to_dataset_pass("app.db.total"))
    print(to_base_pass("app.db.all"))
#
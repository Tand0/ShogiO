import sqlite3
from contextlib import closing
from prop import to_wsl_path


def main():
    file_name = to_wsl_path("app.db.estimate")
    print(f"file_name={file_name}")

    # データベースに接続
    try:
        with (sqlite3.connect(file_name) as conn,
              closing(conn.cursor()) as cursor):
            #
            batch = 1024 * 1024
            sum = 0
            #
            cursor.execute("SELECT key1,key2,key3,key4,win,los FROM mytable ORDER BY key1,key2,key3,key4")
            #
            while True:
                #
                # batch 件ずつデータを取得
                rows = cursor.fetchmany(batch)
                #
                # データがこれ以上なければループを終了
                if not rows:
                    break
                #
                # --- batch 件ずつのバッチ処理をここに記述 ---
                sum = sum + len(rows)
                print(f"--- 新しいバッチ処理を開始（件数: {sum}） ---")
                for key1, key2, key3, key4, win, los in rows:
                    print(f"{key1 & 0xffffffffffffffff:016x} {key2 & 0xffffffffffffffff:016x} {key3 & 0xffffffffffffffff:016x} {key4 & 0xffffffffffffffff:016x} win={win} los={los}")
            print(f"sum={sum}")

    except sqlite3.Error as e:
        print(f"データベースエラー: {e}")


if __name__ == "__main__":
    main()
#

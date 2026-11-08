package com.github.tand0.shogio.tool;


import java.io.File;
import java.io.IOException;

/** パイソンを実行する */
public class A007RunPython {
    /**
     * メイン処理
     * @param arg arg
     * @throws IOException IO Exception
     */
    public static void main(String[] arg) throws IOException {
        File file = new File(".").getAbsoluteFile();
        file = new File(file, "tool/src/main/python");
        System.out.println(file);
        // インスタンス化
        ProcessBuilder pb = new ProcessBuilder(
                //"cmd.exe", "/c", "start",
                // A007RunPython.class.getName(),  // window title
                "wsl.exe", "-e", "bash", "./run.sh");
        //
        // 標準エラー(stderr)を標準出力(stdout)に合流させる
        pb.redirectErrorStream(true);
        //
        // 標準出力(stdout)を出力する
        pb.inheritIO();
        // カレントフォルダを設定（Windows側のパスをFileオブジェクトで指定）
        pb.directory(file);
        try {
            // プロセスの開始
            Process process = pb.start();
            int exitCode = process.waitFor();
            Thread.sleep(1000);
            System.out.println("exit_code: " + exitCode);
        } catch (IOException | InterruptedException e) {
            System.out.println(e.getMessage());
        }
    }
}

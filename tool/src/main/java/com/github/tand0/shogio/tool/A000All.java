package com.github.tand0.shogio.tool;

import java.io.IOException;
import java.sql.SQLException;

/**
 * 全てをいっきにやる
 */
public class A000All {
    /**
     * メイン処理
     * @param arg arg
     * @throws SQLException SQL Exception
     * @throws IOException IO Exception
     */
    public static void main(String[] arg) throws SQLException,IOException {
        System.out.println(A001DBCreate.class.getName());
        A001DBCreate.main(arg);
        //
        System.out.println(A002DBSmall.class.getName());
        A002DBSmall.main(arg);
        //
        System.out.println(A003DBEval.class.getName());
        A003DBEval.main(arg);
        //
        System.out.println(A004DBBadMove.class.getName());
        A004DBBadMove.main(arg);
        //
        System.out.println(A005DBTotal.class.getName());
        A005DBTotal.main(arg);
        //
        System.out.println(A006DBWrite.class.getName());
        A006DBWrite.main(arg);
    }
}

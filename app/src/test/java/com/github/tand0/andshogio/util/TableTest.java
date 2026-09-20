package com.github.tand0.andshogio.util;

import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;


/**
 * 盤面のテスト
 */
public class TableTest {

    /** コンストラクタ */
    public TableTest() {
    }

    /** sfen のテスト
     * @throws IOException exception
     */
    @Test
    public void testRunFileDbStream() throws IOException {
        String baseString = """
sfen l4g1nl/1r1sg1k2/p3sp1p1/2p1p1p1p/1p1R5/2P1PP1PP/PPN3P2/1b2G1SK1/L4G1NL b BN2Ps 41
B*6f 5c4d 0 0 10
N*4e 8h7g+ 0 0 2
""";
        String fileName = "aaa." + TableDefine.EXTENSION_DB;
        boolean learn = false;
        int rate = 0;
        try (InputStream is = new ByteArrayInputStream(
                baseString.getBytes(StandardCharsets.UTF_8))) {
            List<EvalTeTable> eval = new ArrayList<>();
            TableDefine.runFile(fileName, is, eval, learn, rate);
            Assert.assertEquals(5, eval.size());
        }
    }
    /** 基本盤面の確認
     * ｘ軸が反転している場合は、飛車角が反転するので検知できる
     */
    @Test
    public void testP18() {
        String sfen = "sfen lnsgkgsnl/1r5b1/9/9/9/9/9/1B5R1/LNSGKGSNL b 18P";
        Table table = new Table(sfen); // sfenで初期盤面を再現
        Assert.assertEquals(18, table.getTegoma(TableDefine.pP, 0));
        Assert.assertEquals(0 , table.getTegoma(TableDefine.pP, 1));
        Assert.assertEquals(18, table.getTegomaNum(0));
        Assert.assertEquals(0 , table.getTegomaNum(1));
        //
        sfen = "sfen lnsgkgsnl/1r5b1/9/9/9/9/9/1B5R1/LNSGKGSNL b 18p";
        table = new Table(sfen); // sfenで初期盤面を再現
        Assert.assertEquals(0 , table.getTegoma(TableDefine.pP, 0));
        Assert.assertEquals(18, table.getTegoma(TableDefine.pP, 1));
        Assert.assertEquals(0 , table.getTegomaNum(0));
        Assert.assertEquals(18, table.getTegomaNum(1));
    }
    /** 基本盤面の確認
     * ｘ軸が反転している場合は、飛車角が反転するので検知できる
     */
    @Test
    public void baseTest() {
        String sfen = "sfen lnsgkgsnl/1r5b1/ppppppppp/9/9/9/PPPPPPPPP/1B5R1/LNSGKGSNL b -";
        Table sfenOnly = new Table(sfen); // sfenで初期盤面を再現
        Table baseOnly = new Table(null, 0); // 初期盤面
        Assert.assertEquals(sfenOnly,baseOnly); // 初期盤面が同じこと！
    }
    /** 持ちコマのチェック */
    @Test
    public void baseMochiTest() {
        String sfen = "sfen lnsgkgsnl/1r5b1/1pppppppp/9/9/9/PPPPPPPPP/1B5R1/LNSGKGSNL b P 1048";
        Table sfenOnly = new Table(sfen);
        Assert.assertEquals(0, sfenOnly.getTeban()); // 先手番
        Assert.assertEquals(1, sfenOnly.getTegoma(TableDefine.pP, 0)); // 先手歩１枚
        //
        sfen = "sfen 1nsgkgsnl/1r5b1/2ppppppp/9/9/9/PPPPPPPPP/1B5R1/LNSGKGSNL w 2PL";
        Assert.assertEquals(0, sfenOnly.getTeban()); // 後手番
        sfenOnly = new Table(sfen);
        Assert.assertEquals(2, sfenOnly.getTegoma(TableDefine.pP, 0)); // 先手歩2枚
        Assert.assertEquals(1, sfenOnly.getTegoma(TableDefine.pL, 0)); // 桂馬１枚
        //
    }
    
    /**エラーが出たパターン
     */
    @Test
    public void baseMyTest() {
        String sfen = "sfen lr3g1nl/3sg1kb1/p3sp1p1/2p1p1p1p/3S5/PBP2PP1P/1P2P2P1/L+p1RG1SK1/5G1NL b N2Pn 45";
        Table sfenOnly = new Table(sfen); // sfenで初期盤面を再現
        Assert.assertEquals(0, sfenOnly.getTeban());
        Assert.assertEquals(1, sfenOnly.getTegoma(TableDefine.pN, 0));
        Assert.assertEquals(2, sfenOnly.getTegoma(TableDefine.pP, 0));
    }
    
    /** 移動のテスト。飛車先の歩を前進させて２枚の歩を取る
      */
    @Test
    public void baseMove() {
        Table only = new Table(null, 0);
        int csa = TableDefine.changeTeStringToInt("+2726FU");
        int usi = only.changeUsiTeToInt("2g2f");
        Assert.assertEquals(csa,usi);
        only = new Table(only,csa);
        //
        csa =  TableDefine.changeTeStringToInt("-4132KI");
        usi = only.changeUsiTeToInt("4a3b"); // 1a 2b 3c 4d 5e 6f 7g 8h 9i
        Assert.assertEquals(csa,usi);
        only = new Table(only,csa);
        //
        csa =  TableDefine.changeTeStringToInt("+2625FU");
        usi = only.changeUsiTeToInt("2f2e"); // 1a 2b 3c 4d 5e 6f 7g 8h 9i
        Assert.assertEquals(csa,usi);
        only = new Table(only,csa);
        //
        csa =  TableDefine.changeTeStringToInt("-3242KI");
        usi = only.changeUsiTeToInt("3b4b"); // 1a 2b 3c 4d 5e 6f 7g 8h 9i
        Assert.assertEquals(csa,usi);
        only = new Table(only,csa);
        //
        csa =  TableDefine.changeTeStringToInt("+2524FU");
        usi = only.changeUsiTeToInt("2e2d"); // 1a 2b 3c 4d 5e 6f 7g 8h 9i
        Assert.assertEquals(csa,usi);
        only = new Table(only,csa);
        //
        csa =  TableDefine.changeTeStringToInt("-4232KI");
        usi = only.changeUsiTeToInt("4b3b"); // 1a 2b 3c 4d 5e 6f 7g 8h 9i
        Assert.assertEquals(csa,usi);
        only = new Table(only,csa);
        //
        csa =  TableDefine.changeTeStringToInt("+2423TO");
        usi = only.changeUsiTeToInt("2d2c+"); // 1a 2b 3c 4d 5e 6f 7g 8h 9i
        Assert.assertEquals(csa,usi);
        only = new Table(only,csa);
        //
        csa =  TableDefine.changeTeStringToInt("-3242KI");
        usi = only.changeUsiTeToInt("3b4b"); // 1a 2b 3c 4d 5e 6f 7g 8h 9i
        Assert.assertEquals(csa,usi);
        only = new Table(only,csa);
        //
        csa =  TableDefine.changeTeStringToInt("+2333TO");
        usi = only.changeUsiTeToInt("2c3c"); // 1a 2b 3c 4d 5e 6f 7g 8h 9i
        Assert.assertEquals(csa,usi);
        only = new Table(only,csa);
        //
        // 先手に歩が２枚持ちコマに入っていたらOK
        Assert.assertEquals(2, only.getTegoma(TableDefine.pP,0));
        //
    }
    /** 先手のコマの移動が正しいか確認する */
    @Test
    public void komaSenteTest() {
        //
        TableTest.find(TableDefine.pP, 5 - 1, 5 - 1,"+5554FU","+1112OU","+1121OU","+1122OU");
        TableTest.find(TableDefine.pP, 5 - 1, 4 - 1,"+5453TO","+1112OU","+1121OU","+1122OU");
        TableTest.find(TableDefine.pP, 5 - 1, 3 - 1,"+5352TO","+1112OU","+1121OU","+1122OU");
        TableTest.find(TableDefine.pP, 5 - 1, 2 - 1,"+5251TO","+1112OU","+1121OU","+1122OU");
        //
        TableTest.find(TableDefine.pL, 5 - 1, 2 - 1,"+5251NY","+1112OU","+1121OU","+1122OU");
        TableTest.find(TableDefine.pL, 5 - 1, 3 - 1,"+5351NY","+5352NY","+1112OU","+1121OU","+1122OU");
        TableTest.find(TableDefine.pL, 5 - 1, 4 - 1,"+5451NY","+5452NY","+5453KY","+5453NY","+1112OU","+1121OU","+1122OU");
        TableTest.find(TableDefine.pL, 5 - 1, 9 - 1,"+5951NY","+5952NY","+5953NY","+5953KY","+5954KY","+5955KY","+5956KY","+5957KY","+5958KY","+1112OU","+1121OU","+1122OU");
        //
        TableTest.find(TableDefine.pN, 5 - 1, 3 - 1,"+5341NK","+5361NK","+1112OU","+1121OU","+1122OU");
        TableTest.find(TableDefine.pN, 5 - 1, 4 - 1,"+5442NK","+5462NK","+1112OU","+1121OU","+1122OU");
        TableTest.find(TableDefine.pN, 5 - 1, 5 - 1,"+5543KE","+5543NK","+5563KE","+5563NK","+1112OU","+1121OU","+1122OU");
        TableTest.find(TableDefine.pN, 5 - 1, 6 - 1,"+5644KE","+5664KE","+1112OU","+1121OU","+1122OU");
        //
        TableTest.find(TableDefine.pS, 5 - 1, 2 - 1,"+5241GI","+5241NG","+5243GI","+5243NG","+5251GI","+5251NG","+5261GI","+5261NG","+5263GI","+5263NG","+1112OU","+1121OU","+1122OU");
        TableTest.find(TableDefine.pS, 5 - 1, 3 - 1,"+5342GI","+5342NG","+5344GI","+5344NG","+5352GI","+5352NG","+5362GI","+5362NG","+5364GI","+5364NG","+1112OU","+1121OU","+1122OU");
        TableTest.find(TableDefine.pS, 5 - 1, 4 - 1,"+5443GI","+5443NG","+5445GI","+5453GI","+5453NG","+5463GI","+5463NG","+5465GI","+1112OU","+1121OU","+1122OU");
        TableTest.find(TableDefine.pS, 5 - 1, 5 - 1,"+5544GI","+5546GI","+5554GI","+5564GI","+5566GI","+1112OU","+1121OU","+1122OU");
        //
        TableTest.find(TableDefine.pG, 5 - 1, 2 - 1,"+5241KI","+5242KI","+5251KI","+5253KI","+5261KI","+5262KI","+1112OU","+1121OU","+1122OU");
        TableTest.find(TableDefine.pG, 5 - 1, 3 - 1,"+5342KI","+5343KI","+5352KI","+5354KI","+5362KI","+5363KI","+1112OU","+1121OU","+1122OU");
        TableTest.find(TableDefine.pG, 5 - 1, 4 - 1,"+5443KI","+5444KI","+5453KI","+5455KI","+5463KI","+5464KI","+1112OU","+1121OU","+1122OU");
        TableTest.find(TableDefine.pG, 5 - 1, 5 - 1,"+5544KI","+5545KI","+5554KI","+5556KI","+5564KI","+5565KI","+1112OU","+1121OU","+1122OU");
        //
        TableTest.find(TableDefine.pB, 5 - 1, 2 - 1,"+5216UM","+5225UM","+5234UM","+5241UM","+5243UM","+5261UM","+5263UM","+5274UM","+5285UM","+5296UM","+1112OU","+1121OU","+1122OU");
        TableTest.find(TableDefine.pB, 5 - 1, 3 - 1,"+5317UM","+5326UM","+5331UM","+5335UM","+5342UM","+5344UM","+5362UM","+5364UM","+5371UM","+5375UM","+5386UM","+5397UM","+1112OU","+1121OU","+1122OU");
        TableTest.find(TableDefine.pB, 5 - 1, 4 - 1,"+5418KA","+5421UM","+5427KA","+5432UM","+5436KA","+5443UM","+5445KA","+5463UM","+5465KA","+5472UM","+5476KA","+5481UM","+5487KA","+5498KA","+1112OU","+1121OU","+1122OU");
        // TableTest.find(pB, 5 - 1, 5 - 1,"+5519KA","+5522UM","+5528KA","+5533UM","+5537KA","+5544KA","+5546KA","+5564KA","+5566KA","+5573UM","+5577KA","+5582UM","+5591UM","+5588KA","+1112OU","+1121OU","+1122OU");
        //
        TableTest.find(TableDefine.pR, 5 - 1, 2 - 1,"+5212RY","+5222RY","+5232RY","+5242RY","+5251RY","+5253RY","+5254RY","+5255RY","+5256RY","+5257RY","+5258RY","+5259RY","+5262RY","+5272RY","+5282RY","+5292RY","+1112OU","+1121OU","+1122OU");
        TableTest.find(TableDefine.pR, 5 - 1, 3 - 1,"+5313RY","+5323RY","+5333RY","+5343RY","+5351RY","+5352RY","+5354RY","+5355RY","+5356RY","+5357RY","+5358RY","+5359RY","+5363RY","+5373RY","+5383RY","+5393RY","+1112OU","+1121OU","+1122OU");
        TableTest.find(TableDefine.pR, 5 - 1, 4 - 1,"+5414HI","+5424HI","+5434HI","+5444HI","+5451RY","+5452RY","+5453RY","+5455HI","+5456HI","+5457HI","+5458HI","+5459HI","+5464HI","+5474HI","+5484HI","+5494HI","+1112OU","+1121OU","+1122OU");
        TableTest.find(TableDefine.pR, 5 - 1, 5 - 1,"+5515HI","+5525HI","+5535HI","+5545HI","+5551RY","+5552RY","+5553RY","+5554HI","+5556HI","+5557HI","+5558HI","+5559HI","+5565HI","+5575HI","+5585HI","+5595HI","+1112OU","+1121OU","+1122OU");
        //
        TableTest.find(TableDefine.pK, 5 - 1, 3 - 1,"+5342OU","+5343OU","+5344OU","+5352OU","+5354OU","+5362OU","+5363OU","+5364OU");
        //
        TableTest.find(TableDefine.ppP, 5 - 1, 3 - 1,"+5342TO","+5343TO","+5352TO","+5354TO","+5362TO","+5363TO","+1112OU","+1121OU","+1122OU");
        TableTest.find(TableDefine.ppN, 5 - 1, 3 - 1,"+5342NK","+5343NK","+5352NK","+5354NK","+5362NK","+5363NK","+1112OU","+1121OU","+1122OU");
        TableTest.find(TableDefine.ppL, 5 - 1, 3 - 1,"+5342NY","+5343NY","+5352NY","+5354NY","+5362NY","+5363NY","+1112OU","+1121OU","+1122OU");
        TableTest.find(TableDefine.ppS, 5 - 1, 3 - 1,"+5342NG","+5343NG","+5352NG","+5354NG","+5362NG","+5363NG","+1112OU","+1121OU","+1122OU");
        TableTest.find(TableDefine.ppB, 5 - 1, 3 - 1,"+5317UM","+5326UM","+5331UM","+5335UM","+5342UM","+5343UM","+5344UM","+5352UM","+5354UM","+5362UM","+5363UM","+5364UM","+5371UM","+5375UM","+5386UM","+5397UM","+1112OU","+1121OU","+1122OU");
        TableTest.find(TableDefine.ppR, 5 - 1, 3 - 1,"+5313RY","+5323RY","+5333RY","+5342RY","+5343RY","+5344RY","+5351RY","+5352RY","+5354RY","+5355RY","+5356RY","+5357RY","+5358RY","+5359RY","+5362RY","+5363RY","+5364RY","+5373RY","+5383RY","+5393RY","+1112OU","+1121OU","+1122OU");
    }

    /** 後手のコマの移動が正しいか確認する */
    @Test
    public void komaGoteTest() {
        //
        TableTest.find(TableDefine.pp, 5 - 1, 5 - 1,"-5556FU","-9989OU","-9988OU","-9998OU");
        TableTest.find(TableDefine.pp, 5 - 1, 6 - 1,"-5657TO","-9989OU","-9988OU","-9998OU");
        TableTest.find(TableDefine.pp, 5 - 1, 7 - 1,"-5758TO","-9989OU","-9988OU","-9998OU");
        TableTest.find(TableDefine.pp, 5 - 1, 8 - 1,"-5859TO","-9989OU","-9988OU","-9998OU");
        //
        TableTest.find(TableDefine.pl, 5 - 1, 7 - 1,"-5758NY","-5759NY","-9989OU","-9988OU","-9998OU");
        TableTest.find(TableDefine.pl, 5 - 1, 6 - 1,"-5657KY","-5657NY","-5658NY","-5659NY","-9989OU","-9988OU","-9998OU");
        TableTest.find(TableDefine.pl, 5 - 1, 5 - 1,"-5556KY","-5557KY","-5557NY","-5558NY","-5559NY","-9989OU","-9988OU","-9998OU");
        TableTest.find(TableDefine.pl, 5 - 1, 0    ,"-5152KY","-5153KY","-5154KY","-5155KY","-5156KY","-5157KY","-5157NY","-5158NY","-5159NY","-9989OU","-9988OU","-9998OU");
        //
        TableTest.find(TableDefine.pn, 5 - 1, 7 - 1,"-5749NK","-5769NK","-9989OU","-9988OU","-9998OU");
        TableTest.find(TableDefine.pn, 5 - 1, 6 - 1,"-5648NK","-5668NK","-9989OU","-9988OU","-9998OU");
        TableTest.find(TableDefine.pn, 5 - 1, 5 - 1,"-5547KE","-5547NK","-5567KE","-5567NK","-9989OU","-9988OU","-9998OU");
        TableTest.find(TableDefine.pn, 5 - 1, 4 - 1,"-5446KE","-5466KE","-9989OU","-9988OU","-9998OU");
        //
        TableTest.find(TableDefine.ps, 5 - 1, 8 - 1,"-5847GI","-5847NG","-5849GI","-5849NG","-5859GI","-5859NG","-5867GI","-5867NG","-5869GI","-5869NG","-9989OU","-9988OU","-9998OU");
        TableTest.find(TableDefine.ps, 5 - 1, 7 - 1,"-5746GI","-5746NG","-5748GI","-5748NG","-5758GI","-5758NG","-5766GI","-5766NG","-5768GI","-5768NG","-9989OU","-9988OU","-9998OU");
        TableTest.find(TableDefine.ps, 5 - 1, 6 - 1,"-5645GI","-5647GI","-5647NG","-5657GI","-5657NG","-5665GI","-5667GI","-5667NG","-9989OU","-9988OU","-9998OU");
        TableTest.find(TableDefine.ps, 5 - 1, 5 - 1,"-5544GI","-5546GI","-5556GI","-5564GI","-5566GI","-9989OU","-9988OU","-9998OU");
        //
        TableTest.find(TableDefine.pg, 5 - 1, 8 - 1,"-5848KI","-5849KI","-5857KI","-5859KI","-5868KI","-5869KI","-9989OU","-9988OU","-9998OU");
        TableTest.find(TableDefine.pg, 5 - 1, 5 - 1,"-5545KI","-5546KI","-5554KI","-5556KI","-5565KI","-5566KI","-9989OU","-9988OU","-9998OU");
        //
        TableTest.find(TableDefine.pb, 5 - 1, 8 - 1,"-5814UM","-5825UM","-5836UM","-5847UM","-5849UM","-5867UM","-5869UM","-5876UM","-5885UM","-5894UM","-9989OU","-9988OU","-9998OU");
        TableTest.find(TableDefine.pb, 5 - 1, 7 - 1,"-5713UM","-5724UM","-5735UM","-5739UM","-5746UM","-5748UM","-5766UM","-5768UM","-5775UM","-5779UM","-5784UM","-5793UM","-9989OU","-9988OU","-9998OU");
        TableTest.find(TableDefine.pb, 5 - 1, 6 - 1,"-5612KA","-5623KA","-5629UM","-5634KA","-5638UM","-5645KA","-5647UM","-5665KA","-5667UM","-5674KA","-5678UM","-5683KA","-5689UM","-5692KA","-9989OU","-9988OU","-9998OU");
        // TableTest.find(TableDefine.pb, 5 - 1, 5 - 1,"-5519UM","-5522KA","-5528UM","-5533KA","-5537UM","-5544KA","-5546KA","-5564KA","-5566KA","-5573KA","-5577UM","-5582KA","-5588UM","-5591KA","-9989OU","-9988OU","-9998OU");
        //
        TableTest.find(TableDefine.pr, 5 - 1, 8 - 1,"-5818RY","-5828RY","-5838RY","-5848RY","-5851RY","-5852RY","-5853RY","-5854RY","-5855RY","-5856RY","-5857RY","-5859RY","-5868RY","-5878RY","-5888RY","-5898RY","-9989OU","-9988OU","-9998OU");
        TableTest.find(TableDefine.pr, 5 - 1, 7 - 1,"-5717RY","-5727RY","-5737RY","-5747RY","-5751RY","-5752RY","-5753RY","-5754RY","-5755RY","-5756RY","-5758RY","-5759RY","-5767RY","-5777RY","-5787RY","-5797RY","-9989OU","-9988OU","-9998OU");
        TableTest.find(TableDefine.pr, 5 - 1, 6 - 1,"-5616HI","-5626HI","-5636HI","-5646HI","-5651HI","-5652HI","-5653HI","-5654HI","-5655HI","-5657RY","-5658RY","-5659RY","-5666HI","-5676HI","-5686HI","-5696HI","-9989OU","-9988OU","-9998OU");
        TableTest.find(TableDefine.pr, 5 - 1, 5 - 1,"-5515HI","-5525HI","-5535HI","-5545HI","-5551HI","-5552HI","-5553HI","-5554HI","-5556HI","-5557RY","-5558RY","-5559RY","-5565HI","-5575HI","-5585HI","-5595HI","-9989OU","-9988OU","-9998OU");
        //
        TableTest.find((byte)(TableDefine.NARI|TableDefine.pp), 5 - 1, 7 - 1,"-5747TO","-5748TO","-5756TO","-5758TO","-5767TO","-5768TO","-9989OU","-9988OU","-9998OU");
        TableTest.find((byte)(TableDefine.NARI|TableDefine.pn), 5 - 1, 7 - 1,"-5747NK","-5748NK","-5756NK","-5758NK","-5767NK","-5768NK","-9989OU","-9988OU","-9998OU");
        TableTest.find((byte)(TableDefine.NARI|TableDefine.pl), 5 - 1, 7 - 1,"-5747NY","-5748NY","-5756NY","-5758NY","-5767NY","-5768NY","-9989OU","-9988OU","-9998OU");
        TableTest.find((byte)(TableDefine.NARI|TableDefine.ps), 5 - 1, 7 - 1,"-5747NG","-5748NG","-5756NG","-5758NG","-5767NG","-5768NG","-9989OU","-9988OU","-9998OU");
        TableTest.find((byte)(TableDefine.NARI|TableDefine.pb), 5 - 1, 7 - 1,"-5713UM","-5724UM","-5735UM","-5739UM","-5746UM","-5747UM","-5748UM","-5756UM","-5758UM","-5766UM","-5767UM","-5768UM","-5775UM","-5779UM","-5784UM","-5793UM","-9989OU","-9988OU","-9998OU");
        TableTest.find((byte)(TableDefine.NARI|TableDefine.pr), 5 - 1, 7 - 1,"-5717RY","-5727RY","-5737RY","-5746RY","-5747RY","-5748RY","-5751RY","-5752RY","-5753RY","-5754RY","-5755RY","-5756RY","-5758RY","-5759RY","-5766RY","-5767RY","-5768RY","-5777RY","-5787RY","-5797RY","-9989OU","-9988OU","-9998OU");
    }

    /** 王が８方向に動くことができるか */
    @Test
    public void komaGotePkTest() {
        //
        TableTest.find(TableDefine.pK, 4 - 1, 7 - 1,"+4736OU","+4737OU","+4738OU","+4746OU","+4748OU","+4756OU","+4757OU","+4758OU");
        //
        TableTest.find(TableDefine.pK, 5 - 1, 7 - 1,"+5746OU","+5747OU","+5748OU","+5756OU","+5758OU","+5766OU","+5767OU","+5768OU");
        //
        TableTest.find(TableDefine.pk, 5 - 1, 7 - 1,"-5746OU","-5747OU","-5748OU","-5756OU","-5758OU","-5766OU","-5767OU","-5768OU");
    }


    /**
     *  該当の設定があるか確認する
     * @param koma コマ
     * @param x コマの x
     * @param y コマの y
     * @param teStrings 移動先のリスト
     */
    private static void find(byte koma,int x,int y,String... teStrings) {

        // 初期情報を作成する
        Table b = new Table(null, 0);
        System.out.println(b);
        b.clearForCSAProtocol();// 盤面を全消しする
        b.setTegoma(koma, 1, Math.max(0, b.getTegoma(koma, 1) - 1)); // 後手から１枚とって
        b.setKoma(koma, x, y); // 盤面に置く
        System.out.println(b);
        if ((koma & TableDefine.ENEMY) != 0) {
            b.setTeban(1);//後手
            // 先手にコマを寄せる
            b.setTegoma(TableDefine.pP, 0, b.getTegoma(TableDefine.pP,1));
            b.setTegoma(TableDefine.pL, 0, b.getTegoma(TableDefine.pL,1));
            b.setTegoma(TableDefine.pN, 0, b.getTegoma(TableDefine.pN,1));
            b.setTegoma(TableDefine.pS, 0, b.getTegoma(TableDefine.pS,1));
            b.setTegoma(TableDefine.pG, 0, b.getTegoma(TableDefine.pG,1));
            b.setTegoma(TableDefine.pB, 0, b.getTegoma(TableDefine.pB,1));
            b.setTegoma(TableDefine.pR, 0, b.getTegoma(TableDefine.pR,1));
            //
            b.setTegoma(TableDefine.pP, 1, 0);
            b.setTegoma(TableDefine.pL, 1, 0);
            b.setTegoma(TableDefine.pN, 1, 0);
            b.setTegoma(TableDefine.pS, 1, 0);
            b.setTegoma(TableDefine.pG, 1, 0);
            b.setTegoma(TableDefine.pB, 1, 0);
            b.setTegoma(TableDefine.pR, 1, 0);

        }
        b.endForCSAProtocol();// 王がいなかったら適当に置く
        System.out.println(b);
        //
        //
        List<TeTable> keyList = b.createChild();
        System.out.println(b);
        for (TeTable newKey : keyList) {
            int te = newKey.te;
            boolean flag = false;
            for (String teString : teStrings) {
                int target = TableDefine.changeTeStringToInt(teString);
                if (te == target) {
                    flag = true;
                    break;
                }
            }
            if (!flag) {
                String teString = TableDefine.changeTeIntToString(te);
                Assert.fail("NG key=" + TableDefine.getKomaToString(koma)
                                + " x=" + (x+1) + " y=" + (y+1)
                                + " te=" + teString);
            }
        }
        if (teStrings.length != keyList.size()) {
            Assert.fail("NG key=" + TableDefine.getKomaToString(koma)
                            + " x=" + (x+1) + " y=" + (y+1)
                            + " size=" + keyList.size() + " sum=" + teStrings.length);
        }
    }

    /** 初手で何を指せるかの確認 */
    @Test
    public void startTest() {

        for (int i = 0 ; i < 100 ; i++) {
            // 繰り返して問題ないか確認する(パラレル化対策)
            //
            Table topKey = new Table(null, 0);
            //
            List<TeTable> teKyeList = topKey.createChild();
            Assert.assertEquals(30, teKyeList.size()); // 30局面
        }
    }

    /** 文字の手から手に変更することのテスト */
    @Test
    public void changeTeStringToIntTest() {
        String moveString;
        int move;
        String result;
        //
        moveString = "+2315FU";
        move = TableDefine.changeTeStringToInt(moveString);
        System.out.printf("0x%08x%n", move);
        result = TableDefine.changeTeIntToString(move);
        Assert.assertEquals(moveString,result);
        //
        moveString = "-2345OU";
        move = TableDefine.changeTeStringToInt(moveString);
        System.out.printf("0x%08x%n", move);
        result = TableDefine.changeTeIntToString(move);
        Assert.assertEquals(moveString,result);
        //
        moveString = "-0067KI";
        move = TableDefine.changeTeStringToInt(moveString);
        System.out.printf("0x%08x%n", move);
        result = TableDefine.changeTeIntToString(move);
        Assert.assertEquals(moveString,result);
    }

    /** 子作りのテスト */
    @Test
    public void createNextListTest() {
        // 初期情報を作成する
        Table table = new Table(null, 0);
        System.out.println(table);
        //
        List<TeTable> child;
        child = table.createChild();
        Assert.assertEquals(30, child.size()); // 初期盤面の合法手
        //
        int te = TableDefine.changeTeStringToInt("+7776FU");
        Table next = new Table(table,te);
        System.out.println(next);
        child = next.createChild();
        Assert.assertEquals(30, child.size()); // 初期盤面の合法手
        //
        System.out.println("testCreateNextList end");
    }

    /**
     * 打ち歩詰めテスト
     * @throws IOException IO Exception
     */
    @Test
    public void fuMateTest() throws IOException {
        File dir = new File(".").getAbsoluteFile();
        dir = new File(dir, "src/test/assets/mate");
        File[] files = dir.listFiles();
        Assert.assertNotNull(files);
        for (File file : files) {
            List<EvalTeTable> eval = new ArrayList<>();
            try (FileInputStream fr = new FileInputStream(file)) {
                TableDefine.runFile(file.getAbsolutePath(), fr, eval, false, 0);
            }
            if (eval.isEmpty()) {
                Assert.fail("can not get table");
            }
            //
            Table table = eval.getFirst().table;
            //
            // 普通に詰んでいるかチェック
            List<TeTable> childTableList = table.createChild(); // 合法手の取得
            Assert.assertTrue(childTableList.isEmpty());
            //
            // 自分の王の位置を特定する
            int myOuX = table.getMyOuX();
            int myOuY = table.getMyOuY();
            //
            // 王手かどうかチェック
            Assert.assertTrue(table.checkSelfMate(table.getTeban(), myOuX, myOuY));
            //
            // 打ち歩詰めチェック
            Assert.assertTrue(table.checkFuMate());
        }
    }

    /**
     * 打ち歩詰めテスト
     * @throws IOException IO Exception
     */
    @Test
    public void fuNotMateTest() throws IOException {
        File dir = new File(".").getAbsoluteFile();
        dir = new File(dir, "src/test/assets/mate_not");
        File[] files = dir.listFiles();
        Assert.assertNotNull(files);
        for (File file : files) {
            List<EvalTeTable> eval = new ArrayList<>();
            try (FileInputStream fr = new FileInputStream(file)) {
                TableDefine.runFile(file.getAbsolutePath(), fr, eval, false, 0);
            }
            if (eval.isEmpty()) {
                Assert.fail("can not get table");
            }
            //
            // 普通に不詰みがチェックできるか？
            Table table = eval.getFirst().table;
            List<TeTable> childTableList = table.createChild(); // 合法手の取得
            Assert.assertFalse(childTableList.isEmpty());
            //
            //
            // 自分の王の位置を特定する
            int myOuX = table.getMyOuX();
            int myOuY = table.getMyOuY();
            //
            // 王手かどうかチェック
            Assert.assertTrue(table.checkSelfMate(table.getTeban(), myOuX, myOuY));
            //
            // 打ち歩詰めでないことチェック
            Assert.assertFalse(table.checkFuMate());
        }
    }

    /**
     * 反転させて問題ないか？
     */
    @Test
    public void flipTest() {
        Table table = new Table(null, 0);
        Assert.assertEquals(table, table.flippingVertical().flippingVertical());
        Assert.assertEquals(table, table.flippingHorizontal().flippingHorizontal());
        Assert.assertEquals(table, table
                .flippingVertical().flippingHorizontal()
                .flippingVertical().flippingHorizontal());
        //
        table = new Table(table,TableDefine.changeTeStringToInt("+7776FU"));
        Assert.assertEquals(table, table.flippingVertical().flippingVertical());
        Assert.assertEquals(table, table.flippingHorizontal().flippingHorizontal());
        Assert.assertEquals(table, table
                .flippingVertical().flippingHorizontal()
                .flippingVertical().flippingHorizontal());
        //
        table = new Table(table,TableDefine.changeTeStringToInt("-3334FU"));
        Assert.assertEquals(table, table.flippingVertical().flippingVertical());
        Assert.assertEquals(table, table.flippingHorizontal().flippingHorizontal());
        Assert.assertEquals(table, table
                .flippingVertical().flippingHorizontal()
                .flippingVertical().flippingHorizontal());
        //
        table = new Table(table,TableDefine.changeTeStringToInt("+8822KA"));
        Assert.assertEquals(table, table.flippingVertical().flippingVertical());
        Assert.assertEquals(table, table.flippingHorizontal().flippingHorizontal());
        Assert.assertEquals(table, table
                .flippingVertical().flippingHorizontal()
                .flippingVertical().flippingHorizontal());
    }

    /**
     * 手番の設定が反映されるか？
     */
    @Test
    public void testTebanSetAndGet() {
        Table table = new Table(null, 0);
        table.setTeban(0);
        Assert.assertEquals(0, table.getTeban());

        table.setTeban(1);
        Assert.assertEquals(1, table.getTeban());
    }

    /**
     * コマの設定が反映されるか？
     */
    @Test
    public void testSetKomaAndGetKoma() {
        Table table = new Table(null, 0);

        byte koma = TableDefine.pP; // 歩
        table.setKoma(koma, 3, 4);

        Assert.assertEquals(koma, table.getKoma(3, 4));
    }

    /**
     * クローンが生成できるか？
     */
    @Test
    public void testCloneCreatesIndependentCopy() {
        Table table = new Table(null, 0);
        table.setKoma(TableDefine.pP, 1, 1);

        Table cloned = table.clone();

        Assert.assertEquals(table, cloned);
        Assert.assertNotSame(table, cloned);

        // クローン側を変更しても元に影響しない
        cloned.setKoma(TableDefine.pNull, 1, 1);
        Assert.assertNotEquals(table, cloned);
    }

    /**
     * equals() のテスト
     */
    @Test
    public void testEquals() {
        Table t1 = new Table(null, 0);
        Table t2 = new Table(null, 0);

        Assert.assertEquals(t1, t2);

        t1.setKoma(TableDefine.pP, 2, 2);
        Assert.assertNotEquals(t1, t2);

        t2.setKoma(TableDefine.pP, 2, 2);
        Assert.assertEquals(t1, t2);
    }

    /**
     * sfen の動作確認
     */
    @Test
    public void testSfenConstructorBasic() {
        String sfen = "sfen lnsgk1snl/1r4gb1/p1ppppppp/9/1p5P1/2P6/PP1PPPP1P/1B5R1/LNSGKGSNL b - 1";
        Table table = new Table(sfen);

        // 先手番
        Assert.assertEquals(0, table.getTeban());

        // 王の位置（初期配置）
        Assert.assertEquals(4, table.getSenteOuX());
        Assert.assertEquals(8, table.getSenteOuY());
    }

    /** compareTo のテスト */
    @Test
    public void testCompareTo() {
        Table t1 = new Table(null, 0);
        Table t2 = new Table(null, 0);
        Assert.assertEquals(0,t1.compareTo(t2));
        //
        Table t0 = Table.createNullTable();
        Assert.assertEquals(-1, t0.compareTo(t2));
        Assert.assertEquals(1, t2.compareTo(t0));
    }

    /**
     * 入玉勝ちチェック
     */
    @Test
    public void testIsKingWin() {
        Table t1 = new Table(null, 0);
        Assert.assertEquals(0, t1.isKingWin());
    }

}

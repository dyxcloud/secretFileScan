import cnpat.filescan.Rule;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class RuleTest {

    Rule rule = new Rule();

    @Test
    public void tt(){
        /*Assertions.assertTrue(rule.match("机密"));
        Assertions.assertTrue(rule.match("绝密"));
        Assertions.assertTrue(rule.match("机 密"));
        Assertions.assertTrue(rule.match("qq机 密ww"));
        Assertions.assertTrue(rule.match("机\t密"));
        Assertions.assertFalse(rule.match("机..密"));*/
        //Assertions.assertTrue(rule.match("机密"));

        Assertions.assertTrue(rule.checkTest("机密★10年","").isRisk());
        Assertions.assertTrue(rule.checkTest("秘密★10年","").isRisk());
        Assertions.assertTrue(rule.checkTest("绝 密 ★  1年","").isRisk());
        Assertions.assertTrue(rule.checkTest("机 密 ★ 长 期fsdsfsd","").isRisk());
        Assertions.assertTrue(rule.checkTest("机 密 ★","").isRisk());
        Assertions.assertTrue(rule.checkTest("机 密 ★sdfs","").isRisk());
        Assertions.assertFalse(rule.checkTest("机。密").isRisk());
        Assertions.assertTrue(rule.checkTest("ssdf机 密sdfs","").isRisk());
        Assertions.assertFalse(rule.checkTest("中央文件","名称").isRisk());
        Assertions.assertTrue(rule.checkTest("中共中央文件","名称").isRisk());
        Assertions.assertTrue(rule.checkTest(" 中 共 中 央 文 件 ","名称").isRisk());
        Assertions.assertTrue(rule.checkTest("ss中 共 中 央 文 件 ","名称").isRisk());
        Assertions.assertTrue(rule.checkTest("中 共 中 央 文 件fdsjkjs ","名称").isRisk());
        Assertions.assertFalse(rule.checkTest("中共中央文s件").isRisk());
    }
}

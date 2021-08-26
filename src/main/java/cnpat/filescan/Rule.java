package cnpat.filescan;

import cn.hutool.core.util.StrUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Rule {

    final Logger log = LoggerFactory.getLogger(this.getClass());

    /*final String keyWord = "[秘机绝]\\s*密";
    Pattern pattern = Pattern.compile(keyWord);

    public boolean match(String text) {
        if (StrUtil.isBlank(text)) {
            return false;
        }
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            log.info("命中匹配:" + matcher.group());
            return true;
        }
        log.info("未命中：" + text);
        return false;
    }*/


    public CheckResult checkTest(String text) {
        return this.checkTest(text, CheckResult.CheckContentFileContent);
    }

    public CheckResult checkTest(String text, String checkContent) {
        return Rule.match(text, checkContent);
    }


    /*
     * 国家秘密的标志由三部分组成：1、国家秘密的密级：绝密、机密、秘密；2、国家秘密的标识“★”；3、保密期限：年、月，特殊情况也可以是长期。
     * 具体标志的形式为：从左至右：密级+标识★+保密期限。比如：“绝密★长期”的标志，表明该载体属于绝密级国家秘密，保密期限是长期。
     * 例如：“机密★10年”——表示该件为机密级，保密期限为十年；
     * 国家秘密的标志只有密级和标识符★，如“机密★”，不标明保密期限的，即按基本保密期限秘密级十年，机密级二十年，绝密级三十年认定
     */
    public static String CheckRuleSecretOne = "^[秘机绝]\\s*密\\s*★\\s*(([0-9]{0,1}\\s*[0-9]\\s*年)|(长\\s*期)|$)";
    public static String CheckRuleSecretTwo = "^[秘机绝]\\s*密";
    public static String CheckRuleSecretThree = "[秘机绝]\\s*密";
    /*
     * 包含【中共中央文件】文字
     */
    public static String CheckRuleZhongYangOne = "^\\s*中\\s*共\\s*中\\s*央\\s*文\\s*件\\s*$";
    public static String CheckRuleZhongYangTwo = "\\s*中\\s*共\\s*中\\s*央\\s*文\\s*件\\s*";
    static Pattern patternSecretOne = Pattern.compile(CheckRuleSecretOne);
    static Pattern patternSecretTwo = Pattern.compile(CheckRuleSecretTwo);
    static Pattern patternSecretThree = Pattern.compile(CheckRuleSecretThree);
    static Pattern patternZhongYangOne = Pattern.compile(CheckRuleZhongYangOne);
    static Pattern patternZhongYangTwo = Pattern.compile(CheckRuleZhongYangTwo);

    /**
     * @param text
     * @param checkContent
     * @return
     */
    public static CheckResult match(String text, String checkContent) {
        CheckResult result = new CheckResult();
        Matcher matcher = null;
        result.setCheckContent(checkContent);
        result.setResult(CheckResult.Unmacth);

        if (StrUtil.isBlank(text)) {
            return result;
        }

        // 包含【中共中央文件】 1、单独成行-error
        matcher = patternZhongYangOne.matcher(text);
        if (matcher.find()) {
            result.setResult(CheckResult.Error);
            result.setCheckPoint(CheckResult.CheckPointZhongYang);
            result.setCheckMessage(matcher.group());
            return result;
        }
        // 机密★10年
        matcher = patternSecretOne.matcher(text);
        if (matcher.find()) {
            result.setResult(CheckResult.Error);
            result.setCheckPoint(CheckResult.CheckPointSecret);
            result.setCheckMessage(matcher.group());
            return result;
        }

        // 包含【中共中央文件】 2、非单独成行-warn
        matcher = patternZhongYangTwo.matcher(text);
        if (matcher.find()) {
            result.setResult(CheckResult.Warn);
            result.setCheckPoint(CheckResult.CheckPointZhongYang);
            result.setCheckMessage(matcher.group());
            return result;
        }

        // 包含【绝密、机密、秘密】文字： 1、句首出现提示warn；  
        matcher = patternSecretTwo.matcher(text);
        if (matcher.find()) { // 以秘密开头
            result.setResult(CheckResult.Warn);
            result.setCheckPoint(CheckResult.CheckPointSecret);
            result.setCheckMessage(matcher.group());
            return result;
        }

        // 包含【绝密、机密、秘密】文字：\  2、在句中出现提示info
        matcher = patternSecretThree.matcher(text);
        if (matcher.find()) {
            result.setResult(CheckResult.Info);
            result.setCheckPoint(CheckResult.CheckPointSecret);
            result.setCheckMessage(matcher.group());
            return result;
        }
        // 未找到
        return result;
    }

}

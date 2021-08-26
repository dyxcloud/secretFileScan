package cnpat.filescan;

public class CheckResult {
	public static Integer Ignore=5;
	public static Integer Info=10;
    public static Integer Warn = 20;
    public static Integer Error = 30;
    public static Integer Unmacth = 0;
    public static String CheckPointZhongYang="中央文件";
    public static String CheckPointSecret="国家秘密";
    public static String CheckContentFileName="文件名称";
    public static String CheckContentFileContent="文件内容";
    /**
     * 匹配结果，匹配成功>0, 越重要值越大
     */
    private Integer result;
    /**
     * 检查点：中共中央文件 or 国家秘密
     */
    private String checkPoint;
    /**
     * 匹配的内容
     */
    private String checkMessage;
    /**
     * 检查的内容：文件名 or 文件内容
     */
    private String checkContent;
	
    public Integer getResult() {
		return result;
	}
	public void setResult(Integer result) {
		this.result = result;
	}
	public String getCheckPoint() {
		return checkPoint;
	}
	public void setCheckPoint(String checkPoint) {
		this.checkPoint = checkPoint;
	}
	public String getCheckMessage() {
		return checkMessage;
	}
	public void setCheckMessage(String checkMessage) {
		this.checkMessage = checkMessage;
	}
	public String getCheckContent() {
		return checkContent;
	}
	public void setCheckContent(String checkContent) {
		this.checkContent = checkContent;
	}

	public CheckResult() {
	}

	public CheckResult(Integer result) {
		this.result = result;
	}

	public CheckResult(Integer result, String checkPoint, String checkMessage, String checkContent) {
		this.result = result;
		this.checkPoint = checkPoint;
		this.checkMessage = checkMessage;
		this.checkContent = checkContent;
	}

	@Override
	public String toString() {
		return "CheckResult [result=" + result + ", checkPoint=" + checkPoint 
				+ ", checkContent=" + checkContent + "]" + ", checkMessage=" + checkMessage;
	}

	public boolean isRisk() {
		Integer result = getResult();
		return result != null && result > 1;
	}
	
	public static final CheckResult PASS = new CheckResult(0);
	public static final CheckResult IGNORE = new CheckResult(Ignore,CheckPointSecret,CheckContentFileContent,"null");
}

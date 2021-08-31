## 涉密文件扫描工具

用于扫描计算机上可能的涉密文件(包含国家秘密信息), 并输出扫描结果



- jdk要求: java8
- mainClass: `cnpat.filescan.Program`
- 启动: `java.exe -Xms128m -Xmx1024m -jar filescan.jar`

**过滤关键字**:

- 秘密
- 机密
- 绝密
- 中共中央文件

**扫描范围:**

计算机本地磁盘

**校验范围:**

1. 所有文件的文件名
2. doc/docx/xls/xlsx/pdf文件的文本内容




## 问题记录
针对加密PDF解析, 必须添加相关lib
```xml
<dependency>
    <groupId>org.bouncycastle</groupId>
    <artifactId>bcmail-jdk15on</artifactId>
    <version>1.64</version>
</dependency>
<dependency>
    <groupId>org.bouncycastle</groupId>
    <artifactId>bcprov-jdk15on</artifactId>
    <version>1.64</version>
</dependency>
```
<details>
<summary>异常信息</summary>

```aidl
Exception in thread "main" java.lang.NoClassDefFoundError: org/bouncycastle/cms/CMSException
        at java.lang.Class.getDeclaredConstructors0(Native Method)
        at java.lang.Class.privateGetDeclaredConstructors(Class.java:2671)
        at java.lang.Class.getConstructor0(Class.java:3075)
        at java.lang.Class.getDeclaredConstructor(Class.java:2178)
        at org.apache.pdfbox.pdmodel.encryption.SecurityHandlerFactory.newSecurityHandler(SecurityHandlerFactory.java:132)
        at org.apache.pdfbox.pdmodel.encryption.SecurityHandlerFactory.newSecurityHandlerForFilter(SecurityHandlerFactory.java:116)
        at org.apache.pdfbox.pdmodel.encryption.PDEncryption.<init>(PDEncryption.java:97)
        at org.apache.pdfbox.pdfparser.COSParser.prepareDecryption(COSParser.java:2896)
        at org.apache.pdfbox.pdfparser.COSParser.retrieveTrailer(COSParser.java:285)
        at org.apache.pdfbox.pdfparser.PDFParser.initialParse(PDFParser.java:173)
        at org.apache.pdfbox.pdfparser.PDFParser.parse(PDFParser.java:226)
        at org.apache.pdfbox.pdmodel.PDDocument.load(PDDocument.java:1105)
        at org.apache.pdfbox.pdmodel.PDDocument.load(PDDocument.java:1088)
        at org.apache.pdfbox.pdmodel.PDDocument.load(PDDocument.java:1012)
        at cnpat.filescan.FileChecker.checkPDF(FileChecker.java:253)
        at cnpat.filescan.FileChecker.check(FileChecker.java:61)
        at cnpat.filescan.Program.main(Program.java:59)
Caused by: java.lang.ClassNotFoundException: org.bouncycastle.cms.CMSException
        at java.net.URLClassLoader.findClass(URLClassLoader.java:382)
        at java.lang.ClassLoader.loadClass(ClassLoader.java:418)
        at sun.misc.Launcher$AppClassLoader.loadClass(Launcher.java:352)
        at java.lang.ClassLoader.loadClass(ClassLoader.java:351)
        ... 17 more
```
</details>

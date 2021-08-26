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


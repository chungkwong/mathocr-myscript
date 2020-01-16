# Offline handwritten mathematical expression recognition via stroke extraction

The repository provide a proof-of-concept stroke extractor that can extract strokes from clean
bitmap images. The stroke extractor can be used to recognize offline handwritten
mathematical expression if a online recognizer is given. For example, when combined
with MyScript, the resulting offline recognition system was **ranked #3 in the offline
task in CROHME 2019.**

## Accuracy

Dataset|Correct|Up to 1 error|Up to 2 errors|Structural correct
---|---|---|---|---
CROHME 2014|58.22%|71.60%|75.15%|77.38%
CROHME 2016|65.65%|77.68%|82.56%|85.00%
CROHME 2019|65.22%|78.48%|83.07%|84.90%

Although good accuracy is achieved on datasets from CROHME, the program
may produce poor results on real world images. For example, the procedure do not
work well on the following images:
- Image containing other objects. An image should contains exactly one formula and nothing else.
Ordinary text and grid lines are not allowed.
- Image with low contrast. The strokes may not be distinguished from background properly.
- Image with low resolution. The stroke extractor may not segment touching symbols correctly.
- Printed mathematical expressions. Serifs can distract the stroke extractor.

## Usage

In order to use the MyScript Cloud recognition engine, you need to [create a account](https://sso.myscript.com/register)
and create an application.

### Graphical interface

1. Run the JAR by double click or command like `java -jar mathocr-myscript.jar`
2. Choose `Image file` from the menu `Recognize`
3. Choose the image file
4. Click the button `Recognize` under stroke preview

### API

First add the jar file to classpath. If you are using Maven, add the following
to you `pom.xml`:

```xml
<dependency>
	<groupId>com.github.chungkwong</groupId>
	<artifactId>mathocr-myscript</artifactId>
	<version>1.1</version>
</dependency>
```

Then you can recognize images of mathematical expression by using code like:

```java
String applicationKey="your application key for MyScript";
String hmacKey="hmac key of your Myscript account";
String grammarId="an uploaded grammar of your Myscript account";
int dpi=96;
MyscriptRecognizer myscriptRecognizer=new MyscriptRecognizer(applicationKey,hmacKey,grammarId,dpi);
Extractor extractor=new Extractor(myscriptRecognizer);

File file=new File("Path to file to be recognized");
EncodedExpression expression=extractor.recognize(ImageIO.read(file));
String latexCode=expression.getCodes(new LatexFormat());
```

## Citation

The idea used is explained in the article
__Stroke extraction for offline handwritten mathematical expression recognition__
, which is available at [arXiv](https://arxiv.org/abs/1905.06749).
You can cite the article using the following BibTex code:

```bibtex
@misc{1905.06749,
Author = {Chungkwong Chan},
Title = {Stroke extraction for offline handwritten mathematical expression recognition},
Year = {2019},
Eprint = {arXiv:1905.06749},
}
```

# 基于笔划提取的脱机手写数学公式识别

本项目提供一个可从清晰的图片中还原笔划信息的程序原型。与联机手写数学公式识别结合的话，
可以打造出脱机数学公式识别系统。例如与MyScript结合时 **在CROHME 2019的脱机任务中位列第3名**。

## 准确度

数据集|正确|至多一处错误|至多两处错误|结构正确
---|---|---|---|---
CROHME 2014|58.22%|71.60%|75.15%|77.38%
CROHME 2016|65.65%|77.68%|82.56%|85.00%
CROHME 2019|65.22%|78.48%|83.07%|84.90%

虽然在CROHME数据集上取得了良好的表现，本程序对现实世界中的图片表现仍然可能未如理想。
例如对以下类型的图片可能给出差劲的结果：

- 含有其它对象的图片。图片中只应含有一条公式而没有其它东西，不能有普通文本或网格之类。
- 低对比度图片。这时笔划难以从背景区分出来。
- 低清晰度图片。这时粘连在一起的符号难以分割。
- 印刷体数学公式。衬线会干扰笔划提取。

## 用法

如果使用MyScript Cloud作为联机手写数学公式识别器，请[注册一个帐号](https://sso.myscript.com/register)并创建一个应用。

### 图形用户界面


1. 通过双击或命令如`java -jar mathocr-myscript.jar`运行JAR文件
2. 在菜单`识别`中选择`图片文件`
3. 选择图像文件
4. 点击笔划预览下的`识别`按钮（首次使用时需要输入你的MyScript Cloud应用标识和密钥）

### API

首先把JAR文件加到类路径。如果你使用Maven，把以下依赖加到`pom.xml`中`dependencies`下即可（其它构建系统类似）：

```xml
<dependency>
	<groupId>com.github.chungkwong</groupId>
	<artifactId>mathocr-myscript</artifactId>
	<version>1.1</version>
</dependency>
```

然后你可以使用以下样子的代码识别脱机手写数学公式:

```java
String applicationKey="your application key for MyScript";
String hmacKey="hmac key of your Myscript account";
String grammarId="an uploaded grammar of your Myscript account";
int dpi=96;
MyscriptRecognizer myscriptRecognizer=new MyscriptRecognizer(applicationKey,hmacKey,grammarId,dpi);
Extractor extractor=new Extractor(myscriptRecognizer);

File file=new File("Path to file to be recognized");
EncodedExpression expression=extractor.recognize(ImageIO.read(file));
String latexCode=expression.getCodes(new LatexFormat());
```

## 引用

本项目的描述参见文档 __通过笔划提取识别脱机手写数学公式__，它可从
[arXiv](https://arxiv.org/abs/1905.06749)下载。你可以使用以下BibTex代码引用该文:

```bibtex
@misc{1905.06749,
Author = {Chungkwong Chan},
Title = {Stroke extraction for offline handwritten mathematical expression recognition},
Year = {2019},
Eprint = {arXiv:1905.06749},
}
```

# NewOCR Wiki Updater

This small project is made for the [NewOCR wiki website](https://github.com/MSPaintIDE/NewOCR-website/tree/gh-pages) to update all the inline code references across branches. This project is only really meant for contributors to the OCR, though may be of use to others.

## Using It

To use the updater, you can extract the zip file, and run the command

```
java -jar NewOCRWikiUpdater-1.0.jar "C:\path\to\website" "branch hash to update to"
```

Where the first argument is the root directory of your local clone of [the wiki](https://github.com/MSPaintIDE/NewOCR-website/tree/gh-pages), and the second argument being the hash of the branch to update the wiki to.

After running the command, it will give a list of changed links in their files. Some may show "MANUAL" on what they changed to, which means it must be manually updated as lines were added/removed between the start and end of the line references.

Here is a truncated example output of the most recent update from branch `7de96263853df8f63d340ecaf26284cb0d4dbb34` to `7aa211108c8da4d7900b4e89442b1a003dfe1c3e`:

```
E:\NewOCR-website\explanation\training\calculation.md
	https://github.com/RubbaBoy/NewOCR/blob/7de96263853df8f63d340ecaf26284cb0d4dbb34/src/main/java/com/uddernetworks/newocr/recognition/OCRTrain.java#L180-L185 > https://github.com/RubbaBoy/NewOCR/blob/7aa211108c8da4d7900b4e89442b1a003dfe1c3e/src/main/java/com/uddernetworks/newocr/recognition/OCRTrain.java#L220-L225
E:\NewOCR-website\explanation\training\image.md
	https://github.com/RubbaBoy/NewOCR/blob/7de96263853df8f63d340ecaf26284cb0d4dbb34/src/main/java/com/uddernetworks/newocr/train/ComputerTrainGenerator.java#L50-L56 > https://github.com/RubbaBoy/NewOCR/blob/7aa211108c8da4d7900b4e89442b1a003dfe1c3e/src/main/java/com/uddernetworks/newocr/train/ComputerTrainGenerator.java#L57-L63
	https://github.com/RubbaBoy/NewOCR/blob/7de96263853df8f63d340ecaf26284cb0d4dbb34/src/main/java/com/uddernetworks/newocr/recognition/OCRTrain.java#L112-L125 > https://github.com/RubbaBoy/NewOCR/blob/7aa211108c8da4d7900b4e89442b1a003dfe1c3e/src/main/java/com/uddernetworks/newocr/recognition/OCRTrain.java#L145-L158
E:\NewOCR-website\explanation\scanning\spacing.md
	https://github.com/MSPaintIDE/NewOCR/blob/7de96263853df8f63d340ecaf26284cb0d4dbb34/src/main/java/com/uddernetworks/newocr/recognition/OCRScan.java#L166-L180 > https://github.com/MSPaintIDE/NewOCR/blob/7aa211108c8da4d7900b4e89442b1a003dfe1c3e/src/main/java/com/uddernetworks/newocr/recognition/OCRScan.java#L183-L197
	https://github.com/MSPaintIDE/NewOCR/blob/7de96263853df8f63d340ecaf26284cb0d4dbb34/src/main/java/com/uddernetworks/newocr/recognition/OCRScan.java#L209-L214 > https://github.com/MSPaintIDE/NewOCR/blob/7aa211108c8da4d7900b4e89442b1a003dfe1c3e/src/main/java/com/uddernetworks/newocr/recognition/OCRScan.java#L226-L231
	https://github.com/MSPaintIDE/NewOCR/blob/7de96263853df8f63d340ecaf26284cb0d4dbb34/src/main/java/com/uddernetworks/newocr/recognition/OCRScan.java#L195-L197 > https://github.com/MSPaintIDE/NewOCR/blob/7aa211108c8da4d7900b4e89442b1a003dfe1c3e/src/main/java/com/uddernetworks/newocr/recognition/OCRScan.java#L212-L214
E:\NewOCR-website\explanation\scanning\separation.md
	https://github.com/RubbaBoy/NewOCR/blob/7de96263853df8f63d340ecaf26284cb0d4dbb34/src/main/java/com/uddernetworks/newocr/recognition/OCRActions.java#L58-L70 > https://github.com/RubbaBoy/NewOCR/blob/7aa211108c8da4d7900b4e89442b1a003dfe1c3e/src/main/java/com/uddernetworks/newocr/recognition/OCRActions.java#L52-L64
E:\NewOCR-website\explanation\scanning\calculation.md
	https://github.com/MSPaintIDE/NewOCR/blob/7de96263853df8f63d340ecaf26284cb0d4dbb34/src/main/java/com/uddernetworks/newocr/recognition/OCRActions.java#L192-L201 > https://github.com/MSPaintIDE/NewOCR/blob/7aa211108c8da4d7900b4e89442b1a003dfe1c3e/src/main/java/com/uddernetworks/newocr/recognition/OCRActions.java#L184-L193
	https://github.com/MSPaintIDE/NewOCR/blob/7de96263853df8f63d340ecaf26284cb0d4dbb34/src/main/java/com/uddernetworks/newocr/utils/OCRUtils.java#L92-L101 > https://github.com/MSPaintIDE/NewOCR/blob/7aa211108c8da4d7900b4e89442b1a003dfe1c3e/src/main/java/com/uddernetworks/newocr/utils/OCRUtils.java#L96-L105
	https://github.com/MSPaintIDE/NewOCR/blob/7de96263853df8f63d340ecaf26284cb0d4dbb34/src/main/java/com/uddernetworks/newocr/recognition/OCRActions.java#L239-L251 > https://github.com/MSPaintIDE/NewOCR/blob/7aa211108c8da4d7900b4e89442b1a003dfe1c3e/src/main/java/com/uddernetworks/newocr/recognition/OCRActions.java#L231-L243
E:\NewOCR-website\explanation\training\separation.md
	https://github.com/RubbaBoy/NewOCR/blob/7de96263853df8f63d340ecaf26284cb0d4dbb34/src/main/java/com/uddernetworks/newocr/recognition/OCRActions.java#L105-L117 > https://github.com/RubbaBoy/NewOCR/blob/7aa211108c8da4d7900b4e89442b1a003dfe1c3e/src/main/java/com/uddernetworks/newocr/recognition/OCRActions.java#L97-L109
	https://github.com/RubbaBoy/NewOCR/blob/7de96263853df8f63d340ecaf26284cb0d4dbb34/src/main/java/com/uddernetworks/newocr/recognition/OCRActions.java#L127-L174 > https://github.com/RubbaBoy/NewOCR/blob/7aa211108c8da4d7900b4e89442b1a003dfe1c3e/src/main/java/com/uddernetworks/newocr/recognition/OCRActions.java#L119-L166
	https://github.com/RubbaBoy/NewOCR/blob/7de96263853df8f63d340ecaf26284cb0d4dbb34/src/main/java/com/uddernetworks/newocr/recognition/OCRActions.java#L156-L168 > https://github.com/RubbaBoy/NewOCR/blob/7aa211108c8da4d7900b4e89442b1a003dfe1c3e/src/main/java/com/uddernetworks/newocr/recognition/OCRActions.java#L148-L160

```


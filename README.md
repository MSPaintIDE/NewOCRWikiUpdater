# NewOCR Wiki Updater

This small project is made for the [NewOCR wiki website](https://github.com/MSPaintIDE/NewOCR-website/tree/gh-pages) to update all the inline code references across branches. This project is only really meant for contributors to the OCR, though may be of use to others.

## Using It

To use the updater, you can extract the zip file, and run the command

```
java -jar NewOCRWikiUpdater-1.0.jar "C:\path\to\website" "branch hash to update to"
```

Where the first argument is the root directory of your local clone of [the wiki](https://github.com/MSPaintIDE/NewOCR-website/tree/gh-pages), and the second argument being the hash of the branch to update the wiki to.

After running the command, it will give a list of changed links in their files. Some may show "MANUAL" on what they changed to, which means it must be manually updated as lines were added/removed between the start and end of the line references.
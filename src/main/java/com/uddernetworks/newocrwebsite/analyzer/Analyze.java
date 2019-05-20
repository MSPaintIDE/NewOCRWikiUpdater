package com.uddernetworks.newocrwebsite.analyzer;

import org.apache.commons.io.FileUtils;
import org.jsoup.Jsoup;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Analyze {

    private static final boolean USE_COOKIES = true;

    private Map<String, ChromeDriver> documentHashCache = new HashMap<>();
    private Map<String, Map<String, String>> funkyHashCache = new HashMap<>();
    private Scanner scanner;

    public static void main(String[] args) {
        new Analyze().run(args);
    }

    private void run(String[] args) {
        if (args.length != 2) {
            System.err.println("Correct usage:");
            System.err.println("\tjava -jar NewOCRWikiUpdater-1.0.jar \"C:\\path\\to\\website\" \"branch hash to update to\"");
            return;
        }

        var projectLocation = new File(args[0]);
        if (!projectLocation.isDirectory()) {
            System.err.println("Argument requires the root directory of the website!");
            return;
        }

        var newHash = args[1];

        var markdownFiles = getFilesFromDirectory(projectLocation, "md");

        var changeMap = new ArrayList<LineData>();

        scanner = new Scanner(System.in);

        markdownFiles.forEach(file -> {
            try {
                var parsed = Jsoup.parse(file, "UTF-8");
                var select = parsed.select("src");

                select.forEach(element -> {
                    var dataGH = element.attr("data-gh");
                    if (dataGH.startsWith("<") && dataGH.endsWith(">")) dataGH = dataGH.substring(1, dataGH.length() - 1);

                    var parts = getURLParts(dataGH);
                    var permHash = parts[0].orElse("undefined");
                    var filePath = parts[1].orElse("undefined");
                    var withoutLines = parts[2].orElse("undefined");
                    var lineStart = parts[3].orElse("undefined");
                    var lineEnd = parts[4].orElse("undefined");

                    try {
                        if (permHash.equals(newHash)) return;

                        String funkyHash = getFunkyHash(filePath, permHash, newHash);

                        if ("".equals(funkyHash) || funkyHash == null) {
                            changeMap.add(new LineData(file, dataGH, dataGH.replace(permHash, newHash)));
                            return;
                        }

                        var doc = this.documentHashCache.get(permHash);

                        var secondEnabled = !lineStart.equals(lineEnd);

                        var siblingsStart = doc.findElementsByCssSelector("td[id=\"diff-" + funkyHash + "L" + lineStart + "\"] + * + td");
                        if (siblingsStart.isEmpty()) throw new IOException();

                        var siblingsEnd = doc.findElementsByCssSelector("td[id=\"diff-" + funkyHash + "L" + lineEnd + "\"] + * + td");
                        if (siblingsEnd.isEmpty() && secondEnabled) throw new IOException();
                        var start = siblingsStart.get(0);
                        var end = siblingsEnd.get(0);

                        var newStart = Integer.parseInt(start.getAttribute("data-line-number"));
                        var newEnd = Integer.parseInt(end.getAttribute("data-line-number"));

                        var firstOffset = newStart - Integer.parseInt(lineStart);
                        var secondOffset = newEnd - Integer.parseInt(lineEnd);

                        if (secondEnabled && firstOffset != secondOffset) {
                            changeMap.add(new LineData(file, dataGH, waitForLink(dataGH, permHash, newHash)));
                            return;
                        }

                        var lines = "#L" + newStart;
                        if (secondEnabled) lines += "-L" + newEnd;
                        System.out.println("Changing lines from " + lineStart + "-" + lineEnd + " to " + newStart + "-" + newEnd);
                        changeMap.add(new LineData(file, dataGH, withoutLines.replace(permHash, newHash) + lines));
                    } catch (Exception e) {
                        changeMap.add(new LineData(file, dataGH, waitForLink(dataGH, permHash, newHash)));
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        changeMap.stream().collect(Collectors.groupingBy(LineData::getFile)).forEach((file, lineDatas) -> {
            try {
                final String[] string = {FileUtils.readFileToString(file, "UTF-8")};
                System.out.println(file.getAbsolutePath());
                lineDatas.forEach(lineData -> {
                    if (lineData.isManual()) {
                        System.out.println("\t" + lineData.getFrom() + " > Manual");
                    } else {
                        System.out.println("\t" + lineData.getFrom() + " > " + lineData.getTo());
                        string[0] = string[0].replace(lineData.getFrom(), lineData.getTo());
                    }
                });

                FileUtils.write(file, string[0], "UTF-8");
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    private String waitForLink(String preLink, String oldHash, String newHash) {
        System.out.println("The following code snippet requires manual input.");
        System.out.println("Original: " + preLink);
        System.out.println("With new: " + preLink.replace(oldHash, newHash) + "\n");
        return scanner.nextLine().trim();
    }

    /**
     * Separates the URl from its different parts. This method was very lazily written, so if anyone sees this, make an
     * issue or something saying I should make this better.
     *
     * @param URL The input URL
     * @return hash, path, without lines, first num, second num
     */
    private Optional<String>[] getURLParts(String URL) {
        var hashFromURL = Pattern.compile("([a-z0-9]{40})");

        var permHashMatch = hashFromURL.matcher(URL);
        var one = permHashMatch.find() ? permHashMatch.group() : null; //     7aa211108c8da4d7900b4e89442b1a003dfe1c3e

        var otherPattern = Pattern.compile("[a-z0-9]{40}/(.*)#");
        var matcher = otherPattern.matcher(URL);
        var two = matcher.find() ? matcher.group(1) : null; //     /src/main/java/com/uddernetworks/newocr/recognition/OCRActions.java

        var firstNumPattern = Pattern.compile("(^.*?)#|$");
        var matcher2 = firstNumPattern.matcher(URL);
        var three = matcher2.find() ? matcher2.group(1) : null;

        var secondNumPattern = Pattern.compile("[a-z0-9]{40}.*#L(\\d*)");
        var matcher3 = secondNumPattern.matcher(URL);
        var four = matcher3.find() ? matcher3.group(1) : null;

        var thirdNumPattern = Pattern.compile("[a-z0-9]{40}.*#L\\d*-L(\\d*)");
        var matcher4 = thirdNumPattern.matcher(URL);
        var five = matcher4.find() ? matcher4.group(1) : null;

        return new Optional[] {Optional.ofNullable(one), Optional.ofNullable(two), Optional.ofNullable(three), Optional.ofNullable(four), Optional.ofNullable(five)};
    }

    private String getFunkyHash(String file, String fromHash, String toHash) throws InterruptedException {
        return getFunkyHashes(fromHash, toHash).getOrDefault(file, "");
    }

    private Map<String, String> getFunkyHashes(String fromHash, String toHash) throws InterruptedException {
        if (this.funkyHashCache.containsKey(fromHash)) return this.funkyHashCache.get(fromHash);
        var map = new HashMap<String, String>();

        var driver = new ChromeDriver(new ChromeOptions().setHeadless(true));
        if (USE_COOKIES) {
            driver.get("https://github.com/");
            loadCookies(driver);
        }

        driver.get("https://github.com/MSPaintIDE/NewOCR/compare/" + fromHash + "..." + toHash + "?diff=split");

        Thread.sleep(3000);

        this.documentHashCache.put(fromHash, driver);

        driver.executeScript("function expand() {\n" +
                "\tdocument.querySelectorAll(\".blob-num-expandable a\").forEach((elem) => elem.click());\n" +
                "\tif (document.querySelectorAll(\".blob-num-expandable a\").length > 0) {\n" +
                "\t\tconsole.log(\"Scheduling next\");\n" +
                "\t\tscheduleNext();\n" +
                "    } else {\n" +
                "\t\tlocalStorage.setItem(\"finished\", true);\n" +
                "    }\n" +
                "}\n" +
                "\n" +
                "function scheduleNext() {\n" +
                "\tsetTimeout(expand, 1000);\n" +
                "}\n" +
                "expand();");

        while (!"true".equals(driver.getLocalStorage().getItem("finished"))) {
            Thread.sleep(500);
        }

        driver.findElementsByCssSelector(".file .file-info a[href^=\"#diff-\"]").forEach(element -> {
            var href = element.getAttribute("href");
            map.put(element.getText(), href.substring(href.lastIndexOf('#') + 6));
        });
        this.funkyHashCache.put(fromHash, map);
        return map;
    }

    public static List<File> getFilesFromDirectory(File directory, String extension) {
        return getFilesFromDirectory(directory, new String[]{extension});
    }

    public static List<File> getFilesFromDirectory(File directory, String... extensions) {
        List<File> ret = new ArrayList<>();
        for (File file : directory.listFiles()) {
            if (file.isDirectory()) {
                ret.addAll(getFilesFromDirectory(file, extensions));
            } else {
                if (extensions == null || Arrays.stream(extensions).anyMatch(extension -> file.getName().endsWith("." + extension)))
                    ret.add(file);
            }
        }

        return ret;
    }

    private void loadCookies(ChromeDriver driver) {
        try {
            File file = new File("Cookies.data");
            FileReader fileReader = new FileReader(file);
            BufferedReader Buffreader = new BufferedReader(fileReader);
            String strline;
            while ((strline = Buffreader.readLine()) != null) {
                StringTokenizer token = new StringTokenizer(strline, ";");
                while (token.hasMoreTokens()) {
                    String name = token.nextToken();
                    String value = token.nextToken();
                    String domain = token.nextToken();
                    String path = token.nextToken();
                    Date expiry = null;

                    String val;
                    if (!(val = token.nextToken()).equals("null")) {
                        expiry = new Date(val);
                    }

                    boolean isSecure = Boolean.valueOf(token.nextToken());
                    Cookie ck = new Cookie(name, value, domain, path, expiry, isSecure);
                    driver.manage().addCookie(ck); // This will add the stored cookie to your current session
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

}

package com.uddernetworks.newocrwebsite.analyzer;

import org.apache.commons.io.FileUtils;
import org.jsoup.Jsoup;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.chrome.ChromeDriver;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class Analyze {

    private static final boolean USE_COOKIES = false;

    private Map<String, ChromeDriver> documentHashCache = new HashMap<>();
    private Map<String, Map<String, String>> funkyHashCache = new HashMap<>();

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

        markdownFiles.forEach(file -> {
            try {
                var parsed = Jsoup.parse(file, "UTF-8");
                var select = parsed.select("src");

                select.forEach(element -> {
                    try {
                        var dataGH = element.attr("data-gh");
                        var withoutLines = dataGH.substring(0, dataGH.indexOf('#'));
                        var permHash = dataGH.substring(40, 80);
                        var filePath = dataGH.substring(81, dataGH.lastIndexOf('#'));
                        var index = dataGH.lastIndexOf('-');
                        var lineStart = dataGH.substring(dataGH.lastIndexOf('#') + 2, index == -1 ? dataGH.length() : index);
                        var lineEnd = index == -1 ? lineStart : dataGH.substring(index + 2);

                        String funkyHash = getFunkyHash(filePath, permHash, newHash);

                        if ("".equals(funkyHash) || funkyHash == null) {
                            changeMap.add(new LineData(file, dataGH, dataGH.replace(permHash, newHash)));
                            return;
                        }

                        var doc = this.documentHashCache.get(permHash);

                        var secondEnabled = !lineStart.equals(lineEnd);

                        var siblingsStart = doc.findElementsByCssSelector("td[id=\"diff-" + funkyHash + "L" + lineStart + "\"] + * + td");
                        if (siblingsStart.isEmpty()) return;

                        var siblingsEnd = doc.findElementsByCssSelector("td[id=\"diff-" + funkyHash + "L" + lineEnd + "\"] + * + td");
                        if (siblingsEnd.isEmpty() && secondEnabled) return;
                        var start = siblingsStart.get(0);
                        var end = siblingsEnd.get(0);

                        var newStart = Integer.parseInt(start.getAttribute("data-line-number"));
                        var newEnd = Integer.parseInt(end.getAttribute("data-line-number"));

                        var firstOffset = newStart - Integer.parseInt(lineStart);
                        var secondOffset = newEnd - Integer.parseInt(lineEnd);

                        if (secondEnabled && firstOffset != secondOffset) {
                            System.out.println("First offset does not equal second offset, this means the code has been modified and must be done manually.");
                            changeMap.add(new LineData(file, dataGH, true));
                            return;
                        }

                        var lines = "#L" + newStart;
                        if (secondEnabled) lines += "-L" + newEnd;
                        System.out.println("Changing lines from " + lineStart + "-" + lineEnd + " to " + newStart + "-" + newEnd);
                        changeMap.add(new LineData(file, dataGH, withoutLines.replace(permHash, newHash) + lines));
                    } catch (InterruptedException e) {
                        e.printStackTrace();
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

    private String getFunkyHash(String file, String fromHash, String toHash) throws InterruptedException {
        return getFunkyHashes(fromHash, toHash).getOrDefault(file, "");
    }

    private Map<String, String> getFunkyHashes(String fromHash, String toHash) throws InterruptedException {
        if (this.funkyHashCache.containsKey(fromHash)) return this.funkyHashCache.get(fromHash);
        var map = new HashMap<String, String>();

        var driver = new ChromeDriver();
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

package com.uddernetworks.newocrwebsite.analyzer;

import org.openqa.selenium.Cookie;
import org.openqa.selenium.chrome.ChromeDriver;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

public class Cookies {

    private static ChromeDriver driver;

    public static void main(String[] args) throws InterruptedException {
        // Run this class and then log into GitHub like normal, then change USE_COOKIES in Analyze.java to true
        driver = new ChromeDriver();
        driver.get("https://github.com/");

        System.out.println("Saving cookies in 20 seconds...");

        Thread.sleep(20_000);

        saveCookies();
    }

    private static void saveCookies() {
        // create file named Cookies to store Login Information
        File file = new File("Cookies.data");
        try {
            // Delete old file if exists
            file.delete();
            file.createNewFile();
            FileWriter fileWrite = new FileWriter(file);
            BufferedWriter bufferedWriter = new BufferedWriter(fileWrite);
            // loop for getting the cookie information

            // loop for getting the cookie information
            for (Cookie cookie : driver.manage().getCookies()) {
                bufferedWriter.write((cookie.getName() + ";" + cookie.getValue() + ";" + cookie.getDomain() + ";" + cookie.getPath() + ";" + cookie.getExpiry() + ";" + cookie.isSecure()));
                bufferedWriter.newLine();
            }

            bufferedWriter.close();
            fileWrite.close();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

}

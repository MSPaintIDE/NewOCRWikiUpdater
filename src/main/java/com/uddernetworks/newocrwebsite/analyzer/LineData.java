package com.uddernetworks.newocrwebsite.analyzer;

import java.io.File;

public class LineData {

    private File file;
    private String from;
    private String to;
    private boolean manual;

    public LineData(File file, String from, boolean manual) {
        this.file = file;
        this.from = from;
        this.manual = manual;
    }

    public LineData(File file, String from, String to) {
        this.file = file;
        this.from = from;
        this.to = to;
    }

    public File getFile() {
        return file;
    }

    public LineData setFile(File file) {
        this.file = file;
        return this;
    }

    public String getFrom() {
        return from;
    }

    public LineData setFrom(String from) {
        this.from = from;
        return this;
    }

    public String getTo() {
        return to;
    }

    public LineData setTo(String to) {
        this.to = to;
        return this;
    }

    public boolean isManual() {
        return manual;
    }

    public LineData setManual(boolean manual) {
        this.manual = manual;
        return this;
    }
}

package com.bhola.desiKahaniya;

public class RowData {

    String Title,Paragraph,Key,Heading,Date;

    public RowData() {
    }

    public RowData(String title, String paragraph, String key, String heading, String date) {
        Title = title;
        Paragraph = paragraph;
        Key = key;
        Heading = heading;
        Date = date;
    }

    public String getTitle() {
        return Title;
    }

    public void setTitle(String title) {
        Title = title;
    }

    public String getParagraph() {
        return Paragraph;
    }

    public void setParagraph(String paragraph) {
        Paragraph = paragraph;
    }

    public String getKey() {
        return Key;
    }

    public void setKey(String key) {
        Key = key;
    }

    public String getHeading() {
        return Heading;
    }

    public void setHeading(String heading) {
        Heading = heading;
    }

    public String getDate() {
        return Date;
    }

    public void setDate(String date) {
        Date = date;
    }
}

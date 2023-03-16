package com.bhola.desiKahaniya;

public class FirebaseData {
    int id;
    String Date, Heading, Title;
    int Liked;
    String TableName;

    public FirebaseData() {
    }

    public FirebaseData(int id, String date, String heading, String title, int liked, String tableName) {
        this.id = id;
        Date = date;
        Heading = heading;
        Title = title;
        Liked = liked;
        TableName = tableName;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDate() {
        return Date;
    }

    public void setDate(String date) {
        Date = date;
    }

    public String getHeading() {
        return Heading;
    }

    public void setHeading(String heading) {
        Heading = heading;
    }

    public String getTitle() {
        return Title;
    }

    public void setTitle(String title) {
        Title = title;
    }

    public int getLiked() {
        return Liked;
    }

    public void setLiked(int liked) {
        Liked = liked;
    }

    public String getTableName() {
        return TableName;
    }

    public void setTableName(String tableName) {
        TableName = tableName;
    }
}

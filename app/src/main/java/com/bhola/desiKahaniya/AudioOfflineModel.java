package com.bhola.desiKahaniya;

public class AudioOfflineModel {

    String name, path;

    public AudioOfflineModel() {
    }

    public AudioOfflineModel(String name, String path) {
        this.name = name;
        this.path = path;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}

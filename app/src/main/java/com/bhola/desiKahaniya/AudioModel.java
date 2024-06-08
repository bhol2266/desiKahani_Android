package com.bhola.desiKahaniya;

public class AudioModel {
    String Name, URL;

    public AudioModel() {
    }

    public AudioModel(String name, String URL) {
        this.Name = name;
        this.URL = URL;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getURL() {
        return URL;
    }

    public void setURL(String URL) {
        this.URL = URL;
    }
}

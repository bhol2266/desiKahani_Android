package com.bhola.desiKahaniya;

public class AudioCategoryModel {

    String coverImage, collectionName, season, description, ref;

    public AudioCategoryModel() {
    }

    public AudioCategoryModel(String coverImage, String collectionName, String season, String description, String ref) {
        this.coverImage = coverImage;
        this.collectionName = collectionName;
        this.season = season;
        this.description = description;
        this.ref = ref;
    }

    public String getCoverImage() {
        return coverImage;
    }

    public void setCoverImage(String coverImage) {
        this.coverImage = coverImage;
    }

    public String getCollectionName() {
        return collectionName;
    }

    public void setCollectionName(String collectionName) {
        this.collectionName = collectionName;
    }

    public String getSeason() {
        return season;
    }

    public void setSeason(String season) {
        this.season = season;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getRef() {
        return ref;
    }

    public void setRef(String ref) {
        this.ref = ref;
    }
}

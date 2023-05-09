package com.bhola.desiKahaniyaAdult2;

public class StoryItemModel {

    String Title, href, date, views, description, audiolink, category,tags, relatedStories;
    int completeDate;
    String story;
    int like;
    int audio;
    String storiesInsideParagraph;
    int read;

    public StoryItemModel(StoryItemModel dataFROM_db) {
    }

    public StoryItemModel(String title, String href, String date, String views, String description, String audiolink, String category, String tags, String relatedStories, int completeDate, String story, int like, int audio, String storiesInsideParagraph, int read) {
        Title = title;
        this.href = href;
        this.date = date;
        this.views = views;
        this.description = description;
        this.audiolink = audiolink;
        this.category = category;
        this.tags = tags;
        this.relatedStories = relatedStories;
        this.completeDate = completeDate;
        this.story = story;
        this.like = like;
        this.audio = audio;
        this.storiesInsideParagraph = storiesInsideParagraph;
        this.read = read;
    }

    public String getTitle() {
        return Title;
    }

    public void setTitle(String title) {
        Title = title;
    }

    public String getHref() {
        return href;
    }

    public void setHref(String href) {
        this.href = href;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getViews() {
        return views;
    }

    public void setViews(String views) {
        this.views = views;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAudiolink() {
        return audiolink;
    }

    public void setAudiolink(String audiolink) {
        this.audiolink = audiolink;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public String getRelatedStories() {
        return relatedStories;
    }

    public void setRelatedStories(String relatedStories) {
        this.relatedStories = relatedStories;
    }

    public int getCompleteDate() {
        return completeDate;
    }

    public void setCompleteDate(int completeDate) {
        this.completeDate = completeDate;
    }

    public String getStory() {
        return story;
    }

    public void setStory(String story) {
        this.story = story;
    }

    public int getLike() {
        return like;
    }

    public void setLike(int like) {
        this.like = like;
    }

    public int getAudio() {
        return audio;
    }

    public void setAudio(int audio) {
        this.audio = audio;
    }

    public String getStoriesInsideParagraph() {
        return storiesInsideParagraph;
    }

    public void setStoriesInsideParagraph(String storiesInsideParagraph) {
        this.storiesInsideParagraph = storiesInsideParagraph;
    }

    public int getRead() {
        return read;
    }

    public void setRead(int read) {
        this.read = read;
    }
}


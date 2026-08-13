package com.bhola.desiKahaniya;

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

    // description/tags/relatedStories/story/storiesInsideParagraph are held exactly
    // as they come out of the database (encrypted with the same shift-5 scheme as
    // Title/href/audiolink) and decoded on first read instead of in the constructor.
    //
    // Decrypting eagerly meant building a list decoded every story body in it, none
    // of which the list displays - one page of ten stories was ~84k characters of
    // needless work on the main thread, which was enough to ANR.
    private String descriptionPlain, tagsPlain, relatedStoriesPlain, storyPlain, storiesInsideParagraphPlain;

    private static String decryptOrKeep(String value) {
        if (value == null || value.isEmpty()) {
            return value;
        }
        return SplashScreen.decryption(value);
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
        if (descriptionPlain == null) descriptionPlain = decryptOrKeep(description);
        return descriptionPlain;
    }

    public void setDescription(String description) {
        this.description = description;
        this.descriptionPlain = null; // drop the decoded copy
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
        if (tagsPlain == null) tagsPlain = decryptOrKeep(tags);
        return tagsPlain;
    }

    public void setTags(String tags) {
        this.tags = tags;
        this.tagsPlain = null; // drop the decoded copy
    }

    public String getRelatedStories() {
        if (relatedStoriesPlain == null) relatedStoriesPlain = decryptOrKeep(relatedStories);
        return relatedStoriesPlain;
    }

    public void setRelatedStories(String relatedStories) {
        this.relatedStories = relatedStories;
        this.relatedStoriesPlain = null; // drop the decoded copy
    }

    public int getCompleteDate() {
        return completeDate;
    }

    public void setCompleteDate(int completeDate) {
        this.completeDate = completeDate;
    }

    public String getStory() {
        if (storyPlain == null) storyPlain = decryptOrKeep(story);
        return storyPlain;
    }

    public void setStory(String story) {
        this.story = story;
        this.storyPlain = null; // drop the decoded copy
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
        if (storiesInsideParagraphPlain == null) storiesInsideParagraphPlain = decryptOrKeep(storiesInsideParagraph);
        return storiesInsideParagraphPlain;
    }

    public void setStoriesInsideParagraph(String storiesInsideParagraph) {
        this.storiesInsideParagraph = storiesInsideParagraph;
        this.storiesInsideParagraphPlain = null; // drop the decoded copy
    }

    public int getRead() {
        return read;
    }

    public void setRead(int read) {
        this.read = read;
    }
}


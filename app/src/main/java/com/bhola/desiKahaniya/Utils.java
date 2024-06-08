package com.bhola.desiKahaniya;

import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Utils {

    public static HashMap<String, String> FirebaseObject_TO_HashMap(Map<String, Object> data) {

        //this method is used to get each field data from firebase object and add it in object
        String Title = (String) data.get("Title");
        String href = (String) data.get("href");
        Log.d("asdfsadf", "FirebaseObject_TO_HashMap: "+data.get("date")+"  :  Title  :  "+data.get("Title"));
        String date = (String) data.get("date");
        String views = (String) data.get("views");
        String audiolink = (String) data.get("audiolink");
        long completeDate = (long) data.get("completeDate");


        Map<String, String> category_obj = (Map<String, String>) data.get("category");

        String category = (String) category_obj.get("title");
        if (category.equals("Gay Sex Stories In Hindi")) {
            category = "Gay Sex Stories";
        }

        List<String> storyArrayList = (List<String>) data.get("description");
        String description = String.join("\n\n", storyArrayList);


        List<String> tagsArrayList = (List<String>) data.get("description");
        String tags = String.join(", ", tagsArrayList);


        List<Object> relatedStoriesLinks_Array = (List<Object>) data.get("relatedStoriesLinks");
        ArrayList<String> relatedStoriesList = new ArrayList();
        for (int j = 0; j < relatedStoriesLinks_Array.size(); j++) {
            Map<String, String> relatedStoriesLinksObject = (Map<String, String>) relatedStoriesLinks_Array.get(j);
            relatedStoriesList.add(relatedStoriesLinksObject.get("title"));
        }
        String relatedStories = String.join(", ", relatedStoriesList);


        List<Object> storiesInsideParagraph_Array = (List<Object>) data.get("storiesLink_insideParagrapgh");
        ArrayList<String> storiesInsideParagraphList = new ArrayList();
        for (int j = 0; j < storiesInsideParagraph_Array.size(); j++) {
            Map<String, String> storiesInsideParagraph_Object = (Map<String, String>) storiesInsideParagraph_Array.get(j);
            storiesInsideParagraphList.add(storiesInsideParagraph_Object.get("title"));
        }
        String storiesInsideParagraph = String.join(", ", storiesInsideParagraphList);


        //Add your values in your `ArrayList` as below:
        HashMap<String, String> m_li = new HashMap<String, String>();
        m_li.put("Title", Title);
        m_li.put("href", href);
        m_li.put("date", date);
        m_li.put("views", views);
        m_li.put("description", description.substring(0, 100));
        m_li.put("story", description);
        m_li.put("audiolink", audiolink);
        m_li.put("category", category);
        m_li.put("tags", tags);
        m_li.put("relatedStories", relatedStories);
        m_li.put("completeDate", String.valueOf(completeDate));
        m_li.put("storiesInsideParagraph", storiesInsideParagraph);

        return m_li;


    }
}

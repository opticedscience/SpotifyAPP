package advisor;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.net.http.HttpResponse;
import java.util.*;

public class findPlayListStrategy implements ResourceStrategy {
    Map<String, String> IDMap = new HashMap<>();
    Auth auth;
    String listName="";

    public findPlayListStrategy(Auth auth, String listName) {
        this.auth=auth;
        this.listName=listName;
    }

    @Override
    public String prepareAccessPoint(String resourceServer) {
        String id="";
        FindResource findResource=new FindResource(auth);
        findResource.setStrategy(new findCategoriesStrategy());
        findResource.findResource();
        findCategoriesStrategy categoriesStrategy= (findCategoriesStrategy) findResource.getStrategy();
        IDMap=categoriesStrategy.IDMap;

        if(!IDMap.containsKey(listName)){
            System.out.println("Unknown category name.");
            return null;
        }
        else {id=IDMap.get(listName);}

        String fullAccessPoint=resourceServer+"categories/"+id.replaceAll("\"","")+"/playlists";
        return fullAccessPoint;
    }

    @Override
    public List<String> parseResponse(HttpResponse<String> response) {
        List<String> respAsStringList=new ArrayList<>();

        if (response.statusCode() != 200) {
            System.out.println("Specified id doesn't exist");
            return Collections.emptyList();
        }

        JsonObject jsonObject= JsonParser.parseString(response.body()).getAsJsonObject();
        try {
            JsonObject playlists = jsonObject.get("playlists").getAsJsonObject();
            JsonArray items=playlists.get("items").getAsJsonArray();
            items.forEach(jsonElement -> {
                JsonObject item=jsonElement.getAsJsonObject();
                JsonObject external_urls=item.get("external_urls").getAsJsonObject();
                String url=external_urls.get("spotify").toString().replaceAll("\"","");
                String name=item.get("name").toString().replaceAll("\"","");
                respAsStringList.add(name+"\n"+url+"\n");
            });
        } catch (NullPointerException e) {
            System.out.println(response.body());
        }
        return respAsStringList;

    }
}

package advisor;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

public class findFeaturedStrategy implements ResourceStrategy {

    @Override
    public String prepareAccessPoint(String resourceServer) {
        return resourceServer+"featured-playlists";
    }

    @Override
    public List<String> parseResponse(HttpResponse<String> response) {
        List<String> respAsStringList=new ArrayList<>();
        JsonObject jsonObject= JsonParser.parseString(response.body()).getAsJsonObject();
        JsonObject playlists=jsonObject.get("playlists").getAsJsonObject();
        JsonArray items=playlists.get("items").getAsJsonArray();
        items.forEach(jsonElement -> {
            JsonObject item=jsonElement.getAsJsonObject();
            JsonObject external_urls=item.get("external_urls").getAsJsonObject();
            String url=external_urls.get("spotify").toString().replaceAll("\"","");
            String name=item.get("name").toString().replaceAll("\"","");
            respAsStringList.add(name+"\n"+url+"\n");
        });
        return respAsStringList;
    }
}

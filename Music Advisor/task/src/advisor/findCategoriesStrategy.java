package advisor;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class findCategoriesStrategy implements ResourceStrategy {

    Map<String, String> IDMap = new HashMap<>();

    @Override
    public String prepareAccessPoint(String resourceServer) {
        return resourceServer+"categories";
    }

    @Override
    public List<String> parseResponse(HttpResponse<String> response) {
        List<String> respAsStringList=new ArrayList<>();

        JsonObject jsonObject= JsonParser.parseString(response.body()).getAsJsonObject();
        JsonObject categories=jsonObject.get("categories").getAsJsonObject();
        JsonArray items=categories.get("items").getAsJsonArray();
        items.forEach(jsonElement -> {
            JsonObject item=jsonElement.getAsJsonObject();
            String id=item.get("id").toString();
            String name=item.get("name").toString().replaceAll("\"","");
            IDMap.put(name,id);
            respAsStringList.add(name+"\n");
        });
        return respAsStringList;
    }


}

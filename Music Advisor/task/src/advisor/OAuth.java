package advisor;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OAuth {
    private  String accessServer="https://accounts.spotify.com/";
    private String resourceServer="";

    private  String clientID="63f59d90ec444ba4bbba27775548b2bc";
    private  String redirect_uri="http://localhost:8080/";
    private  String secreteCOde="8084666b65ca4375a212281ae9e6706f";
    private  String authenticationCode;
    private String accessToken;
    private Map<String, String> IDMap = new HashMap<>();

    public OAuth(String accessPoint, String resource) {
        this.accessServer=accessPoint;
        this.resourceServer=resource+"/v1/browse/";
    }


    public void getAuthenticationCode() throws IOException, InterruptedException {
        System.out.println("use this link to request the access code:");
        System.out.println(accessServer + "/authorize?"
                + "client_id="
                +clientID
                +"&response_type=code"
                +"&redirect_uri=" +redirect_uri);

        runLocalServer();

//        HttpClient client= HttpClient.newBuilder().build();
//        HttpRequest request = HttpRequest.newBuilder()
//                .uri(URI.create(accessServer + "authorize?" + "client_id="
//                         +clientID+
//                        "&response_type=code" +
//                        "&redirect_uri=" +redirect_uri))
//                .GET()
//                .build();
//
//        client.send(request, HttpResponse.BodyHandlers.ofString());

    }

    public void runLocalServer() throws IOException {
        // start a http server

        try {
            HttpServer server = HttpServer.create();
            server.bind(new InetSocketAddress(8080), 0);
            server.start();

            server.createContext("/",  exchange ->  {
                            String query = exchange.getRequestURI().getQuery();
                            // seemed string split took too long to do in such case.
//                            String[] querySplit = query.split("=");
                        String message = "";

                            if (query != null&&query.contains("code")) {
                                System.out.println("code received");
                                message = "Got the code. Return back to your program.";
//                                authenticationCode = querySplit[1];
                                authenticationCode=query.substring(5);
                            } else {
                                message = "Authorization code not found. Try again.";
                            }
                            exchange.sendResponseHeaders(200, message.length());
                            exchange.getResponseBody().write(message.getBytes());
                            exchange.getResponseBody().close();
                        }
                    );
            System.out.println("waiting for code...");
            while (authenticationCode==null) {
                Thread.sleep(100);
            }
            server.stop(5);

//            server.createContext("/", exchange -> {
//                        String query = exchange.getRequestURI().getQuery();
//                        String request;
//
//                        if (query != null && query.contains("code")) {
//                            authenticationCode = query.substring(5);
//                            System.out.println("code received");
//                            System.out.println(authenticationCode);
//                            request = "Got the code. Return back to your program.";
//                        } else {
//                            request = "Authorization code not found. Try again.";
//                        }
//                        exchange.sendResponseHeaders(200, request.length());
//                        exchange.getResponseBody().write(request.getBytes());
//                        exchange.getResponseBody().close();
//                    });
//
//            System.out.println("waiting for code...");
//            while (authenticationCode == null) {
//                Thread.sleep(100);
//            }
//            server.stop(5);
//
        } catch (IOException | InterruptedException e) {
            System.out.println("There is an error on server");
        }

    }

    public void postAccessToken() {
        Map<Object, Object> body = new HashMap<>();
        body.put("client_id", clientID);
        body.put("client_secret", secreteCOde);
        body.put("grant_type", "authorization_code");
        body.put("code",authenticationCode);
        body.put("redirect_uri", redirect_uri);

        System.out.println("making http request for access_token...");
        System.out.println("response:");

        HttpRequest request=HttpRequest.newBuilder()
                .POST(buildBodyFromMap(body))
                .uri(URI.create(accessServer+"/api/token"))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .build();

        try {
            HttpClient client = HttpClient.newBuilder().build();
            HttpResponse<String> response = client.send
                    (request, HttpResponse.BodyHandlers.ofString());
            assert response != null;

            System.out.println(response.body());
            JsonObject jsonObject=JsonParser.parseString(response.body()).getAsJsonObject();
            accessToken=jsonObject.get("access_token").getAsString();
            System.out.println( "\n---SUCCESS---");

        } catch (InterruptedException | IOException e) {
            System.out.println("Error response"); }

    }

    private HttpRequest.BodyPublisher buildBodyFromMap(Map<Object, Object> body) {
        var builder = new StringBuilder();

        for (Map.Entry<Object, Object> entry : body.entrySet()) {
            if (builder.length() > 0) {
                builder.append("&");
            }
            builder.append(URLEncoder.encode(entry.getKey().toString(), StandardCharsets.UTF_8));
            builder.append("=");
            builder.append(URLEncoder.encode(entry.getValue().toString(), StandardCharsets.UTF_8));

        }
        return HttpRequest.BodyPublishers.ofString(builder.toString());
    }

    public void getNewRelease() {
        String accessPoint=resourceServer+"new-releases";
        HttpResponse<String> response=getResource(accessPoint);

        JsonObject jsonObject=JsonParser.parseString(response.body()).getAsJsonObject();
        JsonObject playlists=jsonObject.get("albums").getAsJsonObject();
        JsonArray items=playlists.get("items").getAsJsonArray();
        items.forEach(jsonElement -> {
            JsonObject item=jsonElement.getAsJsonObject();

            JsonObject external_urls=item.get("external_urls").getAsJsonObject();
            String url=external_urls.get("spotify").toString().replaceAll("\"","");

            String name=item.get("name").toString().replaceAll("\"","");

            JsonArray artists=item.get("artists").getAsJsonArray();
            List<String> artistName=new ArrayList<>();
            artists.forEach(jsonE -> {JsonObject artist=jsonE.getAsJsonObject();
            String artistNm=artist.get("name").toString().replaceAll("\"","");
            artistName.add(artistNm);});

            System.out.println(name);
            System.out.println(artistName);
            System.out.println(url+"\n");
        });
    }

    public void getPlayList(String listName) {
        String id="";

        if (IDMap.isEmpty()) {
            getCategories(false);
        }

        if(!IDMap.containsKey(listName)){
            System.out.println("Unknown category name.");
            return;
        }
        else {id=IDMap.get(listName);}

        String accessPoint=resourceServer+"categories/"+id.replaceAll("\"","")+"/playlists";
        HttpResponse<String> response=getResource(accessPoint);
        if (response.statusCode() != 200) {
            System.out.println("Specified id doesn't exist");
            return;
        }

        JsonObject jsonObject=JsonParser.parseString(response.body()).getAsJsonObject();
        try {
            JsonObject playlists = jsonObject.get("playlists").getAsJsonObject();
            JsonArray items=playlists.get("items").getAsJsonArray();
            items.forEach(jsonElement -> {
                JsonObject item=jsonElement.getAsJsonObject();
                JsonObject external_urls=item.get("external_urls").getAsJsonObject();
                String url=external_urls.get("spotify").toString().replaceAll("\"","");
                String name=item.get("name").toString().replaceAll("\"","");
                System.out.println(name);
                System.out.println(url+"\n");
            });
        } catch (NullPointerException e) {
            System.out.println(response.body());
        }

    }
    public void getFeatured() {
        String accessPoint=resourceServer+"featured-playlists";
        HttpResponse<String> response=getResource(accessPoint);

        JsonObject jsonObject=JsonParser.parseString(response.body()).getAsJsonObject();
        JsonObject playlists=jsonObject.get("playlists").getAsJsonObject();
        JsonArray items=playlists.get("items").getAsJsonArray();
        items.forEach(jsonElement -> {
            JsonObject item=jsonElement.getAsJsonObject();
            JsonObject external_urls=item.get("external_urls").getAsJsonObject();
            String url=external_urls.get("spotify").toString().replaceAll("\"","");
            String name=item.get("name").toString().replaceAll("\"","");
            System.out.println(name);
            System.out.println(url+"\n");
        });
    }

    public void getCategories(boolean print) {
        String accessPoint=resourceServer+"categories";
        HttpResponse<String> response=getResource(accessPoint);

        JsonObject jsonObject=JsonParser.parseString(response.body()).getAsJsonObject();
        JsonObject categories=jsonObject.get("categories").getAsJsonObject();
        JsonArray items=categories.get("items").getAsJsonArray();
        items.forEach(jsonElement -> {
            JsonObject item=jsonElement.getAsJsonObject();
            String id=item.get("id").toString();
            String name=item.get("name").toString().replaceAll("\"","");
            IDMap.put(name,id);
            });

        if (print) {
            IDMap.forEach((key,val)->{
                System.out.println(key);
            });
        }
    }

    private HttpResponse<String> getResource(String accessPoint) {
        HttpRequest httpRequest = HttpRequest.newBuilder()
                .header("Authorization", "Bearer " + accessToken)
                .uri(URI.create(accessPoint))
                .GET()
                .build();
        try {
            HttpClient client = HttpClient.newBuilder().build();
            HttpResponse<String> response = client.send
                    (httpRequest, HttpResponse.BodyHandlers.ofString());
            assert response != null;
            return response;

        } catch (InterruptedException | IOException e) {
            System.out.println("Error response"); return null;}
    }
}

package advisor;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;


// singleton for authentication, run only once, use lazy mode.
public class Auth {

    private String accessServer="https://accounts.spotify.com/";
    private String resourceServer="";

    private String clientID="";
    private String redirect_uri="http://localhost:8080/";
    private String secreteCOde="";
    private String authenticationCode;
    private String accessToken;

    private static Auth auth;

    private Auth(String accessPoint, String resource) {
        this.accessServer=accessPoint;
        this.resourceServer=resource+"/v1/browse/";
        try {
            getAuthenticationCode();
            postAccessToken();
        } catch (IOException | InterruptedException e) {
            System.out.println("Authentication error!");
        }
    }

    public static Auth getInstance(String accessServer, String resourceServer) {
        if (auth == null) {
            auth = new Auth(accessServer, resourceServer);
        }
        return auth;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getResourceServer() {
        return resourceServer;
    }


    public void getAuthenticationCode() throws IOException, InterruptedException {
        System.out.println("use this link to request the access code:");
        System.out.println(accessServer + "/authorize?"
                + "client_id="
                +clientID
                +"&response_type=code"
                +"&redirect_uri=" +redirect_uri);

        runLocalServer();

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
            JsonObject jsonObject= JsonParser.parseString(response.body()).getAsJsonObject();
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
}

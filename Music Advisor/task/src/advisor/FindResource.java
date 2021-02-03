package advisor;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

public class FindResource {
    private ResourceStrategy strategy;
    private Auth auth;


    public FindResource(Auth auth) {
        this.auth=auth;
    }

    public void setStrategy(ResourceStrategy strategy) {
        this.strategy = strategy;
    }

    public List<String> findResource(){
        String accesspoint=strategy.prepareAccessPoint(auth.getResourceServer());
        HttpResponse<String> response=getResource(accesspoint);
        return strategy.parseResponse(response);
    }

    public ResourceStrategy getStrategy() {
        return strategy;
    }

    private HttpResponse<String> getResource(String accessPoint) {
        HttpRequest httpRequest = HttpRequest.newBuilder()
                .header("Authorization", "Bearer " + auth.getAccessToken())
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

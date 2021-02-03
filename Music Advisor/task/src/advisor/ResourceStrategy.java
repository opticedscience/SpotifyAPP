package advisor;

import java.net.http.HttpResponse;
import java.util.List;

public interface ResourceStrategy {

    String prepareAccessPoint(String resourceServer);

    List<String> parseResponse(HttpResponse<String> response);
}

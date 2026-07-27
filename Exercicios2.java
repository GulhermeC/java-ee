import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class Exercicios2 {
    public static void main(String[] args) throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        int nAlive = 0, nDead = 0, nUnknown = 0;

        for (int i = 1; i <= 20; i++) {
            String url = "https://rickandmortyapi.com/api/character/" + i;
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.body().contains("\"status\":\"Alive\"")) {
                nAlive++;
            }
            
            if (response.body().contains("\"status\":\"Dead\"")) {
                nDead++;
            }

            if (response.body().contains("\"status\":\"unknown\"")) {
                nUnknown++;
            }
        }

        System.out.println("CENSO: Detetados " + nAlive + " personagens VIVOS, " + nDead + " personagens MORTOS e " + nUnknown + " personagens DESCONHECIDOS nos primeiros 20 registos");
    }
}
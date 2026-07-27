import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class Exercicios3 {
    public static void main(String[] args) throws Exception {
        HttpClient client = HttpClient.newHttpClient();

        for (int i = 1; i <= 20; i++) {
            String url = "https://rickandmortyapi.com/api/character/" + i;
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.body().contains("\"species\":\"Alien\"") && response.body().contains("\"status\":\"Dead\"")){
                System.out.println("[PERIGO] Um Alien foi encontrado morto com o ID " + i + " !");
            }
        }
    }
}
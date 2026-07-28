package automizador;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

public class App 
{
    public static void main( String[] args )
    {
        try {
            HttpClient client = HttpClient.newHttpClient();

            ObjectMapper mapper = new ObjectMapper();

            for (int i = 1; i <= 20; i++) {
                String url = "https://rickandmortyapi.com/api/character/" + i;
                HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();

                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                JsonNode jsonNode = mapper.readTree(response.body());
                
                //int id = jsonNode.get("id").asInt();
                String species= jsonNode.get("species").asText();
                String status = jsonNode.get("status").asText();
                JsonNode episodes = jsonNode.get("episode");
                List<String> episodeList = mapper.convertValue(episodes, new TypeReference<List<String>>() {});

                //System.out.println("ID: " + id);
                //System.out.println("Species: " + species);
                //System.out.println("Status: " + status);
                

                if(species.equals("Alien") && status.equals("Dead")) {
                    System.out.println("[PERIGO] Um Alien foi encontrado morto com o ID " + i + "!");
                    //System.out.println("Episodes: " + episodeList.size());
                    
                    String lastEpisode = episodeList.get(episodeList.size() - 1);
                    //System.out.println("Last episode: " + lastEpisode);
                    
                    HttpRequest requestLastEpisode = HttpRequest.newBuilder()
                        .uri(URI.create(lastEpisode))
                        .GET()
                        .build();

                    HttpResponse<String> responseLastEpisode = client.send(requestLastEpisode, HttpResponse.BodyHandlers.ofString());
                    JsonNode jsonNodeLastEpisode = mapper.readTree(responseLastEpisode.body());
                    //System.out.println("Episode info: " + jsonNodeLastEpisode);
                    String episodeName = jsonNodeLastEpisode.get("name").asText();
                    System.out.println("[ALERTA FORENSE] O último registo do alien morto foi no episódio: '" + episodeName + "'.");
                }
            }    
        }
        catch(Exception e) {
            System.out.println("Exception generica.");
        }
    }
}

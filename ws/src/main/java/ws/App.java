package ws;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces; 
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/census")
public class App {

    @GET
    @Produces(MediaType.TEXT_HTML)
    public Response doGet(
        @QueryParam("offset") String offsetParam,
        @QueryParam("limit") String limitParam,
        @QueryParam("showAlerts") String showAlertsParam
    ) {
        ObjectMapper mapper = new ObjectMapper();
        StringBuilder html = new StringBuilder();

        int offset;
        if (offsetParam == null) {
            offset = 1;
        } else {
            try {
                offset = Integer.parseInt(offsetParam.trim());
            } catch (NumberFormatException e) {
                return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"status\":400,\"error\":\"Bad Request\",\"message\":\"O parâmetro 'offset' deve ser um numero inteiro.\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
            }
        }

        int limit;
        if (limitParam == null) {
            limit = 20;
        } else {
            try {
                limit = Integer.parseInt(limitParam.trim());
            } catch (NumberFormatException e) {
                return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"status\":400,\"error\":\"Bad Request\",\"message\":\"O parâmetro 'limit' deve ser um numero inteiro.\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
            }
            if (limit < 1 || limit > 50) {
                return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"status\":400,\"error\":\"Bad Request\",\"message\":\"O parâmetro 'limit' deve ser um numero inteiro entre 1 e 50.\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
            }
        }

        boolean showAlerts;
        if (showAlertsParam == null || showAlertsParam.equals("true")) {
            showAlerts = true;
        } else if (showAlertsParam.equals("false")) {
            showAlerts = false;
        } else {
            return Response.status(Response.Status.BAD_REQUEST)
                .entity("{\"status\":400,\"error\":\"Bad Request\",\"message\":\"O parâmetro 'showAlerts' deve ser true ou false.\"}")
                .type(MediaType.APPLICATION_JSON)
                .build();
        }

        int start = offset;
        int end = offset + limit - 1;

        html.append("<h2>---------------</h2>");
        html.append("<h2>Census Online!</h2>");
        html.append("<h2>---------------</h2>");

        try {
            HttpClient client = HttpClient.newHttpClient();
            int nAlive = 0, nDead = 0, nUnknown = 0;

            for (int i = start; i <= end; i++) {
                String url = "https://rickandmortyapi.com/api/character/" + i;
                HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();

                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                JsonNode jsonNode = mapper.readTree(response.body());

                String species = jsonNode.get("species").asText();
                String status = jsonNode.get("status").asText();
                JsonNode episodes = jsonNode.get("episode");
                List<String> episodeList = mapper.convertValue(episodes, new TypeReference<List<String>>() {});

                if (status.equals("Alive")) nAlive++;
                if (status.equals("Dead")) nDead++;
                if (status.equals("unknown")) nUnknown++;

                if (showAlerts && species.equals("Alien") && status.equals("Dead")) {
                    html.append("<p style=\"color:red;\">[PERIGO] Um Alien foi encontrado morto com o ID ").append(i).append("!</p>");

                    String lastEpisode = episodeList.get(episodeList.size() - 1);
                    HttpRequest reqEp = HttpRequest.newBuilder().uri(URI.create(lastEpisode)).GET().build();
                    HttpResponse<String> respEp = client.send(reqEp, HttpResponse.BodyHandlers.ofString());
                    JsonNode epNode = mapper.readTree(respEp.body());
                    String episodeName = epNode.get("name").asText();
                    html.append("<p>[ALERTA FORENSE] O último registo do alien morto foi no episódio: '").append(episodeName).append("'.</p>");
                }

                Thread.sleep(100);
            }

            html.append("<p>CENSO: Detetados ").append(nAlive).append(" personagens VIVOS, ")
                .append(nDead).append(" personagens MORTOS e ")
                .append(nUnknown).append(" personagens DESCONHECIDOS entre os registos ")
                .append(start).append(" e ").append(end).append(" registos.</p>");

        } catch (Exception e) {
            html.append("<p>Exception: ").append(e.getClass().getName()).append(" - ").append(e.getMessage()).append("</p>");
            e.printStackTrace();
        }

        return Response.ok(html.toString(), MediaType.TEXT_HTML).build();
    }
}
package serv.censos;

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;


@WebServlet("/census")
public class App extends HttpServlet
{
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        
        resp.setContentType("text/html;charset=UTF-8");
        
        ObjectMapper mapper = new ObjectMapper();

        String offsetParam = req.getParameter("offset");
        String limitParam = req.getParameter("limit");
        String showAlertsParam = req.getParameter("showAlerts");

        int offset;

        if (offsetParam == null) {
            offset = 1;
        } else {
            try {
                offset = Integer.parseInt(offsetParam.trim());
            } catch (NumberFormatException e) {
                sendBadRequest(resp, mapper, "O parâmetro 'offset' deve ser um numero inteiro.");
                return;
            }
        }

        int limit;

        if (limitParam == null) {
            limit = 20;
        } else {
            try {
                limit = Integer.parseInt(limitParam.trim());
            } catch (NumberFormatException e) {
                sendBadRequest(resp, mapper, "O parâmetro 'limit' deve ser um numero inteiro.");
                return;
            }
            if (limit < 1 || limit > 50) {
                sendBadRequest(resp, mapper, "O parâmetro 'limit' deve ser um numero inteiro entre 1 e 50.");
                return;
            }
        }

        boolean showAlerts;
        if (showAlertsParam == null) {
            showAlerts = true;
        } else if (showAlertsParam.equals("true")) {
            showAlerts = true;
        } else if (showAlertsParam.equals("false")) {
            showAlerts = false;
        } else {
            sendBadRequest(resp, mapper, "O parâmetro 'showAlerts' deve ser true ou false.");
            return;
        }

        int start = offset;
        int end = offset + limit - 1; 

        resp.getWriter().println("<h2>---------------</h2>");
        resp.getWriter().println("<h2>Census Online!</h2>");
        resp.getWriter().println("<h2>---------------</h2>"); 

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
                
                String species= jsonNode.get("species").asText();
                String status = jsonNode.get("status").asText();
                

                JsonNode episodes = jsonNode.get("episode");
                
                List<String> episodeList = mapper.convertValue(episodes, new TypeReference<List<String>>() {});
                if (status.equals("Alive")) {
                    nAlive++;
                }
                
                if (status.equals("Dead")) {
                    nDead++;
                }

                if (status.equals("unknown")) {
                    nUnknown++;
                }

                if (showAlerts) {
                    if(species.equals("Alien") && status.equals("Dead")) {
                        resp.getWriter().println("<p style=\"color:red;\">[PERIGO] Um Alien foi encontrado morto com o ID " + i + "!</p>");

                        String lastEpisode = episodeList.get(episodeList.size() - 1);
                        
                        HttpRequest requestLastEpisode = HttpRequest.newBuilder()
                            .uri(URI.create(lastEpisode))
                            .GET()
                            .build();

                        HttpResponse<String> responseLastEpisode = client.send(requestLastEpisode, HttpResponse.BodyHandlers.ofString());
                        JsonNode jsonNodeLastEpisode = mapper.readTree(responseLastEpisode.body());
                        String episodeName = jsonNodeLastEpisode.get("name").asText();
                        resp.getWriter().println("<p>[ALERTA FORENSE] O último registo do alien morto foi no episódio: '" + episodeName + "'.</p>");
                    }
                }  
                Thread.sleep(100);
            }
            resp.getWriter().println("<p>CENSO: Detetados " + nAlive + " personagens VIVOS, " + nDead + " personagens MORTOS e " + nUnknown + " personagens DESCONHECIDOS entre os registos " + start + " e " + end + " registos.</p>");
        }
        catch(Exception e) {
            //resp.getWriter().println("<p>Exception generica.</p>");
            resp.getWriter().println("<p>Exception: " + e.getClass().getName() + " - " + e.getMessage() + "</p>");
            e.printStackTrace();
        }
    }

    private void sendBadRequest(HttpServletResponse resp, ObjectMapper mapper, String message) throws IOException {
        resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        resp.setContentType("application/json;charset=UTF-8");

        ObjectNode errorJson = mapper.createObjectNode();
        errorJson.put("status", 400);
        errorJson.put("error", "Bad Request");
        errorJson.put("message", message);

        resp.getWriter().println(mapper.writeValueAsString(errorJson));
    }
}
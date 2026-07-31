package crud.personagem;

import java.util.ArrayList;
import java.util.List;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/personagem/")
public class App 
{
    private static List<Personagem> personagens = new ArrayList<>();
    public class Personagem {
        private String nome;
        private String especie;
        private String comidaFavorita;

        public Personagem() {}

        public Personagem(String nome, String especie, String comidaFavorita) {
            this.nome = nome;
            this.especie = especie;
            this.comidaFavorita = comidaFavorita;
        }

        public String getNome() {
            return nome;
        }
        public String getEspecie() {
            return especie;
        }
        public String getComidaFavorita() {
            return comidaFavorita;
        }
        public void setNome(String nome) {
            this.nome = nome;
        }
        public void setEspecie(String especie) {
            this.especie = especie;
        }
        public void setComidaFavorita(String comidaFavorita) {
            this.comidaFavorita = comidaFavorita;
        }        
    }

    @GET
    public Response getAll() {
        return Response.ok(personagens).build();
    }

    @POST
    public Response create(
        @QueryParam("nome") String nome,
        @QueryParam("especie") String especie,
        @QueryParam("comidaFavorita") String comidaFavorita
    ) {
        Personagem personagem = new Personagem(nome, especie, comidaFavorita);
        personagens.add(personagem);
        String html = "<h2>Personagem criado com sucesso!</h2>" +
                  "<p><b>Nome:</b> " + nome + "</p>" +
                  "<p><b>Espécie:</b> " + especie + "</p>" +
                  "<p><b>Comida Favorita:</b> " + comidaFavorita + "</p>" +
                  "<a href='/api/personagem'>Ver todos os personagens</a>";
        return Response.status(Response.Status.CREATED).entity(html).build();
    }
}

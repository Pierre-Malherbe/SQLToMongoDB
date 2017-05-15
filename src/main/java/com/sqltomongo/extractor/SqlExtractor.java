package com.sqltomongo.extractor;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.mongodb.*;
import com.sqltomongo.datamodel.imdb.Actor;
import com.sqltomongo.datamodel.imdb.Movie;

public class SqlExtractor {

    // variables statiques pour le login

    private static final String MONGO_LOCAL_HOST = "localhost";
    private static final String MONGO_REMOTE_HOST = "127.0.0.1";
    private static final Integer MONGO_LOCAL_PORT = 32133;
    private static final Integer MONGO_REMOTE_PORT = 27017;

    private static final String REMOTE_HOST = "upjv.utard.me";
    private static final String HOST = "upjv.utard.me";
    private static final String SSH_USER = "USER";
    private static final String SSH_PASS = "PASS";

    private static final Integer MYSQL_LOCAL_HOST = 3232;
    private static final Integer MYSQL_REMOTE_HOST = 3306;
    private static final String MYSQL_USER = "USER";
    private static final String MYSQL_PASS = "PASS";
    private static final String MYSQL_URL = "jdbc:mysql://localhost:"+MYSQL_LOCAL_HOST+"/imdbidx";
    private static final String MYSQL_DRIVER_NAME = "com.mysql.jdbc.Driver";

    public static void main(String[] args) throws SQLException, IOException {

        Connection conn = null;
        Session session= null;

        try{
            java.util.Properties config = new java.util.Properties();
            config.put("StrictHostKeyChecking", "no");

            JSch jsch = new JSch();

            session=jsch.getSession(SSH_USER, HOST, 22);
            session.setPassword(SSH_PASS);
            session.setConfig(config);
            session.connect();

            // port forwading MYSQL
            session.setPortForwardingL(MYSQL_LOCAL_HOST, REMOTE_HOST, MYSQL_REMOTE_HOST);

            // port forwading Mongo
            session.setPortForwardingL(MONGO_LOCAL_PORT, MONGO_REMOTE_HOST, MONGO_REMOTE_PORT);

            // SQL QUERY
            Class.forName(MYSQL_DRIVER_NAME).newInstance();
            conn = DriverManager.getConnection (MYSQL_URL, MYSQL_USER, MYSQL_PASS);

            // MONGO DB PARTY
            MongoClient mongoClient = new MongoClient(MONGO_LOCAL_HOST, MONGO_LOCAL_PORT);

            System.out.println("Welcome, Start Querry, please wait ...");

            // Les films
            String query = "SELECT\n" +
                    "  title.id,\n" +
                    "  title.title,\n" +
                    "  GROUP_CONCAT(`movie_info`.`info` SEPARATOR ' ~ ') AS movie_info,\n" +
                    "  title.production_year\n" +
                    "FROM title\n" +
                    "LEFT JOIN movie_info ON movie_info.movie_id = title.id WHERE movie_info.info_type_id = 3\n" +
                    "GROUP BY title.id\n" +
                    "LIMIT 50;";

            // Les keywords
            String query2 = "SELECT\n" +
                    "  title.id,\n" +
                    "  title.title,\n" +
                    "  GROUP_CONCAT(`keyword`.`keyword` SEPARATOR ' ~ ') AS keywords\n" +
                    "FROM title\n" +
                    "JOIN movie_keyword ON movie_keyword.movie_id = title.id\n" +
                    "JOIN keyword ON keyword.id = movie_keyword.keyword_id\n" +
                    "GROUP BY title.id\n" +
                    "LIMIT 50;";

            // Les acteurs
            String query3 = "SELECT\n" +
                    "  title.id,\n" +
                    "  title.title,\n" +
                    "  GROUP_CONCAT(CONCAT_WS(' | ', `name`.`name`, `cast_info`.`role_id`) SEPARATOR '~') AS casting\n" +
                    "FROM title\n" +
                    "JOIN cast_info ON cast_info.movie_id = title.id\n" +
                    "JOIN name ON cast_info.person_id = name.id\n" +
                    "GROUP BY title.id\n" +
                    "LIMIT 50;";

            // On initialise et on execute la requete des films
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);

            // On initialise et on execute la requete des keywords
            Statement stmt2 = conn.createStatement();
            ResultSet rs2 = stmt2.executeQuery(query2);

            // On initialise et on execute la requete des acteurs
            Statement stmt3 = conn.createStatement();
            ResultSet rs3 = stmt3.executeQuery(query3);

            System.out.println("Querry OK, start mapping ...");

            //On créer une liste de films
            List<Movie> movies = new ArrayList<Movie>();

            // Tant que nos 3 requetes on encore des valeurs
            while (rs.next() && rs2.next() && rs3.next() ) {
                // On créer une liste d'acteur pour chaque film
                List<Actor> actors = new ArrayList<Actor>();
                // On créer un Movie temporaire qu'on ajoutera a notre liste de films
                Movie movieTemp = new Movie();

                // On cherche le casting, ou il y a le nom des différents acteurs
                String casting = rs3.getString("casting");

                // On utilise un substring pour decouper le casting
                String[] splitted = casting.split("[|]");
                for (String split: splitted) {
                    String[] splitByGenreSplitted = split.split("[~]");

                    if(splitByGenreSplitted[0].equals(" 1")) {  // Ici c'est un acteur

                        String nameactor = splitByGenreSplitted[1];

                        String query4 = "SELECT\n" +
                                "  name.name,\n" +
                                "  GROUP_CONCAT(`title`.`title` SEPARATOR ' ~ ') AS Movies\n" +
                                "FROM name\n" +
                                "JOIN cast_info ON cast_info.person_id = name.id\n" +
                                "JOIN title ON cast_info.movie_id = title.id\n" +
                                "WHERE name.name = \"" + nameactor + "\" "+
                                "GROUP BY name.id;";

                        Statement stmt4 = conn.createStatement();
                        ResultSet rs4 = stmt4.executeQuery(query4);

                        List<String> listMovies = new ArrayList<String>();

                        while (rs4.next()) {
                            String actorMovie = rs4.getString("Movies");
                            String[] listActorMovies = actorMovie.split("[~]");
                            for (String oneActorMovie: listActorMovies) {
                                listMovies.add(oneActorMovie);
                            }
                        }

                        Actor actorTemp = new Actor();
                        String[] splitFirstNameLastName = splitByGenreSplitted[1].split("[,]");
                        actorTemp.setNom(splitFirstNameLastName[0]);
                        actorTemp.setPrenom(splitFirstNameLastName[1]);
                        actorTemp.setGenre("Actor");
                        actorTemp.setDate_born("18 august 1996");
                        actorTemp.setMovies(listMovies);
                        actors.add(actorTemp);
                    } else if (splitByGenreSplitted[0].equals(" 2")) {  // Ici c'est une actrice
                        String nameactor = splitByGenreSplitted[1];

                        String query4 = "SELECT\n" +
                                "  name.name,\n" +
                                "  GROUP_CONCAT(`title`.`title` SEPARATOR ' ~ ') AS Movies\n" +
                                "FROM name\n" +
                                "JOIN cast_info ON cast_info.person_id = name.id\n" +
                                "JOIN title ON cast_info.movie_id = title.id\n" +
                                "WHERE name.name = \"" + nameactor + "\" "+
                                "GROUP BY name.id;";

                        Statement stmt4 = conn.createStatement();
                        ResultSet rs4 = stmt4.executeQuery(query4);

                        List<String> listMovies = new ArrayList<String>();

                        while (rs4.next()) {
                            String actorMovie = rs4.getString("Movies");
                            String[] listActorMovies = actorMovie.split("[~]");
                            for (String oneActorMovie: listActorMovies) {
                                listMovies.add(oneActorMovie);
                            }
                        }

                        Actor actorTemp = new Actor();
                        String[] splitFirstNameLastName = splitByGenreSplitted[1].split("[,]");
                        actorTemp.setNom(splitFirstNameLastName[0]);
                        actorTemp.setPrenom(splitFirstNameLastName[1]);
                        actorTemp.setGenre("Actress");
                        actorTemp.setDate_born("18 august 1996");
                        actorTemp.setMovies(listMovies);
                        actors.add(actorTemp);
                    } else if (splitByGenreSplitted[0].equals(" 8 ")) {  // Ici c'est le metteur en scene
                        movieTemp.setDirector(splitByGenreSplitted[1]);
                    }
                }

                // On set toutes les informations du film

                movieTemp.setId(rs.getString("id"));
                movieTemp.setTitle(rs.getString("title"));
                movieTemp.setInfo(rs.getString("movie_info"));
                movieTemp.setYear(rs.getString("production_year"));
                movieTemp.setKeywords(rs2.getString("keywords"));
                movieTemp.setActors(actors); // On set les acteurs
                movies.add(movieTemp);
            }

            System.out.println("Mapping ok ... Convert BD to Mongo ... Please wait");

            // On créer une DB en mongoDB
            DB db = mongoClient.getDB("BEUZEMALHRBE");
            // On utilise cette DB
            DBCollection table = db.getCollection("BEUZEMALHERBE");

            // On ittére sur tous les films de notre liste ci-dessus
            for (Movie movie : movies) {
                BasicDBObject document = new BasicDBObject();
                document.put("Id", movie.getId());
                document.put("Title", movie.getTitle());
                document.put("Movie_info", movie.getInfo());
                document.put("Production_Year", movie.getYear());
                document.put("Keywords", movie.getKeywords());
                document.put("Director", movie.getDirector());

                List<BasicDBObject> listActor = new ArrayList<BasicDBObject>();

                // Pour chaque film on ittére sur tous les acteurs du film
                for (Actor actor : movie.getActors()) {
                    BasicDBObject oneActor = new BasicDBObject();
                    oneActor.put("nom", actor.getNom());
                    oneActor.put("prenom", actor.getPrenom());
                    oneActor.put("genre", actor.getGenre());
                    oneActor.put("date", actor.getDate_born());
                    oneActor.put("movies", actor.getMovies());
                    listActor.add(oneActor);
                }
                document.put("actors", listActor);

                // On insert dans la base de donnée
                table.insert(document);
            }

            System.out.println("Done, have fun :) Théo Beuze & Pierre Malherbe INSSET");
        }catch(Exception e){
            e.printStackTrace();
        }finally{
            if(conn != null && !conn.isClosed()){
                System.out.println("Closing Database Connection");
                conn.close();
            }
            if(session !=null && session.isConnected()){
                System.out.println("Closing SSH Connection");
                session.disconnect();
            }
        }
    }
}

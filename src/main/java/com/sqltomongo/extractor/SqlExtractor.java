package com.sqltomongo.extractor;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.mongodb.*;
import com.sqltomongo.datamodel.imdb.Movie;

public class SqlExtractor {

    private static final String MONGO_LOCAL_HOST = "";
    private static final String MONGO_REMOTE_HOST = "";
    private static final Integer MONGO_LOCAL_PORT = ;
    private static final Integer MONGO_REMOTE_PORT = ;

    private static final String REMOTE_HOST = "";
    private static final String HOST = "";
    private static final String SSH_USER = "";
    private static final String SSH_PASS = "";

    private static final Integer MYSQL_LOCAL_HOST = ;
    private static final Integer MYSQL_REMOTE_HOST = ;
    private static final String MYSQL_USER = "";
    private static final String MYSQL_PASS = "";
    private static final String MYSQL_URL = "";
    private static final String MYSQL_DRIVER_NAME = "com.mysql.jdbc.Driver";

    /**
     * Java Program to connect to remote database through SSH using port forwarding
     * @author Pierre Malherbe
     * @throws SQLException
     */
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

            // Set port forwading MYSQL
            session.setPortForwardingL(MYSQL_LOCAL_HOST, REMOTE_HOST, MYSQL_REMOTE_HOST);

            // Set port forwading Mongo
            session.setPortForwardingL(MONGO_LOCAL_PORT, MONGO_REMOTE_HOST, MONGO_REMOTE_PORT);

            // SQL QUERY
            Class.forName(MYSQL_DRIVER_NAME).newInstance();
            conn = DriverManager.getConnection (MYSQL_URL, MYSQL_USER, MYSQL_PASS);

            // MONGO DB PARTY
            System.out.println("MONGODB");
            MongoClient mongoClient = new MongoClient(MONGO_LOCAL_HOST, MONGO_LOCAL_PORT);


            String query = "SELECT title.id, title.title, title.production_year, name.name AS director, movie_info.info FROM title,name, movie_info\n" +
                    "INNER JOIN cast_info\n" +
                    "WHERE title.id = cast_info.movie_id\n" +
                    "AND cast_info.person_id = name.id\n" +
                    "AND cast_info.role_id = 8\n" +
                    "AND title.id = movie_info.movie_id\n" +
                    "AND movie_info.info_type_id = 3\n" +
                    "LIMIT 10;";

            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);

            List<Movie> movies = new ArrayList<Movie>();

            while (rs.next()) {
                Movie movieTemp = new Movie();

                Integer id = rs.getInt("id");
                Integer production_year = rs.getInt("production_year");
                String director = rs.getString("director");
                String info = rs.getString("info");

                movieTemp.setId(id);
                movieTemp.setYear(production_year);
                movieTemp.setDirector(director);
                movieTemp.setInfo(info);

                movies.add(movieTemp);
            }

            DB db = mongoClient.getDB("pierremalherbe");
            DBCollection table = db.getCollection("pierremalherbe");

            Set<String> tables = db.getCollectionNames();

            for (Movie movie : movies) {
                BasicDBObject document = new BasicDBObject();
                document.put("Id", movie.getId());
                document.put("production_year", movie.getYear());
                document.put("Director", movie.getDirector());
                document.put("Id", movie.getInfo());
                table.insert(document);
            }

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

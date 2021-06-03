package src;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import src.StarsinMovies;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class StarsinMoviesParser{

    List<StarsinMovies> slist = new ArrayList<>();
    HashSet<String> databaseMovieIds = new HashSet<>();
    HashMap<String, String> databaseStars = new HashMap<>();
    Document dom;
    FileWriter inconsistencies;
    FileWriter sqlFile;
    int highestID;

    public StarsinMoviesParser() {
        try {
            inconsistencies = new FileWriter("starMoviesInconsistencies.txt");
            sqlFile = new FileWriter("casts124Inserts.sql");
            sqlFile.write("USE moviedbexample;\n" +
                    "BEGIN;\n");
            highestID = -1;
        } catch (Exception e) {
            System.out.println("error from creating file: " + e.getMessage());
        }
    }

    public void runExample() {

        // parse the xml file and get the dom object
        parseXmlFile();

        loadDatabaseMovieIds();
        // get each employee element and create a Employee object
        parseDocument();

        // iterate through the list and print the data
        printData();

    }
    public void loadDatabaseMovieIds() {
        String query = "select id from movies";
        String query2 = "select name,id from stars";
        try
        {
            Class.forName("com.mysql.cj.jdbc.Driver").newInstance();
            Connection conn = DriverManager.getConnection("jdbc:" + "mysql" + ":///" + "moviedbexample" + "?autoReconnect=true&useSSL=false",
                    "mytestuser", "Nonie127");
            PreparedStatement statement = conn.prepareStatement(query);
            PreparedStatement statement2 = conn.prepareStatement(query2);
            ResultSet rs = statement.executeQuery();
            while (rs.next())
            {
                databaseMovieIds.add(rs.getString("id"));
            }
            ResultSet rs2 = statement2.executeQuery();
            while (rs2.next())
            {
                databaseStars.put(rs2.getString("name"), rs2.getString("id"));
            }
            conn.close();
            rs.close();
            rs2.close();
            statement.close();
            statement2.close();
        }
        catch (Exception e) {
            System.out.println(e.getMessage());

        }
    }

    private void parseXmlFile() {
        // get the factory
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();

        try {

            // using factory get an instance of document builder
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();

            // parse using builder to get DOM representation of the XML file
            dom = documentBuilder.parse("casts124.xml");

        } catch (ParserConfigurationException | SAXException | IOException error) {
            error.printStackTrace();
        }
    }

    private void parseDocument() {
        // get the document root Element
        Element documentElement = dom.getDocumentElement();

        // get a nodelist of employee Elements, parse each into Employee object
        NodeList nodeList = documentElement.getElementsByTagName("m");
        if (nodeList != null) {
            for (int i = 0; i < nodeList.getLength(); i++) {

                // get the employee element
                Element element = (Element) nodeList.item(i);

                // get the Employee object
                StarsinMovies s = parseMovie(element);

                // add it to list
                try {
                    String m = s.getMovieId();
                    String n = s.getStarName();
                    if (databaseMovieIds.contains(s.getMovieId()) && databaseStars.containsKey(s.getStarName())) {
//                        sqlFile.write("INSERT INTO movies VALUES(\"" + movie.getTitle() + "\"," + movie.getYear() + ",\"" +
//                                movie.getDirector() + "\");\n");
                        sqlFile.write(String.format("INSERT INTO stars_in_movies VALUES(\"%s\",\"%s\");\n",
                                databaseStars.get(s.getStarName()), s.getMovieId()));
                    } else {
                        inconsistencies.write("src.Movie ID: " + s.getMovieId() + "\n" +
                                "Star Name:        " + s.getStarName() + "\n" + "\n");
                    }
                } catch (Exception e) {
                    System.out.println("error writing to file: " + e.getMessage());
                }
            }
        }
    }

    /**
     * It takes an employee Element, reads the values in, creates
     * an Employee object for return
     */
    private StarsinMovies parseMovie(Element element) {

        // for each <employee> element get text or int values of
        // name ,id, age and name
        String movieId = getTextValue(element, "f");
        String starName = getTextValue(element, "a");


        // create a new Employee with the value read from the xml nodes
        return new StarsinMovies(movieId, starName);
    }

    /**
     * It takes an XML element and the tag name, look for the tag and get
     * the text content
     * i.e for <Employee><Name>John</Name></Employee> xml snippet if
     * the Element points to employee node and tagName is name it will return John
     */
    private String getTextValue(Element element, String tagName) {
        String textVal = null;
        NodeList nodeList = element.getElementsByTagName(tagName);
        if (nodeList != null && nodeList.getLength() > 0) {
            // here we expect only one <Name> would present in the <Employee>
            if (nodeList.item(0).getFirstChild() != null)
            {
                textVal = nodeList.item(0).getFirstChild().getNodeValue();
            }

        }
        return textVal;
    }


    /**
     * Iterate through the list and print the
     * content to console
     */
    private void printData() {

        try {
            sqlFile.write("COMMIT;");
            sqlFile.close();
            inconsistencies.close();
            System.out.println("Finished parsing casts124.xml\n" +
                               "queries are in casts124Inserts.sql\n" +
                               "inconsistencies in starMoviesInconsistencies.txt");
        } catch (Exception e) {
            System.out.println("error writing to file:" + e.getMessage());
        }
    }

    public static void main(String[] args) {
        // create an instance
        StarsinMoviesParser domParserExample = new StarsinMoviesParser();

        // call run example
        domParserExample.runExample();
    }

}

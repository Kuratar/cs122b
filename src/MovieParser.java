import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.*;
import java.util.HashSet;

public class MovieParser{
    HashSet<String> databaseMovies = new HashSet<>();
    FileWriter inconsistencies;
    FileWriter sqlFile;
    int highestID;
    Document dom;

    public MovieParser() {
        try {
            inconsistencies = new FileWriter("movieInconsistencies.txt");
            sqlFile = new FileWriter("mains243Inserts.sql");
            sqlFile.write("USE moviedbexample;\n" +
                    "BEGIN;\n");
            highestID = -1;
        } catch (Exception e) {
            System.out.println("error from creating file: " + e.getMessage());
        }
    }

    public void loadDatabaseMovies() {
        String query = "select title,year,director from movies";
        String query2 = "select max(id) from movies";
        try
        {
            Class.forName("com.mysql.cj.jdbc.Driver").newInstance();
            Connection conn = DriverManager.getConnection("jdbc:" + "mysql" + ":///" + "moviedbexample" + "?autoReconnect=true&useSSL=false&allowPublicKeyRetrieval=true",
                    "mytestuser", "My6$Password");
            PreparedStatement statement = conn.prepareStatement(query);
            PreparedStatement statement2 = conn.prepareStatement(query2);

            ResultSet rs = statement.executeQuery();
            while (rs.next())
            {
                String info = "";
                info += rs.getString("title") + rs.getString("year") + rs.getString("director");
                databaseMovies.add(info);
            }

            ResultSet rs2 = statement2.executeQuery(); rs2.next();
            highestID = Integer.parseInt(rs2.getString("max(id)").substring(2));
            rs.close();
            statement.close();
            conn.close();
        }
        catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public void runExample() {

        // parse the xml file and get the dom object
        parseXmlFile();

        loadDatabaseMovies();

        // get each employee element and create a Employee object
        parseDocument();


        // iterate through the list and print the data
        printData();

    }

    private void parseXmlFile() {
        // get the factory
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();

        try {

            // using factory get an instance of document builder
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();

            // parse using builder to get DOM representation of the XML file
            dom = documentBuilder.parse("mains243.xml");

        } catch (ParserConfigurationException | SAXException | IOException error) {
            error.printStackTrace();
        }
    }

    private void parseDocument() {
        // get the document root Element
        Element documentElement = dom.getDocumentElement();

        // get a nodelist of employee Elements, parse each into Employee object
        NodeList nodeList = documentElement.getElementsByTagName("film");
        if (nodeList != null) {
            for (int i = 0; i < nodeList.getLength(); i++) {

                // get the employee element
                Element element = (Element) nodeList.item(i);

                // get the Employee object
                Movie movie = parseMovie(element);

                String info = movie.getTitle() + movie.getYear() + movie.getDirector();
                // add it to list
                try {
                    if (!databaseMovies.contains(info))
                    {
//                        sqlFile.write("INSERT INTO movies VALUES(\"" + movie.getTitle() + "\"," + movie.getYear() + ",\"" +
//                                movie.getDirector() + "\");\n");
                        sqlFile.write(String.format("INSERT INTO movies VALUES(\"%s\",\"%s\",%d,\"%s\");\n",
                        "tt"+(highestID+1),movie.getTitle(),movie.getYear(),movie.getDirector()));
                        highestID++;
                        databaseMovies.add(info);
                    }
                    else {
                        inconsistencies.write("Movie Title: " + movie.getTitle() + "\n" +
                                "Year:        " + movie.getYear() + "\n" +
                                "Director:    " + movie.getDirector()+ "\n\n");
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
    private Movie parseMovie(Element element) {

        // for each <employee> element get text or int values of
        // name ,id, age and name
        String id = getTextValue(element, "fid");
        String title = getTextValue(element, "t");
        int year = getIntValue(element, "year");
        String director = getTextValue(element, "dirn");
        String genres = convertGenres(getGenres(element));

        // create a new Employee with the value read from the xml nodes
        return new Movie(id, title, year, director, genres);
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
    private String getGenres(Element element) {
        String textVal = "";
        NodeList nodeList = element.getElementsByTagName("cat");
        for (int i = 0; i < nodeList.getLength(); i++) {
            if (nodeList != null && nodeList.getLength() > 0) {
                // here we expect only one <Name> would present in the <Employee>
                if (nodeList.item(0).getFirstChild() != null)
                {
                    textVal += nodeList.item(i).getFirstChild().getNodeValue() + ",";
                }

            }
        }
        textVal = textVal.replaceAll(",$", "");
        return textVal;
    }

    /**
     * Calls getTextValue and returns a int value
     */
    private int getIntValue(Element ele, String tagName) {
        // in production application you would catch the exception
        if (getTextValue(ele, tagName).matches("[0-9]+"))
        {
            return Integer.parseInt(getTextValue(ele, tagName));
        }
        return 0;
    }

    private String convertGenres(String genres) {
        String result = "";

        for (String genre: genres.split(","))
        {
            switch (genre){
                case "Actn":
                    result += "Action,";
                    break;
                case "Cart":
                    result += "Animation";
                    break;
                case "Advt":
                    result += "Adventure,";
                    break;
                case "BioP":
                    result += "Biography,";
                    break;
                case "Comd":
                    result += "Comedy,";
                    break;
                case "Docu":
                    result += "Documentary,";
                    break;
                case "Dram":
                    result += "Drama,";
                    break;
                case "Faml":
                    result += "Family,";
                    break;
                case "Fant":
                    result += "Fantasy";
                case "Hist":
                    result += "History";
                    break;
                case "Horr":
                    result += "Horror,";
                    break;
                case "Musc":
                    result += "Musical,";
                    break;
                case "Myst":
                    result += "Mystery,";
                    break;
                case "Romt":
                    result += "Romance,";
                    break;
                case "S.F.":
                    result += "Sci-Fi,";
                    break;
                case "Susp":
                    result += "Thriller,";
                    break;
                case "West":
                    result += "Western,";
                    break;
                default:
                    result += genre + ",";
            }
        }
        result = result.replaceAll(",$", "");
        return result;
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
        } catch (Exception e) {
            System.out.println("error writing to file:" + e.getMessage());
        }
    }

    public static void main(String[] args) {
        // create an instance
        MovieParser domParserExample = new MovieParser();

        // call run example
        domParserExample.runExample();
    }

}
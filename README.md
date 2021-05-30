# cs122b-spring21-team-93

Project 1 Video Demo URL:                   https://vimeo.com/535382852

Project 2 Video Demo URL:                   https://vimeo.com/542397737

Project 3 Video Demo Redo URL (No pauses):  https://vimeo.com/552690167

Project 4 Video Demo URL:                   https://youtu.be/qcjEw1WuSNo

Instructions for Deployment:
    1. git clone into repository
    2. run mvn clean package
    3. move built war into tomcat/webapps
    4. go to URL: http://ec2-18-188-209-46.us-east-2.compute.amazonaws.com:8080/cs122b-spring21-project1-api-example/

Project 4 Instructions for Deployment:
    1. git clone into repository
    2. run mvn clean package
    3. move built war into tomcat/webapps
    4. go to URL: https://18.144.57.54:8443/cs122b-spring21-project1-api-example/

Contributions:

Project 1:

    Rich:   Worked on MoviesServlet.java, single-star.html, single-star.js, AWS server testing, video demo

    Eric:   Worked on index.html, index.js, single-movie.html, single-movie.js, SingleMovieServlet.java, SingleStarServlet.java

Project 2:

    Rich:   CartServlet, DecreaseCartServlet, PaymentServlet, PlaceOrderServlet, SearchServlet,
            cart.html, cart.js, payment.html, payment.js, place-order.html, search.html, search.js,
            single-movie.html

    Eric:   BrowseByGenreServlet, BrowseByTitleServlet, IndexBrowseGenreServlet, LoginFilter, LoginServlet,
            MoviesServlet, SearchServlet, SingleMovieServlet, SingleStarServlet, User,
            browse.html, browse.js, browse-genre.html, browse-genre.js, cart.html, cart.js, index.html, index.js,
            login.html, login.js, movielist.html, movielist.js, payment.html, place-order.html,
            search.html, search.js, single-movie.html, single-movie.js, single-star.html, single-star.js

Project 3:

    Rich:   Task 2, Task 3, creating DOM Parser files with basic framework, partial completion of Stars and
            StarsinMovies parsers for insert statement creations, Video Demo

    Eric:   Task 4, Task 5, Task6, Debugging and completion of all parser files

    Task 7 Optimization Report
    Unfortunately, we did not initially implement our parsers with a naive implementation and test the timings. We
    immediately started to implement our optimization techniques along with developing the parser when we started
    working on the Task 7.

    Optimization Technique 1
    Our first optimization technique was using a in-memory hash table to keep the database's current and added movies,
    stars, and genres. This technique allowed us to eliminate duplicates, preveting the parser from creating insertion
    queries that would insert duplicates. The parser would initially get all the current movies/stars/genres and store
    them into the correspoding HashSet/HashMap. As the parser parses the xml files, it will add the new 
    movies/stars/genres into the HashSet/HashMap to prevent duplicates coming from the same xml file.

    Optimization Technique 2
    Our second optimization technique was writing to a SQL file. This technique although not covered in lectures, seems
    similar to writing to a csv file and using load data. We first thought to write to a csv file then we remembered a
    piazza post (https://piazza.com/class/kmp5a06y7zw243?cid=150) where the entire movie-data.sql file took only 3-4 
    mins to execute all (187k) insertion statements with this technique. Essentially, this is also similar to batch 
    insert except all the insertion statements generated by the parser is one transaction instead of multiple, 
    large transactions.

    Time Total of Parsing
    MoviesParser parsing time + mains243Inserts.sql execution time + mains243Genres.sql execution time = 
    5.618 s + 1 s + 1 s = 7.618 seconds

    StarsParser parsing time + actors63Inserts.sql execution time + =
    4.223 s + 1s = 5.223 seconds

    StarsinMoviesParser parsing time + casts124Inserts.sql execution time = 
    5.143 s + 4 s = 9.143 seconds

    Total time = 21.984 seconds

Project 4:

    Rich:   Task 2 bulk of android Implementation

    Eric:   Task 1, Task 2 movie list pagination and recaptcha filtering, video demo

Project 5:

    Rich:   asdasd

    Eric:   asdasd

    Task 1 JDBC Connection Pooling and Prepared Statements:
    
    Connection Pooling: cs122b-spring21-team-93/WebContent/META-INF/context.xml
    The path listed above is where we enabled JDBC Connection Pooling. We added the two statements:
        factory="org.apache.tomcat.jdbc.pool.DataSourceFactory"
        maxTotal="100" maxIdle="30" maxWaitMillis="10000"

    maxTotal        -   Represents the maximum number of connections allowed in the pool at any time which is set to 
                        100 connections
    maxIdle         -   Represents the total number of idle connections or "unused" connections allowed in the pool 
                        at any time which is set to 30 connections
    maxWaitMillis   -   Represents the timeout in terms of milliseconds. The timeout is described as the amount of time 
                        the connection "waits" for the database to accept the connection. 
                        This is set to 10,000 milliseconds which is 10 seconds.

    Prepared Statements: cs122b-spring21-team-93/src
    Prepared Statements are all created within the folder where the path of the folder is listed above. 
    Files included are:
        AutoSearchServlet.java
        BrowseByGenreServlet.java
        BrowseByTitleServlet.java
        CartServlet.java
        CheckMovieProcedure.java
        IndexBrowseGenreServlet.java
        InsertMovieServlet.java
        InsertStarServlet.java
        LoginServlet.java
        LoginStaffServlet.java
        PlaceOrderServlet.java
        SearchServlet.java
        ShowDatabaseMetadata.java
        SingleMovieServlet.java
        SingleStarServlet.java

    Most of the Java files use Prepared Statements in their code since they are Java Servlets that talk to the database 
    to get the data that they need. We created Prepared Statements by these steps:
        1.  Defining the MySQL query as a string with the symbol "?" at places within the string where the servlet 
            inputs information entered by the user or information sent from the webpage. 

        2.  Then in a try-catch block, we create the Connection to the database and create the Prepared Statements
            using the Connection's prepareStatement() call by passing in the query string we created earlier.

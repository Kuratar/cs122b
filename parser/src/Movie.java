public class Movie {

    private final String id;

    private final String title;

    private final int year;

    private final String director;

    private final String genres;

    public Movie(String id, String title, int year, String director, String genres) {
        this.id = id;
        this.title = title;
        this.year = year;
        this.director = director;
        this.genres = genres;

    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public int getYear() {
        return year;
    }

    public String getDirector() {
        return director;
    }

    public String getGenres() {
        return genres;
    }

    public String toString() {

        return "ID:" + getId() + ", " +
                "Title:" + getTitle() + ", " +
                "Year:" + getYear() + ", " +
                "Director:" + getDirector() + "." +
                "Genres:" + getGenres() + ".";
    }
}

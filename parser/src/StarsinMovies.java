public class StarsinMovies {

    private final String movieId;

    private final String starName;

    public StarsinMovies(String movieId, String starName) {
        this.movieId = movieId;
        this.starName = starName;

    }

    public String getMovieId() {
        return movieId;
    }

    public String getStarName() {
        return starName;
    }


    public String toString() {

        return "ID:" + getMovieId() + ", " +
                "Star Name:" + getStarName() + ".";
    }
}

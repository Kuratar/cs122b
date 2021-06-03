package src;

public class Stars {

    private final String name;

    private final int birthYear;

    public Stars(String name, int birthYear) {

        this.name = name;
        this.birthYear = birthYear;

    }


    public String getName() {
        return name;
    }

    public int getBirthYear()
    {
        return birthYear;
    }

    public String toString() {

        return "Name:" + getName() + ", " +
                "DOB:" + getBirthYear() + ".";
    }
}

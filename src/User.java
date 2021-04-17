/**
 * This User class only has the username field in this example.
 * You can add more attributes such as the user's shopping cart items.
 */
public class User {

    private final int id;
    private final String firstName;
    private final String lastName;
    private final int ccId;
    private final String address;

    public User(int id, String firstName, String lastName,
                int ccId, String address) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.ccId = ccId;
        this.address = address;
    }

}

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class RecaptchaConstants {
    private String SECRET_KEY;

    public RecaptchaConstants() {
        try {
            File file = new File("/home/ubuntu/cs122b-spring21-team-93/secretKey.txt");
            Scanner reader = new Scanner(file);
            if (reader.hasNextLine()) {
                SECRET_KEY = reader.nextLine();
            }
            reader.close();
        } catch (FileNotFoundException e) {
            System.out.println(new File(".").getAbsolutePath());
            System.out.println("Could not find file");
            e.printStackTrace();
        }
    }

    public String getKey() {
        return SECRET_KEY;
    }
}

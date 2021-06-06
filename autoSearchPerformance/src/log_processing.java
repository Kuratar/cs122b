import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class log_processing {
    public static void main(String[] args) {
        try {
            File f = new File("autoSearchPerformance/performances/logs/single1.txt");
            Scanner reader = new Scanner(f);

            long lines = 0;
            long totalTS = 0;
            long totalTJ = 0;
            while (reader.hasNextLine()) {
                String data = reader.nextLine();
                lines++;
                String[] dataSplit = data.split(" ");

                // TJ convert to long, add all, convert to ms
                // TS convert to long, convert to ms
                long TJ = (Long.parseLong(dataSplit[0]) + Long.parseLong(dataSplit[1]) + Long.parseLong(dataSplit[2])) / 1000000;
                long TS = Long.parseLong(dataSplit[3]) / 1000000;

                totalTS += TS;
                totalTJ += TJ;
            }
            System.out.println("Samples: " + lines);

            System.out.println("**TS** Average Search Servlet Time(ms): " + (totalTS / lines));
            System.out.println("**TJ** Average JDBC Time(ms):           " + (totalTJ / lines));

            reader.close();
        } catch (FileNotFoundException e) {
            System.out.print("FileNotFoundException: " + e.getMessage());
        }
    }
}

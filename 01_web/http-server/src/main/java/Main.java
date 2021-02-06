import java.io.*;
import java.nio.file.Path;

public class Main {
    public static void main(String[] args) {
        final var filePath = Path.of(".", "public", "data.jpg");

        try (var in = new BufferedInputStream(new FileInputStream(new File("/Users/maksim/Desktop/image.jpg")));
             var out = new BufferedOutputStream(new FileOutputStream(new File(filePath.toString())))) {
            out.write(in.readAllBytes());
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

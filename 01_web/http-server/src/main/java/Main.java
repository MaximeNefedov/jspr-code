import java.io.*;

public class Main {
    public static void main(String[] args) {
        // Путь для финальной картинки
        File file = new File("data.jpg");
        // Путь до исходной (если записать ее в текстовый документ, то по итогу запишется 1023 строки (см originalImage.txt))
        File original = new File("image.jpg");

        File originalTxt = new File("originalImage.txt");

        // Байты изображения, переданные в теле POST запроса (multipart/form-data) см test.html в директории public
        // Я решил их предварительно проверить на целостность, скопировав прямо из консоли (в итоге в тектовом документе 510 строк)
        File file2 = new File("image.txt");

        try (FileInputStream in = new FileInputStream(file2);
             FileOutputStream out = new FileOutputStream(file)) {

            // Content-Length= 127129
            int x;
            int available = in.available();
            byte[] bytes = new byte[available];
            int counter = 0;
            while ((x = in.read()) != -1) {
                bytes[counter++] = (byte) x;
            }
            System.out.println(bytes.length);
            out.write(bytes);
            out.flush();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

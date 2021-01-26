import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {
    public static void main(String[] args) throws URISyntaxException {
//        String url = "GET /messages.html?name=Max&surname=Nefedov&name=Neil&surname=Smith&name=John&surname=Davis /HTTP/1.1";
//        String[] parts = url.split(" ");
////        String url = "GET /messages.html /HTTP/1.1";
//        Pattern pattern = Pattern.compile(".+\\?");
//        Matcher matcher = pattern.matcher(parts[1]);
//        while (matcher.find()) {
//            System.out.println(matcher.group().replaceAll("\\?", ""));
//        }

//        String[] parts = url.split(" ");
//        System.out.println(parts[1]);
//        System.out.println(parts[1].matches(".+\\?.+"));
//        if (parts[1].matches("\\?.+")) {
//            List<NameValuePair> parse = URLEncodedUtils.parse(new URI(parts[1]), StandardCharsets.UTF_8);
//            Map<String, List<String>> map = new HashMap<>();
//            for (NameValuePair nameValuePair : parse) {
//                if (map.get(nameValuePair.getName()) != null) {
//                    map.get(nameValuePair.getName()).add(nameValuePair.getValue());
//                } else {
//                    List<String> list = new ArrayList<>();
//                    list.add(nameValuePair.getValue());
//                    map.put(nameValuePair.getName(), list);
//                }
//            }
//            System.out.println(parse);
//            System.out.println();
//            System.out.println(map);
//        } else {
//            System.out.println("nope");
//        }

        String url = "Name=%D0%9C%D0%B0%D0%BA%D1%81%D0%B8%D0%BC&" +
                "Surname=%D0%98%D0%B2%D0%B0%D0%BD%D0%BE%D0%B2";
//        String url = "Name=Max&Surname=Nefedov";
        System.out.println(URLDecoder.decode(url, StandardCharsets.UTF_8));


    }

}

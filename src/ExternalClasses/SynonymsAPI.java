package ExternalClasses;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URLConnection;
import java.io.IOException;
import java.util.ArrayList;
public class SynonymsAPI {
    private final String USER_AGENT = "Mozilla/5.0";

    public String[] getSynonyms(String wordInput) {
        String[] resultArray = {};
        if (wordInput.equals("")) {
            return resultArray;
        }
        try {
            URL url = new URL("https://www.thesaurus.com/browse/" + wordInput);
            //URLConnection yc = url.openConnection();
            HttpURLConnection con = (HttpURLConnection) url.openConnection();

            con.setRequestMethod("GET");
            con.setRequestProperty("User-Agent", USER_AGENT);
            String foundWords;
            try (BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()))) {
                String inputLine;
                foundWords = "";
                boolean foundListStart = false;
                while ((inputLine = in.readLine()) != null) {
                    String iLine = inputLine.trim();
                    String[] splited=null;
                  if (iLine.contains("<ul class=\"css-1lc0dpe et6tpn80\">")) {
                        foundListStart = true;
                        splited=iLine.split(">");
                    }
                    if (foundListStart) {
                      boolean checker=false;
                        for (int i = 0; i <splited.length ; i++) {
                            if (splited[i].contains("<ul class=\"css-1lc0dpe et6tpn80\""))
                                checker=true;
                            String check=splited[i];
                            if (checker&&splited[i].contains("</ul")) {
                                foundListStart = false;
                                break;
                            }
                            if (splited[i].startsWith("<a href=")) {
                                String[] codeLines = splited[i].split(" ");
                                int index = codeLines[1].lastIndexOf('/');
                                String word = codeLines[1].substring(index + 1, codeLines[1].length());
                                word = word.replace("%27", "'").replace("%20", " ").replace("\"", "");
                                word = word.trim();
                                if (word.length() > 1 && !word.equals("www.Dictionary.com") && !word.equals("www.Thesaurus.com")&&!word.equals("www.thesaurus.com")&&!word.equals(wordInput.toLowerCase())&&!word.contains(" ")&&!word.contains("href" )) {
                                    if (foundWords.equals("")) {
                                        foundWords += word;
                                    } else {
                                        foundWords += "," + word;
                                    }
                                }
                            }
                        }
                    }
                }
            }
            // Convert built comma delimited string to String Array
            if (!foundWords.equals("")) {
                resultArray = foundWords.split(",");
            }
        } catch (MalformedURLException ex) {
            // Do what you want with exception
        } catch (IOException ex) {
            // Do what you want with exception
        }
        if(resultArray!=null&&resultArray.length>1){
            String[] result=new String[1];
            for (int i = 0; i <4 ; i++) {
                result[i]=resultArray[i];
            }
        }
        return resultArray;
    }

    public static void main(String[] args) throws Exception {
        SynonymsAPI http = new SynonymsAPI();

        String[] syns=http.getSynonyms("baby");
        if(syns!=null) {
            for (int i = 0; i < syns.length; i++) {
                System.out.println( syns[i] );

            }
        }
    }
}

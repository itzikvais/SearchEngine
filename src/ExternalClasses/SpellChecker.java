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
import java.util.List;

public class SpellChecker {
    public List<String> splitCrudeAnswer(String crudeAnswer){
        List<String> words = new ArrayList<>();
        for (int nextIndex = crudeAnswer.indexOf("{\"word\":\""); nextIndex>0; nextIndex = crudeAnswer.indexOf("{\"word\":\"")){
            crudeAnswer = crudeAnswer.substring(nextIndex+9);
            int scoreStartIndex = crudeAnswer.indexOf("\",\"score\":");
            String word = crudeAnswer.substring(0,scoreStartIndex);
            if(word.contains(" ")) {
                word=word.replaceAll(" ","-");
            }
            words.add(word);
        }
        return words;
    }
    String spelledSimilarMaxResults(String word, int maxResults) {
        String s = word.replaceAll(" ", "+");
        return null;
        //return getJSON("http://api.datamuse.com/words?sp=" + s + "&max=" + maxResults);
    }
}

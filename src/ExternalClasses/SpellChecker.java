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
import java.util.Scanner;
public class SpellChecker {
    private final String USER_AGENT = "Mozilla/5.0";

    public static void main(String[] args) throws Exception {
        SpellChecker http = new SpellChecker();

        String syns=http.checkSpell("pilloq");
        System.out.println(syns);

    }



    public String checkSpell(String wordToSearch) throws Exception {
        String synWords="";
        String url = "http://api.datamuse.com/words?sp=" + wordToSearch+ "&max=1";

        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();

        con.setRequestMethod("GET");
        con.setRequestProperty("User-Agent", USER_AGENT);

        int responseCode = con.getResponseCode();

        // ordering the response
        StringBuilder response;
        try (BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()))) {
            String inputLine;
            response = new StringBuilder();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
        }

        ObjectMapper mapper = new ObjectMapper();

        try {
            // converting JSON array to ArrayList of words
            ArrayList<Word> words = mapper.readValue(
                    response.toString(),
                    mapper.getTypeFactory().constructCollectionType(ArrayList.class, Word.class)
            );

            if(words.size() > 0) {
                for(Word word : words) {
                    if(!word.getWord().contains( " " ))
                        synWords+=word.getWord()+",";
                }
            }
        }
        catch (IOException e) {
            e.getMessage();
        }
        if(synWords!=null&&synWords.length()>1) {
            synWords = synWords.substring(0, synWords.length() - 1);
            if (synWords.equals(wordToSearch))
                return null;
            else
                return synWords;
        }
        return null;

    }

    // word and score attributes are from DataMuse API
    static class Word {
        private String word;
        private int score;

        public String getWord() {
                return this.word;
        }
        public int getScore() {return this.score;}
    }

}

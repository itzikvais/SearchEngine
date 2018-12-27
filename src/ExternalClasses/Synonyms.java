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
public class Synonyms {
    private final String USER_AGENT = "Mozilla/5.0";

    public static void main(String[] args) throws Exception {
        Synonyms http = new Synonyms();

        String[] syns=http.searchSynonym("car");
        if(syns!=null) {
            for (int i = 0; i < syns.length; i++) {
                System.out.println( syns[i] );

            }
        }
    }



    public String[] searchSynonym(String wordToSearch) throws Exception {
        String synWords="";
        String url = "https://api.datamuse.com/words?rel_syn=" + wordToSearch;

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
        if(synWords.length()>0){
            synWords=synWords.substring( 0,synWords.length()-1 );
            return synWords.split( "," );
        }
        return null;

    }

    // word and score attributes are from DataMuse API
    static class Word {
        private String word;
        private int score;

        public String getWord() {return this.word;}
        public int getScore() {return this.score;}
    }

}

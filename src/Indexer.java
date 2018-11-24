import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import  ReadFile.*;

public class Indexer {
    HashMap<String,StringBuilder> currDocTerms;
    int chunkCounter;
    String postingFilePath;

    public  void createTempPostingFile() throws FileNotFoundException {
        ArrayList<String> termsList = new ArrayList<>(currDocTerms.keySet());
        Collections.sort(termsList);

        String tempPostingDirPath = ReadFile.postingsPath +"\\"+"temp";

        File f = new File(tempPostingDirPath);
        if (!f.exists())
            f.mkdir();
        File file = new File(tempPostingDirPath + "\\"  + chunkCounter + ".txt");
        PrintWriter pw = new PrintWriter(new FileOutputStream(file,true));

        if (pw==null){
            System.out.println("Posting folder not found!! - Cannot create temp posting number: " + chunkCounter);
            return;
        }

        for (String term : termsList) {
            pw.println(term + "#" + currDocTerms.get(term).toString());
        }

        pw.flush();
        pw.close();
    }

}

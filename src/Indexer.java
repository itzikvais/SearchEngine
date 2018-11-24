import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.*;

import ExternalClasses.Document;
import  ReadFile.*;

public class Indexer {
    public HashMap<String,StringBuilder> currDocTerms;
    public int chunksCounter;
    public String postingFilePath;

    /*Creates a temporary dictionary from entire documents in iteration -> currentTErmsDictionary*/
    public void createDicFromParsedDocs(HashSet<Document> docsFromParser) throws FileNotFoundException {

        for (Document d : docsFromParser) {
            Iterator termsIterator = d.getDocTerms().iterator();

            while (termsIterator.hasNext()){
                String term = (String)termsIterator.next();
                StringBuilder sb = currDocTerms.get(term);
                if (sb==null) {
                    sb = new StringBuilder();
                    currDocTerms.put(term, sb);
                }
                if (sb.length()!=0) sb.append(",");
                sb.append(d.getDocID());
                sb.append(":");
                sb.append(d.getTermCount().get(term));

            }

            d.termCount.clear();
            d.termCount = null;
            Document.docCollection.put(d.getDocID(), d);
        }
        createTempPostingFile();
        currDocTerms.clear();
        chunksCounter++;

    }
    public  void createTempPostingFile() throws FileNotFoundException {
        ArrayList<String> termsList = new ArrayList<>(currDocTerms.keySet());
        Collections.sort(termsList);

        String tempPostingDirPath = ReadFile.postingsPath +"\\"+"temp";

        //craeting directory for temp posting files
        File f = new File(tempPostingDirPath);
        if (!f.exists())
            f.mkdir();

        //create a temp-posting-text-file for from all the docs in current chunk
        File file = new File(tempPostingDirPath + "\\"  + chunksCounter + ".txt");

        PrintWriter pw = new PrintWriter(new FileOutputStream(file,true));
        if (pw==null){
            System.out.println("Posting folder not found!! - Cannot create temp posting number: " + chunksCounter);
            return;
        }

        //writing all the terms from "termList" to the temp file
        for (String term : termsList) {
            pw.println(term + "#" + currDocTerms.get(term).toString());
        }

        pw.flush();
        pw.close();
    }

}

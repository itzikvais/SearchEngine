package Indexer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.*;

import ExternalClasses.Document;
import ExternalClasses.Term;
import  ReadFile.*;

public class Indexer {
    public HashMap<String,StringBuilder> currDocTerms;
    public int chunksCounter;
    public String postingFilePath;

    /*Creates a temporary dictionary from entire documents in chunk*/
    public void createDicFromParsedDocs(HashSet<Document> docsFromParser) throws FileNotFoundException {

        for (Document d : docsFromParser) {
            Iterator termsIterator = d.getDocTerms().iterator();

            while (termsIterator.hasNext()){
                Term term = (Term)termsIterator.next();
                StringBuilder sb = currDocTerms.get(term.termString);
                if (sb==null) {
                    sb = new StringBuilder();
                    currDocTerms.put(term.termString, sb);
                }
                if (sb.length()!=0) sb.append("|");
                sb.append(d.getDocID());
                sb.append(":");
                double normalizedTF =  d.getTermCount().get(term)/d.mostFreqTermVal;
                sb.append(","+normalizedTF);
                if (term.isBold) sb.append(",B");
                if (term.isTitle) sb.append(",T");
                // DocID:TF,B,T|DocID:TF,B,T|DocID:TF,B,T...
            }

            d.termCount.clear();
            d.termCount = null;
            Document.docCollection.put(d.getDocID(), d);
        }
        createTempPostingFile();
        currDocTerms.clear();
        chunksCounter++;

    }
    /* helper function to create temp posting file*/
    public  void createTempPostingFile() throws FileNotFoundException {
        ArrayList<String> termsList = new ArrayList<>(currDocTerms.keySet());
        Collections.sort(termsList);

        String tempPostingDirPath = ReadFile.postingsPath +"\\"+"temp";

        //creating directory for temp posting files
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
            //term1#DocID:TF,B,T|DocID:TF,B,T|DocID:TF,B,T...\n
            //term2#DocID:TF,B,T|DocID:TF,B,T|DocID:TF,B,T...\n
            // SORTED!
        }

        pw.flush();
        pw.close();
    }


}

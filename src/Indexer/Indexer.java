package Indexer;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Stream;

import ExternalClasses.Document;
import ExternalClasses.ReaderForMerge;
import ExternalClasses.Term;
////////////////////////////////////////////////////////////////////////////////////
public class Indexer {
    private HashSet<Document> docsFromParser;
    private HashMap<String,StringBuilder> currDocTerms;
    private int chunksCounter;
    private String postingDirPath;
    private String finalPostingFilePath;
    public static int totalDocsNum;
    public static int totalUniqueTerms;

    public Indexer(String postingDirPath) {
        this.postingDirPath = postingDirPath;
        this.finalPostingFilePath = this.postingDirPath + "\\" + "PostingFile" + ".txt";
        this.currDocTerms = new HashMap<>();
        this.docsFromParser = new HashSet<>();

    }

    public HashSet<Document> getDocsFromParser() {
        return docsFromParser;
    }

    public void setDocsFromParser(HashSet<Document> docsFromParser) {
        this.docsFromParser = docsFromParser;
    }

    /*Creates a temporary dictionary from entire documents in chunk*/
    public void createTempPostingFileFromParsedDocs() throws FileNotFoundException {

        for (Document d : docsFromParser) {
            Iterator<Map.Entry<Term, Integer>> iterator = d.docTermsAndCount.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry pair = iterator.next();
                Term term = (Term)pair.getKey();
                String termString = term.termString;

                StringBuilder sb = currDocTerms.get(termString);
                if (sb == null) {
                    sb = new StringBuilder();
                    currDocTerms.put(termString, sb);
                }
                if (sb.length() != 0) sb.append("|");
                sb.append(d.getDocID());
                sb.append(":");
                double normalizedTF = (double)d.docTermsAndCount.get(term) / (double)d.mostFreqTermVal;
                sb.append(",").append(normalizedTF);
                if (term.isTitle) sb.append(",T");
                // DocID:TF,T|DocID:TF,T|DocID:TF,T...

            }

            d.docTermsAndCount.clear();
            d.docTermsAndCount = null;
            Document.docCollection.put(d.getDocID(), d);
            totalDocsNum++;
        }
        createTempPostingFile();
        currDocTerms.clear();
        chunksCounter++;

    }

    /* helper function to create temp posting file*/
    private void createTempPostingFile() throws FileNotFoundException {
        ArrayList<String> termsList = new ArrayList<>(currDocTerms.keySet());
        Collections.sort(termsList);

        String tempPostingDirPath = postingDirPath +"\\"+"temp";

        //creating directory for temp posting files, this dic will removed after the final posting file will created
        File f = new File(tempPostingDirPath);
        if (!f.exists())
            f.mkdir();

        //create a temp-posting-text-file from all the docs in current chunk
        File file = new File(tempPostingDirPath + "\\"  + chunksCounter + ".txt");
        if (file.exists()) file.delete();

        PrintWriter pw = new PrintWriter(new FileOutputStream(file,true));
        if (pw==null){
            System.out.println("Posting folder not found!! - Cannot create temp posting number: " + chunksCounter);
            return;
        }

        //writing all the terms from "termList" to the temp file
        for (String term : termsList) {
            pw.println(term + "#" + currDocTerms.get(term).toString());
            //term1#DocID:TF,T|DocID:TF,T|DocID:TF,T...\n
            //term2#DocID:TF,T|DocID:TF,T|DocID:TF,T...\n
            // SORTED!
        }

        pw.flush();
        pw.close();
    }

    /* merge all the temp files to one posting file*/
    public void mergeSort(){
        try {

            PriorityQueue<ReaderForMerge> queue = new PriorityQueue<>( new Comparator<ReaderForMerge>() {
                @Override
                public int compare(ReaderForMerge o1, ReaderForMerge o2) {
                    return o1.key.compareTo( o2.key );
                }
            });
            //creating the final posting file
            File posting_file = new File(finalPostingFilePath);
            if (posting_file.exists()) posting_file.delete();
            PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(finalPostingFilePath)));

            //filling the priority queue
            for (int i = 0; i < chunksCounter; i++) {
                String tempPostingFilesPath = finalPostingFilePath + "\\" + "temp" +"\\"+ i + ".txt";
                File f = new File(tempPostingFilesPath);
                queue.add(new ReaderForMerge(new BufferedReader(new FileReader(f)), tempPostingFilesPath));
            }

            if (!(queue.peek() == null)&&!(queue.peek().line == null)) {

                ReaderForMerge reader = queue.poll();
                if(isFirstLatterCapital(reader.key)){
                    reader.key = reader.key.toUpperCase();
                    reader.line = reader.key+"#"+reader.val;
                }
                writer.print(reader.line);
                totalUniqueTerms++;
                String lastTermWritten = reader.key;

                while (queue.size() > 0) {
                    reader = queue.poll();
                    String nextTermToWrite = reader.key;
                    if(isChangeToUcNecessary(lastTermWritten,nextTermToWrite)){
                        reader.key = reader.key.toUpperCase();
                        reader.line = reader.key+"#"+reader.val;
                        nextTermToWrite = reader.key;
                    }
                    else if(isChangeToLcNecessary(lastTermWritten,nextTermToWrite)){
                        reader.key = reader.key.toLowerCase();
                        reader.line = reader.key+"#"+reader.val;
                        nextTermToWrite = reader.key;
                    }
                    if (nextTermToWrite.equals(lastTermWritten)) {
                        writer.print(",");
                        writer.print(reader.val);
                    } else {
                        lastTermWritten = nextTermToWrite;
                        writer.println();
                        writer.print(reader.line);
                        totalUniqueTerms++;
                    }

                    //read next line in the used temp posting file, if finish delete the file
                    reader.reload();
                    if (!(reader.line == null)) queue.add(reader);
                    else {
                        reader.close();
                        reader.deleteFile();
                    }
                }
                writer.close();
            }
        } catch(Exception e){e.printStackTrace();}

        new File(postingDirPath +"\\"+"temp").delete();
        System.out.println("Size of Inverted Index file: " + new File(finalPostingFilePath).length() + " [bytes]");

    }

    /*create dictionary file*/
    public void createDictionary(){
        File f = new File(finalPostingFilePath);
        try (Stream<String> lines = Files.lines(Paths.get(finalPostingFilePath))){
            lines.forEach((line)->{
                String[] splitted = line.split("#");
                String term = splitted[0];
                String data = splitted[1];

                String[] docs = data.split("|");

                //dictionary data
                int sumTF = 0;
                for (int i=0;i<docs.length; i++){
                    String[] docData = docs[i].split(",");
                    String number = docData[0].split(":")[1];
                    sumTF += Integer.parseInt(number);
                }



            });
        } catch (IOException e){}
    }

    /*helper function that check if the first letter of a string is capital*/
    private boolean isFirstLatterCapital (String s){
        String upperCaseString = s.toUpperCase();
        return s.charAt(0)==upperCaseString.charAt(0);
    }

    /*helper function that check if we have to change the second term to Upper case*/
    private boolean isChangeToUcNecessary(String prev, String next){
        if (prev.toUpperCase().equals(next.toUpperCase())) {
            if (isFirstLatterCapital(prev))
                return true;
        }
        return false;
    }

    /*helper function that check if we have to change the second term to Lower case*/
    private boolean isChangeToLcNecessary(String prev, String next){
        if (prev.toUpperCase().equals(next.toUpperCase())) {
            if (!isFirstLatterCapital(prev) && isFirstLatterCapital(next))
                return true;
        }
        return false;
    }
}

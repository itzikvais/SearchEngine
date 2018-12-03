package ReadFile;

import ExternalClasses.Document;
import Indexer.Indexer;
import Parse.Parse;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;

public class ReadFile {

    private String corpusPath;
    private String postingDirPath;
    private ArrayList<String[]> docsBuffer=new ArrayList<String[]>(  ); // buffer for one chunk of docs
    private Parse parser;
    private boolean toStem;
    private Indexer indexer;

    public ReadFile(String path, String postingsPath, boolean toStem) {
        this.corpusPath = path;
        this.postingDirPath = postingsPath;
        this.parser = new Parse(toStem);
        this.indexer = new Indexer(postingDirPath);
    }


    public void start() {
        File[] dirs = new File(corpusPath).listFiles(File::isDirectory);
        ArrayList<File> files = new ArrayList<>();

        //initial files ArrayList
        for (File dir : dirs) {
            File[] subFiles = dir.listFiles(File::isFile);
            Collections.addAll(files, subFiles);
        }
        //add all the docs from all the files in current chunk to docBuffer
        for (int i = 0; i < files.size(); i++) {
            try {
                BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(files.get(i))));
                String line ;
                int lineNum = 1;
                while ((line = br.readLine()) != null) {
                    StringBuilder doc = new StringBuilder();
                    String [] toDocBuffer = new String[4];
                    toDocBuffer[0] = files.get(i).getAbsolutePath();  // file path
                    while (!(line == null) && (!line.contains( "<DOC>" ))) {
                        line = br.readLine();
                        lineNum++;
                    }
                    toDocBuffer[1] = String.valueOf( lineNum ); // doc start line
                    while (!(line == null) && (!line.contains( "</DOC>" ))) {
                        line = br.readLine();
                        doc.append( line );
                        lineNum++;
                    }
                    toDocBuffer[2] = String.valueOf( lineNum ); // doc end line
                    toDocBuffer[3] = doc.toString(); // docText
                    if(doc.length()!=0)
                        docsBuffer.add( toDocBuffer );
                }
                if ((i + 1) % 10 == 0 || i == files.size() - 1) {
                    prosesChunk();
                }

            } catch (IOException ioException) {
                System.out.println("Exception thrown in readFile start function!");
            }

        }
        // *MERGESORT*
    }
    private void prosesChunk() throws FileNotFoundException {
        if (docsBuffer.isEmpty()) return;
        parser.setDocsBuffer(docsBuffer);
        HashSet<Document> docsFromParse =  parser.parse();
        indexer.setDocsFromParser(docsFromParse);
        indexer.createTempPostingFileFromParsedDocs();
        docsBuffer=new ArrayList<String[]>(  );
        //docsBuffer.clear();
    }

    private void printDoc(HashSet<Document> docsFromParse) {
        System.out.println(docsFromParse);
    }

    public void setCorpusPath(String f){
        corpusPath = f;
    }
    public void setPostingDirPath(String f){
        postingDirPath = f;
    }

    public static void main(String[] args) {
        ReadFile rf=new ReadFile( "/Users/itzikvais/Documents/מערכות מידע/שנה ג/איחזור/corpusTest" ,"/Users/itzikvais/Documents/מערכות מידע/שנה ג/איחזור/check/FB396028",false);
        rf.start();
    }
}
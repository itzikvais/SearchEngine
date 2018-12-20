package ReadFile;

import ExternalClasses.Document;
import Indexer.Indexer;
import Parse.Parse;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import static Indexer.Indexer.printData;

public class ReadFile {
    private String swPath;
    private String corpusPath;
    private String postingDirPath;
    private ArrayList<String[]> docsBuffer=new ArrayList<String[]>(  ); // buffer for one chunk of docs
    private Indexer indexer;
    private int chunknum=0;
    private PrintWriter documentsFilePW;

    public ReadFile(String path, String postingsPath) {
        this.swPath=path;
        this.corpusPath = path+"/corpusTest";
        this.postingDirPath = postingsPath;
    }
    public void setCorpusPath(String f){
        corpusPath = f;
    }
    public void setPostingDirPath(String f){
        postingDirPath = f;
    }

    public HashSet<String> start(boolean toStem) throws IOException {
        indexer.totalUniqueTerms=0;
        indexer.totalDocsNum=0;
        if (toStem){
            indexer = new Indexer(postingDirPath+ "\\" + "withStemming");
            File stemDir = new File(postingDirPath+ "\\" + "withStemming" );
            if(!stemDir.exists())
                stemDir.mkdir();
        }
        else {
            indexer = new Indexer(postingDirPath + "\\" + "withoutStemming");
            File stemDir = new File(postingDirPath+ "\\" + "withoutStemming" );
            if(!stemDir.exists())
                stemDir.mkdir();
        }
        //create document posting file
        File documentsFile = new File(postingDirPath + "\\" + "documentsFile" + ".txt");
        if (documentsFile.exists()) documentsFile.delete();

        try {
            documentsFilePW = new PrintWriter(new FileOutputStream(documentsFile, true));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        if (documentsFilePW == null) {
            System.out.println("Posting folder not found!! - Cannot create DocsPostingFile");
            return null;
        }

        //initial files list
        File[] dirs = new File(corpusPath).listFiles(File::isDirectory);
        ArrayList<File> files = new ArrayList<>();
        //initial files ArrayList
        for (File dir : dirs) {
            File[] subFiles = dir.listFiles(File::isFile);
            Collections.addAll(files, subFiles);
        }

        //add all the docs from all the files to docBuffer
        for (int i = 0; i < files.size(); i++) {
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
                        doc.append( line);
                        lineNum++;
                        line = br.readLine();
                    }
                    toDocBuffer[2] = String.valueOf( lineNum ); // doc end line
                    toDocBuffer[3] = doc.toString(); // docText
                    lineNum++;
                    if(doc.length()!=0)
                        docsBuffer.add( toDocBuffer );
                }
                if ((i + 1) % 50 == 0 || i == files.size() - 1) {
                    prosesChunk(documentsFilePW, toStem);
                }

            /*} catch (IOException ioException) {
                System.out.println("Exception thrown in readFile start function!");
                System.out.println(ioException.getStackTrace());
            }*/

        }

        documentsFilePW.flush();
        documentsFilePW.close();
        indexer.mergeSort();
        try {
            indexer.createDictionary();
            indexer.createDictionaryForReport();
            indexer.createCityFile();
            indexer.howManyNumbersTerms();
            //printData();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return indexer.getLanguages();
    }
    private void prosesChunk(PrintWriter documentsFilePW, boolean toStem) throws FileNotFoundException {
        Parse parser=new Parse( swPath,toStem );
        if (docsBuffer.isEmpty()) return;
        System.out.println("check" + chunknum);
        chunknum++;
        parser.setDocsBuffer(docsBuffer);
        HashSet<Document> docsFromParse =  parser.parse();
        indexer.setDocsFromParser(docsFromParse);
        indexer.indexChunk(documentsFilePW);

        docsBuffer.clear();
        docsBuffer=new ArrayList<String[]>();
    }

    public Indexer getIndexer() {
        return indexer;
    }

    public void  reset(){
        indexer.delete();
        startDeleting(postingDirPath);
    }
    private void startDeleting(String path) {
        List<String> filesList = new ArrayList<String>();
        List<String> folderList = new ArrayList<String>();
        fetchCompleteList(filesList, folderList, path);
        for(String filePath : filesList) {
            File tempFile = new File(filePath);
            tempFile.delete();
        }
        for(String filePath : folderList) {
            File tempFile = new File(filePath);
            tempFile.delete();
        }
    }
    private void fetchCompleteList(List<String> filesList, List<String> folderList, String path) {
        File file = new File(path);
        File[] listOfFile = file.listFiles();
        for (File tempFile : listOfFile) {
            if (tempFile.isDirectory()) {
                folderList.add(tempFile.getAbsolutePath());
                fetchCompleteList(filesList,
                        folderList, tempFile.getAbsolutePath());
            } else {
                filesList.add(tempFile.getAbsolutePath());
            }

        }

    }

    public void loadDictionary(boolean toStem){
        try {
            String dictionaryFullPath;
            if (toStem) {
                dictionaryFullPath = postingDirPath+ "\\" + "withStemming"+ "\\" + "dictionary" + ".txt";
            }
            else {
                dictionaryFullPath = postingDirPath+ "\\" + "withoutStemming"+ "\\" + "dictionary" + ".txt";
            }
            BufferedReader reader = new BufferedReader(new FileReader(dictionaryFullPath));
            indexer.loadDictionary(reader);
            reader.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static void main(String[] args) {

//        rf.reset();
    }

    public String showDictionary() throws IOException {
        return indexer.showDictionary();
    }
}
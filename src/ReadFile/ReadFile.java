package ReadFile;

import ExternalClasses.DocForSearcher;
import ExternalClasses.Document;
import Indexer.Indexer;
import Parse.Parse;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;


public class ReadFile {
    private String swPath;
    private String corpusPath;
    private String postingDirPath;
    private ArrayList<String[]> docsBuffer=new ArrayList<String[]>(  ); // buffer for one chunk of docs
    private Indexer indexer;
    private int chunknum=0;
    private PrintWriter documentsFilePW;
    private PrintWriter entitiesFilePW;
    private int totalTerms;
    private boolean toStem;

    public ReadFile(String path, String postingsPath) {
        this.swPath=path;
        this.corpusPath = path+"\\corpus";
        this.postingDirPath = postingsPath;
    }
    public void setCorpusPath(String f){
        corpusPath = f;
    }
    public void setPostingDirPath(String f){
        postingDirPath = f;
    }

    public HashSet<String>[] start(boolean toStem) throws IOException {
        this.toStem=toStem;
        if (toStem){
            indexer = new Indexer(postingDirPath+ "\\" + "withStemming");
            File stemDir = new File(postingDirPath+ "\\" + "withStemming" );
            if(!stemDir.exists()) {
                stemDir.mkdir();
            }
        }
        else {
            indexer = new Indexer(postingDirPath + "\\" + "withoutStemming");
            File stemDir = new File(postingDirPath+ "\\" + "withoutStemming" );
            if(!stemDir.exists()) {
                stemDir.mkdir();
            }
        }
        indexer.totalUniqueTerms=0;
        indexer.totalDocsNum=0;

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

        //create entities posting file
        File entitiesFile = new File(indexer.getPostingDirPath() + "\\" + "entitiesFile.txt");
        if (entitiesFile.exists()) entitiesFile.delete();

        try {
            entitiesFilePW = new PrintWriter(new FileOutputStream(entitiesFile, true));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        if (entitiesFilePW == null) {
            System.out.println("Posting folder not found!! - Cannot create entitiesFilePW");
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
                        doc.append( line +" ");
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
                    prosesChunk(documentsFilePW, entitiesFilePW, toStem);
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
            indexer.createCityFile();
            indexer.createFinalEntitiesFile(entitiesFilePW);
            indexer.splitFinalPostingFile();
//            indexer.howManyNumbersTerms();
//            printData();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        writeDataForRanker();
        HashSet<String>[] cityAndLang;
        writeCitiesAndLanguages(indexer.getCities(),indexer.getLanguages());
        cityAndLang=new HashSet[2];
        cityAndLang[0]=indexer.getLanguages();
        cityAndLang[1]=indexer.getCities();
        return cityAndLang;
    }
    private void writeDataForRanker() {
        //create dataForRanker file
        File dataForRanker=null;
        if(toStem)
            dataForRanker = new File(postingDirPath+"\\withStemming"+"\\dataForRanker.txt");
        else
            dataForRanker = new File(postingDirPath+"\\withoutStemming"+"\\dataForRanker.txt");
        if (dataForRanker.exists()) dataForRanker.delete();

        PrintWriter dataForRankerPW = null;
        try {
            dataForRankerPW = new PrintWriter(new FileOutputStream(dataForRanker, true));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        if (dataForRankerPW == null) {
            System.out.println("Posting folder not found!! - Cannot create dataForRankerPW");
        }

        //fill with data
        dataForRankerPW.println(indexer.totalDocsNum);
        dataForRankerPW.println((double)totalTerms/(double)indexer.totalDocsNum);

        dataForRankerPW.flush();
        dataForRankerPW.close();

    }

    private void prosesChunk(PrintWriter documentsFilePW, PrintWriter entitiesFilePW, boolean toStem) throws FileNotFoundException {
        Parse parser=new Parse( swPath,toStem );
        if (docsBuffer.isEmpty()) return;
        System.out.println("check" + chunknum);
        chunknum++;
        parser.setDocsBuffer(docsBuffer);
        HashSet<Document> docsFromParse =  parser.parse();
        for (Document d : docsFromParse){
            totalTerms += d.getDocLength();
        }
        indexer.setDocsFromParser(docsFromParse);
        indexer.indexChunk(documentsFilePW,entitiesFilePW);
        docsBuffer.clear();
        docsBuffer=new ArrayList<String[]>();
    }

    public Indexer getIndexer() {
        return indexer;
    }

    public void  reset(){
        indexer.clear();
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
    public void writeCitiesAndLanguages(HashSet<String> cities, HashSet<String> languages){
        //create citiesFile file
        File citiesFile = new File(System.getProperty("user.dir")+"\\citiesFile"+".txt");
        if (citiesFile.exists()) citiesFile.delete();

        PrintWriter citiesFilePW = null;
        try {
            citiesFilePW = new PrintWriter(new FileOutputStream(citiesFile, true));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        if (citiesFilePW == null) {
            System.out.println("citiesFilePW didn't create ");
        }

        for (String city : cities){
            citiesFilePW.println(city);
        }
        citiesFilePW.flush();
        citiesFilePW.close();

        //create languagesFile file
        File languagesFile = new File(System.getProperty("user.dir")+"\\languagesFile"+".txt");
        if (languagesFile.exists()) languagesFile.delete();

        PrintWriter languagesFilePW = null;
        try {
            languagesFilePW = new PrintWriter(new FileOutputStream(languagesFile, true));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        if (languagesFilePW == null) {
            System.out.println("languagesFilePW didn't create ");
        }

        for (String language : languages){
            languagesFilePW.println(language);
        }
        languagesFilePW.flush();
        languagesFilePW.close();
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
        System.out.println(System.getProperty("user.dir"));
    }

    public String showDictionary() throws IOException {
        return indexer.showDictionary();
    }
    public void clear(){
        docsBuffer=null;
        indexer.clear();
    }
}
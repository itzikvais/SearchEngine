package Indexer;

import java.io.*;
import java.util.*;

import ExternalClasses.*;
import com.sun.deploy.util.StringUtils;

////////////////////////////////////////////////////////////////////////////////////
public class Indexer {
    private HashSet<Document> docsFromParser;
    private HashMap<String,DicEntry> dictionary;
    private HashMap<String,StringBuilder> cities;
    private HashSet<String> languages ;
    private HashSet<String> entities;

    private String postingDirPath;
    private String finalPostingFilePath;

    public static int totalDocsNum;
    public int totalUniqueTerms;
    public int capitalCitiesNum;
    public int notCapitalCitiesNum;
    public int allCitiesNum;
    public int numberTerms;
    public int countriesNum;


    private int chunksCounter;

    /* constructor */
    public Indexer(String postingDirPath) {
        this.postingDirPath = postingDirPath;
        this.finalPostingFilePath = this.postingDirPath + "\\" + "PostingFile" + ".txt";
        this.cities = new HashMap<>();
        this.docsFromParser = new HashSet<>();
        this.dictionary = new HashMap<>();
        this.languages = new HashSet<>();
        this.entities = new HashSet<>();
        this.chunksCounter = 0;
    }
    public void delete() {
        docsFromParser.clear();
        dictionary.clear();
        cities.clear();
        languages.clear();
    }

    /* getters and setters */
    public HashSet<Document> getDocsFromParser() {
        return docsFromParser;
    }
    public void setDocsFromParser(HashSet<Document> docsFromParser) {
        this.docsFromParser = docsFromParser;
    }
    public String getPostingDirPath() {
        return postingDirPath;
    }

    /* index the chunk - the main function of the class */
    public void indexChunk(PrintWriter documentsFilePW,PrintWriter entitiesFilePW) throws FileNotFoundException {
        //open documentsFilePW

        //create curr doc's terms
        TreeMap<String,StringBuilder> currChunkTerms = new TreeMap<>();

        //creating directory for term temp posting files, this dic will removed after the final posting file will created
        String tempTermPostingDirPath = postingDirPath +"\\" + "temp";
        File tempTermPostingDir = new File(tempTermPostingDirPath );
        if (!tempTermPostingDir.exists()) {
            tempTermPostingDir.mkdir();
        }
        for( Document d : docsFromParser){
            Iterator termsIterator = d.docTermsAndCount.keySet().iterator();
            while (termsIterator.hasNext()){
                Term term = (Term)(termsIterator.next());
                String termString = term.termString;
                int count = d.docTermsAndCount.get(term);
                boolean isTitle = term.isTitle;

                String updatedTermString = updateDictionary(termString,count);

                //terms
                StringBuilder sb = currChunkTerms.get(updatedTermString);
                if (sb == null) {
                    sb = new StringBuilder();
                    currChunkTerms.put(updatedTermString, sb);
                }
                if (sb.length() != 0) sb.append(";");
                sb.append(d.getDocID());
                if (isTitle) sb.append("*");
                sb.append(":");
                sb.append(count);
                // DocID*:TF;DocID:TF;DocID*:TF...
            }
            updateCityDic(d);
            updateDocFile(d, documentsFilePW);
            updateEntitiesFile(d,entitiesFilePW);
            updateLanguages(d.getLanguage());

            d.docTermsAndCount.clear();
            d.docTermsAndCount = null;
            totalDocsNum++;
        }

        createTempPostingFile(currChunkTerms);
        currChunkTerms.clear();
        chunksCounter++;
    }
    /* helper functions to indexChunk */
    private void updateDocFile(Document doc, PrintWriter documentsFilePW) {
        StringBuilder sb = new StringBuilder();
        sb.append(doc.getDocID());sb.append("#");
        sb.append(doc.getFilePath());sb.append(",");
        sb.append(doc.getStartLine());sb.append(",");
        sb.append(doc.getEndLine());sb.append(",");
        sb.append(doc.getCityOfOrigin());sb.append(":");
        sb.append(doc.getNumOfCityLocations());sb.append(",");
        sb.append(doc.getDate());sb.append(",");
        sb.append(doc.getMostFreqTermVal());sb.append(",");
        sb.append(doc.getUniqueTermsCounter());documentsFilePW.println(sb.toString());
        //docId#filePath,startLine,endLine,originCity:numOfCityLocations,date,mostFreqTermVal,uniqueTermsCounter
    }
    private String updateDictionary(String term, int count) {
        DicEntry dicEntry = dictionary.get(term);
        //first letter is'nt capital
        if (term.charAt(0) >= 97 && term.charAt(0) <= 122) {
            term = term.toLowerCase();
            if (dicEntry == null) {
                dicEntry = new DicEntry(count, 1, -1);
                dictionary.put(term, dicEntry);
            }
            else if (dictionary.containsKey(term)) {
                dicEntry.df++;
                dicEntry.sumTF += count;
            }
            else if (dictionary.containsKey(term.toUpperCase())) {  //if upperCase exist in the dic it from another doc!
                // and lowerCase cant be exist
                //(no override when put on dictionary)
                DicEntry dicEntryUpperCase = dictionary.get(term.toUpperCase());
                int upperCaseTf = dicEntryUpperCase.sumTF;
                int upperCaseDf = dicEntryUpperCase.df;
                dictionary.remove(term.toUpperCase());
                dictionary.put(term,new DicEntry(upperCaseTf+count, upperCaseDf+1,-1));
            }
            return term;
        }
        //first letter is capital
        else if (term.charAt(0) >= 65 && term.charAt(0) <= 90) {
            if (dictionary.containsKey(term.toLowerCase())) {
                dicEntry = dictionary.get(term.toLowerCase());
                dicEntry.sumTF += count;
                dicEntry.df++;
                return term.toLowerCase();
            }
            else if (dictionary.containsKey(term.toUpperCase())) {
                dicEntry = dictionary.get(term.toUpperCase());
                dicEntry.df++;
                dicEntry.sumTF += count;
                return term.toUpperCase();
            }
            else if (dictionary.get(term.toUpperCase()) == null) {
                dicEntry = new DicEntry(count, 1, -1);
                dictionary.put(term.toUpperCase(), dicEntry);
                return term.toUpperCase();
            }
        }
        else if(dicEntry == null) {
            dicEntry = new DicEntry(count, 1, -1);
            dictionary.put(term, dicEntry);
        }
        return term;
    }
    private void updateCityDic(Document d) {
        String city = d.getCityOfOrigin();
        if (city == null) return;
        StringBuilder citySB = cities.get(city);
        if (citySB == null) {
            citySB = new StringBuilder();
            cities.put(city, citySB);
        }
        if (citySB.length() != 0) citySB.append(";");
        citySB.append(d.getDocID());
        citySB.append(":");
        citySB.append(d.getPositions());
        //city#DocID:pos,pos,pos;DocID:pos,pos,pos;DocID:pos...\n
        //city#DocID:pos,pos,pos;DocID:pos,pos,pos;DocID:pos...\n
    }
    private void updateLanguages(String language){
        languages.add(language);
    }
    private void updateEntitiesFile(Document d,PrintWriter entitiesFilePW){
        StringBuilder sb = new StringBuilder();
        sb.append(d.getDocID());sb.append("#");
        sb.append(d.getDocLength());sb.append("#");

        ArrayList<String> docEntities = d.getEntities();
        for (String entity : docEntities){
            sb.append(entity);
            sb.append("@");
            sb.append(d.docTermsAndCount.get(new Term(entity,false)));
            sb.append(",");
        }
        sb.deleteCharAt(sb.length()-1);
        entitiesFilePW.println(sb.toString());
        //docID#DocLength#entity,entity,entity,entity,entity...
    }
    private void createTempPostingFile(TreeMap<String, StringBuilder> currChunkTerms) throws FileNotFoundException {
        //create a temp-posting-text-file from all the docs in current chunk
        File file = new File(postingDirPath + "\\" + "temp" + "\\"  + chunksCounter + ".txt");
        if (file.exists()) file.delete();

        PrintWriter tempPostingFileSB = new PrintWriter(new FileOutputStream(file,true));
        if (tempPostingFileSB==null){
            System.out.println("Posting folder not found!! - Cannot create temp posting number: " + chunksCounter);
            return;
        }

//        ArrayList<String> termsList = new ArrayList<>(currChunkTerms.keySet());
//        Collections.sort(termsList);

        //writing all the terms from "termList" to the temp file
        for (String term : currChunkTerms.keySet()) {
            tempPostingFileSB.println(term + "#" + currChunkTerms.get(term).toString() );
        }

        tempPostingFileSB.flush();
        tempPostingFileSB.close();
    }

    /* merge all the temp files to one posting file */
    public void mergeSort(){
        try {
            PriorityQueue<ReaderForMerge> queue = new PriorityQueue<>(new Comparator<ReaderForMerge>() {
                @Override
                public int compare(ReaderForMerge o1, ReaderForMerge o2) {
                    return o1.key.compareTo(o2.key);
                }
            });

            //creating the final posting file
            File posting_file = new File(finalPostingFilePath);
            if (posting_file.exists()) posting_file.delete();
            PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(finalPostingFilePath)));

            //filling the priority queue
            for (int i = 0; i < chunksCounter; i++) {
                String tempPostingFilesPath = postingDirPath + "\\" + "temp" +"\\"+ i + ".txt";
                File f = new File(tempPostingFilesPath);
                queue.add(new ReaderForMerge(new BufferedReader(new FileReader(f)), tempPostingFilesPath));
            }

            int lineNum = 0;
            if (!(queue.peek() == null)&&!(queue.peek().line == null)) {

                ReaderForMerge reader = queue.poll();
                //while the term's first char is capital letter and lowerCase exist in dictionary(Ex 1)
                while(reader!=null&&(reader.key.charAt(0) >= 65 && reader.key.charAt(0) <= 90  && dictionary.containsKey(reader.key.toLowerCase()))){
                    //update dictionary
                    HashMap.Entry upperCasePair = (HashMap.Entry) dictionary.get(reader.key);
                    HashMap.Entry lowerCasePair = (HashMap.Entry) dictionary.get(reader.key.toLowerCase());
                    DicEntry upperCaseDicEntry = (DicEntry) upperCasePair.getValue();
                    DicEntry lowerCaseDicEntry = (DicEntry) lowerCasePair.getValue();
                    int newTf = upperCaseDicEntry.sumTF + lowerCaseDicEntry.sumTF;
                    int newDf = upperCaseDicEntry.df + lowerCaseDicEntry.df;
                    dictionary.remove(upperCasePair);
                    dictionary.put(reader.key.toLowerCase(), new DicEntry(newTf,newDf,-1));
                    //update queue
                    reader.line.replaceAll(reader.key,reader.key.toLowerCase());
                    reader.key.toLowerCase();
                    queue.add(reader);
                    reader = queue.poll();
                }
                writer.print(reader.line);
                //first term
                lineNum++;
                DicEntry dicEntry =dictionary.get(reader.key);
                dicEntry.postingLine = lineNum;
                String lastTermWritten = reader.key;
                reader.reload();
                if (reader.line != null) {
                    queue.add(reader);
                }
                else {
                    reader.close();
                    reader.deleteFile();
                }

                //all terms
                while (queue.size()>0){
                    reader = queue.poll();
                    String nextTermToWrite = reader.key;
                    if (nextTermToWrite.equals(lastTermWritten)) {
                        writer.print(";");
                        writer.print(reader.val);
                    } else {
                        lineNum++;
                        dicEntry =dictionary.get(reader.key);
                        dicEntry.postingLine = lineNum;
                        lastTermWritten = nextTermToWrite;
                        writer.println("");
                        writer.print(reader.line);
                    }
                    //read next line in the used temp posting file, if finish delete the file
                    reader.reload();
                    if ((reader.line != null)) queue.add(reader);
                    else {
                        reader.close();
                        reader.deleteFile();
                    }
                }
                writer.close();
            }
            //delete the temp posting files dir

            startDeleting(postingDirPath +"\\"+"temp");
            new File(postingDirPath +"\\"+"temp").delete();
        } catch(Exception e){e.printStackTrace();}
    }
    /* helper functions for MergeSort */
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

    public HashSet<String> getLanguages() {
        return languages;
    }

    /* dictionary functions */
    public void createDictionary() throws FileNotFoundException {
        SortedSet<String> termsSet = new TreeSet<>(dictionary.keySet());
        String dicFilePath = postingDirPath +"\\" + "dictionary" + ".txt";
        File dictionaryFile = new File(dicFilePath);
        if (dictionaryFile .exists()) dictionaryFile .delete();

        PrintWriter pw = new PrintWriter(new FileOutputStream(dictionaryFile ,true));
        if (pw==null){
            System.out.println("Posting folder not found!! - Cannot create dictionary");
            return;
        }

        for(String term : termsSet) {
            //update entities
            if(term.charAt(0)>='A' && term.charAt(0) <= 'Z')
                entities.add(term);
            totalUniqueTerms++;
            DicEntry de = dictionary.get(term);
            StringBuilder sb = new StringBuilder();
            sb.append(term); sb.append("#");
            sb.append(de.sumTF); sb.append(",");
            sb.append(de.df); sb.append(",");
            sb.append(de.idf); sb.append(",");
            sb.append(de.postingLine);
            // term#sumTf,df,idf,postingLine
            pw.println(sb.toString());
        }

        pw.flush();
        pw.close();

    }
    public void loadDictionary(BufferedReader reader){
        String line;
        try {
            HashMap<String, DicEntry> newDictionary = new HashMap<>();
            while((line=reader.readLine())!=null){
                String[] parts = line.split("#|,");
                String term = parts[0];
                newDictionary.put(term,new DicEntry(Integer.parseInt(parts[1]),
                        Integer.parseInt(parts[2]),
                        Double.parseDouble(parts[3]),
                        Integer.parseInt(parts[4])));
            }

            dictionary.clear();
            dictionary = newDictionary;

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /* cities functions */
    public HashSet<String> getCities(){
        return (HashSet<String>) cities.keySet();
    }
    public void createCityFile() throws FileNotFoundException {
        File CityFile = new File(postingDirPath +"\\" + "cityFile" + ".txt");
        if (CityFile.exists()) CityFile.delete();

        PrintWriter pw = new PrintWriter(new FileOutputStream(CityFile,true));
        if (pw==null){
            System.out.println("Posting folder not found!! - Cannot create cityFile");
            return;
        }

        SortedSet<String> citiesSet = new TreeSet<>(cities.keySet());
        allCitiesNum = cities.size();
        notCapitalCitiesNum = cities.size();
        capitalCitiesNum = cities.size();
        for(String city : citiesSet) {
            StringBuilder toChain = cities.get(city);
            ApiCity apiCity = new ApiCity(city);
            if(apiCity.getCountry() == null) capitalCitiesNum--;
            else notCapitalCitiesNum--;
            StringBuilder sb = new StringBuilder();
            sb.append(city.toUpperCase());sb.append("#");
            String country = apiCity.getCountry();
            if (country != null) countriesNum++;
            sb.append(country);sb.append(",");
            sb.append(apiCity.getCurrency());sb.append(",");
            sb.append(apiCity.getPopulation());sb.append("#");
            sb.append(toChain.toString());
            // city#Country,Currency,Population#docId:123,123,123
            pw.println(sb.toString());
        }

        pw.flush();
        pw.close();
    }
    public void howManyNumbersTerms(){
        SortedSet<String> termsSet = new TreeSet<>(dictionary.keySet());
        for (String term : termsSet){
            if (term.matches("-?\\d+(\\.\\d+)?")) numberTerms++;
        }
    }

    /* entities functions */
    public void createFinalEntitiesFile(PrintWriter entitiesPW){
        //close and create reader buffer
        entitiesPW.flush();
        entitiesPW.close();
        BufferedReader br = null;
        try {
            br = new BufferedReader(new InputStreamReader(new FileInputStream(new File(postingDirPath + "\\" + "entitiesFile" + ".txt"))));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        //create entities final file
        PrintWriter finalEntitiesFilePW = null;
        File finalEntitiesFile = new File(postingDirPath + "\\" + "finalEntitiesFile" + ".txt");
        if (finalEntitiesFile.exists()) finalEntitiesFile.delete();
        try {
            finalEntitiesFilePW = new PrintWriter(new FileOutputStream(finalEntitiesFile, true));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        if (finalEntitiesFilePW  == null) {
            System.out.println("Posting folder not found!! - Cannot create entitiesFile");
        }

        //write to the file
        String line;
        try {
            while (br != null && (line = br.readLine()) != null) {
                StringBuilder sb = new StringBuilder();
                String[] splited = line.split("#");
                sb.append(splited[0]);sb.append("#");
                sb.append(splited[1]);sb.append("#");
                String[] entities = splited[2].split(",");
                int counter = 0;
                for (int i = 0; i < entities.length && counter <5 ; i++) {
                    if (this.entities.contains(entities[i].split("@")[0])){
                        sb.append(entities[i]);
                        sb.append(",");
                        counter++;
                    }
                }
                sb.deleteCharAt(sb.length()-1);
                finalEntitiesFilePW.println(sb.toString());
            }
            finalEntitiesFilePW.flush();
            finalEntitiesFilePW.close();
            br.close();
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }



    public String showDictionary() throws IOException {
        /*
        BufferedReader br= new BufferedReader( new FileReader( postingDirPath +"\\" + "dictionary" + ".txt" ) );
        String dictionary="";
        String line;
        while((line=br.readLine())!=null){
            String[] splitLine=line.split("#|,");
            if(splitLine.length>=2)
                dictionary+=splitLine[0]+","+splitLine[1] +"\n";
        }
        */
        SortedSet<String> termsSet = new TreeSet<>(dictionary.keySet());
        StringBuilder sb= new StringBuilder();
        for(String term : termsSet) {
            DicEntry de = dictionary.get(term);
            if(de.sumTF>=1) {
                sb.append(term);
                sb.append(",");
                sb.append(de.sumTF);
                sb.append("\n");
            }
            // term#sumTf,df,idf,postingLine
        }

        return sb.toString();
    }
}
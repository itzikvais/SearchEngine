package Ranker;

import ExternalClasses.DocForSearcher;
import ExternalClasses.Synonyms;

import java.io.*;
import java.util.*;

import static java.lang.Integer.parseInt;


public class Ranker {
    private ArrayList<String> queryTerms;
    private Synonyms synonyms;
    private HashMap<String,DocForSearcher> withRank;
    private HashMap<String, Integer> currTermDocAndSynonymsCount;
    private ArrayList<String> cities;
    private String postingDirPath;
    private boolean toSynonym;
    private boolean toStem;
    private HashMap<String,ArrayList<String>> titlesInDocs;
    private int totalDocs;
    private double avgDocLength;

    public Ranker(ArrayList<String> queryTerms,String postingDirPath,ArrayList<String> cities,boolean toSynonym,boolean toStem) {
        this.queryTerms = queryTerms;
        this.synonyms = new Synonyms();
        this.withRank= new HashMap<>();
        this.postingDirPath = postingDirPath;
        this.titlesInDocs = new HashMap<>();
        this.currTermDocAndSynonymsCount = new HashMap<>();
        this.toSynonym = toSynonym;
        this.toStem = toStem;
        this.cities = cities;
        BufferedReader br = null;
        try {
            br = new BufferedReader(new InputStreamReader(new FileInputStream(new File(System.getProperty("user.dir")+"\\dataForRanker.txt"))));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        String line = null;
        try {
            if (br != null) {
                totalDocs = Integer.parseInt(br.readLine());
                avgDocLength = Double.parseDouble(br.readLine());
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    public void rank(){
        if(queryTerms == null || queryTerms.size()==0) return;
        ArrayList<String> finalQueryTerms = null;

        //initial
        for (String qi : queryTerms) {
            if(toSynonym) {
                try {
                    finalQueryTerms = new ArrayList<String>(Arrays.asList(synonyms.searchSynonym(qi)));
                    for(String sym : finalQueryTerms){
                        sym = adaptToDic(sym,toStem);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            else {
                finalQueryTerms = new ArrayList<>();
            }
            finalQueryTerms.add(qi);
            if(!cities.isEmpty())
                finalQueryTerms.addAll(cities);
            bm25Update(finalQueryTerms);
        }
    }
    private String adaptToDic(String sym, boolean toStem) {
        try {
            String dictionaryFullPath;
            if (toStem) {
                dictionaryFullPath = postingDirPath + "\\" + "withStemming" + "\\" + "dictionary" + ".txt";
            } else {
                dictionaryFullPath = postingDirPath + "\\" + "withoutStemming" + "\\" + "dictionary" + ".txt";
            }
            BufferedReader br = new BufferedReader(new FileReader(dictionaryFullPath));

            String line = null;
            String[] splited;
            if (br != null) {
                //search the line
                line = br.readLine();
                String term = null;
                while (line != null) {
                    term = line.split("#")[0];
                    if (sym.equals(term.toUpperCase())){
                        return term.toUpperCase();
                    }
                    if (sym.equals(term.toLowerCase())){
                        return term.toLowerCase();
                    }
                }
                if (line == null){
                    System.out.println("No such Term (sym) in Dic");
                    return null;
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("somthing wrong with finding synonyms as they show in Dictionary");
        return null;
    }
    private void bm25Update(ArrayList<String> finalQueryTerms) {
        //refresh for next qi
        currTermDocAndSynonymsCount.clear();
        currTermDocAndSynonymsCount = new HashMap<>();
        int docNum = 0;
        for (String qi :finalQueryTerms){
            docNum += getDocsNum(qi);
        }

        //foreach doc update doc length and entities
        for(DocForSearcher doc : withRank.values()){
            updateDocLengthEntitiesAndCities(doc);
        }
        //calculate idf for qwery term
        double idf = Math.log10((totalDocs - docNum + 0.5)/(docNum +0.5));
        //foreach doc that related to one qwery term (qi and it's synonyms) update it rank.
        for (String docId : currTermDocAndSynonymsCount.keySet()){
            int docLength = withRank.get(docId).docLength;
            int freqTermInDoc = currTermDocAndSynonymsCount.get(docId);
            double rank = (freqTermInDoc*(2+1))/(freqTermInDoc+2*((1-0.75)+0.75*(docLength/avgDocLength)));
            rank *= idf;
            if(titlesInDocs.containsKey(docId)){
                ArrayList<String> titlesTerms = titlesInDocs.get(docId);
                for( String sym : queryTerms){
                    if( titlesTerms.contains(sym)){
                        rank += idf;
                        break;
                    }
                }
            }
            withRank.get(docId).rank += rank;
        }
    }
    private int getDocsNum(String qi){
        int docNum = 0;
        //create buffer reader
        BufferedReader br = null;
        try {
            br = new BufferedReader(new InputStreamReader(new FileInputStream(new File(postingDirPath + "\\" + "PostingFile" + ".txt"))));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        String line = null;
        try {
            if (br != null) {

                //search the line
                line = br.readLine();
                while (line != null) {
                    String term = line.split("#")[0].trim();
                    if (term.equals(qi)) break;
                    line = br.readLine();
                }
                if (line == null){
                    System.out.println("No such Term in posting file");
                    return 0;
                }

                //use the line
                String[] splited = line.split("#");
                String term = splited[0];
                String docsString[] = splited[1].split(";");
                docNum = docsString.length;

                //add all docs without double and update currTermDocAndSynonymsCount
                for (int i = 0; i < docsString.length; i++) {
                    String currDocID = docsString[i].split(":")[0];
                    int count = Integer.parseInt(docsString[i].split(":")[1]);
                    if (currDocID.contains("*")) {
                        currDocID=currDocID.replace("*", "");
                        addTitleDoc(qi, currDocID);
                    }
                    if(!withRank.containsKey(currDocID))
                        withRank.put(currDocID,new DocForSearcher(currDocID,"", 0, null));
                    if (!currTermDocAndSynonymsCount.containsKey(currDocID)) {
                        currTermDocAndSynonymsCount.put(currDocID, count);
                    } else {
                        int val = currTermDocAndSynonymsCount.get(currDocID);
                        currTermDocAndSynonymsCount.put(currDocID, val + count);
                    }
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return docNum;
    }
    private void updateDocLengthEntitiesAndCities(DocForSearcher doc) {
        String docID = doc.getDocID();
        //find entities
        //create buffer reader
        BufferedReader postingFileBR = null;
        BufferedReader documentsFileBR = null;
        try {
            String path;
            if(toStem)
                path = postingDirPath + "\\withStemming";
            else path = postingDirPath + "\\withoutStemming";
            postingFileBR = new BufferedReader(new InputStreamReader(new FileInputStream(new File(path + "\\" + "finalEntitiesFile" + ".txt"))));
            documentsFileBR = new BufferedReader(new InputStreamReader(new FileInputStream(new File(postingDirPath + "\\" + "documentsFile.txt"))));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        String line = null;
        try {
            if (postingFileBR != null) {
                //search the line
                line = postingFileBR.readLine();
                while (line != null) {
                    String currDocID = line.split("#")[0];
                    if (currDocID.equals(docID)) break;
                    line = postingFileBR.readLine();
                }
                //use the line
                if(line==null)
                    System.out.println(docID+" postingFile");
                String[] splited = line.split("#");
                doc.docLength = parseInt(splited[1]);
                String docEntities = splited[2];
                doc.entities = new ArrayList<>(Arrays.asList(docEntities));

                line = documentsFileBR.readLine();
                while (line != null) {
                    String currDocID = line.split("#")[0];
                    if (currDocID.equals(docID)) break;
                    line = documentsFileBR.readLine();
                }
                if(line==null)
                    System.out.println(docID+" documentFile");
                splited = line.split("#");
                String[] data = splited[1].split(",");
                String city = data[3].split(":")[0].trim();
                doc.cityOfOrigin=city;

            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }
    private void addTitleDoc(String qi, String currDocID) {
        if (!titlesInDocs.containsKey(currDocID)){
            titlesInDocs.put(currDocID,new ArrayList<>());
        }
            titlesInDocs.get(currDocID).add(qi);
    }

    public ArrayList<DocForSearcher> getDocsWithRank(){
        ArrayList<DocForSearcher> docs = new ArrayList<>(withRank.values());
        ArrayList<DocForSearcher> docsByCity = new ArrayList<>();

        if (cities != null){
            for(DocForSearcher doc : docs){
                if(cities.contains(doc.cityOfOrigin))
                    docsByCity.add(doc);
            }
            docs = docsByCity;
        }
        docs.sort(new Comparator<DocForSearcher>() {
            @Override
            public int compare(DocForSearcher o1, DocForSearcher o2) {
                return Double.compare(o2.rank,o1.rank);
            }
        });
        if (docs.size() > 50){
            docs = (ArrayList<DocForSearcher>) docs.subList(0,49);
        }
        return docs;
    }

    public static void main(String[] args) {
        String postingDirPath = "C:\\Users\\tsizer\\Documents\\לימודים\\שנה ג\\סמסטר א\\אחזור מידע\\מנוע\\postingFileCheck";
        ArrayList<String> entitiesArrayList = new ArrayList<>();
        entitiesArrayList.add("exchange");
        entitiesArrayList.add("export");

        ArrayList<String> citiesArrayList = new ArrayList<>();
        citiesArrayList.add("HONG");
        citiesArrayList.add("PARIS");

        Ranker r = new Ranker(entitiesArrayList, postingDirPath,citiesArrayList,true,false);
        r.rank();

        for (String docID : r.withRank.keySet()) {
            double rank = r.withRank.get(docID).rank;
            System.out.println("DocId: "+docID+" rank: " + rank);
        }

        System.out.println("*************************");
        ArrayList<DocForSearcher> sorted = r.getDocsWithRank();
        for (DocForSearcher d : sorted){
            System.out.println("DocId: "+d.getDocID()+" rank: " + d.rank);
        }
    }

    public void printData() {
        for(DocForSearcher doc:withRank.values())
            System.out.println(doc.getDocID());
    }
}

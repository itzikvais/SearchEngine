package Ranker;

import ExternalClasses.DocForSearcher;
import ExternalClasses.SpellChecker;
import ExternalClasses.Synonyms;

import java.io.*;
import java.util.*;

import static java.lang.Integer.parseInt;


public class Ranker {
    private ArrayList<String> queryTerms;
    private ArrayList<String> descriptionTerms;
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
    private static int term=0;

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
            if(toStem)
                br = new BufferedReader(new InputStreamReader(new FileInputStream(new File(postingDirPath+"\\withStemming"+"\\dataForRanker.txt"))));
            else
                br = new BufferedReader(new InputStreamReader(new FileInputStream(new File(postingDirPath+"\\withoutStemming"+"\\dataForRanker.txt"))));
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

    public HashMap<String, DocForSearcher> getWithRank() {
        return withRank;
    }

    public void setDescriptionTerms(ArrayList<String> descriptionTerms) {
        this.descriptionTerms = descriptionTerms;
    }

    public void rank(){
        if(queryTerms == null || queryTerms.size()==0) return;
        ArrayList<String> finalQueryTerms = null;

        //initial
        for (String qi : queryTerms) {
            finalQueryTerms = new ArrayList<>();
            if(toSynonym) {
                String[] symArray;
                try {
                    symArray = synonyms.searchSynonym(qi);
                    SpellChecker sc=new SpellChecker();
                    String spellCheck=sc.checkSpell(qi);
                    if(spellCheck!=null)
                        finalQueryTerms.add(spellCheck);
                    ArrayList<String> termSynonyms = null;
                    if (symArray!= null && symArray.length != 0)
                        termSynonyms = new ArrayList<String>(Arrays.asList(symArray));
                    if (termSynonyms!= null && !termSynonyms.isEmpty()) {
                        for (String sym : termSynonyms) {
                            if (sym != null) {
                                String adaptedSym = adaptToDic(sym, toStem);
                                if (adaptedSym != null){
                                    finalQueryTerms.add(sym);
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            else {
                finalQueryTerms = new ArrayList<>();
            }
            finalQueryTerms.add(qi);
            bm25Update(finalQueryTerms,false);
        }
        if(descriptionTerms!=null &&!descriptionTerms.isEmpty()){
            bm25Update(descriptionTerms,true);
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
                    line = br.readLine();
                }
                if (line == null){
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
    private void bm25Update(ArrayList<String> finalQueryTerms,boolean isDesc) {
        //refresh for next qi
        currTermDocAndSynonymsCount.clear();
        currTermDocAndSynonymsCount = new HashMap<>();
        int docNum = 0;
        for (String qi :finalQueryTerms){
            docNum += getDocsNum(qi);
        }
        //foreach doc update doc length and entities
        updateDocLengthEntitiesAndCities();
        //calculate idf for qwery term
        double idf = Math.log10((totalDocs - docNum + 0.5)/(docNum +0.5));
        //foreach doc that related to one qwery term (qi and it's synonyms) update it rank.
        for (String docId : currTermDocAndSynonymsCount.keySet()){
            int docLength = withRank.get(docId).docLength;
            int freqTermInDoc = currTermDocAndSynonymsCount.get(docId);
            double rank = (freqTermInDoc*(1.2+1))/(freqTermInDoc+1.2*((1-0.75)+0.75*(docLength/avgDocLength)));
            rank *= idf;
            if(isDesc)
                rank=0.6*rank;
            if(titlesInDocs.containsKey(docId)){
                ArrayList<String> titlesTerms = titlesInDocs.get(docId);
                for( String sym : queryTerms){
                    if( titlesTerms.contains(sym)){
                        rank += 0.4*idf;
                        break;
                    }
                }
            }
            withRank.get(docId).rank += rank;
        }
    }
    private int getDocsNum(String qi){
        System.out.println("term"+term);
        term++;
        int docNum = 0;
        //create buffer reader
        BufferedReader br = null;
        String postingFilesDirFullPath;
        if (toStem) {
            postingFilesDirFullPath = postingDirPath + "\\" + "withStemming" + "\\" + "PostingFiles";
        } else {
            postingFilesDirFullPath = postingDirPath + "\\" + "withoutStemming" + "\\" + "PostingFiles";
        }
        try {
            char firstChar = qi.charAt(0);
            String postingFileFullPath = null;
            if (firstChar >= '0' && firstChar <= '9')
                postingFileFullPath = postingFilesDirFullPath + "\\numbers.txt";
            else if (firstChar >= 'A' && firstChar <= 'G')
                postingFileFullPath = postingFilesDirFullPath + "\\CAG.txt";
            else if (firstChar >= 'H' && firstChar <= 'O')
                postingFileFullPath = postingFilesDirFullPath + "\\CHO.txt";
            else if (firstChar >= 'P' && firstChar <= 'Z')
                postingFileFullPath = postingFilesDirFullPath + "\\CPZ.txt";
            else if (firstChar >= 'a' && firstChar <= 'g') {
                postingFileFullPath = postingFilesDirFullPath + "\\ag.txt";
            }
            else if (firstChar >= 'h' && firstChar <= 'o')
                postingFileFullPath = postingFilesDirFullPath + "\\ho.txt";
            else if (firstChar >= 'p' && firstChar <= 'z')
                postingFileFullPath = postingFilesDirFullPath + "\\pz.txt";
            if (postingFileFullPath != null)
                br = new BufferedReader(new InputStreamReader(new FileInputStream(new File(postingFileFullPath))));
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
                    if(term.compareTo(qi)>0) {
                        return 0;
                    }
                    line = br.readLine();
                }
                if (line == null){
                    System.out.println("No such Term in posting file");
                    return 0;
                }

                //use the line
                String[] splited = line.split("#");
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
            br.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return docNum;
    }
    private void updateDocLengthEntitiesAndCities() {
        //find entities
        //create buffer reader
        BufferedReader finalEntitiesFileBR = null;
        BufferedReader documentsFileBR = null;
        try {
            String path;
            if(toStem)
                path = postingDirPath + "\\withStemming";
            else path = postingDirPath + "\\withoutStemming";
            finalEntitiesFileBR = new BufferedReader(new InputStreamReader(new FileInputStream(new File(path + "\\" + "finalEntitiesFile" + ".txt"))));
            documentsFileBR = new BufferedReader(new InputStreamReader(new FileInputStream(new File(postingDirPath + "\\" + "documentsFile.txt"))));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        String line = null;
        try {
            if (finalEntitiesFileBR != null) {
                //search the line
                line = finalEntitiesFileBR.readLine();
                while (line != null) {
                    String currDocID = line.split("#")[0];
                    if (withRank.containsKey(currDocID)){
                        //use the line
                        if(line==null)
                            System.out.println(" finalEntitiesFile");
                        DocForSearcher doc=withRank.get(currDocID);
                        String[] splited = line.split("#");
                        doc.docLength = parseInt(splited[1]);
                        if(splited.length>2) {
                            String docEntities = splited[2];
                            doc.entities = new ArrayList<>(Arrays.asList(docEntities));
                        }
                    }
                    line = finalEntitiesFileBR.readLine();
                }
                finalEntitiesFileBR.close();
                line = documentsFileBR.readLine();
                while (line != null) {
                    String currDocID = line.split("#")[0];
                    if (withRank.containsKey(currDocID)){
                        if(line==null)
                            System.out.println(" documentFile");
                        DocForSearcher doc=withRank.get(currDocID);
                        String []splited = line.split("#");
                        String[] data = splited[1].split(",");
                        String city = data[3].split(":")[0].trim();
                        doc.cityOfOrigin=city;
                    }
                    line = documentsFileBR.readLine();
                }
            }
            documentsFileBR.close();
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
        if (cities != null&& cities.size()>=1) {
            for (DocForSearcher doc : docs) {
                if (cities.contains(doc.cityOfOrigin))
                    docsByCity.add(doc);
            }
            docsByCity.addAll(getDocsByCity(cities));

            docsByCity.sort(new Comparator<DocForSearcher>() {
                @Override
                public int compare(DocForSearcher o1, DocForSearcher o2) {
                    return Double.compare(o2.rank, o1.rank);
                }
            });
            if (docsByCity.size() > 50) {
                docsByCity = new ArrayList<>(docsByCity.subList(0, 50));
            }
            return docsByCity;
        }

        docs.sort(new Comparator<DocForSearcher>() {
            @Override
            public int compare(DocForSearcher o1, DocForSearcher o2) {
                return Double.compare(o2.rank, o1.rank);
            }
        });
        if(docs.size()>50)
            docs=new ArrayList<>(docs.subList(0, 50));
        return docs;

    }
    private ArrayList<DocForSearcher> getDocsByCity(ArrayList<String> citiesFromUser) {
        ArrayList<DocForSearcher> docsByCity = new ArrayList<>();
        BufferedReader postingFileBR = null;
        String line = null;
        try {
            String postingFilesDirFullPath;
            if (toStem) {
                postingFilesDirFullPath = postingDirPath + "\\" + "withStemming" + "\\" + "PostingFiles";
            } else {
                postingFilesDirFullPath = postingDirPath + "\\" + "withoutStemming" + "\\" + "PostingFiles";
            }
            if (citiesFromUser != null && !citiesFromUser.isEmpty()) {
                for (String city : citiesFromUser) {
                    char firstChar = city.charAt(0);
                    String postingFileFullPath = null;
                    if (firstChar >= '0' && firstChar <= '9')
                        continue;
                    else if (firstChar >= 'A' && firstChar <= 'G')
                        postingFileFullPath = postingFilesDirFullPath + "\\CAG.txt";
                    else if (firstChar >= 'H' && firstChar <= 'O')
                        postingFileFullPath = postingFilesDirFullPath + "\\CHO.txt";
                    else if (firstChar >= 'P' && firstChar <= 'Z')
                        postingFileFullPath = postingFilesDirFullPath + "\\CPZ.txt";
                    else if (firstChar >= 'a' && firstChar <= 'g')
                        postingFileFullPath = postingFilesDirFullPath + "\\ag.txt";
                    else if (firstChar >= 'h' && firstChar <= 'o')
                        postingFileFullPath = postingFilesDirFullPath + "\\ho.txt";
                    else if (firstChar >= 'p' && firstChar <= 'z')
                        postingFileFullPath = postingFilesDirFullPath + "\\pz.txt";
                    if (postingFileFullPath != null)
                        postingFileBR = new BufferedReader(new InputStreamReader(new FileInputStream(new File(postingFileFullPath))));

                    if (postingFileBR != null) {
                        //search the line
                        line = postingFileBR.readLine();
                        while (line != null) {
                            String currCity = line.split("#")[0];
                            if (citiesFromUser.contains(currCity)) {
                                String[] splited = line.split("#")[1].split(";");
                                for (String cell : splited) {
                                    String currDocID = cell.split(":")[0].replace("*", "").trim();
                                    if (withRank.keySet().contains(currDocID))
                                        docsByCity.add(withRank.get(currDocID));
                                }
                            }
                            line = postingFileBR.readLine();
                        }
                    }
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return docsByCity;
    }

    public static void main(String[] args) {
        String postingDirPath = "C:\\Users\\tsizer\\Documents\\לימודים\\שנה ג\\סמסטר א\\אחזור מידע\\מנוע\\postingFileCheck";
        ArrayList<String> queryTerms = new ArrayList<>();
        queryTerms.add("exchange");
        queryTerms.add("export");

        ArrayList<String> citiesArrayList = new ArrayList<>();
//        citiesArrayList.add("HONG");
//        citiesArrayList.add("PARIS");

        Ranker r = new Ranker(queryTerms, postingDirPath,citiesArrayList,false,false);
        r.rank();

        for (String docID : r.withRank.keySet()) {
            double rank = r.withRank.get(docID).rank;
        }

        ArrayList<DocForSearcher> sorted = r.getDocsWithRank();
    }
    public void printData() {
        for(DocForSearcher doc:withRank.values())
            System.out.println(doc.getDocID());
    }
}

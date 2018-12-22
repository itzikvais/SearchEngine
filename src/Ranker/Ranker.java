package Ranker;

import ExternalClasses.DocForSearcher;
import ExternalClasses.Synonyms;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

import static java.lang.Integer.parseInt;


public class Ranker {
    ArrayList<String> queryTerms;
    Synonyms synonyms;
    HashMap<String,DocForSearcher> withRank;
    HashMap<String, Integer> currTermDocAndSynonymsCount;
    String postingDirPath;
    boolean toSynonym;
    HashMap<String,ArrayList<String>> titlesInDocs;
    int totalDocs;
    double avgDocLength;

    public Ranker(ArrayList<String> queryTerms,String postingDirPath) {
        this.queryTerms = queryTerms;
        this.synonyms = new Synonyms();
        this.withRank= new HashMap<>();
        this.postingDirPath = postingDirPath;
        this.titlesInDocs = new HashMap<>();
        this.currTermDocAndSynonymsCount = new HashMap<>();
        BufferedReader br = null;
        try {
            br = new BufferedReader(new InputStreamReader(new FileInputStream(new File(System.getProperty("user.dir")+"\\dataForRanker"))));
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
        for (String qi : queryTerms) {

        }
        ArrayList<String> finalqueryTerms = null;

        //initial
        for (String qi : queryTerms) {
            if(toSynonym) {
                try {
                    finalqueryTerms = new ArrayList<String>(Arrays.asList(synonyms.searchSynonym(qi)));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            else {
                finalqueryTerms = new ArrayList<>();
            }
            finalqueryTerms.add(qi);
        }

        bm25Update(finalqueryTerms);


    }

    private void bm25Update(ArrayList<String> finalqueryTerms) {
        //refresh for next qi
        currTermDocAndSynonymsCount.clear();
        currTermDocAndSynonymsCount = new HashMap<>();
        int docNum = 0;
        for (String qi :finalqueryTerms){
            docNum += getDocsNum(qi);
        }

        //foreach doc update doc length and entities
        for(DocForSearcher doc : withRank.values()){
            updateDocLengthAndEntities(doc);
        }
        //calculate idf for qwery term
        double idf = Math.log10((totalDocs - docNum + 0.5)/(docNum +0.5));
        //foreach doc that related to one qwery term (qi and it's synonyms) update it rank.
        for (String docId : currTermDocAndSynonymsCount.keySet()){
            int docLength = withRank.get(docId).docLength;
            int freqTermInDoc = currTermDocAndSynonymsCount.get(docId);
            double rank = (freqTermInDoc*(2+1))/(freqTermInDoc+2*((1-0.75)+0.75*(docLength/avgDocLength)));
            rank *= idf;
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
                    String term = line.split("#")[0];
                    if (term.equals(qi)) break;
                    line = br.readLine();
                }
                if (line == null) System.out.println("No such Term in posting file");

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
                        currDocID.replace("*", "");
                        addTitleDoc(qi, currDocID);
                    }
                    withRank.put(currDocID,new DocForSearcher(currDocID, 0, null));
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
    private void updateDocLengthAndEntities(DocForSearcher doc) {
        String docID = doc.getDocID();
        //find entities
        //create buffer reader
        BufferedReader br = null;
        try {
            br = new BufferedReader(new InputStreamReader(new FileInputStream(new File(postingDirPath + "\\" + "finalEntitiesFile" + ".txt"))));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        String line = null;
        try {
            if (br != null) {

                //search the line
                line = br.readLine();
                while (line != null) {
                    String currDocID = line.split("#")[0];
                    if (currDocID.equals(docID)) break;
                    line = br.readLine();
                }
                //use the line
                String[] splited = line.split("#");
                doc.docLength = parseInt(splited[1]);
                String docEntities = splited[2];
                doc.entities = new ArrayList<>(Arrays.asList(docEntities));

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
}

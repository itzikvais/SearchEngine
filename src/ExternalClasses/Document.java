package ExternalClasses;

import java.util.HashMap;
import java.util.HashSet;

public class Document {
    public static HashMap<String,Document> docCollection = new HashMap<>(); //all the docs in this chunk
    private String docID;
    private HashSet<Term> docTerms;
    public HashMap<String, Integer> termCount;
    private int uniqueTermsCounter;
    private String mostFreqTerm;
    public int mostFreqTermVal;
    private String cityOfOrigin;
    private int docLength;

    public Document(String docID, String cityOfOrigin) {
        this.docID = docID;
        this.docTerms = new HashSet<>();
        this.termCount = new HashMap<>();
        this.cityOfOrigin = cityOfOrigin;
    }

    public String getDocID() {
        return docID;
    }

    public HashSet<Term> getDocTerms() {
        return docTerms;
    }

    public HashMap<String, Integer> getTermCount() {
        return termCount;
    }

    public boolean isUniqueTerm(String term){ return !docTerms.contains(term);}
    public void addTerm(Term term){
        if(isUniqueTerm(term.termString)){
            docTerms.add(term);
            uniqueTermsCounter++;
            termCount.put(term.termString,1);
        }
        else {
            int val = termCount.get(term.termString) + 1;
            termCount.put(term.termString,val);
            if (val > mostFreqTermVal) {
                mostFreqTermVal = val;
                mostFreqTerm = term.termString;
            }
        }
        docLength++;
    }
    public static long getNumberOfDocuments(){
        return docCollection.size();
    }
}

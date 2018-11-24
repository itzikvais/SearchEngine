package ExternalClasses;

import ExternalClasses.Term;

import java.util.HashMap;
import java.util.HashSet;

public class Document {
    private static HashMap<String,Document> docCollection = new HashMap<>(); //all the docs in this chunk
    private String docID;
    private HashSet<Term> docTerms;
    private HashMap<String, Integer> termCount;
    private int uniqueTermsCounter;
    private String mostFreqTerm;
    private int mostFreqTermVal;
    private String cityOfOrigin;
    private int docLength;

    public Document(String docID, String cityOfOrigin) {
        this.docID = docID;
        this.docTerms = new HashSet<>();
        this.termCount = new HashMap<>();
        this.cityOfOrigin = cityOfOrigin;
    }

    public boolean isUniqueTerm(String term){ return !docTerms.contains(term);}
    public void addTerm(Term term){
        if(isUniqueTerm(term.term)){
            docTerms.add(term);
            uniqueTermsCounter++;
            termCount.put(term.term,1);
        }
        else {
            int val = termCount.get(term.term) + 1;
            termCount.put(term.term,val);
            if (val > mostFreqTermVal) {
                mostFreqTermVal = val;
                mostFreqTerm = term.term;
            }
        }
        docLength++;
    }
    public static long getNumberOfDocuments(){
        return docCollection.size();
    }
}

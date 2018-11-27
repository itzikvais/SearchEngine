package ExternalClasses;

import java.util.HashMap;

public class Document {
    public static HashMap<String,Document> docCollection = new HashMap<>(); //all the docs in this chunk
    private String docID;
    public HashMap<Term, Integer> docTermsAndCount;
    private int uniqueTermsCounter;
    private String mostFreqTerm;
    public int mostFreqTermVal;
    private String cityOfOrigin;
    private int docLength;

    public Document(String docID, String cityOfOrigin) {
        this.docID = docID;
        this.docTermsAndCount = new HashMap<>();
        this.cityOfOrigin = cityOfOrigin;
        docCollection.put(docID,this);
    }

    @Override
    public int hashCode() {return docID.hashCode(); }

    public String getDocID() {
        return docID;
    }

    public boolean isUniqueTerm(Term term){ return !docTermsAndCount.containsKey(term);}

    public void addTerm(Term term){
        if(isUniqueTerm(term)){
            docTermsAndCount.put(term,1);
            uniqueTermsCounter++;
        }
        else {
            int val = docTermsAndCount.get(term) + 1;
            docTermsAndCount.put(term,val);
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

package ExternalClasses;

import java.util.HashMap;

public class Document {
    public static HashMap<String,Document> docCollection = new HashMap<>(); //all the docs in this chunk
    private String docID;
    private String filePath;
    private int startLine;
    private int endLine;
    public HashMap<Term, Integer> docTermsAndCount;
    private int uniqueTermsCounter;
    private String mostFreqTerm;
    public int mostFreqTermVal;
    private String cityOfOrigin;
    private int docLength;

    public Document(String docID, String cityOfOrigin, String filePath, int startLine, int endLine) {
        this.docID = docID;
        this.docTermsAndCount = new HashMap<>();
        this.cityOfOrigin = cityOfOrigin;
        this.filePath = filePath;
        this.startLine = startLine;
        this.endLine = endLine;
        docCollection.put(docID,this);
    }

    public String getFilePath() {
        return filePath;
    }

    public int getStartLine() {
        return startLine;
    }

    public int getEndLine() {
        return endLine;
    }

    @Override
    public int hashCode() {return docID.hashCode(); }

    public String getDocID() {
        return docID;
    }

    private boolean isUniqueTerm(Term term){ return !docTermsAndCount.containsKey(term);}

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

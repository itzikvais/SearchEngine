package ExternalClasses;

import java.util.HashMap;
import java.util.*;

public class Document {
    public static HashMap<String,Document> docCollection = new HashMap<>(); //all the docs in this chunk
    private String docID;
    private String filePath;
    private int startLine;
    private int endLine;
    public HashMap<Term, Integer> docTermsAndCount;
    public String date;
    private int uniqueTermsCounter;
    private String mostFreqTerm;
    public int mostFreqTermVal;
    private String cityOfOrigin;
    private int docLength;

    public Document(Document doc){
        this.docID = doc.docID;
        this.docTermsAndCount = doc.docTermsAndCount;
        this.filePath = doc.filePath;
        this.startLine = doc.startLine;
        this.endLine = doc.endLine;
    }
    public Document(String docID,  String filePath, int startLine, int endLine) {
        this.docID = docID;
        this.docTermsAndCount = new HashMap<>();
        this.filePath = filePath;
        this.startLine = startLine;
        this.endLine = endLine;
        docCollection.put(docID,this);
    }

    public void setCityOfOrigin(String cityOfOrigin) {
        this.cityOfOrigin = cityOfOrigin;
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
            docTermsAndCount.remove( term );
            term.tf=val;
            docTermsAndCount.put(term,val);
            if (val > mostFreqTermVal) {
                mostFreqTermVal = val;
                mostFreqTerm = term.termString;
            }
        }
        docLength++;
    }

    @Override
    public String toString() {
        String toStr="DOC NUM:" + docID +"\n";
        if(docTermsAndCount!=null) {
            Iterator it = docTermsAndCount.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry pair = (Map.Entry) it.next();
                Term t = (Term) pair.getKey();
                toStr += (t.termString + " = " + pair.getValue() + "\n");
                it.remove(); // avoids a ConcurrentModificationException
            }
        }
        return toStr;
    }
}

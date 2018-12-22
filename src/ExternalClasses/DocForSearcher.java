package ExternalClasses;

import java.util.ArrayList;

public class DocForSearcher {
    private String docID;
    public int docLength;
    public ArrayList<String> entities;
    public double rank;

    public DocForSearcher(String docID,int docLength, ArrayList<String> entities) {
        this.docID = docID;
        this.docLength = docLength;
        this.entities = entities;
    }

    public String getDocID() {
        return docID;
    }
}

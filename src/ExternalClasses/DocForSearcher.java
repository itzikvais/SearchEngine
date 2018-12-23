package ExternalClasses;

import java.util.ArrayList;

public class DocForSearcher {
    private String docID;
    public String cityOfOrigin;
    public int docLength;
    public ArrayList<String> entities;
    public double rank;

    public DocForSearcher(String docID, String cityOfOrigin, int docLength, ArrayList<String> entities) {
        this.docID = docID;
        this.cityOfOrigin = cityOfOrigin;
        this.docLength = docLength;
        this.entities = entities;
    }

    public String getDocID() {
        return docID;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj.getClass() != this.getClass()) return false;
        DocForSearcher docForSearcher = (DocForSearcher)obj;
        return (this.getDocID().equals(docForSearcher.getDocID()));
    }
}

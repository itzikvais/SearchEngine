package ExternalClasses;

import javax.lang.model.type.ArrayType;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.*;

public class Document {

    private String docID;
    private String filePath;
    private int startLine;
    private int endLine;
    public HashMap<Term, Integer> docTermsAndCount;
    private ArrayList<String> entities;
    private String date;
    private int uniqueTermsCounter;
    private String mostFreqTerm;
    private int mostFreqTermVal;
    private String cityOfOrigin;
    private int docLength;
    private String cityLocations;
    private int numOfCityLocations;
    private String language;

    public Document(Document doc) {
        this.docID = doc.docID;
        this.docTermsAndCount = doc.docTermsAndCount;
        this.filePath = doc.filePath;
        this.startLine = doc.startLine;
        this.endLine = doc.endLine;
        this.language = "";
    }
    public void setNewCityLocation(int location){
        if(cityLocations.length()==0)
            cityLocations= ""+location;
        else
            cityLocations+= "," +location;
        numOfCityLocations++;

    }
    public Document(String docID, String filePath, int startLine, int endLine) {
        this.docID = docID;
        this.docTermsAndCount = new HashMap<>();
        this.filePath = filePath;
        this.startLine = startLine;
        this.endLine = endLine;
        this.entities=new ArrayList<String>(  );
    }

    public int getNumOfCityLocations() {
        return numOfCityLocations;
    }
    public String getPositions(){
        return cityLocations;
    }
    public int getUniqueTermsCounter() {
        return uniqueTermsCounter;
    }
    public String getMostFreqTerm() {
        return mostFreqTerm;
    }
    public String getCityOfOrigin() {
        return cityOfOrigin;
    }
    public int getDocLength() {
        return docLength;
    }
    public void setCityOfOrigin(String cityOfOrigin) {
        cityLocations="";
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
    public String getDocID() {
        return docID;
    }
    public void setDate(String date){
        this.date=date;
    }
    public String getDate() {
        return date;
    }
    public int getMostFreqTermVal() {
        return mostFreqTermVal;
    }
    public String getLanguage() {
        return language;
    }
    public void setLanguage(String language) {
        this.language = language;
    }


    @Override
    public int hashCode() {
        return docID.hashCode();
    }
    @Override
    public String toString() {
        String toStr = "DOC NUM:" + docID + "\n";
        if (docTermsAndCount != null) {
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

    private boolean isUniqueTerm(Term term) {
        return !docTermsAndCount.containsKey(term);
    }
    public void addTerm(Term term) {
        if (isUniqueTerm(term)) {
            docTermsAndCount.put(term, 1);
            uniqueTermsCounter++;
        } else {
            int val = docTermsAndCount.get(term) + 1;
            docTermsAndCount.remove(term);
            docTermsAndCount.put(term, val);
            if (val > mostFreqTermVal) {
                mostFreqTermVal = val;
                mostFreqTerm = term.termString;
            }
        }
        docLength++;
    }

    public void printTermAndCount()  {
        System.out.println(docTermsAndCount.size());
        SortedSet<Term> docTermsAndCountSet = new TreeSet<>(new Comparator<Term>() {
            @Override
            public int compare(Term o1, Term o2) {
                return o1.termString.compareTo(o2.termString);
            }
        });
        docTermsAndCountSet.addAll(docTermsAndCount.keySet());
        if(docTermsAndCountSet==null)
            System.out.println("null");
        for(Term term : docTermsAndCountSet){
            System.out.println(term.termString+","+docTermsAndCount.get(term));
        }

    }


    public void sort() {

        Object[] a = docTermsAndCount.entrySet().toArray();
        Arrays.sort(a, new Comparator() {
            public int compare(Object o1, Object o2) {
                return ((Map.Entry<Term, Integer>) o2).getValue()
                        .compareTo(((Map.Entry<Term, Integer>) o1).getValue());
            }
        });
        for (Object e : a) {
            String term=((HashMap.Entry<Term, Integer>) e).getKey().termString;
            if(term.charAt( 0 )>='A'&&term.charAt( 0 )<='Z') {
                entities.add( term );
            }
        }
    }

    public void setDocTermsAndCount(HashMap<Term, Integer> docTermsAndCount) {
        this.docTermsAndCount.clear();
        this.docTermsAndCount = docTermsAndCount;
    }

    public ArrayList<String> getEntities() {
        return entities;
    }
}
package ExternalClasses;

import javafx.util.Pair;

public class Term {
    public String termString;
    public int tf;
    public boolean isTitle;
    public boolean isBold;
    public boolean isCity;
    public Pair<Integer,Integer> positionInDoc;
    public Term(String term,boolean isBold, boolean isCity,boolean isTitle){
        termString=term;
        this.isBold=isBold;
        this.isCity=isCity;
        this.isTitle=isTitle;
        tf=1;
    }
    public boolean equals(Object o){
        return (o instanceof String )&&o.equals( this.termString );
    }
    @Override
    public int hashCode() {
        return termString.hashCode();
    }
}

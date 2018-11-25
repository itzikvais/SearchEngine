package ExternalClasses;

import javafx.util.Pair;

public class Term {
    public String termString;
    public int tf;
    public boolean isTitle;
    public boolean isBold;
    public boolean isCity;
    public Pair<Integer,Integer> positionInDoc;

    @Override
    public int hashCode() {
        return termString.hashCode();
    }
}

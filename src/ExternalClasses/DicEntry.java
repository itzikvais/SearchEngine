package ExternalClasses;
import java.util.Objects;

import static Indexer.Indexer.totalDocsNum;

public class DicEntry{
    public int sumTF;
    public int df;
    public Double idf;
    public int postingLine;

    public DicEntry(int sumTF, int df, int postingLine){
        this.sumTF = sumTF;
        this.df = df;
        this.postingLine = postingLine;
        this.idf = Math.log10(totalDocsNum / df);
    }
    public DicEntry(int sumTF, int df, Double idf, int postingLine){
        this.sumTF = sumTF;
        this.df = df;
        this.postingLine = postingLine;
        this.idf = idf;
    }

    @Override
    public String toString(){
        return (this.df +":"+this.sumTF+":" + this.postingLine);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) {System.out.println("not same type"); return false;}
        DicEntry dicEntry = (DicEntry) o;
        return sumTF == dicEntry.sumTF &&
                df == dicEntry.df &&
                postingLine == dicEntry.postingLine &&
                idf == dicEntry.idf;
    }

}
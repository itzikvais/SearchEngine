package ExternalClasses;

public class DicEntry{
    public int df;
    public int sumTF;
    public Double idf;
    public int postingLine;

    public DicEntry(int df, int sumTF, int postingLine){
        this.df = df;
        this.sumTF = sumTF;
        this.postingLine = postingLine;
        this.idf = Double.valueOf(0);
    }

    @Override
    public String toString(){
        return (this.df +":"+this.sumTF+":" + this.postingLine);
    }

}
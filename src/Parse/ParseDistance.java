package Parse;

public class ParseDistance implements IParse {
    double number;
    String distance;
    public ParseDistance(double number, String distance) {
        this.number=number;
        this.distance=distance;
    }
    @Override
    public String parse()
    {
        if(number>1000&&distance.equals( "m" ))
            return number/1000 + " km";
        else
            return number + " " + distance;
    }
}

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
        if(distance==null)
            return "";
        if(number>1000&&(distance.equals( "m" )||distance.equals( "meters" )))
            return number/1000 + "km";
        else if(distance.equals( "m" )||distance.equals( "meters" ))
            return number+"m";
        else if(distance.equals( "km" )||distance.equals( "kilometers" ))
            return number+"km";
        else if(distance.equals( "miles" ))
            return number*1.609344 +"km";
        else
            return number  + distance;
    }
}

package Parse;

public class ParseWeight implements IParse{
    double number;
    String weight;
    public ParseWeight(double number, String weight) {
        this.number=number;
        this.weight=weight;
    }
    @Override
    public String parse()
    {
        if(weight==null)
            return "";
        if(number>1000&&(weight.equals( "grams" )||weight.equals( "g" )))
            return number/1000 + "kg";
        else if(weight.equals( "grams" )||weight.equals( "g" ))
            return number+"g";
        else if(weight.equals( "kg" )||weight.equals( "kgs" )||weight.equals( "kilograms" ))
            return number+"kg";
        else
            return number  + weight;
    }
}

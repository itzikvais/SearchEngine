package Parse;

public class ParsePercent implements IParse{
    String number;
    public ParsePercent(String number){
        if(Double.parseDouble( number )<0)
            this.number="0";
        else
            this.number=number;
    }
    public String parse(){
        return number +"%";
    }
}

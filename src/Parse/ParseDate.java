package Parse;

public class ParseDate implements IParse{
    int month;
    int number;
    boolean year=false;

    /**
     *
     * @param month the month number
     * @param number the day or year
     */
    public ParseDate(int month,int number){
        this.month=month;
        this.number=number;

    }

    @Override
    public String parse() {
        String month= "" + this.month;
        String day = "" + number;
        if(this.month<10)
            month= "0"+month;
        if(number<=31&&number>0) {
            if(number<10)
                day="0" +number;
            return month + "-" + day;
        }
        else
            return number + "-" + month;

    }
}

package Parse;

public class ParsePrice implements IParse {
    String price;
    String nextWord;
    boolean fraction=false;
    boolean amount=false;

    /**
     * create new instance of price parsing
     * @param number the price
     * @param fractionOrAmount fracion or amount
     */
    public ParsePrice(String number, String fractionOrAmount){
        if(number!=null) {
            price = number;
            nextWord = fractionOrAmount;
        }
        if(nextWord!=null){
            if(nextWord.contains( "/" ))
                fraction=true;
            else
                amount=true;
        }
    }
    public String parse()  {
        removeDollar();
        if(!isNumeric( price.replace( ",","" ) ))
            return null;
        parseAmount();
        if(fraction)
            price=price+ " " + nextWord;
        price=price +" Dollars";
        return price;
    }

    private void parseAmount() {
        double num=Double.parseDouble( price.replace( ",","" ) );
        if(amount){
            if(nextWord.equals("m")|| nextWord.equals( "million" ) )
                num= num*1000000;
            else if(nextWord.equals("bn")|| nextWord.equals( "billion" ))
                num= num*1000000000;
            else if( nextWord.equals( "trillion" ))
                num= num*1000000000000.0;
        }
        if(num>=1000000){
            num= num/1000000;
            if(num%1==0)
                price= (int)num +" M";
            else
                price=num + " M";
        }
    }

    public static boolean isNumeric(String str)
    {
        return str.matches("-?\\d+(\\.\\d+)?");  //match a number with optional '-' and decimal.
    }
    /**
     * romove the $ if it include in price
     */
    private void removeDollar() {
        if(price.contains( "$" ))
            price= price.replace( "$","" );
    }
    private void test(String ans, int test) {
        try {
            parse();
            if (price.equals(ans ))
                System.out.println("Ok Test"+ test);
            else
                System.out.println("Bad Test" + test);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
    public static void main(String[] args){
        ParsePrice pp= new ParsePrice("1.7320",null);
        pp.test("1.7320 Dollars",1);
        pp= new ParsePrice("22", "3/4");
        pp.test("22 3/4 Dollars",2);
        pp= new ParsePrice("$450,000", null);
        pp.test("450,000 Dollars",3);
        pp= new ParsePrice("1,000,000",null);
        pp.test("1 M Dollars",4);
        pp= new ParsePrice("$450,000,000", null);
        pp.test("450 M Dollars",5);
        pp= new ParsePrice("$100", "million");
        pp.test("100 M Dollars",6);
        pp= new ParsePrice("20.6", "m");
        pp.test("20.6 M Dollars",7);
        pp= new ParsePrice("$100", "billion");
        pp.test("100000 M Dollars",8);
        pp= new ParsePrice("100", "bn");
        pp.test("100000 M Dollars",9);
        pp= new ParsePrice("100", "billion");
        pp.test("100000 M Dollars",10);
        pp= new ParsePrice("320", "million");
        pp.test("320 M Dollars",11);
        pp= new ParsePrice("1", "trillion");
        pp.test("1000000 M Dollars",12);


    }
}

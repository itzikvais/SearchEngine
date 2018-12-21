package Parse;

public class ParseNumber implements IParse {
    String number = null;
    String amount = null;
    boolean negative = false;

    /**
     * @param number a number in string
     * @param amount Thousand Million Billion or Trillion
     */
    public ParseNumber(String number, String amount) {
        this.number = number;
        this.amount = amount;
        if (this.number.contains(","))
            this.number = this.number.replace(",", "");
        /*
        if(this.number.contains( "." ))
            this.number="" +Double.parseDouble( this.number );
        else
            this.number="" +Integer.parseInt( this.number );
            */
        if (number != null && number.charAt(0) == '-') {
            this.number = this.number.substring(1);
            negative = true;
        }
    }

    /**
     * @return a parse number
     * @throws Exception if the number is null
     */
    public String parse() {
        if (number != null) {
            if (amount != null)
                parseAmount();
            representationNumber();
            if (negative)
                number = "-" + number;
            return number;
        }
        return null;
    }

    /**
     * make all the string a number if the user puts an amount
     */
    private void parseAmount() {
        if (amount.equals("Thousand"))
            number = "" + Double.parseDouble(number) * 1000;
        else if (amount.equals("Million"))
            number = "" + Double.parseDouble(number) * 1000000;
        else if (amount.equals("Billion"))
            number = "" + Double.parseDouble(number) * 1000000000;
        else if (amount.equals("Trillion"))
            number = "" + Double.parseDouble(number) * 1000000000000.0;

    }

    /**
     * parse the number
     */
    private void representationNumber() {
        double num = Double.parseDouble(number);
        if (num >= 1000 && num < 1000000) {
            num /= 1000;
            if (num % 1 > 0)
                number = String.format("%.2f", num);
            if (num % 1 == 0)
                number = (int) num + "K";
            else
                number = number + "K";
        } else if (num >= 1000000 && num < 1000000000) {
            num /= 1000000;
            if (num % 1 == 0)
                number = (int) num + "M";
            else {
                number = String.format("%.2f", num) + "M";
            }
        } else if (num >= 1000000000) {
            num /= 1000000000;
            if (num % 1 == 0)
                number = (int) num + "B";
            else
                number = String.format("%.2f", num) + "B";
        } else {
            if (num % 1 == 0)
                number = "" + (int) num;
            else
                number = String.format("%.2f", num);
        }
        //checkNumAfterDot();
    }


    private void test(String ans, int test) {
        try {
            parse();
            if (number.equals(ans))
                System.out.println("Ok Test" + test);
            else
                System.out.println("Bad Test" + test);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static void main(String[] args) {
        ParseNumber ps = new ParseNumber("10,123", null);
        ps.test("10.123K", 1);
        ps = new ParseNumber("123", "Thousand");
        ps.test("123K", 2);
        ps = new ParseNumber("1010.56", null);
        ps.test("1.01056K", 3);
        ps = new ParseNumber("10,123,000", null);
        ps.test("10.123M", 4);
        ps = new ParseNumber("55", "Million");
        ps.test("55M", 5);
        ps = new ParseNumber("1010.56", "Thousand");
        ps.test("1.01056M", 6);
        ps = new ParseNumber("10,123,000,000", null);
        ps.test("10.123B", 7);
        ps = new ParseNumber("55", "Billion");
        ps.test("55B", 8);
        ps = new ParseNumber("7", "Trillion");
        ps.test("7000B", 9);
        ps = new ParseNumber("-7", "Trillion");
        ps.test("-7000B", 9);
    }
}
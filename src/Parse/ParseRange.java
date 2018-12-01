package Parse;

public class ParseRange {
    private String[] range;
    public ParseRange(String[] range){
        if(range!=null)
            this.range=range;

    }
    public String[] parse() {
        String[] toReturn=null;
        if(range!=null){
            if(isNumeric( range[0] )){
                if(range.length>1&&isNumeric( range[1] )){
                    toReturn=new String[3];
                    toReturn[0]=range[0]+"-"+range[1];
                    toReturn[1]=range[0];
                    toReturn[2]=range[1];
                }
                else{
                    toReturn=new String[1];
                    toReturn[0]="";
                    for (int i = 0; i <range.length ; i++) {
                        toReturn[0]+=range[i]+"-";
                    }
                    toReturn[0]=toReturn[0].substring( 0,toReturn[0].length()-1 );
                }
            }
            else{
                if((range[0].equals( "between" )||range[0].equals( "Between" ))&&range.length==4&&isNumeric( range[1] )&&isNumeric( range[3] )){
                    toReturn=new String[3];
                    toReturn[0]=range[1]+"-"+range[3];
                    toReturn[1]=range[1];
                    toReturn[2]=range[3];
                }
                else {
                    toReturn=new String[1];
                    toReturn[0]="";
                    for (int i = 0; i < range.length; i++) {
                        toReturn[0] += range[i] + "-";
                    }
                    toReturn[0]=toReturn[0].substring( 0, toReturn[0].length() - 1 );
                }
            }
        }
        range=toReturn;
        return toReturn;
    }
    private boolean isNumeric(String str)
    {
        return str.matches("-?\\d+(\\.\\d+)?");  //match a number with optional '-' and decimal.
    }
    private void test(String[] ans, int test) {
        try {
            parse();
            for (int i = 0; i <range.length ; i++) {
                if (range[i].equals(ans[i] ))
                    System.out.println("Ok Test"+ test);
                else
                    System.out.println("Bad Test" + test);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
    public static void main(String[] args){
        String[] check=new String[3];
        check[0]="7-9";
        check[1]="7";
        check[2]="9";
        ParseRange pr= new ParseRange(check[0].split( "-" ));
        pr.test(check,1);
        check=new String[1];
        check[0]="Value-added";
        pr= new ParseRange(check[0].split( "-" ));
        pr.test(check,2);
        check[0]="step-by-step";
        pr= new ParseRange(check[0].split( "-" ));
        pr.test(check,3);
        check[0]="10-part";
        pr= new ParseRange(check[0].split( "-" ));
        pr.test(check,4);
        check[0]="part-10";
        pr= new ParseRange(check[0].split( "-" ));
        pr.test(check,5);
        check=new String[4];
        check[0]="between";
        check[1]="10";
        check[2]="to";
        check[3]="50";
        String[] ans=new String[3];
        ans[0]=check[1]+"-"+check[3];
        ans[1]=check[1];
        ans[2]=check[3];
        pr= new ParseRange(check[0].split( "-" ));
        pr.test(check,6);


    }
}

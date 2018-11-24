package Parse;
import java.text.DateFormatSymbols;
import java.util.*;
import java.io.*;
import Parse.*;

public class Parse {
    private HashMap<String, Integer> terms=new HashMap<String, Integer>(  );//String for the termString and int for the number of return's
    private HashSet<String> conjuctions = new HashSet<String>();
    private HashSet<String> tags=new HashSet<String>(  );
    private HashMap<String,Integer> monthList=new HashMap<String,Integer>(  );
    public Parse(){
        addConjuctions();//add all the conjuction to the HashSet
        addTags();
    }

    /**
     * add all the tags to an HashSet
     */
    private void addTags() {
        tags.add( "<DOC>" );
        tags.add("<DOCNO>");
        tags.add("<DATE1>");
        tags.add("<TI>");
        tags.add("<TEXT>");
        tags.add("<P>");
        tags.add( "</DOC>" );
        tags.add("</DOCNO>");
        tags.add("</DATE1>");
        tags.add("</TI>");
        tags.add("</TEXT>");
        tags.add("</P>");
    }

    /**
     *
     * @return an HashMap of terms and how many time they appeared
     */
    public HashMap<String, Integer> parse(String filePath){
        try {
            File file = new File( "conjuctions" );
            BufferedReader br = new BufferedReader( new FileReader( "conjuctions" ) );
            String st;
            while ((st = br.readLine()) != null){
                String[] line= st.split( " " );
                for (int i = 0; i < line.length; i++) {
                    parseLine(line);
                }
            }
        } catch (FileNotFoundException e) {
            System.out.println("file not found");
        } catch (IOException e) {
            conjuctions=null;
        }
        return terms;
    }

    /**
     * seperate line to terms
     * @param line line in an array of strings
     */
    private void parseLine(String[] line) {
        for (int i = 0; i <line.length ; i++) {
            if(!conjuctions.contains( line[i] ) && !tags.contains( line[i] ))
                if ((i<line.length-4 &&line[i+3].equals( "dollars" )) && line[i+2].equals( "US" ) || (i<line.length-3 &&line[i+2].equals( "Dollars" ) ))
            {
               parsePrice( line[i],line[i+1] );
               if(line[i+2].equals( "Dollars" ))
                   i=i+2;
               else
                   i=i+3;
            }
                else if(i<line.length-1)
                    parseTerm(line[i],line[i+1]);
                else
                    parseTerm( line[i],null );
        }

    }

    private void parsePrice(String number, String amount) {
        if(isNumeric( number.replace( ",","" ).replace( "$","" ) ) && (amount.contains( "/" )||
                amount.equals( "m" )) || amount.equals( "bn" ) || amount.equals( "million" ) || amount.equals( "billion" ) || amount.equals( "trillion" )) {
            ParsePrice pp = new ParsePrice( number, amount );
            try {
                String term=pp.parse();
                if(terms.containsKey( term )){
                    terms.replace( term ,terms.get( term )+1 );
                }
                else
                    terms.put( term,1 );
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    private String parseDate(String term, String nextTerm) {
        ParseDate pd=null;
        if(monthList.containsKey( term )&&isNumeric( nextTerm )) {
            pd = new ParseDate( monthList.get( term ), Integer.parseInt( nextTerm ) );
            return pd.parse();
        }
        else if(isNumeric( nextTerm )) {
            pd = new ParseDate( Integer.parseInt( term ), monthList.get( nextTerm ) );
            return pd.parse();
        }
        return null;
    }

    private void parseTerm(String term,String nextTerm) {
        String parseTerm="";
        if(term.charAt( term.length()-1 )=='%' || (nextTerm!=null && (nextTerm.equals( "percent" ) || nextTerm.equals( "percentage" ))))
            parsePercent(term);
        else if(monthList.containsKey( term ) || (nextTerm!=null&&monthList.containsKey( nextTerm )) )
            parseDate(term,nextTerm  );
        else if(isNumeric( term.replace( ",","" ) ))
            parseNumeric(term,nextTerm);
        else if(term.contains( "$" )|| (nextTerm!=null && nextTerm.equals( "Dollars" )))
            parsePrice(term,nextTerm);

    }

    private String parsePercent(String term) {
        ParsePercent pp=new ParsePercent( term );
        try {
            return pp.parse();
        } catch (Exception e) {
            return null;
        }
    }

    private String parseNumeric(String term, String nextTerm) {
        ParseNumber pn= new ParseNumber( term,nextTerm );
        try {
            return pn.parse();
        } catch (Exception e) {
            return null;
        }
    }

    public static boolean isNumeric(String str)
    {
        return str.matches("-?\\d+(\\.\\d+)?");  //match a number with optional '-' and decimal.
    }

    private void addConjuctions() {

        try {
            File file = new File( "conjuctions" );
            BufferedReader br = new BufferedReader( new FileReader( "conjuctions" ) );
            String st;
            while ((st = br.readLine()) != null){
                conjuctions.add( st );
            }
        } catch (FileNotFoundException e) {
            System.out.println("file not found");
        } catch (IOException e) {
            conjuctions=null;
        }
    }
    private void addMonths(){
        String[] months = new DateFormatSymbols().getMonths();
        for (int i = 0; i < months.length; i++) {
            monthList.put( months[i],i );
            monthList.put( months[i].toUpperCase(),i );
        }
    }
    public static void main(String[] args) {
        Parse parse=new Parse();
        parse.addMonths();
    }


}

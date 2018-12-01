package Parse;
import java.text.DateFormatSymbols;
import java.util.*;
import java.io.*;
import Parse.*;
import ExternalClasses.*;
import java.util.regex.*;

public class Parse {
    private HashMap<String,Term> terms=new HashMap<String,Term>(  );//String for the termString and int for the number of return's
    private HashSet<String> conjuctions = new HashSet<String>();
    private HashSet<String> tags=new HashSet<String>(  );
    private HashMap<String,Integer> monthList=new HashMap<String,Integer>(  );
    private String text;
    private Document doc=null;
    private static final Pattern UNWANTED_SYMBOLS = Pattern.compile("(?:|[\\[\\]{}()+/\\\\])");
    public Parse(String postingDirPath,ArrayList<String[]> docsBuffer){
        addConjuctions();//add all the conjuction to the HashSet
        addTags();
    }
    /**
     * add all the tags to an HashSet
     */
    private void addTags() {
        tags.add( "<DOC>" );
        tags.add("<TI>");
        tags.add("<TEXT>");
        tags.add("<PUB>");
        tags.add( "</DOC>" );
        tags.add("</TI>");
        tags.add("</TEXT>");
        tags.add("</PUB>");
    }

    /**
     *
     * @return an HashMap of terms and how many time they appeared
     */
    public HashSet<Term> parse(){
        text.replace( "<HEADLINE>","!H@" );
        text.replace( "</HEADLINE>","!/H@" );
        text.replace( "</<DOCNO>>","!D@" );
        text.replaceAll("<.*?>", "");
        String [] lines=text.split( "\n" );
        for (int i = 0; i <lines.length ; i++) {
            parseLine( lines[i].split( "    " ) );
        }
        return null;
    }

    /**
     * seperate line to terms
     * @param line line in an array of strings
     */
    private void parseLine(String[] line) {
        boolean title=false;
        for (int i = 0; i <line.length ; i++) {
            Matcher unwantedMatcher = UNWANTED_SYMBOLS.matcher(line[i]);
            line[i] = unwantedMatcher.replaceAll("");
            if(line[i].equals( "!H@" )) {
                title = true;
                i++;
            }
            if(line[i].equals( "!/H@>" )) {
                title = false;
                i++;
            }
            if(!conjuctions.contains( line[i] ) && !tags.contains( line[i] ))
                if ((i<line.length-4 &&line[i+3].equals( "dollars" )) && line[i+2].equals( "US" ) || (i<line.length-3 &&line[i+2].equals( "Dollars" ) ))
            {
               parsePrice( line[i],line[i+1],title );
               if(line[i+2].equals( "Dollars" ))
                   i=i+2;
               else
                   i=i+3;
            }
                else if(i<line.length-1)
                    parseTerm(line[i],line[i+1],title);
                else
                    parseTerm( line[i],null ,title);
        }

    }

    private int createDocument(String[] line) {
        for (int i = 0; i <line.length ; i++) {
            if(line[i].equals( "<DOC>" ))
                i++;
            if(line[i].equals( "<DOCNO>" )&&i<line.length-2){
                //TODO: create doc
                return i+3;
            }

        }
        return 0;
    }

    private void parsePrice(String number, String amount,boolean title) {
        if(isNumeric( number.replace( ",","" ).replace( "$","" ) ) && (amount.contains( "/" )||
                amount.equals( "m" )) || amount.equals( "bn" ) || amount.equals( "million" ) || amount.equals( "billion" ) || amount.equals( "trillion" )) {
            ParsePrice pp = new ParsePrice( number, amount );
            try {
                String term=pp.parse();
                addTerm( term ,title);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    private void parseDate(String term, String nextTerm,boolean title) {
        ParseDate pd=null;
        String newTerm=null;
        if(monthList.containsKey( term )&&isNumeric( nextTerm )) {
            pd = new ParseDate( monthList.get( term ), Integer.parseInt( nextTerm ) );
            newTerm=pd.parse();
        }
        else if(isNumeric( nextTerm )) {
            pd = new ParseDate( Integer.parseInt( term ), monthList.get( nextTerm ) );
            newTerm=pd.parse();
        }
        addTerm( newTerm,title );

    }

    private void parseTerm(String term,String nextTerm,boolean title) {
        String parseTerm="";
        if(term.charAt( term.length()-1 )=='%' || (nextTerm!=null && (nextTerm.equals( "percent" ) || nextTerm.equals( "percentage" )))) {
            parsePercent( term,title );
        }
        else if(monthList.containsKey( term ) || (nextTerm!=null&&monthList.containsKey( nextTerm )) )
            parseDate(term,nextTerm,title  );
        else if(isNumeric( term.replace( ",","" ) ))
            parseNumeric(term,nextTerm,title);
        else if(term.contains( "$" )|| (nextTerm!=null && nextTerm.equals( "Dollars" )))
            parsePrice(term,nextTerm,title);
        else if(term.contains( "-" )){
            parseRange(term,title);
        }

    }

    private void parseRange(String term,boolean title) {
        ParseRange pr=new ParseRange( term.split( "-" ) );
        String[] parse= pr.parse();
        for (int i = 0; i <parse.length ; i++) {
            addTerm( parse[i],title );
        }
    }

    private void parsePercent(String term,boolean title) {
        ParsePercent pp=new ParsePercent( term );
        try {
            String newTerm= pp.parse();
            addTerm( newTerm,title );
        } catch (Exception e) {
        }
    }

    private void parseNumeric(String term, String nextTerm,boolean title) {
        ParseNumber pn= new ParseNumber( term,nextTerm );
        try {
            addTerm( pn.parse(),title);
        } catch (Exception e) {
        }
    }

    private boolean isNumeric(String str)
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
    private void addTerm(String term,boolean title){
        if(terms.containsKey( term )){
            terms.get( term ).tf=terms.get( term ).tf+1;
        }
        else{
            Term t=new Term(term,false,false,title);
            terms.put( term,t );
        }
    }
    public static void main(String[] args) {
        Parse parse=new Parse(null,null);
        parse.addMonths();
    }


}

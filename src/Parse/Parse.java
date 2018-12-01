package Parse;
import java.text.DateFormatSymbols;
import java.util.*;
import java.io.*;
import Parse.*;
import ExternalClasses.*;
import java.util.regex.*;

public class Parse {
    private HashSet<String> conjuctions = new HashSet<String>();
    private HashMap<String,Integer> monthList=new HashMap<String,Integer>(  );
    private Document doc;
    private String text;
    private String pathToFile;
    private String startLine;
    private String endLine;
    private ArrayList<String[]> docsBuffer;
    private HashSet<Document> docsToIndexer;
    private static final Pattern UNWANTED_SYMBOLS = Pattern.compile("(?:|[\\[\\]{}()+/\\\\])");
    public Parse(){
        addConjuctions();//add all the conjuction to the HashSet
        addMonths();
    }
    /**
     * add all the tags to an HashSet
     */
    public void setDocsBuffer(ArrayList<String[]> docsBuffer) {
        this.docsBuffer.addAll( docsBuffer );
    }

    /**
     *
     * @return an HashMap of terms and how many time they appeared
     */

    public HashSet<Document> parse(){
        for (int i = 0; i < docsBuffer.size(); i++) {
            String[] docProp=docsBuffer.get(0);
            pathToFile=docProp[0];
            startLine=docProp[1];
            endLine=docProp[2];
            text=docProp[3];
            text.replace( "<TI>","!H@ " );
            text.replace( "</TI>","!/H@ " );
            text.replace( "<TEXT>","!T@ " );
            text.replace( "</TEXT>","!/T@ " );
            text.replace( "<DATE1>","!DA@ " );
            text.replace( "<F P=104>","!F@ " );
            text.replace( "</<DOCNO>>","!D@ " );
            text.replaceAll("<.*?>", "");
            String [] lines=text.split( "\n" );
            for (int j = 0; j <lines.length ; j++) {
                parseLine( lines[i].split( "    " ) );
            }
        }
        return docsToIndexer;
    }

    /**
     * seperate line to terms
     * @param line line in an array of strings
     */
    private void parseLine(String[] line) {
        boolean title=false;
        boolean parse=false;
        for (int i = 0; i <line.length ; i++) {
            Matcher unwantedMatcher = UNWANTED_SYMBOLS.matcher( line[i] );
            line[i] = unwantedMatcher.replaceAll( "" );
            if (line[i].equals( "!F@" )) {
                doc.setCityOfOrigin( line[i + 1] );
            }
            if (line[i].equals( "!D@" ))
                doc = new Document( line[i + 1], pathToFile, Integer.parseInt( startLine ), Integer.parseInt( endLine ) );
            if (line[i].equals( "!H@" )) {
                title = true;
                parse = true;
                i++;
            }
            if (line[i].equals( "!/H@>" )) {
                title = false;
                parse = false;
                i++;
            }
            if (line[i].equals( "!T@" )) {
                parse = true;
                i++;
            }
            if (line[i].equals( "!/T@>" )) {
                parse = false;
                i++;
            }
            if (line[i].equals( "!DA@" )){
                String date= line[i+1]+"-"+monthList.get( line[i+2] )+"-"+line[i+3];
                doc.date=date;
            }
            if (parse) {
                if (!conjuctions.contains( line[i] ))
                    if ((i < line.length - 4 && line[i + 3].equals( "dollars" )) && line[i + 2].equals( "US" ) || (i < line.length - 3 && line[i + 2].equals( "Dollars" ))) {
                        parsePrice( line[i], line[i + 1], title );
                        if (line[i + 2].equals( "Dollars" ))
                            i = i + 2;
                        else
                            i = i + 3;
                    } else if (i < line.length - 1)
                        parseTerm( line[i], line[i + 1], title );
                    else
                        parseTerm( line[i], null, title );
            }
        }

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
            File file = new File( "stop_words" );
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
        Term t=new Term(term,title);
        doc.addTerm( t);
    }
    public static void main(String[] args) {
    }


}

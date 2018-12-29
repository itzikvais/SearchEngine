package Parse;
import java.text.DateFormatSymbols;
import java.util.*;
import java.io.*;
import ExternalClasses.*;
import Stemmer.*;
import javax.print.Doc;
import javax.print.attribute.standard.DocumentName;
import java.util.regex.*;


public class Parse {
    private static HashSet<String> stopWords = new HashSet<String>();
    private static int numOfPArse=0;
    private static HashMap<String,Integer> monthList=new HashMap<String,Integer>(  );
    private Document doc;
    private String text;
    private String pathToFile;
    private String startLine;
    private String endLine;
    private static Stemmer stem;
    private ArrayList<String[]> docsBuffer=new ArrayList<String[]>(  );
    private HashSet<Document> docsToIndexer=new HashSet<Document>(  );
    private static final Pattern UNWANTED_SYMBOLS = Pattern.compile("(?:|[\\[\\]{}()+/\\\\])");
    private boolean toStem;
    private int termPlace;
    private boolean isSearcher=false;
    private ArrayList<String> termsForSearcher;
    /**
     * a constructor for a sercher
     */
    public Parse(boolean isSearcher,boolean toStem){
        this.isSearcher=isSearcher;
        termsForSearcher=new ArrayList<>(  );
        this.toStem=toStem;
        doc=new Document( null,null,0,0 );
        numOfPArse++;
        stem=new Stemmer();
    }
    /**
     *
     * @param toStem using stem or not
     */
    public Parse(String corpusPath,boolean toStem){
        numOfPArse++;
        if(numOfPArse==1) {
            addStopwords(corpusPath);//add all the stopwards to the HashSet
            addMonths();
        }
        this.toStem = toStem;
        if (toStem)
            stem = new Stemmer();
    }
    /**
     * add all the tags to an HashSet
     */
    public void setDocsBuffer(ArrayList<String[]> docsBuffer) {
        this.docsBuffer.addAll( docsBuffer );
    }
    public ArrayList<String> parseForSearcher(String query){
        if(query==null)
            return null;
        parseLine( query.split( " " ) );
        clearParser();
        return termsForSearcher;
    }
    /**
     *
     * @return an HashSet of parsed documents
     */
    public HashSet<Document> parse(){
        for (int i = 0; i < docsBuffer.size(); i++) {
            String[] docProp=docsBuffer.get(i);
            termPlace=1;
            pathToFile=docProp[0];
            startLine=docProp[1];
            endLine=docProp[2];
            text=docProp[3];
            text=text.replace( "<TI>"," !H@ " ).replace( "</TI>"," !/H@ " ).replace( "<TEXT>"," !T@ " ).replace( "</TEXT>"," !/T@ " ).replace( "<DATE1>"," !DA@ " ).replace( "<F P=104>"," !F@ " ).replace( "<F P=105>"," !L@ " ).replace( "<DOCNO>"," !D@ " ).replace( "</DOCNO>"," !/D@ " );
            text=text.replaceAll("<.*?>", " ");
            String [] lines=text.split( "\n" );
            for (int j = 0; j <lines.length ; j++) {//parse any line in the doc
                parseLine( lines[j].split("\\s+") );
            }
            mergeTerms();
            doc.sort();
            docsToIndexer.add( doc );
            doc=null;
        }
        clearParser();
        text=null;
        docsBuffer.clear();
        return docsToIndexer;
    }

    /**
     * merge terms with upper case and lower case
     */
    private void mergeTerms() {
        HashMap<Term, Integer> docTerms=new HashMap<Term, Integer>(  );
        Iterator it = doc.docTermsAndCount.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            Term t = (Term) pair.getKey();
            String term=t.termString;
            String lowTerm=null;
            if(!stopWords.contains( term )){
                if(term.charAt( 0 )>=65&&term.charAt( 0 )<=90) {
                    lowTerm = term.toLowerCase();
                    Term Nterm = new Term( lowTerm, t.isTitle );
                    if (doc.docTermsAndCount.containsKey( Nterm )) {
                        int val = doc.docTermsAndCount.get( Nterm ) + doc.docTermsAndCount.get( t );
                        for(Term lt:doc.docTermsAndCount.keySet()){
                            if(lt.equals( Nterm )){
                                Nterm.isTitle=Nterm.isTitle||lt.isTitle;
                                break;
                            }
                        }
                        docTerms.put( Nterm, val );
                    }
                    else {
                        int val=doc.docTermsAndCount.get( t );
                        t.termString=t.termString.toUpperCase();
                        docTerms.put( t, val );
                    }

                }
                else if(!docTerms.containsKey( t ))
                    docTerms.put( t,doc.docTermsAndCount.get( t ) );
            }
        }

        doc.setDocTermsAndCount( docTerms );

    }

    /**
     * delete all vars in the parser
     */
    private void clearParser() {
        text=null;
        pathToFile=null;
        startLine=null;
        endLine=null;
        docsBuffer.clear();
    }


    /**
     * seperate line to terms
     * @param line line in an array of strings
     */
    private void parseLine(String[] line) {
        boolean title=false;
        boolean parse=false;
        if(isSearcher)
            parse=true;
        for (int i = 0; i <line.length ; i++) {
            if (i+1<line.length&&line[i].equals( "!F@" )&&!line[i+1].equals( "!/T@" )) {
                line[i+1]=line[i+1].replaceAll( "[\\p{Punct}]+","" );
                if(line[i+1]!=null&&!isNumeric(line[i+1])) {
                    doc.setCityOfOrigin(line[i + 1]);
                }
            }
            if (i+1<line.length&&line[i].equals( "!L@" )&&!line[i+1].equals( "!/T@" )) {
                line[i+1]=line[i+1].replaceAll( "[\\p{Punct}]+","" );
                if(line[i+1]!=null&&!isNumeric(line[i+1])) {
                    doc.setLanguage(line[i + 1]);
                }
            }
            if (i<line.length&&line[i].equals( "!D@" )) {
                doc = new Document( line[i + 1], pathToFile, Integer.parseInt( startLine ), Integer.parseInt( endLine ) );
                i++;
            }
            if (i<line.length&&line[i].equals( "!H@" )) {
                title = true;
                parse = true;
                i++;
            }
            if (i<line.length&&line[i].equals( "!/H@" )) {
                title = false;
                parse = false;
                i++;
            }
            if (i<line.length&&line[i].equals( "!T@" )) {
                parse = true;
                i++;
            }
            if (i<line.length&&line[i].equals( "!/T@" )) {
                parse = false;
                i++;
            }
            if (i<line.length&&line[i].equals( "!DA@" )){
                String date= line[i+1]+"-"+monthList.get( line[i+2] )+"-"+line[i+3];
                doc.setDate(date);
            }
            if (parse) {
                line[i]=line[i].replaceAll("[\\p{Punct}&&[^-.,%$/]]+", "--"  );
                if(line[i].contains( "/" )&&(line[i].split( "/" ).length!=2||!isInteger( line[i].split( "/" )[0])||isInteger( line[i].split( "/" )[1] )))
                    line[i]=line[i].replaceAll( "/","--" );
                String punctuations = ".,:;/";
                if(line[i]!=null&&line[i].contains( "," )&&!isNumeric( line[i].replaceAll( ",","" ) ))
                    line[i].replaceAll( ",","--" );
                while(line[i].length()>=1&&punctuations.contains( ""+line[i].charAt( 0 ) ))
                    line[i]=line[i].substring( 1 );
                if(line[i].length()>=1&&line[i].charAt( line[i].length()-1 )=='.')
                    line[i]=line[i].substring( 0,line[i].length()-1 );
                if(line[i].length()<1)
                    continue;
                String[] terms=null;
                if(line[i].contains( "--" )||line[i].contains( "--" )){
                    terms=line[i].split( "--" );
                }
                if(terms!=null){
                    for (int j = 0; j <terms.length ; j++) {
                        while(terms[j].length()>=1&&punctuations.contains(""+terms[j].charAt(0)))
                            terms[j]=terms[j].substring(1);
                        if(terms[j].length()<1)
                            continue;
                        if(!stopWords.contains(terms[j]))
                            parseTerm( terms[j], null, title,i );
                    }
                }
                else if (!stopWords.contains( line[i] ))
                    if ((i < line.length - 4 && line[i + 3].equals( "dollars" )) && line[i + 2].contains( "US" )&&isNumeric( line[i] ) || (i < line.length - 3 && line[i + 2].equals( "Dollars" )&&isNumeric( line[i] )&&(line[i+1].equals( "bn" )||line[i+1].equals( "m" )))) {
                        parsePrice( line[i], line[i + 1], title );
                        if (line[i + 2].equals( "Dollars" ))
                            i = i + 2;
                        else
                            i = i + 3;
                    }
                    else if(i<line.length-4&&(line[i].equals( "Between" )||line[i].equals( "between" ))&&line[i+2].equals( "to" )&&isNumeric( line[i+1] )&&isNumeric( line[i+3] )){
                        String[] range=new String[4];
                        range[0]=line[i];
                        range[1]=line[i+1];
                        range[2]=line[i+2];
                        range[3]=line[i+3];
                        ParseRange pr=new ParseRange( range );
                        String[] parsed=pr.parse();
                        if(parsed.length>=1)
                            addTerm(parsed[0],title);
                        for (int j = 1; j <parsed.length ; j++) {
                            parseTerm(parsed[j],null,title,i);
                        }
                    }
                    else if (i < line.length - 1)
                        i= parseTerm( line[i], line[i + 1], title,i );
                    else
                        i= parseTerm( line[i], null, title,i );
            }
        }

    }



    private void parsePrice(String number, String amount,boolean title) {
        if(amount==null)
            amount="";
        if(amount!=null&&isNumeric( number.replace( ",","" ).replace( "$","" ) ) &&(amount.contains( "/" )||
                amount.equals( "m" )) || amount.equals( "bn" ) || amount.equals( "million" ) || amount.equals( "billion" ) || amount.equals( "trillion" )) {
            ParsePrice pp = new ParsePrice( number, amount );
            try {
                String term=pp.parse();
                addTerm( term ,title);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        else if(isNumeric( number.replace( ",","" ).replace( "$","" )  )){
            ParsePrice pp = new ParsePrice( number, null );
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
        else if( monthList.containsKey( nextTerm )&&isNumeric( term )){
            pd = new ParseDate( Integer.parseInt( term ), monthList.get( nextTerm ) );
            newTerm=pd.parse();
        }
        addTerm( newTerm,title );

    }

    /**
     * parse every term
     * @param term the term
     * @param nextTerm the next term
     * @param title if the term is on title
     * @param i
     * @return the place to continue
     */
    private int parseTerm(String term,String nextTerm,boolean title,int i) {
        String parseTerm="";
        if(nextTerm!=null&&nextTerm.contains( "-" ))
            nextTerm=null;
        term=term.trim();
        if(term.contains( "," ))
            term=term.replaceAll( ",","" );
        if(term.length()>=1&&!isNumeric(term)&&!term.contains( "%" ))
            term=clearTerm(term);
        if(isNumeric( term )&&nextTerm!=null&&nextTerm.contains( "/" )&&nextTerm.split( "/" ).length==2&&isNumeric( nextTerm.split( "/" )[0])&&isNumeric( nextTerm.split( "/" )[1] ) ){
            if(term.equals( "0" ))
                addTerm( nextTerm,title );
            else
                addTerm( term+" "+nextTerm,title );
            return i+1;
        }
        if(term!=null&&!isNumeric( term )&&term.contains( "." )) {
            String[] splited=term.split( "." );
            for (int j = 0; j <splited.length ; j++) {
                parseTerm(splited[i],null,title,i);
            }
            return i;
        }
        term=term.replaceAll( "/","" );
        if(nextTerm!=null&&!isNumeric(nextTerm)&&!nextTerm.contains( "%" ))
            nextTerm=clearTerm(nextTerm);
        if(term!=null&&term.length()>=3&&isNumeric(term)&&term.contains("."))
            term=String.format("%.2f", Double.parseDouble(term));
        if(nextTerm!=null&&nextTerm.length()>=3&&isNumeric(nextTerm)&&nextTerm.contains("."))
            nextTerm=String.format("%.2f", Double.parseDouble(nextTerm));
        while(term.length()>=1&&((term.charAt( 0 )=='%'&&!isNumeric( term.substring( 1 ) ))||term.charAt( 0 )=='.'))
            term=term.substring( 1 );
        if(term.length()>=1&&term.charAt( 0 )=='%'&&isNumeric( term.substring( 1 ) ))
            term=term.substring( 1 ) +"%";
        if(term.length()<=1)
            return i;
        if(term.contains( "%" )&&term.split( "%" ).length>=2){
            String[] splited=term.split( "%" );
            splited[0]+="%";
            for (int j = 0; j < splited.length; j++) {
                parseTerm(splited[j],null,title,i);
            }
            return i;
        }
        if(term.equals( doc.getCityOfOrigin() ))
            doc.setNewCityLocation( termPlace );
        if(term.contains( "$" )&&term.contains( "-" )){
            if(term.split( "-" ).length>=2&&isNumeric( term.split( "-" )[0].replace( "$","" ) )&&isNumeric( term.split( "-" )[1].replace( "$","" ) )){
                parsePrice( term.split( "-" )[0], nextTerm, title );
                parsePrice( term.split( "-" )[1], nextTerm, title );
                if((nextTerm!=null && (nextTerm.equals( "billion" )||nextTerm.equals( "million" )||nextTerm.equals( "trillion" ))))
                    return i+1;
            }
            else if(term.split( "-" ).length>=2&&isNumeric( term.split( "-" )[0].replace( "$","" ) )&&(term.split( "-" )[1].equals( "million" )||term.split( "-" )[1].equals( "trilliom" )||term.split( "-" )[1].equals( "billion" )||term.split( "-" )[1].equals( "Million" )||term.split( "-" )[1].equals( "Trilliom" )||term.split( "-" )[1].equals( "Billion" )))
                parsePrice(term.split( "-" )[0], term.split( "-" )[1].toLowerCase(), title);
            else if(term.split( "-" ).length>=2&&isNumeric( term.split( "-" )[0].replace( "$","" ))&&term.split( "-" )[1].equals( "a" ))
                parsePrice(term.split( "-" )[0],null,title);
            else if((term.split( "-" ).length>=1&&isNumeric( term.split( "-" )[0].replace( "$","" ))))
                parsePrice(term.split( "-" )[0],null,title);

        }
        else if(term.contains( "-" )){
            parseRange(term,title);
        }
        else if((term.contains( "%")&&isNumeric( term.substring( 0,term.length()-1 ) )) || (nextTerm!=null &&isNumeric (term)&&(nextTerm.equals( "percent" ) || nextTerm.equals( "percentage" )))) {
            parsePercent( term,title );
            if((nextTerm!=null && (nextTerm.equals( "percent" ) || nextTerm.equals( "percentage" ))))
                return i+1;
        }
        else if(monthList.containsKey( term )&&isInteger( nextTerm ) || (nextTerm!=null&&nextTerm.length()>1&&monthList.containsKey( nextTerm )&& isInteger( term )) ) {
            parseDate( term, nextTerm, title );
            return i+1;
        }
        else if(isNumeric( term )&&nextTerm!=null&&nextTerm.equals( "Dollars" )){
            parsePrice( term,null,title );
            return i+1;
        }
        else if(isNumeric( term )&&(nextTerm!=null&&(nextTerm.equals( "m" )||nextTerm.equals( "mile" )||nextTerm.equals( "km" )||nextTerm.equals( "kilometers" )||nextTerm.equals( "meters" )))){
            parseDistance(term,nextTerm,title);
            return i+1;
        }
        else if(isNumeric( term )&&(nextTerm!=null&&(nextTerm.equals( "g" )||nextTerm.equals( "grams" )||nextTerm.equals( "kg" )||nextTerm.equals( "kilograms" )||nextTerm.equals( "kgs" )))){
            parseWeight(term,nextTerm,title);
            return i+1;
        }
        else if(isNumeric( term )) {
            parseNumeric( term, nextTerm, title );
            if(nextTerm!=null&&(nextTerm.equals( "Billion" )||nextTerm.equals( "Trillion" )||nextTerm.equals( "Million" )||nextTerm.equals( "Thousand" )))
                return i+1;
            return i;
        }
        else if(term.contains( "$" )) {
            if(term.replace( "$","" ).length()==0)
                return i;
            parsePrice( term, nextTerm, title );
            if((nextTerm!=null && (nextTerm.equals( "billion" )||nextTerm.equals( "million" )||nextTerm.equals( "trillion" ))))
                return i+1;
        }
        else {
            addTerm( term, title );
        }
        return i;
    }

    private String clearTerm(String term) {
        int i=0;
        int j=0;
        for(i=0;i<term.length();i++){
            if((term.charAt(i)>='A'&&term.charAt(i)<='Z')||(term.charAt(i)>='a'&&term.charAt(i)<='z')||(term.charAt( i )=='$')){
                break;
            }
        }
        for(j=i;j<term.length();j++){
            if(!(term.charAt(j)>='A'&&term.charAt(j)<='Z')&&!(term.charAt(j)>='a'&&term.charAt(j)<='z')&&!(term.charAt( i )=='$')&&!(term.charAt(j)=='-')){
                break;
            }
        }
        if(j<term.length()-1&&((term.charAt(j)=='$')||(term.charAt(j)=='%')))
            j++;
        term=term.substring(i,j);
        return term;

    }

    private void parseWeight(String term, String nextTerm, boolean title) {
        ParseWeight pw= new ParseWeight( Double.parseDouble( term ) ,nextTerm);
        String newTerm=pw.parse();
        addTerm( newTerm,title );
    }

    private void parseDistance(String term, String nextTerm, boolean title) {
        ParseDistance pd= new ParseDistance( Double.parseDouble( term ) ,nextTerm);
        String newTerm=pd.parse();
        addTerm( newTerm,title );
    }

    private void parseRange(String term,boolean title) {
        if(!(term.length() <3)) {
            ParseRange pr = new ParseRange( term.split( "-" ) );
            String[] parse = pr.parse();
            if(parse!=null) {
                if(parse.length>=1)
                    addTerm(parse[0],title);
                for (int i = 1; i < parse.length; i++) {
                    parseTerm( parse[i],null, title,i );
                }
            }
        }
    }

    private void parsePercent(String term,boolean title) {
        ParsePercent pp=new ParsePercent( term.replace( "%","" ) );
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

    private void addStopwords(String path) {

        try {
            BufferedReader br = new BufferedReader( new FileReader( path+"\\stop_words.txt" ) );
            String st;
            while ((st = br.readLine()) != null){
                stopWords.add( st );
                st=st.substring(0, 1).toUpperCase() + st.substring(1);
                stopWords.add(st);
                stopWords.add(st.toUpperCase());
            }
            br.close();
        } catch (FileNotFoundException e) {
            System.out.println("file not found");
        } catch (IOException e) {
        }
    }
    private void addMonths(){
        String[] months = new DateFormatSymbols().getMonths();
        for (int i = 0; i < months.length; i++) {

            monthList.put( months[i],i+1 );
            monthList.put( months[i].toUpperCase(),i+1 );
        }
    }
    private static boolean isInteger(String s) {
        try {
            Integer.parseInt(s);
        } catch(NumberFormatException e) {
            return false;
        } catch(NullPointerException e) {
            return false;
        }
        // only got here if we didn't return false
        return true;
    }
    private void addTerm(String term,boolean title){
        if(term!=null&&term.contains( "%" )&&term.charAt( 0 )=='.')
            term=term.substring( 1 );
        if(term!=null&&term.length()>=1&&term.charAt(0)=='-')
            term=term.substring(1);
        if(term==null||term.length()<1)
            return;
        else {
            termPlace++;
            if (toStem) {
                stem.add( term.toCharArray(), term.length() );
                stem.stem();
                term = stem.toString();

            }
            if (term != null && term.length() > 0) {
                Term t = new Term( term, title );
                if(isSearcher){
                    termsForSearcher.add( term );
                }
                else
                    doc.addTerm( t );
            }
        }
    }


}
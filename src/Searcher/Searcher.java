package Searcher;

import ExternalClasses.DocForSearcher;
import Parse.Parse;
import Ranker.Ranker;

import javax.xml.crypto.dom.DOMCryptoContext;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;

public class Searcher {
    private ArrayList<String> citys;
    private String queries;
    private boolean isFile;
    private boolean useSemantic;
    private String postingPath;
    private boolean toStem;
    public Searcher(ArrayList<String> citys, String queries, boolean isFile, boolean useSemantic,boolean toStem,String postingPath ) {
        this.citys = citys;
        this.queries = queries;
        this.isFile = isFile;
        this.useSemantic = useSemantic;
        this.postingPath=postingPath;
        this.toStem=toStem;
    }

    /**
     * start the searcher
     * @return the most 50 relevant docs
     */
    public HashMap<String, DocForSearcher> start(){
        Parse parse=new Parse( true,toStem );
        if(!isFile){
            Ranker ranker=null;
            ArrayList<String> parsedTerms=parse.parseForSearcher( queries );
            parsedTerms=changeToUpperOrLower(parsedTerms);
            for(String s: parsedTerms)
                System.out.println(s);
            ranker=new Ranker( parsedTerms,postingPath,citys,useSemantic,toStem );
            ranker.rank( );
            ArrayList<DocForSearcher> rankedDocs= ranker.getDocsWithRank();
            ranker.printData();
            printDate( rankedDocs );
        }
        else{
            BufferedReader br=null;
            try {
                br = new BufferedReader(new InputStreamReader(new FileInputStream(new File(queries))));
                parseFileAndSendToParser(br);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * changing upper case terms to lower case if there is a same lowercase terms in dictionary
     * @param parsedTerms array list of parsed terms
     * @return
     */
    private ArrayList<String> changeToUpperOrLower(ArrayList<String> parsedTerms) {
        ArrayList<String> upperOrLowerTerms=new ArrayList<>(  );
        for (int i = 0; i < parsedTerms.size(); i++) {
            parsedTerms.set( i,parsedTerms.get( i ).toLowerCase() );
            System.out.println(parsedTerms.get( i ));
        }
        BufferedReader br=null;
        try {
            if(toStem)
                br = new BufferedReader(new InputStreamReader(new FileInputStream(new File(postingPath+ "\\" + "withStemming" + "\\" + "dictionary" + ".txt"))));
            else
                br=new BufferedReader(new InputStreamReader(new FileInputStream(new File(postingPath+ "\\" + "withoutStemming" + "\\" + "dictionary" + ".txt"))));
            //parseFileAndSendToParser(br);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        String line=null;
        try {
            while ((line = br.readLine()) != null){
                String term=line.split( "#" )[0];
                if(parsedTerms.contains( term )||parsedTerms.contains( term.toLowerCase() )){
                    upperOrLowerTerms.add(term);
                    System.out.println(term);
                }

            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return upperOrLowerTerms;
    }

    private void printDate(ArrayList<DocForSearcher> rankedDocs) {
        for (DocForSearcher d:rankedDocs) {
            System.out.println(d.getDocID() +" :"+d.rank);
        }
    }

    private void parseFileAndSendToParser(BufferedReader br) {
        String line=null;
        try {
            while ((line = br.readLine()) != null){
                int queryNum=0;
                String[] splited=line.split( " " );
                if(splited[0].equals( "<num>" ))
                    queryNum=Integer.parseInt( splited[3] );
                if(splited[0].equals( "<title>" )){
                    String query="";
                    for (int i = 1; i <splited.length ; i++) {
                        splited[i]=splited[i].trim();
                        if(splited[i].length()>0)
                            query+=splited[i]+" ";
                    }
                    query=query.substring( 0,query.length()-1 );
                    Parse parse=new Parse( true,toStem );
                    ArrayList<String> termsToRanker=parse.parseForSearcher( query );
                    termsToRanker=changeToUpperOrLower( termsToRanker );
                    Ranker ranker=new Ranker( termsToRanker, postingPath,citys,useSemantic,toStem );
                    ranker.rank(  );
                    ArrayList<DocForSearcher> rankedDocs=ranker.getDocsWithRank();
                }
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        Searcher searcher=new Searcher( null,"ISRAEL PALESTINE",false,true,false,"/Users/itzikvais/Documents/ISE/year c/ir/reset" );
        searcher.start();
    }
}

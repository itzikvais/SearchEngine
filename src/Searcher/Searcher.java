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
    private String resultPath;
    private boolean isFile;
    private boolean useSemantic;
    private String postingPath;
    private boolean toStem;
    private  PrintWriter resultFilePW;
    private Ranker ranker;
    public Searcher(ArrayList<String> citys, String queries, boolean isFile, boolean useSemantic,boolean toStem,String postingPath,String resultPath ) {
        this.citys = citys;
        this.queries = queries;
        this.isFile = isFile;
        this.useSemantic = useSemantic;
        this.postingPath=postingPath;
        this.toStem=toStem;
        this.resultPath=resultPath+ "\\resultFile.txt";
        //create PrintWriter
        File resultFile = new File(this.resultPath);
        if (resultFile.exists()) resultFile.delete();

        resultFilePW = null;
        try {
            resultFilePW = new PrintWriter(new FileOutputStream(resultFile, true));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        if (resultFilePW == null) {
            System.out.println("resultFile is null!! - Cannot create resultFilePW");
        }
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
            ranker=new Ranker( parsedTerms,postingPath,citys,useSemantic,toStem );
            ranker.rank( );
            ArrayList<DocForSearcher> rankedDocs= ranker.getDocsWithRank();
            writeToFile(100,0,rankedDocs);
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
        resultFilePW.flush();
        resultFilePW.close();
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
            int queryNum=0;
            boolean desc=false;
            boolean discuss=false;
            String description="";
            while ((line = br.readLine()) != null){
                String[] splited=line.split( " " );
                if(splited.length>=1&&splited[0].equals( "<num>" )) {
                    System.out.println(line);
                    System.out.println(splited[2].trim());
                    queryNum = Integer.parseInt(splited[2].trim());
                    System.out.println(queryNum);
                }
                if(splited.length>1&&splited[0].equals( "<title>" )){
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
                    ranker=new Ranker( termsToRanker, postingPath,citys,useSemantic,toStem );
                }
                if(desc) {
                    if (!discuss) {
                        for (int i = 0; i < splited.length; i++) {
                            if(discuss)
                                description+= " " + splited[i];
                            if (splited[i].equals("discuss"))
                                discuss = true;
                        }
                    }
                    else {
                        for (int i = 0; i <splited.length ; i++) {
                            if(splited[i].charAt(splited[i].length()-1)=='.') {
                                description+= " "+splited[i].substring(0,splited[i].length()-1).trim();
                                Parse parse=new Parse(true,toStem);
                                ArrayList<String> descriptionParsed=parse.parseForSearcher(description);
                                descriptionParsed=changeToUpperOrLower(descriptionParsed);
                                ranker.setDescriptionTerms(descriptionParsed);
                                description="";
                                discuss = false;
                                desc=false;
                            }
                            else{
                                description+=" " +splited[i];
                            }
                        }
                    }
                }
                if(splited.length>1&&splited[0].equals("<desc>" )){
                    desc=true;
                }
                if(splited.length>1&&splited[0].equals("<narr>" )){
                    desc=false;
                }
                if(line.contains("</top>" )){
                    ranker.rank();
                    ArrayList<DocForSearcher> rankedDocs=ranker.getDocsWithRank();
                    writeToFile(queryNum, 0,rankedDocs);
                }
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    private void writeToFile( int queryNum, int i, ArrayList<DocForSearcher> rankedDocs) {
        for (DocForSearcher d: rankedDocs) {
            resultFilePW.println(queryNum+" "+i+" "+ d.getDocID()+" "+d.rank + " "+ 42.38 + " " + "mt" );
        }

    }

    public static void main(String[] args) {
        //Searcher searcher=new Searcher( null,"ISRAEL PALESTINE",false,true,false,"/Users/itzikvais/Documents/ISE/year c/ir/reset" );
        //searcher.start();
    }
}

package Searcher;

import ExternalClasses.DocForSearcher;
import Parse.Parse;
import Ranker.Ranker;

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
    public HashMap<String, DocForSearcher> start(){
        Parse parse=new Parse( true,toStem );
        if(!isFile){
            Ranker ranker=null;
            ranker=new Ranker(parse.parseForSearcher( queries ),postingPath );
            printDate(ranker.rank( toStem ));
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

    private void printDate(HashMap<String, DocForSearcher> rank) {
        for (DocForSearcher d:rank.values()) {
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
                    Ranker ranker=new Ranker( termsToRanker, postingPath );
                    HashMap<String,DocForSearcher> afterRank=ranker.rank( toStem );
                }
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        Searcher searcher=new Searcher( null,"ISRAEL PALESTINE",false,true,false,"/Users/itzikvais/Documents/ISE/year c/ir/reset\\withoutStemming" );
        searcher.start();
    }
}

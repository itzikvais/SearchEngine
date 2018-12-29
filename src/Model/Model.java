package Model;

import Controller.Controller;
import ExternalClasses.DocForSearcher;
import ReadFile.ReadFile;
import Searcher.Searcher;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;

import java.io.IOException;
import java.util.*;

public class Model extends Observable implements ModelInt {
    private ReadFile rf;
    private Controller cont;

    public Alert getExitMessage() {
        Alert exit = new Alert(Alert.AlertType.CONFIRMATION);
        exit.setTitle("exit");
        exit.setHeaderText("Are you sure you want to exit?");
        ButtonType yes = new ButtonType("yes");
        ButtonType no = new ButtonType("no", ButtonBar.ButtonData.CANCEL_CLOSE);
        exit.getButtonTypes().setAll(yes,no);
        return exit;
    }

    @Override
    // delete all files
    public void reset() {
        rf.reset();
        rf=null;
        setChanged();
        notifyObservers();
    }
    public void setController(Controller c){
        this.cont=c;
    }
    @Override
    public HashSet<String>[] start(String corpusPath, String filesPath, boolean toStem) throws IOException {
        rf=new ReadFile( corpusPath,filesPath );
        HashSet<String>[] toReturn=rf.start(toStem);
        cont.uniqueTerms=rf.getIndexer().totalUniqueTerms;
        cont.numOfDocs=rf.getIndexer().totalDocsNum;
        return toReturn;

    }

    @Override
    public String showDictionary() {
        try {
            return rf.showDictionary();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return  null;
    }

    @Override
    public void loadDictionary(boolean b) {
        rf.loadDictionary(b);
        setChanged();
        notifyObservers();
    }

    @Override
    public ArrayList<DocForSearcher> searchSingleQuery(String query, ArrayList<String> cities, String postingPath,String resultFile, boolean stem, boolean selected) {
        rf.clear();
        Searcher searcher=new Searcher( cities,query,false,selected,stem,postingPath,resultFile );
        HashMap<String,DocForSearcher> docs=searcher.start();
        ArrayList<DocForSearcher> toReturn=new ArrayList<>(  );
        if(docs==null)
            return null;
        for(DocForSearcher doc:docs.values()){
            toReturn.add(doc);
        }
        toReturn.sort( new Comparator<DocForSearcher>() {
            @Override
            public int compare(DocForSearcher o1, DocForSearcher o2) {
                if(o1.rank>o2.rank)
                    return 1;
                return 0;
            }
        } );
        return toReturn;
    }

    @Override
    public void searchFileQuery(String query, ArrayList<String> cities, String postingPath, String resultFile, boolean stem, boolean selected) {
        rf.clear();
        Searcher searcher=new Searcher( cities,query,true,selected,stem,postingPath,resultFile );
        searcher.start();
    }


}

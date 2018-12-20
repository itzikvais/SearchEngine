package Model;

import Controller.Controller;
import ReadFile.ReadFile;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;

import java.io.IOException;
import java.util.HashSet;
import java.util.Observable;

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
    public HashSet<String> start(String corpusPath, String filesPath, boolean toStem) throws IOException {
        rf=new ReadFile( corpusPath,filesPath );
        HashSet<String> toReturn=rf.start(toStem);
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

}

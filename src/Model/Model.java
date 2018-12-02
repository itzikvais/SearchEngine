package Model;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;

import java.util.Observable;

public class Model extends Observable implements ModelInt {

    /*@Override
    public boolean createTable(String[] args) {
        return false;
    }

    @Override
    public boolean update(String[] args) {
        return false;
    }

    @Override
    public boolean add(String[] args) {
        return false;
    }

    @Override
    public boolean delete(String[] args) {
        return false;
    }
*/
    public Alert getExitMessage() {
        Alert exit = new Alert(Alert.AlertType.CONFIRMATION);
        exit.setTitle("exit");
        exit.setHeaderText("Are you sure you want to exit?");
        ButtonType yes = new ButtonType("yes");
        ButtonType saveFirst = new ButtonType("yes, but save maze first");
        ButtonType no = new ButtonType("no", ButtonBar.ButtonData.CANCEL_CLOSE);
        exit.getButtonTypes().setAll(yes,saveFirst,no);
        return exit;
    }
}

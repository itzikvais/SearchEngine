package Model;

import javafx.scene.control.Alert;

import java.io.IOException;
import java.util.HashSet;

public interface ModelInt {
    Alert getExitMessage();
    public void reset();
    public HashSet<String> start(String corpusPath, String filesPath, boolean toStem) throws IOException;

    String showDictionary();

    void loadDictionary(boolean b);
}

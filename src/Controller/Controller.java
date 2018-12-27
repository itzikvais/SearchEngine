package Controller;


import ExternalClasses.DocForSearcher;
import Model.Model;
import Model.ModelInt;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.stage.*;
import javafx.collections.ObservableList;
import java.io.File;
import java.io.IOException;
import java.util.*;
import javafx.geometry.Insets;

public class Controller implements Observer {
    ModelInt myModel;
    private boolean stem = false;
    private boolean semantic=false;
    private double time;
    public javafx.scene.control.Button closeButton;
    public javafx.scene.control.Button btn_start;
    public javafx.scene.control.Button btn_reset;
    public javafx.scene.control.Button btn_shDic;
    public javafx.scene.control.Button btn_loadDic;
    public javafx.scene.control.CheckBox stemmer;
    private HashSet<String> languages;
    private HashSet<String> cities;
    private MenuButton citiesMenu;
    public boolean start;
    public boolean reset;
    private Group docsGroup=null;
    private Group group;
    public int uniqueTerms;
    public int numOfDocs;
    private String dictionary;
    private Stage primaryStage;
    public javafx.scene.control.TextField txtfld_corpus;
    public javafx.scene.control.TextField txtfld_path;
    private boolean load;
    private TextField queryFilePlace;
    private TextField queryPlace;
    private String postingPath;
    private CheckBox useSemantic;
    private int height=50;
    private Parent root;

    public Controller( ) {
        myModel=null;
    }

    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    public void SetModel(ModelInt model){
        this.myModel=model;
    }
    //public StringProperty userName = new SimpleStringProperty();
    //public StringProperty password = new SimpleStringProperty();
    @Override
    public void update(Observable o, Object arg) {
        if(o==myModel){
            if(reset){
                reset=false;
                btn_start.setDisable( false );
                btn_reset.setDisable( true );
                btn_shDic.setDisable( true );
                btn_loadDic.setDisable( true );
                Alert result = new Alert(Alert.AlertType.INFORMATION);
                result.setHeaderText("All files has been deleted");
                result.showAndWait();
            }
            else if(load){
                load=true;
                btn_start.setDisable( false );
                btn_reset.setDisable( false );
                btn_shDic.setDisable( false );
                btn_loadDic.setDisable( false );
            }

        }
    }

    /**
     * create language and cities drop dawn content
     */
    private void createLanguageAndCitiesDropDawn() {
        Label languageLabel = new Label("Languages:");
        Label citiesLabel = new Label("Cities:");
        ComboBox<String> language = new ComboBox<String>();
        citiesMenu = new MenuButton(  );
        language.setEditable(true);
        addLanguages(language);
        addCities();
        VBox form = new VBox(20);
        HBox languageHbox = new HBox(25);
        language.setPrefWidth( 230 );
        citiesMenu.setPrefWidth( 230 );
        languageHbox.getChildren().addAll(languageLabel, language,citiesLabel,citiesMenu);
        form.getChildren().addAll( languageHbox );
        form.setLayoutX( 138 );
        form.setLayoutY( 356 );
        group=new Group( group,form );
        Scene scene = new Scene(group, 1000, 800);
        scene.getStylesheets().add("/View/MyStyle.css");
        primaryStage.setScene( scene );
        primaryStage.show();

    }

    private void addCities() {
        for (String city:this.cities){
            CheckBox cityCB = new CheckBox(city);
            CustomMenuItem item = new CustomMenuItem(cityCB);
            item.setHideOnClick( false );
            citiesMenu.getItems().add( item );
        }
    }

    private void addLanguages(ComboBox<String> language) {
        for (String lang:languages) {
            language.getItems().add(lang);
        }
    }

    public void chooseCorpusFolder(ActionEvent actionEvent){
        try {
            DirectoryChooser dirChooser = new DirectoryChooser();
            dirChooser.setTitle( "Select a folder" );
            File selectedDir = dirChooser.showDialog( primaryStage );
            String dirName = String.valueOf( selectedDir );
            txtfld_corpus.setText( dirName );
        }
        catch (Exception e){
            System.out.println("problem in corpus");
        }
    }
    public void chooseDestinationFolder(ActionEvent actionEvent){
        try {
            DirectoryChooser dirChooser = new DirectoryChooser();
            dirChooser.setTitle( "Select a folder" );
            File selectedDir = dirChooser.showDialog( primaryStage );
            String dirName = String.valueOf( selectedDir );
            txtfld_path.setText( dirName );
        }
        catch (Exception e){
            System.out.println("problem in files destination");
        }

    }
    public void start(ActionEvent actionEvent) {
        try {
            time=System.nanoTime()*Math.pow(10,-9);
            btn_start.setDisable( true );
            btn_reset.setDisable( true );
            btn_shDic.setDisable( true );
            btn_loadDic.setDisable( true );
            if (txtfld_corpus.getText().length() == 0 || txtfld_path.getText().length() == 0) {
                Alert result = new Alert( Alert.AlertType.WARNING );
                result.setHeaderText( "Missing information" );
                result.setContentText( "Please fill corpus path and files path" );
                result.showAndWait();
                btn_start.setDisable( false );
                return;
            }

            if (stemmer.isSelected()) {
                stem = true;
            }
            else
                stem=false;
            start = true;
            HashSet<String>[] cityAndLang=myModel.start( txtfld_corpus.getText(), txtfld_path.getText(), stem );
            if(cityAndLang!=null&&cityAndLang.length>=2) {
                languages =cityAndLang[0];
                cities=cityAndLang[1];
            }
            endIndexer();
        }
        catch (Exception e){
            System.out.println("problem with start button");
        }
    }

    /**
     * create new stage after the indexer has done
     */
    private void endIndexer() {
        if(start){
            start=false;
            postingPath=txtfld_path.getText();
            btn_start.setDisable( false );
            btn_reset.setDisable( false );
            btn_shDic.setDisable( false );
            btn_loadDic.setDisable( false );
            createLanguageAndCitiesDropDawn();
            Alert result = new Alert(Alert.AlertType.INFORMATION);
            showQuerySearch();
            time=System.nanoTime()*Math.pow(10,-9)-time;
            time=time/60;
            result.setHeaderText("Indexing done in " + String.format("%.3f", time) + " minutes!");
            result.setContentText("number of documents: "+ numOfDocs +"\n" + "number of unique terms: " +uniqueTerms);
            result.showAndWait();
        }
    }

    /**
     * create a query text field and a query browse from a file
     */
    private void showQuerySearch() {
        Label querySearch=new Label( "enter a query:" );
        Label queryFile=new Label( "enter a query file:" );
        useSemantic=new CheckBox(  );
        Label semantic=new Label( "use semantic search" );
        queryPlace = new TextField ();
        queryFilePlace = new TextField ();
        queryPlace.setPrefWidth( 350 );
        querySearch.setLayoutX( 500 );
        querySearch.setLayoutY( 185 );
        queryPlace.setLayoutX( 500 );
        queryPlace.setLayoutY( 230 );
        queryFile.setLayoutX( 500 );
        queryFile.setLayoutY( 290 );
        useSemantic.setLayoutX( 940 );
        useSemantic.setLayoutY( 355 );
        semantic.setLayoutX( 800 );
        semantic.setLayoutY( 355 );
        queryFilePlace.setLayoutX( 500 );
        queryFilePlace.setLayoutY( 315 );
        queryFilePlace.setPrefWidth( 230 );
        Button querySearchButton = new Button("Search");
        querySearchButton.setOnAction( e->SearchQuery() );
        querySearchButton.setLayoutX( 880 );
        querySearchButton.setPrefWidth( 100 );
        querySearchButton.setLayoutY( 220 );
        querySearchButton.setPrefHeight( 30 );
        Button browseQueryFile = new Button("Browse");
        browseQueryFile.setOnAction( e->chooseQueryFile() );
        browseQueryFile.setLayoutY( 310 );
        browseQueryFile.setLayoutX( 750 );
        browseQueryFile.setPrefWidth( 100 );
        browseQueryFile.setPrefHeight( 30 );
        Button browseQuerySearch = new Button("Search");
        browseQuerySearch.setLayoutY( 310 );
        browseQuerySearch.setLayoutX( 880 );
        browseQuerySearch.setPrefWidth( 100 );
        browseQuerySearch.setPrefHeight( 30 );
        group=new Group( group,querySearch,queryPlace,queryFile,queryFilePlace,querySearchButton,browseQueryFile,browseQuerySearch,useSemantic,semantic );
        Scene scene = new Scene(group, 1000, 800);
        scene.getStylesheets().add("/View/MyStyle.css");
        primaryStage.setScene( scene );
        System.out.println("check");
        primaryStage.show();
    }
    // open a file chooser for a query file
    private void chooseQueryFile() {
        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle( "Select a query file" );
            File selectedFile = fileChooser.showOpenDialog( primaryStage );
            String dirName = String.valueOf( selectedFile );
            queryFilePlace.setText( dirName );
        }
        catch (Exception e){
            System.out.println("problem in corpus");
        }
    }

    // search a single query
    private void SearchQuery() {
        ArrayList<String> cities= new ArrayList<>(  );
        addChoosedCities(cities);
        if(queryPlace.getText().length()<1){
            Alert queryWarning=new Alert( Alert.AlertType.WARNING );
            queryWarning.setContentText( "please Insert a query" );
            queryWarning.showAndWait();
            return;
        }
        String query= queryPlace.getText();
        ArrayList<DocForSearcher> docs=myModel.searchSingleQuery(query,cities,postingPath,stem,useSemantic.isSelected());
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/View/ViewDocuments.fxml"));
        try {
            root = (Parent) fxmlLoader.load();
            for(DocForSearcher doc:docs)
                showAllDocs(doc);
            Scene scene = new Scene( docsGroup );
            Stage stage = new Stage();
            stage.setScene( scene );
            stage.initModality( Modality.APPLICATION_MODAL);
            stage.initStyle( StageStyle.UNDECORATED);
            stage.setTitle("show documents");
            stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
                public void handle(WindowEvent windowEvent) {
                    windowEvent.consume();
                    stage.close();
                }
            });
            stage.show();
        }
        catch (IOException e) {
        e.printStackTrace();
        }
    }

    /**
     * show all the returned documents in a new fxml window
     * @param docs
     */
    private void showAllDocs(DocForSearcher docs) {
        Label parameters=new Label( "document ID"+docs.getDocID() + ", document rank: " +docs.rank);
        Button entities = new Button("show entities");
        entities.setOnAction( e->showEntities() );
        HBox hb= new HBox(  );
        hb.setSpacing( 10 );
        hb.setMargin( parameters, new Insets(20, 20, 20, 20) );
        hb.setLayoutX( 40 );
        hb.setLayoutY( height );
        hb.setPrefWidth( 200 );
        ObservableList list = hb.getChildren();
        list.addAll(parameters,entities  );
        height+=120;
        if(docsGroup==null){
            docsGroup=new Group(root, hb );

        }
        else
            docsGroup=new Group( group,hb );
        group.getStylesheets().add("/View/MyStyle.css");
    }

    private void showEntities() {
    }

    /**
     * add the choosed cities to an array list
     * @param cities
     */
    private void addChoosedCities(ArrayList<String> cities) {
        for (int i = 0; i < citiesMenu.getItems().size(); i++) {
            if(((CustomMenuItem) citiesMenu.getItems().get( i )).isHideOnClick())
                cities.add(((CustomMenuItem) citiesMenu.getItems().get( i )).getText());
        }
    }

    public void reset(ActionEvent actionEvent) {
        try {
            btn_start.setDisable( true );
            btn_reset.setDisable( true );
            reset = true;
            myModel.reset();
        }
        catch (Exception e) {
            System.out.println("problem with reset button");
        }
    }

    public void exit(ActionEvent actionEvent) {
        showExitMessage();
    }
    private void showExitMessage(){
        Alert exit = myModel.getExitMessage();
        Optional<ButtonType> result = exit.showAndWait();

    }

    public void setResizeEvent(Scene scene) {
        long width = 0;
        long height = 0;
        scene.widthProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number oldSceneWidth, Number newSceneWidth) {
            }
        });
        scene.heightProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number oldSceneHeight, Number newSceneHeight) {
            }
        });
    }
    public void showDictionary(ActionEvent actionEvent){
        String dic=myModel.showDictionary();
        if(dic==null){
            Alert result = new Alert( Alert.AlertType.WARNING );
            result.setHeaderText( "Cannot find dictionary file" );
            result.showAndWait();
        }
        else {
            TextArea textArea = new TextArea( dic );
            textArea.setEditable( false );
            FXMLLoader fxmlLoader = new FXMLLoader();
            fxmlLoader.setLocation( getClass().getResource( "/View/dictionary.fxml" ) );
            Scene scene = null;
            try {
                scene = new Scene( fxmlLoader.load(), 800, 700 );
            } catch (IOException e) {
                e.printStackTrace();
            }
            textArea.setEditable(false);
            Stage stage = new Stage();
            stage.setScene( scene );
            stage.setScene( new Scene( textArea ) );
            stage.show();
        }
    }
    public void loadDictionary(ActionEvent actionEvent){
        btn_start.setDisable( true );
        btn_reset.setDisable( true );
        btn_shDic.setDisable( true );
        btn_loadDic.setDisable( true );
        load=true;
        if(stemmer.isSelected())
           myModel.loadDictionary(true);
        else
            myModel.loadDictionary(false);
    }

    public void closeButtonAction(ActionEvent actionEvent) {
        Stage stage = (Stage) closeButton.getScene().getWindow();
        stage.close();
    }

    public void setModel(Model model) {
        myModel=model;
    }

    public void setGroup(Group group) {
        this.group=group;
    }
}

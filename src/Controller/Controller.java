package Controller;


import ExternalClasses.DocForSearcher;
import Model.Model;
import Model.ModelInt;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Orientation;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.stage.*;
import javafx.collections.ObservableList;

import java.io.*;
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
    ArrayList<DocForSearcher> currentDocsRetrived=null;
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
    private TextField queryResult;
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
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(new File(System.getProperty("user.dir")+"\\citiesFile"+".txt"))));
            String city = br.readLine().trim();
            while(city != null){
                CheckBox cityCB = new CheckBox(city);
                CustomMenuItem item = new CustomMenuItem(cityCB);
                item.setHideOnClick(false);
                citiesMenu.getItems().add(item);
                city = br.readLine();
            }
            br.close();


        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void addLanguages(ComboBox<String> language) {
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(new File(System.getProperty("user.dir") + "\\languagesFile" + ".txt"))));

            String lang = br.readLine().trim();
            while (lang != null) {
                if(!lang.equals("null")&&lang.length()>1)
                    language.getItems().add(lang);
                lang = br.readLine();
            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
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
            e.printStackTrace();
            System.out.println("problem with start button");
        }
    }

    /**
     * create new stage after the indexer has done
     */
    public void endIndexer() {
        postingPath = txtfld_path.getText();
        createLanguageAndCitiesDropDawn();
        Alert result = new Alert(Alert.AlertType.INFORMATION);
        showQuerySearch();
        if (start){
            btn_start.setDisable(false);
            btn_reset.setDisable(false);
            btn_shDic.setDisable(false);
            btn_loadDic.setDisable(false);
            start=false;
            time = System.nanoTime() * Math.pow(10, -9) - time;
            time = time / 60;
            result.setHeaderText("Indexing done in " + String.format("%.3f", time) + " minutes!");
            result.setContentText("number of documents: " + numOfDocs + "\n" + "number of unique terms: " + uniqueTerms);
            result.showAndWait();
    }

    }

    /**
     * create a query text field and a query browse from a file
     */
    private void showQuerySearch() {
        Label querySearch=new Label( "enter a query:" );
        Label queryFile=new Label( "enter a query file:" );
        Label resultFile=new Label( "query result destination:" );
        useSemantic=new CheckBox(  );
        Label semantic=new Label( "use semantic search" );
        queryResult=new TextField();
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
        resultFile.setLayoutX(520);
        resultFile.setLayoutY(390);
        queryResult.setLayoutX(520);
        queryResult.setLayoutY(410);
        queryResult.setPrefWidth(210);
        Button browseResult = new Button("Browse");
        browseResult.setOnAction(e->chooseResultDestinationFolder());
        browseResult.setLayoutX( 750 );
        browseResult.setPrefWidth( 100 );
        browseResult.setLayoutY( 400 );
        browseResult.setPrefHeight( 30 );
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
        browseQuerySearch.setOnAction(e-> searchQueryFile());
        browseQuerySearch.setLayoutY( 310 );
        browseQuerySearch.setLayoutX( 880 );
        browseQuerySearch.setPrefWidth( 100 );
        browseQuerySearch.setPrefHeight( 30 );
        group=new Group( group,querySearch,queryPlace,queryFile,queryFilePlace,querySearchButton,browseQueryFile,browseQuerySearch,useSemantic,semantic,resultFile,browseResult,queryResult );
        Scene scene = new Scene(group, 1000, 800);
        scene.getStylesheets().add("/View/MyStyle.css");
        primaryStage.setScene( scene );
        primaryStage.show();
    }

    /**
     * search a couple of queries from a file
     */
    private void searchQueryFile() {
        if(!checkBeforeSearch())
            return;
        if(queryFilePlace.getText()==null||queryFilePlace.getText().length()<1){
            Alert queryWarning=new Alert( Alert.AlertType.WARNING );
            queryWarning.setContentText( "please Insert a query" );
            queryWarning.showAndWait();
            return;
        }
        postingPath=txtfld_path.getText();
        ArrayList<String> cities= new ArrayList<>(  );
        addChoosedCities(cities);
        myModel.searchFileQuery(queryFilePlace.getText(),cities,postingPath,queryResult.getText(),stemmer.isSelected(),useSemantic.isSelected());
    }

    private void chooseResultDestinationFolder() {
        try {
            DirectoryChooser dirChooser = new DirectoryChooser();
            dirChooser.setTitle( "Select a folder" );
            File selectedDir = dirChooser.showDialog( primaryStage );
            String dirName = String.valueOf( selectedDir );
            queryResult.setText( dirName );
        }
        catch (Exception e){
            System.out.println("problem in files destination");
        }
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
    private boolean checkBeforeSearch(){
        if(txtfld_corpus.getText()==null||txtfld_corpus.getText().length()<1){
            Alert alert= new Alert(Alert.AlertType.WARNING);
            alert.setContentText("you must choose corpus path!");
            alert.showAndWait();
            return false;
        }
        if(txtfld_path.getText()==null||txtfld_path.getText().length()<1){
            Alert alert= new Alert(Alert.AlertType.WARNING);
            alert.setContentText("you must choose posting path!");
            alert.showAndWait();
            return false;
        }
        if(queryResult.getText()==null||queryResult.getText().length()<1){
            Alert alert= new Alert(Alert.AlertType.WARNING);
            alert.setContentText("you must choose query result destination");
            alert.showAndWait();
            return false;
        }
        return true;
    }
    // search a single query
    private void SearchQuery() {
        if(!checkBeforeSearch())
            return;
        if(queryPlace.getText().length()<1){
            Alert queryWarning=new Alert( Alert.AlertType.WARNING );
            queryWarning.setContentText( "please Insert a query" );
            queryWarning.showAndWait();
            return;
        }
        postingPath=txtfld_path.getText();
        ArrayList<String> cities= new ArrayList<>(  );
        addChoosedCities(cities);
        String query= queryPlace.getText();
        ArrayList<DocForSearcher> docs=myModel.searchSingleQuery(query,cities,postingPath,queryResult.getText(),stemmer.isSelected(),useSemantic.isSelected());
        try {
            /*
            String path="/View/ViewDocuments.fxml";
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(path));
            root =(Parent) fxmlLoader.load();
            */
            if(docs!=null) {
                for (DocForSearcher doc : docs) {
                    showAllDocs(doc);
                }
            }
            Button close=new Button("Exit");
            close.setOnAction(e->closeButtonAction(close));
            if(docsGroup!=null)
                docsGroup=new Group(docsGroup,close);
            else
                docsGroup=new Group(close);
            currentDocsRetrived=docs;
            ScrollPane sp = new ScrollPane();
            sp.setContent(docsGroup);
            sp.setPannable(true);
            /*
            ScrollBar sc = new ScrollBar();
            sc.setMin(0);
            sc.setLayoutY( 1000 );
            sc.setLayoutX( 960 );
            sc.setOrientation(Orientation.VERTICAL);
            sc.setPrefHeight(750);
            sc.setMax(750 );
            sc.valueProperty().addListener(new ChangeListener<Number>() {
                public void changed(ObservableValue<? extends Number> ov,
                                    Number old_val, Number new_val) {
                    docsGroup.setLayoutY(-new_val.doubleValue());
                }
            });
            */
            Scene scene = new Scene( sp, 700,800 );
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
        catch (Exception e) {
        e.printStackTrace();
        }
    }

    /**
     * show all the returned documents in a new fxml window
     * @param docs
     */
    private void showAllDocs(DocForSearcher docs) {
        Double docRank=docs.rank;
        String rank=String.format("%.3f", docRank);
        Label parameters=new Label( "document ID"+docs.getDocID() + ", document rank: " +rank);
        Button entities = new Button("show entities");
        entities.setOnAction( e->showEntities(docs) );
        HBox hb= new HBox(  );
        hb.setSpacing( 10 );
        hb.setMargin( parameters, new Insets(20, 20, 20, 20) );
        hb.setMargin(entities, new Insets(0, 0, 0, 0));
        hb.setLayoutX( 40 );
        hb.setLayoutY( height );
        hb.setPrefWidth( 600 );
        ObservableList list = hb.getChildren();
        list.addAll(parameters,entities  );
        height+=60;
        if(docsGroup==null){
            docsGroup=new Group( hb );
        }
        else
            docsGroup=new Group( docsGroup,hb );
        docsGroup.getStylesheets().add("/View/MyStyle.css");
    }

    /**
     * show a document entities
     * @param docs
     */
    private void showEntities(DocForSearcher docs) {
        String entities="";
        if(docs.entities.size()==1){
            String[] entitiesString=docs.entities.get(0).split(",");
            for (int i = 0; i <entitiesString.length ; i++) {
                entities += i+1 + ". " + entitiesString[i].split("@")[0] + "\n";
            }
        }
        else {
            for (int i = 0; i < docs.entities.size(); i++) {
                int place = i + 1;
                System.out.println(docs.entities.get(i));
                entities += place + ". " + docs.entities.get(i) + "\n";
            }
        }
        Alert result= new Alert(Alert.AlertType.INFORMATION);
        if(!entities.equals("")){
            result.setContentText(entities);
        }
        else{
            result.setContentText("doc doesnt have entities");
        }
        result.setHeaderText("Entities");
        result.showAndWait();
    }

    /**
     * add the choosed cities to an array list
     * @param cities
     */
    private void addChoosedCities(ArrayList<String> cities) {
        ObservableList<MenuItem> list=citiesMenu.getItems();
        for(MenuItem m:list){
            if(((CheckBox)((CustomMenuItem)m).getContent()).isSelected()) {
                cities.add(((CheckBox)((CustomMenuItem)m).getContent()).getText());
            }
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
            e.printStackTrace();
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

    public void closeButtonAction(Button close) {
        Stage stage = (Stage) close.getScene().getWindow();
        stage.close();
        height=50;
        docsGroup=null;
    }

    public void setModel(Model model) {
        myModel=model;
    }

    public void setGroup(Group group) {
        this.group=group;
    }
}

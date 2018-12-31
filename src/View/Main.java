package View;
import Controller.Controller;
import Model.Model;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.application.Application;
import javafx.scene.layout.VBox;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.io.File;
import java.io.FileInputStream;

public class Main extends Application {
    public static Stage primaryStage;
    Model model;
    Controller view;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        //createDB();
        //Creating an image
        String current = new java.io.File( "." ).getCanonicalPath();
        Image image = new Image(new FileInputStream(current+"/Resources/HideAndSeek.jpg"));
        //Setting the image view
        ImageView imageView = new ImageView(image);
        //Setting the position of the image
        imageView.setX(50);
        imageView.setY(50);
        //setting the fit height and width of the image view
        imageView.setFitHeight(1500);
        imageView.setFitWidth(900);
        //Setting the preserve ratio of the image view
        imageView.setPreserveRatio(true);
        this.primaryStage = primaryStage;
        BorderPane root1 = new BorderPane();
        model = new Model();
        FXMLLoader fxmlLoader = new FXMLLoader();
        Parent root = fxmlLoader.load(getClass().getResource("View.fxml").openStream());
        Group group = new Group(imageView,root);
        Scene scene = new Scene(group, 1000, 800);
        scene.getStylesheets().add("/View/MyStyle.css");
        primaryStage.setScene(scene);
        primaryStage.setTitle("HideAndSeek");
        primaryStage.setScene(scene);
        view = fxmlLoader.getController();
        view.setPrimaryStage( primaryStage );
        view.setResizeEvent(scene);
        view.setModel(model);
        view.setGroup(group);
        model.addObserver(view);
        model.setController(view);
        SetStageCloseEvent(primaryStage);
        view.endIndexer();
    }
    private void configuringDirectoryChooser(DirectoryChooser directoryChooser) {
        // Set title for DirectoryChooser
        directoryChooser.setTitle("Select corpus directory");

        // Set Initial Directory
        directoryChooser.setInitialDirectory(new File(System.getProperty("user.home")));
    }

    private void SetStageCloseEvent(Stage primaryStage) {
        primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            public void handle(WindowEvent windowEvent) {
                windowEvent.consume();
                primaryStage.close();
            }
        });
    }
}

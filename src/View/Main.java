package View;
import Controller.Controller;
import Model.Model;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

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
        this.primaryStage = primaryStage;
        BorderPane root1 = new BorderPane();
        model = new Model();
        FXMLLoader fxmlLoader = new FXMLLoader();
        Parent root = fxmlLoader.load(getClass().getResource("View.fxml").openStream());
        Scene scene = new Scene(root, 800, 700);
        scene.getStylesheets().add("/View/MyStyle.css");
        primaryStage.setScene(scene);
        primaryStage.setTitle("Vacation4U");
        primaryStage.setScene(scene);
        view = fxmlLoader.getController();
        view.setResizeEvent(scene);
        model.addObserver(view);
        SetStageCloseEvent(primaryStage);
        primaryStage.show();
    }

    /*private void createDB() throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        DataBase DB = new DataBase("DataBase.sqlite");
        DB.connect();
    }*/

    private void SetStageCloseEvent(Stage primaryStage) {
        primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            public void handle(WindowEvent windowEvent) {
                windowEvent.consume();
                primaryStage.close();
            }
        });
    }
}

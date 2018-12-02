package Controller;

import Model.*;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;
import java.util.Observable;
import java.util.Observer;
import java.util.Optional;

public class Controller implements Observer {
    ModelInt myModel;
    public javafx.scene.control.Button closeButton;
    public javafx.scene.control.Button btn_updtrcd;
    public javafx.scene.control.Button btn_create;
    public javafx.scene.control.Button btn_read;
    public javafx.scene.control.Button btn_delete;
    public javafx.scene.control.TextField txtfld_pkey;
    public Controller( ) {
        myModel=null;
    }
    public void SetModel(ModelInt model){
        this.myModel=model;
    }
    //public StringProperty userName = new SimpleStringProperty();
    //public StringProperty password = new SimpleStringProperty();
    @Override
    public void update(Observable o, Object arg) {
    }

    public void updateRecord(ActionEvent actionEvent) {
        btn_updtrcd.setDisable( true );
        try{
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/View/updateRecord.fxml"));
            Parent root1 = (Parent) fxmlLoader.load();
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initStyle(StageStyle.UNDECORATED);
            stage.setTitle("update");
            stage.setScene(new Scene(root1));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
        btn_updtrcd.setDisable( false );
    }

    public void delete(ActionEvent actionEvent) {
    }

    public void create(ActionEvent actionEvent) {
        btn_create.setDisable( true );
        try{
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/View/CreateUser.fxml"));
            Parent root1 = (Parent) fxmlLoader.load();
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initStyle(StageStyle.UNDECORATED);
            stage.setTitle("Create new user");
            stage.setScene(new Scene(root1));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
        btn_create.setDisable( false );
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
                System.out.println("Width: " + newSceneWidth);
            }
        });
        scene.heightProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number oldSceneHeight, Number newSceneHeight) {
                System.out.println("Height: " + newSceneHeight);
            }
        });
    }

    public void read(ActionEvent actionEvent) {
        btn_read.setDisable( true );
        try{
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/View/ReadUser.fxml"));
            Parent root1 = (Parent) fxmlLoader.load();
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initStyle(StageStyle.UNDECORATED);
            stage.setTitle("Search for user");
            stage.setScene(new Scene(root1));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
        btn_read.setDisable( false );
    }

    public void closeButtonAction(ActionEvent actionEvent) {
        Stage stage = (Stage) closeButton.getScene().getWindow();
        stage.close();
    }

    public void deleteRecord(ActionEvent actionEvent) {
        btn_delete.setDisable( true );
        try{
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/View/DeleteUser.fxml"));
            Parent root1 = (Parent) fxmlLoader.load();
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initStyle(StageStyle.UNDECORATED);
            stage.setTitle("Delete user");
            stage.setScene(new Scene(root1));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
        btn_delete.setDisable( false );
    }
}

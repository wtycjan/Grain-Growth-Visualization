package sample;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class Main extends Application {

    static Controller myController;

    @Override
    public void start(Stage primaryStage) throws Exception{
        FXMLLoader loader = new FXMLLoader(getClass().getResource("sample.fxml"));
        Parent root= loader.load();
        primaryStage.setTitle("CA - rozrost ziaren");
        primaryStage.setScene(new Scene(root, 880, 550));
        myController = loader.getController();
        myController.Initialize();
        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }
}

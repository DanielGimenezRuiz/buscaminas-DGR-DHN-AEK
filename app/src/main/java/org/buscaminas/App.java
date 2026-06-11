package org.buscaminas;

import javafx.application.Application;
import javafx.stage.Stage;
import org.buscaminas.MenuScreen;

public class App extends Application {

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Buscaminas");
        primaryStage.setResizable(false);
        MenuScreen menuScreen = new MenuScreen(primaryStage);
        menuScreen.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

}

package console;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class ASM extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
 
        VBox root = new VBox();
        Scene scene = new Scene(root, 600, 400);
        new Console(root, primaryStage);
        primaryStage.setScene(scene);
        primaryStage.setTitle("ASM Console");
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}

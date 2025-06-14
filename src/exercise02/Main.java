package exercise02;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application{

	@Override
	public void start(Stage primaryStage) throws Exception {
		Parent parent = (Parent)FXMLLoader.load(getClass().getResource("MainView.fxml"));
		primaryStage.setTitle("이미지 분석기");
		Scene scene = new Scene(parent);
		
		scene.getStylesheets().add(getClass().getResource("style.css").toExternalForm());
		
		primaryStage.setScene(scene);
		primaryStage.show();
	}
	
	public static void main(String[] args) {
		Application.launch(args);
	}
}

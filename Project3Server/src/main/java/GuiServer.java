import java.util.HashMap;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;


public class GuiServer extends Application{
	Server serverConnection;

	HashMap<String, Scene> sceneMap;
	ListView<String> activityLog;
	
	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		serverConnection = new Server(data -> {
			Platform.runLater(()->{
				if (data.type == Message.Types.SERVER_MSG) {
					activityLog.getItems().add(data.msg);
				} else {
					activityLog.getItems().add("Invalid Message Type!");
				}
			});
		});

		activityLog = new ListView<String>();

		sceneMap = new HashMap<String, Scene>();
		sceneMap.put("server",  createServerGui());
		
		primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent t) {
                Platform.exit();
                System.exit(0);
            }
        });

		primaryStage.setScene(sceneMap.get("server"));
		primaryStage.setTitle("Server");
		primaryStage.show();
	}
	
	public Scene createServerGui() {
		BorderPane pane = new BorderPane();
		pane.setPadding(new Insets(70));
		pane.setStyle("-fx-background-color: coral");
		pane.setCenter(activityLog);
		pane.setStyle("-fx-font-family: 'serif'");
		return new Scene(pane, 400, 400);
	}
}

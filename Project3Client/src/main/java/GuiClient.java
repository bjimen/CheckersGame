import java.util.HashMap;
import java.util.Objects;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class GuiClient extends Application{
	Client clientConnection;
	String ID = "";
	String opponent = "";

	HashMap<String, Scene> sceneMap;
	BorderPane bpMainMenu, bpQueue, bpGame;
	VBox vbUsername, vbMainMenu;
	Text txtUsername, txtQueue, txtGame;
	TextField tfUsername;
	Button btUsername, btPlay, btQuit;
	Image imLogo;
	ImageView ivLogo;
	
	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		clientConnection = new Client(data->{
				Platform.runLater(()->{
					switch(data.type) {
						case ACCEPT_ID:
							ID = data.msg;
							primaryStage.setScene(sceneMap.get("main menu"));
							break;
						case REJECT_ID:
							txtUsername.setText("Username taken!\nPlease enter a different username:");
							break;
						case QUEUE:
							opponent = data.sender;
							txtQueue.setText("Opponent found!");
							primaryStage.setScene(sceneMap.get("game"));
							break;
					}
			});
		});
		clientConnection.start();
		
		primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent t) {
                Platform.exit();
                System.exit(0);
            }
        });

		sceneMap = new HashMap<String, Scene>();

		txtUsername = new Text("Welcome!\nPlease enter a username:");
		txtUsername.setTextAlignment(TextAlignment.CENTER);
		tfUsername = new TextField();
		tfUsername.setMaxWidth(150);
		btUsername = new Button("Enter");
		btUsername.setOnAction(e->{
			if (Objects.equals(tfUsername.getText(), "")) {
				txtUsername.setText("Username can't be empty!\nPlease enter a username:");
			} else {
				clientConnection.send(new Message(tfUsername.getText(), "", "", Message.Types.SEND_ID));
			}
			tfUsername.clear();
		});
		sceneMap.put("username", createUsernameGUI());

		imLogo = new Image("logo.png", 505, 114, true, false);
		ivLogo = new ImageView(imLogo);
		btPlay = new Button("Play");
		btPlay.setOnAction(e->{primaryStage.setScene(sceneMap.get("queue")); clientConnection.send(new Message("", "", "", Message.Types.QUEUE));});
		btQuit = new Button("Quit");
		btQuit.setOnAction(e->{Platform.exit(); System.exit(0);});
		sceneMap.put("main menu", createMainMenuGUI());

		txtQueue = new Text("Looking for players...");
		sceneMap.put("queue", createQueueGUI());

		txtGame = new Text();
		sceneMap.put("game", createGameGUI());

		primaryStage.setScene(sceneMap.get("username"));
		primaryStage.show();
	}

	public Scene createUsernameGUI() {
		vbUsername = new VBox(10, txtUsername, tfUsername, btUsername);
		vbUsername.setAlignment(Pos.CENTER);
		vbUsername.setStyle("-fx-font-family: 'serif';");
		return new Scene(vbUsername, 800, 450);
	}

	public Scene createMainMenuGUI() {
		vbMainMenu = new VBox(10, ivLogo, btPlay, btQuit);
		vbMainMenu.setAlignment(Pos.CENTER);
		bpMainMenu = new BorderPane();
		bpMainMenu.setPadding(new Insets(100));
		bpMainMenu.setCenter(vbMainMenu);
		return new Scene(bpMainMenu, 800, 450);
	}

	public Scene createQueueGUI() {
		bpQueue = new BorderPane();
		bpQueue.setPadding(new Insets(100));
		bpQueue.setCenter(txtQueue);
		return new Scene(bpQueue, 800, 450);
	}

	public Scene createGameGUI() {
		txtGame.setText(opponent);
		bpGame = new BorderPane();
		bpGame.setPadding(new Insets(100));
		bpGame.setCenter(txtGame);
		return new Scene(bpGame, 800, 450);
	}
}

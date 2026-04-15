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
	VBox vbUsername, vbMainMenu, vbBoard, vbChat;
	HBox hbGame;
	Text txtUsername, txtQueue, txtOpp;
	TextField tfUsername, tfChat;
	Button btUsername, btPlay, btQuit, btSend;
	Image imLogo, imBoard;
	ImageView ivLogo, ivBoard;
	ListView<String> lvChat;
	
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
							txtOpp.setText(opponent);
							primaryStage.setScene(sceneMap.get("game"));
							break;
						case SEND_MSG:
							lvChat.getItems().add(opponent+" said: "+data.msg);
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

		txtOpp = new Text();
		imBoard = new Image("board.png", 400, 400, true, false);
		ivBoard = new ImageView(imBoard);
		lvChat = new ListView<>();
		lvChat.setMaxHeight(350);
		lvChat.setMaxWidth(200);
		tfChat = new TextField();
		tfChat.setMaxWidth(200);
		btSend = new Button("Send");
		btSend.setMaxWidth(200);
		btSend.setOnAction(e->{
			String text = tfChat.getText();
			if (!Objects.equals(text, "")) {
				lvChat.getItems().add(ID+" said: "+text);
				clientConnection.send(new Message(text, ID, opponent, Message.Types.SEND_MSG));
				tfChat.clear();
			}
		});
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
		vbMainMenu = new VBox(10, btPlay, btQuit);
		vbMainMenu.setAlignment(Pos.CENTER);
		bpMainMenu = new BorderPane();
		bpMainMenu.setPadding(new Insets(25));
		bpMainMenu.setCenter(vbMainMenu);
		bpMainMenu.setTop(ivLogo);
		bpMainMenu.setAlignment(ivLogo, Pos.TOP_CENTER);
		return new Scene(bpMainMenu, 800, 450);
	}

	public Scene createQueueGUI() {
		bpQueue = new BorderPane();
		bpQueue.setPadding(new Insets(100));
		bpQueue.setCenter(txtQueue);
		return new Scene(bpQueue, 800, 450);
	}

	public Scene createGameGUI() {
		vbBoard = new VBox(txtOpp, ivBoard);
		vbBoard.setAlignment(Pos.CENTER);
		vbChat = new VBox(lvChat, tfChat, btSend);
		hbGame = new HBox(150, vbBoard, vbChat);
		bpGame = new BorderPane();
		bpGame.setPadding(new Insets(25));
		bpGame.setCenter(hbGame);
		return new Scene(bpGame, 800, 450);
	}
}

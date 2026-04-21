import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class GuiClient extends Application{
	Client clientConnection;
	String ID = "";
	String opponent = "";

	HashMap<String, Scene> sceneMap;
	BorderPane bpMainMenu, bpQueue, bpGame, bpEnd;
	VBox vbUsername, vbMainMenu, vbBoard, vbChat;
	HBox hbGame;
	Text txtUsername, txtQueue, txtOpp, txtEnd;
	TextField tfUsername, tfChat;
	Button btUsername, btPlay, btQuit, btSend;
	Image imLogo, imMenuBG, imBlackSqr, imWhiteSqr, imBlackPcReg, imRedPcReg, imHighlightPc;
	ImageView ivLogo;
	ListView<String> lvChat;

	Game newgame;
	
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
						case BLACK:
							opponent = data.sender;
							txtQueue.setText("Opponent found!");
							txtOpp.setText(opponent);
							newgame = new Game(true);
							sceneMap.put("game", createGameGUI());
							primaryStage.setScene(sceneMap.get("game"));
							break;
						case RED:
							opponent = data.sender;
							txtQueue.setText("Opponent found!");
							txtOpp.setText(opponent);
							newgame = new Game(false);
							sceneMap.put("game", createGameGUI());
							primaryStage.setScene(sceneMap.get("game"));
							break;
						case SEND_MSG:
							lvChat.getItems().add(opponent+" said: "+data.msg);
							break;
						case MOVE:
							newgame.opponentMove(data.msg);
							break;
						case WIN:
							txtEnd.setText("You've won!");
							break;
						case LOSE:
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
		txtUsername.setStyle("-fx-fill: white");
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
		imMenuBG = new Image("bg-menu.jpg");
		sceneMap.put("username", createUsernameGUI());

		imLogo = new Image("logo.png", 515, 123, true, false);
		ivLogo = new ImageView(imLogo);
		btPlay = new Button("Play");
		btPlay.setOnAction(e->{primaryStage.setScene(sceneMap.get("queue")); clientConnection.send(new Message("", "", "", Message.Types.QUEUE));});
		btQuit = new Button("Quit");
		btQuit.setOnAction(e->{Platform.exit(); System.exit(0);});
		sceneMap.put("main menu", createMainMenuGUI());

		txtQueue = new Text("Looking for players...");
		txtQueue.setStyle("-fx-fill: white; -fx-font-size: 15");
		sceneMap.put("queue", createQueueGUI());

		imBlackSqr = new Image("black-square.png");
		imWhiteSqr = new Image("white-square.png");
		imBlackPcReg = new Image("black-piece-reg.png");
		imRedPcReg = new Image("red-piece-reg.png");
		imHighlightPc = new Image("highlight-pc.png");

		txtOpp = new Text();
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

		txtEnd = new Text();
		txtEnd.setStyle("-fx-fill: white; -fx-font-size: 15");
		sceneMap.put("end", createEndGUI());

		primaryStage.setScene(sceneMap.get("username"));
		primaryStage.show();
	}

	private class Game {
		GridPane gpBoard;
		ImageView[][] board;
		Piece[] myPieces;
		Piece[] theirPieces;
		Piece selected;
		boolean isBlack;
		boolean myTurn;

		Game(boolean black) {
			gpBoard = new GridPane();
			board = new ImageView[8][8];
			myPieces = new Piece[12];
			theirPieces = new Piece[12];
			selected = null;
            isBlack = black;
			myTurn = isBlack;
			initBoard();
		}

		private class Piece {
			ImageView ivPiece;
			ImageView highlight;
			boolean isKing;

			Piece(Image img) {
				ivPiece = new ImageView(img);
				highlight = null;
				isKing = false;
			}
		}

		void initBoard() {
			int theirPcIndex = 0;
			int myPcIndex = 0;

			// Black Player
			if (isBlack) {
				for (int row = 0; row < 8; row++) {
					for (int col = 0; col < 8; col++) {
						if (((row + col) % 2) == 0) {
							board[row][col] = new ImageView(imWhiteSqr);
							gpBoard.add(board[row][col], col, row);
						} else {
							board[row][col] = new ImageView(imBlackSqr);
							gpBoard.add(board[row][col], col, row);
							if (row < 3) {
								theirPieces[theirPcIndex] = new Piece(imRedPcReg);
								theirPieces[theirPcIndex].ivPiece.setPickOnBounds(true);
								gpBoard.add(theirPieces[theirPcIndex].ivPiece, col, row);
								theirPcIndex++;
							}
							if (row > 4) {
								myPieces[myPcIndex] = new Piece(imBlackPcReg);
								myPieces[myPcIndex].ivPiece.setPickOnBounds(true);
								gpBoard.add(myPieces[myPcIndex].ivPiece, col, row);
								myPcIndex++;
							}
						}
					}
				}
			// Red Player
			} else {
				for (int row = 0; row < 8; row++) {
					for (int col = 0; col < 8; col++) {
						if (((row + col) % 2) == 0) {
							board[row][col] = new ImageView(imWhiteSqr);
							gpBoard.add(board[row][col], col, row);
						} else {
							board[row][col] = new ImageView(imBlackSqr);
							gpBoard.add(board[row][col], col, row);
							if (row < 3) {
								theirPieces[theirPcIndex] = new Piece(imBlackPcReg);
								theirPieces[theirPcIndex].ivPiece.setPickOnBounds(true);
								gpBoard.add(theirPieces[theirPcIndex].ivPiece, col, row);
								theirPcIndex++;
							}
							if (row > 4) {
								myPieces[myPcIndex] = new Piece(imRedPcReg);
								myPieces[myPcIndex].ivPiece.setPickOnBounds(true);
								gpBoard.add(myPieces[myPcIndex].ivPiece, col, row);
								myPcIndex++;
							}
						}
					}
				}
			}
			initPieces();
		}

		void initPieces() {
			for (int row = 0; row < 8; row++) {
				for (int col = 0; col < 8; col++) {
					int r = row;
					int c = col;
					board[row][col].setOnMouseClicked(e->{
						if (selected != null) {
							int oldR = GridPane.getRowIndex(selected.ivPiece);
							int oldC = GridPane.getColumnIndex(selected.ivPiece);

							if (makeMove(oldR, oldC, r, c)) {
								gpBoard.getChildren().remove(selected.highlight);
								selected.highlight = null;
								gpBoard.getChildren().remove(selected.ivPiece);
								if (r == 0 && !selected.isKing) {
									if (isBlack) {
										selected.ivPiece.setImage(new Image("black-piece-king.png"));
									} else {
										selected.ivPiece.setImage(new Image("red-piece-king.png"));
									}
									selected.isKing = true;
								}
								gpBoard.add(selected.ivPiece, c, r);
								clientConnection.send(new Message(String.valueOf(oldR)+String.valueOf(oldC)+String.valueOf(r)+String.valueOf(c), ID, opponent, Message.Types.MOVE));
								selected = null;
								myTurn = false;

								if (checkGameEnd()) {
									clientConnection.send(new Message("", ID, opponent, Message.Types.WIN));
									txtEnd.setText("You've lost!");
								}
							}
						}
					});
				}
			}

			for (int i = 0; i < 12; i++) {
				int j = i;
				myPieces[i].ivPiece.setOnMouseClicked(e->{
					if (myTurn && selected == null) {
						myPieces[j].highlight = new ImageView(imHighlightPc);
						myPieces[j].highlight.setPickOnBounds(true);
						myPieces[j].highlight.setOnMouseClicked(f->{gpBoard.getChildren().remove(myPieces[j].highlight); selected = null;});
						gpBoard.add(myPieces[j].highlight, GridPane.getColumnIndex(myPieces[j].ivPiece), GridPane.getRowIndex(myPieces[j].ivPiece));
						selected = myPieces[j];
					}
				});
			}
		}

		boolean makeMove(int fromR, int fromC, int toR, int toC) {
			if ((fromR - toR) == 1 && Math.abs(fromC - toC) == 1) {
				return true;
			}
			if ((fromR - toR) == -1 && Math.abs(fromC - toC) == 1 && getMyPiece(fromR, fromC).isKing) {
				return true;
			}
			// Move with left jump
			if ((fromR - toR) == 2 && (fromC - toC) == 2 && getTheirPiece(fromR - 1, fromC - 1) != null) {
				gpBoard.getChildren().remove(getTheirPiece(fromR - 1, fromC - 1).ivPiece);
				return true;
			}
			// Move with right jump
			if ((fromR - toR) == 2 && (fromC - toC) == -2 && getTheirPiece(fromR - 1, fromC + 1) != null) {
				gpBoard.getChildren().remove(getTheirPiece(fromR - 1, fromC + 1).ivPiece);
				return true;
            }
			// King move with left jump
			if ((fromR - toR) == -2 && (fromC - toC) == 2 && getTheirPiece(fromR + 1, fromC - 1) != null && getMyPiece(fromR, fromC).isKing) {
				gpBoard.getChildren().remove(getTheirPiece(fromR + 1, fromC - 1).ivPiece);
				return true;
			}
			// King move with right jump
			if ((fromR - toR) == -2 && (fromC - toC) == -2 && getTheirPiece(fromR + 1, fromC + 1) != null && getMyPiece(fromR, fromC).isKing) {
				gpBoard.getChildren().remove(getTheirPiece(fromR + 1, fromC + 1).ivPiece);
				return true;
			}
			return false;
		}

		boolean checkGameEnd() {
			for (Node child : gpBoard.getChildren()) {
				for (Piece pc : myPieces) {
					if (child == pc.ivPiece) {
						return false;
					}
				}
			}
			return true;
		}

		void opponentMove(String move) {
			int fromR = 7 - Integer.parseInt(String.valueOf(move.charAt(0)));
			int fromC = 7 - Integer.parseInt(String.valueOf(move.charAt(1)));
			int toR = 7 - Integer.parseInt(String.valueOf(move.charAt(2)));
			int toC = 7 - Integer.parseInt(String.valueOf(move.charAt(3)));

			ImageView n = getTheirPiece(fromR, fromC).ivPiece;
			gpBoard.getChildren().remove(n);
			if (toR == 7) {
				if (isBlack) {
					n.setImage(new Image("red-piece-king.png"));
				} else {
					n.setImage(new Image("black-piece-king.png"));
				}
			}
			gpBoard.add(n, toC, toR);

			// Move with left jump
			if ((toR - fromR) == 2 && (toC - fromC) == 2) {
				gpBoard.getChildren().remove(getMyPiece(fromR + 1, fromC + 1).ivPiece);
			}
			// Move with right jump
			if ((toR - fromR) == 2 && (toC - fromC) == -2) {
				gpBoard.getChildren().remove(getMyPiece(fromR + 1, fromC - 1).ivPiece);
			}
			// King move with left jump
			if ((toR - fromR) == -2 && (toC - fromC) == 2) {
				gpBoard.getChildren().remove(getMyPiece(fromR - 1, fromC + 1).ivPiece);
			}
			// King move with right jump
			if ((toR - fromR) == -2 && (toC - fromC) == -2) {
				gpBoard.getChildren().remove(getMyPiece(fromR - 1, fromC - 1).ivPiece);
			}

			myTurn = true;
		}

		Piece getMyPiece(int row, int col) {
			for (Piece pc : myPieces) {
				if (GridPane.getRowIndex(pc.ivPiece) == row && GridPane.getColumnIndex(pc.ivPiece) == col) {
					return pc;
				}
			}
			return null;
		}

		Piece getTheirPiece(int row, int col) {
			for (Piece pc : theirPieces) {
				if (GridPane.getRowIndex(pc.ivPiece) == row && GridPane.getColumnIndex(pc.ivPiece) == col) {
					return pc;
				}
			}
			return null;
		}
	}

	private Scene createUsernameGUI() {
		vbUsername = new VBox(10, txtUsername, tfUsername, btUsername);
		vbUsername.setAlignment(Pos.CENTER);
		vbUsername.setStyle("-fx-font-size: 13");
		vbUsername.setBackground(new Background(new BackgroundImage(imMenuBG, BackgroundRepeat.REPEAT, BackgroundRepeat.REPEAT, BackgroundPosition.CENTER, BackgroundSize.DEFAULT)));
		return new Scene(vbUsername, 800, 450);
	}

	private Scene createMainMenuGUI() {
		vbMainMenu = new VBox(10, btPlay, btQuit);
		vbMainMenu.setAlignment(Pos.CENTER);
		vbMainMenu.setStyle("-fx-font-size: 13");
		bpMainMenu = new BorderPane();
		bpMainMenu.setPadding(new Insets(25));
		bpMainMenu.setCenter(vbMainMenu);
		bpMainMenu.setTop(ivLogo);
		BorderPane.setAlignment(ivLogo, Pos.TOP_CENTER);
		bpMainMenu.setBackground(new Background(new BackgroundImage(imMenuBG, BackgroundRepeat.REPEAT, BackgroundRepeat.REPEAT, BackgroundPosition.CENTER, BackgroundSize.DEFAULT)));
		return new Scene(bpMainMenu, 800, 450);
	}

	private Scene createQueueGUI() {
		bpQueue = new BorderPane();
		bpQueue.setCenter(txtQueue);
		bpQueue.setBackground(new Background(new BackgroundImage(imMenuBG, BackgroundRepeat.REPEAT, BackgroundRepeat.REPEAT, BackgroundPosition.CENTER, BackgroundSize.DEFAULT)));
		return new Scene(bpQueue, 800, 450);
	}

	private Scene createGameGUI() {
		vbBoard = new VBox(txtOpp, newgame.gpBoard);
		vbBoard.setAlignment(Pos.CENTER);
		vbChat = new VBox(lvChat, tfChat, btSend);
		hbGame = new HBox(150, vbBoard, vbChat);
		bpGame = new BorderPane();
		bpGame.setPadding(new Insets(25));
		bpGame.setCenter(hbGame);
		return new Scene(bpGame, 800, 450);
	}

	private Scene createEndGUI() {
		bpEnd = new BorderPane();
		bpEnd.setCenter(txtEnd);
		bpEnd.setBackground(new Background(new BackgroundImage(imMenuBG, BackgroundRepeat.REPEAT, BackgroundRepeat.REPEAT, BackgroundPosition.CENTER, BackgroundSize.DEFAULT)));
		return new Scene(bpEnd, 800, 450);
	}
}

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;
import java.util.function.Consumer;


public class Server{
	int count = 1;
	ArrayList<ClientThread> clients = new ArrayList<ClientThread>();
	HashMap<String, ClientThread> users = new HashMap<>();
	TheServer server;
	private Consumer<Message> callback;

	Server(Consumer<Message> call){
		callback = call;
		server = new TheServer();
		server.start();
	}


	public class TheServer extends Thread{
		public void run() {
			try(ServerSocket mysocket = new ServerSocket(5555)){
		    System.out.println("Server is waiting for a client!");

		    while(true) {
				ClientThread c = new ClientThread(mysocket.accept(), count);
				callback.accept(new Message("Client has connected to server: client "+count, "", "", Message.Types.SERVER_MSG));
				clients.add(c);
				c.start();

				count++;
			    }
			}//end of try
				catch(Exception e) {
					callback.accept(new Message("Server socket did not launch", "", "", Message.Types.SERVER_MSG));
				}
			}//end of while
	}

		class ClientThread extends Thread{
			Socket connection;
			int count;
			String ID = "";
			String state = "idle";
			ObjectInputStream in;
			ObjectOutputStream out;

			ClientThread(Socket s, int count){
				this.connection = s;
				this.count = count;
			}

			public void run(){

				try {
					in = new ObjectInputStream(connection.getInputStream());
					out = new ObjectOutputStream(connection.getOutputStream());
					connection.setTcpNoDelay(true);
				}
				catch(Exception e) {
					System.out.println("Streams not open");
				}

				 while(true) {
					    try {
					    	Message data = (Message) in.readObject();

							switch(data.type) {
								case SEND_ID:
									if (usernameTaken(data.msg)) {
										out.writeObject(new Message("", "", "", Message.Types.REJECT_ID));
									} else {
										ID = data.msg;
										users.put(ID, this);
										callback.accept(new Message("Client "+count+" chose username: "+data.msg, "", "", Message.Types.SERVER_MSG));
										out.writeObject(new Message(ID, "", "", Message.Types.ACCEPT_ID));
									}
									break;
								case QUEUE:
									state = "looking";
									callback.accept(new Message("Client: "+ID+" is looking for a game", "", "", Message.Types.SERVER_MSG));
									for (ClientThread ct : clients) {
										if (Objects.equals(ct.state, "looking") && ct != this) {
											state = "playing";
											ct.state = "playing";
											callback.accept(new Message("Client: "+ID+" is playing against client: "+ct.ID, "", "", Message.Types.SERVER_MSG));
											out.writeObject(new Message("", ct.ID, "", Message.Types.QUEUE));
											ct.out.writeObject(new Message("", ID, "", Message.Types.QUEUE));
											break;
										}
									}
									break;
								case SEND_MSG:
									callback.accept(new Message("Client: "+data.sender+" sent: "+data.msg+" to client: "+data.receiver, "", "", Message.Types.SERVER_MSG));
									users.get(data.receiver).out.writeObject(new Message(data.msg, "", "", Message.Types.SEND_MSG));
									break;
							}
						}
					    catch(Exception e) {
							clients.remove(this);
							if (!Objects.equals(ID, "")) {
								callback.accept(new Message("Client: "+ID+" has disconnected from the server", "", "", Message.Types.SERVER_MSG));
							} else {
								callback.accept(new Message("Client: "+count+" has disconnected from the server", "", "", Message.Types.SERVER_MSG));
							}
					    	break;
					    }
					}
				}//end of run
		}//end of client thread

	public boolean usernameTaken(String ID) {
		boolean contains = false;
		for (ClientThread cl : clients) {
			if (Objects.equals(cl.ID, ID)) {
				contains = true;
				break;
			}
		}
		return contains;
	}
}

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

public class ClientHandler implements Runnable{
    public static ArrayList<ClientHandler> clientHandlers = new ArrayList<>();// allow broadcast a message to multiple client
    private Socket socket;// used to establish a connection between the client and server
    private BufferedReader bufferedReader;// read message sent from client
    private BufferedWriter bufferedWriter;// send messages to our client, and th
    private String clientUsername;

    public ClientHandler(Socket socket) {
        try{
            this.socket = socket;
            this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())) ;
            this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.clientUsername = bufferedReader.readLine();// waiting message from client
            clientHandlers.add(this); // this represents ClientHandler Object
            // then send the message to any connected client that a new user has just joined the chat
            broadcastMessage("SERVER: " + clientUsername + " has entered the chat!");
        }catch (IOException e){
            closeEverything(socket, bufferedReader, bufferedWriter);
        }
    }

    @Override
    public void run() {
        String messageFromClient;
        while(socket.isConnected()){
            try {
                messageFromClient = bufferedReader.readLine();
                broadcastMessage(messageFromClient);
            }catch (IOException e){
                closeEverything(socket, bufferedReader,bufferedWriter);
                break;// when the client disconnects this, we break this while loop out.
            }
        }
    }

    public void broadcastMessage(String messageToSend){
        for(var clientHandler : clientHandlers){
            try{
                if(!clientHandler.clientUsername.equals(clientUsername)){
                    clientHandler.bufferedWriter.write(messageToSend);
                    clientHandler.bufferedWriter.newLine();// essentially says i'm done here, no need to wait for any more data from me;
                    clientHandler.bufferedWriter.flush();
                }
            }catch (IOException e){
                closeEverything(socket, bufferedReader,bufferedWriter);
            }
        }
    }

    public void removeClientHandler(){
        clientHandlers.remove(this);
        broadcastMessage("SERVER: " + this.clientUsername + " has left the chat!");
    }

    public void closeEverything(Socket socket, BufferedReader bufferedReader, BufferedWriter bufferedWriter){
        removeClientHandler();

        try{
            if(bufferedReader != null){
                bufferedReader.close();
            }
            if(bufferedWriter != null){
                bufferedWriter.close(); // also closed InputStreamReader/OutputStreamWriter
            }
            if(socket != null){
                socket.close(); // close socket will also close InputStream/OutputStream
            }
        }catch (IOException e){
            e.printStackTrace();
        }

    }
}

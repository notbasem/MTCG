import com.company.Server.Server;

import java.io.IOException;
import java.net.ServerSocket;

public class Main {
    ServerSocket serverSocket;
    public static void main(String[] args)  {
        Server server = new Server(10001);
        try {
            server.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

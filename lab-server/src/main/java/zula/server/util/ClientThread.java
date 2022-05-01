package zula.server.util;

import zula.common.exceptions.PrintException;
import zula.server.ServerApp;

import java.io.IOException;

public class ClientThread extends Thread {
    private Client client;
    public ClientThread(Client client1) {
        client = client1;
    }
    @Override
    public void run() {
        ServerApp serverApp = new ServerApp();
        try {
            serverApp.startApp(client);
        } catch (IOException | PrintException e) {
            e.printStackTrace();
        }
    }

}
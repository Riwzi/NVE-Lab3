/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package network;

import com.jme3.network.Client;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Quentin
 */
public class ClientSender implements Runnable{
    
    private Client serverConnection;
    private LinkedBlockingQueue<Integer> requestToSend; 
    
    public ClientSender(Client serverConnection, LinkedBlockingQueue<Integer> requestToSend) {
        this.serverConnection = serverConnection;
        this.requestToSend = requestToSend;
    }
    
    
    private void sendMessage(int keyboardInput){
        //Message msg;
        //serverConnection.send(message);
        
    }

    @Override
    public void run() {
        int toSend;
        while(true){
            try {
                toSend = requestToSend.take();
                sendMessage(toSend);
            } catch (InterruptedException ex) {
                Logger.getLogger(ClientSender.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    
    
    
    
}

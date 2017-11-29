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
import network.Util.MoveMessage;

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
    
    
    private void sendMessage(MoveMessage msg){
        this.serverConnection.send(msg);
        
    }

    @Override
    public void run() {
        int toSend;
        while(true){
            try {
                toSend = requestToSend.take();
                MoveMessage msg = new MoveMessage();
                msg.incrementDirection(toSend);
                sendMessage(msg);
            } catch (InterruptedException ex) {
                Logger.getLogger(ClientSender.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    
    
    
    
}

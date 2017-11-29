/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package network;

import com.jme3.math.Vector2f;
import com.jme3.network.Client;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;
import network.Util.MoveMessage;
import network.Util.PlayerInput;

/**
 *
 * @author Quentin
 */
public class ClientSender implements Runnable{
    private static final float CLIENT_SEND_RATE = 30f;
    
    private Client serverConnection;
    private LinkedBlockingQueue<PlayerInput> requestToSend;
    
    public ClientSender(Client serverConnection, LinkedBlockingQueue<PlayerInput> requestToSend) {
        this.serverConnection = serverConnection;
        this.requestToSend = requestToSend;
    }
    
    
    private void sendMessage(MoveMessage msg){
        this.serverConnection.send(msg);
        
    }

    @Override
    public void run() {
        PlayerInput input;
        float timer = 0f;
        MoveMessage msg = new MoveMessage();
        while(true){
            try {
                input = requestToSend.take();
                Vector2f accel = input.getAcceleration();
                float tpf = input.getTpf();
                msg.updateAcceleration(accel);
                timer = timer + tpf;

                if (timer >= 1/CLIENT_SEND_RATE) {
                    sendMessage(msg);
                    msg = new MoveMessage();
                    timer = 0f;
                }
            } catch (InterruptedException ex) {
                Logger.getLogger(ClientSender.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    
    
    
    
}

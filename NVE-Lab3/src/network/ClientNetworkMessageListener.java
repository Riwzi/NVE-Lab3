/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package network;

import com.jme3.app.SimpleApplication;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.network.Client;
import com.jme3.network.Message;
import com.jme3.network.MessageListener;
import com.jme3.network.Network;
import com.jme3.system.JmeContext;
import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import mygame.Ask;
import mygame.Game;
import network.Util.*;
import network.Util.OpenConnectionMessage;

/**
 *
 * @author Quentin
 */
public class ClientNetworkMessageListener
            implements MessageListener<Client>, Runnable{
    
    private Client serverConnection;
    private TheClient theClient;

    public ClientNetworkMessageListener(Client server, TheClient theClient) {
        serverConnection = server;
        this.theClient = theClient;
    }
    
         

    // this method is called whenever network packets arrive
    @Override
    public void messageReceived(Client source, Message m) {
        // these if statements is a clumsy but simple (and working) 
        // solution; better would be to code behavour in the message 
        // classes and call them on the message
        if (m instanceof Util.WelcomeClientMessage) {
            // 1) carry out the change and 2) send back an ack to sender
            Util.WelcomeClientMessage msg = (Util.WelcomeClientMessage) m;
            int originalSender = msg.getSenderID();
            int messageID = msg.getMessageID();

            // NB! ALL CHANGES TO THE SCENE GRAPH MUST BE DONE IN THE 
            // MAIN THREAD! THE TECHNIQUE IS TO SEND OVER A PIECE OF CODE 
            // (A "CALLABLE") FROM THE NETWORKING THREAD (THIS THREAD) TO 
            // THE MAIN THREAD (THE ONE WITH THE SCENE GRAPH) AND HAVE 
            // THE MAIN THREAD EXECUTE IT. (This is part of how threads 
            // communicate in Java and NOT something specific to 
            // JMonkeyEngine)

            Future result;
            result = theClient.enqueue(new Callable() {
                @Override
                public Object call() throws Exception {
                    theClient.createGame();
                    return true;
                }
            });

        }else if (m instanceof Util.GameActiveMessage){
            Future result = theClient.enqueue(new Callable() {
                @Override
                public Object call() throws Exception {
                    theClient.startGame();
                    return true;
                }
            });
        }
    }

    @Override
    public void run() {
         serverConnection.addMessageListener(this,
                    PlayerLight.class,
                    OpenConnectionMessage.class,
                    WelcomeClientMessage.class,
                    NameTakenMessage.class,
                    GameActiveMessage.class,
                    DisconnectMessage.class,
                    GameSetupMessage.class,
                    GameStartMessage.class,
                    GameOverMessage.class,
                    VelocityChangeMessage.class,
                    PositionChangeMessage.class,
                    PositionAndVelocityChangeMessage.class,
                    ScoreChange.class,
                    PositionsUpdateMessage.class,
                    ScoreUpdateMessage.class,
                    TimeUpdateMessage.class);
    }
}
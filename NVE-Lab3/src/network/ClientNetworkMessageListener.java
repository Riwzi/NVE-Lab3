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
import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
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
            implements MessageListener<Client>{
    
    private Client serverConnection;
    private TheClient theClient;
    ConcurrentHashMap< Integer, InformationReceived > updateInfos;

    public ClientNetworkMessageListener(Client server, TheClient theClient, ConcurrentHashMap< Integer, InformationReceived > updateInfos) {
        serverConnection = server;
        this.theClient = theClient;
        this.updateInfos = updateInfos;
    }
    
         

    // this method is called whenever network packets arrive
    @Override
    public void messageReceived(Client source, Message m) {
        // these if statements is a clumsy but simple (and working) 
        // solution; better would be to code behavour in the message 
        // classes and call them on the message
        
        
        if (m instanceof Util.GameStartMessage) {
            

            Future result;
            result = theClient.enqueue(new Callable() {
                @Override
                public Object call() throws Exception {
                    theClient.startGame();
                    return true;
                }
            });

        }else if (m instanceof Util.GameSetupMessage){
            GameSetupMessage msg = (GameSetupMessage) m;
            final ArrayList<PlayerLight> players = msg.getPlayers();
            
            Future result = theClient.enqueue(new Callable() {
                @Override
                public Object call() throws Exception {
                    //Todo change
                    theClient.putConfig(1, players);
                    return true;
                }
            });
        }
    }

}
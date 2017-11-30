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
import com.jme3.math.Vector2f;
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
import static mygame.Game.TIMEINDEX;
import network.Util.*;

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
            
            System.out.println("need to start game");
            Future result;
            result = theClient.enqueue(new Callable() {
                @Override
                public Object call() throws Exception {
                    theClient.startGame();
                    return true;
                }
            });

        }else if (m instanceof Util.GameSetupMessage){
            System.out.println("Receive put config");

            final GameSetupMessage msg = (GameSetupMessage) m;
            final ArrayList<PlayerLight> players = msg.getPlayers();
            
            Future result = theClient.enqueue(new Callable() {
                @Override
                public Object call() throws Exception {
                    //Todo change
                    theClient.putConfig(msg.getMyId(), players);
                    return true;
                }
            });
        }else if(m instanceof VelocityChangeMessage){
            VelocityChangeMessage msg = (VelocityChangeMessage) m;
     
            InformationReceived info = updateInfos.get(msg.getDiskID());
            //System.out.println(updateInfos.get(msg.getDiskID()).getVelocity());
            info.setVelocity(msg.getMessageID(), msg.getNewVelocity());
            //System.out.println(updateInfos.get(msg.getDiskID()).getVelocity());


        }
        else if(m instanceof PositionChangeMessage){
            PositionChangeMessage msg = (PositionChangeMessage) m;
            InformationReceived info = updateInfos.get(msg.getDiskID());
            info.setPosition(msg.getMessageID(), msg.getNewPosition());
            
        }
        else if(m instanceof PositionAndVelocityChangeMessage){
            PositionAndVelocityChangeMessage msg = (PositionAndVelocityChangeMessage) m;
            InformationReceived info = updateInfos.get(msg.getDiskID());
            info.setPosition(msg.getMessageID(), msg.getNewPosition());
            info.setVelocity(msg.getMessageID(), msg.getNewVelocity());
            
        }
        else if(m instanceof ScoreChange){
            ScoreChange msg = (ScoreChange) m;
            InformationReceived info = updateInfos.get(msg.getPlayerID());
            info.setScore(msg.getMessageID(), msg.getNewScore());
            
        }
        else if(m instanceof ScoreChange){
            ScoreChange msg = (ScoreChange) m;
            InformationReceived info = updateInfos.get(msg.getPlayerID());
            info.setScore(msg.getMessageID(), msg.getNewScore());
        }
        else if(m instanceof ScoreUpdateMessage){
            ScoreUpdateMessage msg = (ScoreUpdateMessage) m;
            for (PlayerLight player : msg.getPlayers()){
                InformationReceived info = updateInfos.get(player.getID());
                info.setScore(msg.getMessageID(), player.getScore());

            }
            
        }
        else if(m instanceof TimeUpdateMessage){
            TimeUpdateMessage msg = (TimeUpdateMessage) m;
            InformationReceived info = updateInfos.get(TIMEINDEX);
            info.setTime(msg.getMessageID(), msg.getTime());

        }
        else if(m instanceof GameOverMessage){
            final GameOverMessage msg = (GameOverMessage) m;
            Future result = theClient.enqueue(new Callable() {
                @Override
                public Object call() throws Exception {
                    System.out.println("GameOver");
                    theClient.gameOver(msg.getWinners());
                    return true;
                }
            });
        }
    }

}
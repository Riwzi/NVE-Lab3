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
import com.jme3.network.ClientStateListener;
import com.jme3.network.Message;
import com.jme3.network.MessageListener;
import com.jme3.network.Network;
import com.jme3.system.JmeContext;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import mygame.Ask;
import mygame.Game;
import network.Util.*;

/**
 *
 * @author Quentin
 */
public class TheClient extends SimpleApplication implements ClientStateListener{

    private Ask ask = new Ask();
    private Game game = new Game();
    private float time = 0f;
    private boolean running = true;
    private ClientNetworkMessageListener clientListener;
    private ClientSender clientSender;
    
    // the connection back to the server
    private Client serverConnection;
    // the scene contains just a rotating box
    private final String hostname; // where the server can be found
    private final int port; // the port att the server that we use
    private float factor = 1; // rotating direction (1=left-right, -1=opposite)
    
    public TheClient(String hostname, int port) {
        ask.setEnabled(true);
        game.setEnabled(false);
        running = false;
        
        stateManager.attach(game);
        stateManager.attach(ask);
        this.hostname = hostname;
        this.port = port;
    }
    
    public static void main(String[] args) {
        Util.initialiseSerializables();
        new TheClient(Util.HOSTNAME, Util.PORT).start();
    }

    @Override
    public void simpleInitApp() {
        setDisplayStatView(false);
        setDisplayFps(false);
        
        try {
            //Initialize the queue to use to send informations
            LinkedBlockingQueue<PlayerInput> requestToSend = new LinkedBlockingQueue<>();

            game.setRequestToSend(requestToSend);
                        
            
           
            serverConnection = Network.connectToServer(hostname, port);            
            addAskInputs();
            this.clientSender = new ClientSender(serverConnection, requestToSend);

            // this make the client react on messages when they arrive by
            // calling messageReceived in ClientNetworkMessageListener
            clientListener = new ClientNetworkMessageListener(serverConnection, this, game.getUpdateInfo());
            serverConnection.addMessageListener(clientListener,
                GameSetupMessage.class,
                GameStartMessage.class,
                GameOverMessage.class,
                VelocityChangeMessage.class,
                PositionChangeMessage.class,
                PositionAndVelocityChangeMessage.class,
                ScoreChange.class,
                PositionsUpdateMessage.class,
                ScoreUpdateMessage.class,
                MoveMessage.class,
                TimeUpdateMessage.class);
            
            // finally start the communication channel to the server
            serverConnection.addClientStateListener(this);
            serverConnection.start();
            new Thread(clientSender).start();
            
        }catch (IOException ex) {
            ex.printStackTrace();
            this.destroy();
            this.stop();
        }
        
    }
    
    private final ActionListener actionListener = new ActionListener() {
        @Override
        public void onAction(String name, boolean isPressed, float tpf) {
            if (isPressed) {
                if (name.equals("Exit")) {
                    TheClient.this.stop();
                } else if (name.equals("Restart")) {
                    createGame();
                }
            }
        }
    };

    @Override
    public void simpleUpdate(float tpf) {
        if (running) {
            if (Game.getRemainingTime() <= 0) {
                game.setEnabled(false);
                addAskInputs();
                ask.setEnabled(true);
                time = 0f;
                this.running = false;
            }
        }
    }
    
    // takes down all communication channels gracefully, when called
    @Override
    public void destroy() {
        serverConnection.close();
        super.destroy();
    }
    
    public void createGame(){
        ask.setEnabled(false);
        game.setEnabled(true);
        inputManager.deleteMapping("Restart");
        inputManager.deleteMapping("Exit");
        
    }
    
    public void startGame(){
        game.startGame();
        running = true;
    }
    
    public void putConfig(int userID ,ArrayList<PlayerLight> playersList){
        for (PlayerLight player : playersList){
            if(player.getID() != userID)
                game.addPlayer(player.getID(), player.getName(), player.getPosition());
            else
                game.addLocalPlayer(player.getID(), player.getName(), player.getPosition());
        }
    }
    
    public void addAskInputs(){
        inputManager.addMapping("Restart", new KeyTrigger(KeyInput.KEY_P));
        inputManager.addMapping("Exit", new KeyTrigger(KeyInput.KEY_E));
        inputManager.addListener(actionListener, "Restart", "Exit");
    }
    
    public void gameOver(ArrayList<Integer> winners){
        this.running = false;
        game.setWinner(winners);
    }
    
    @Override
    public void clientConnected(Client c) {
        System.out.println("Client connected succesfully !");
        createGame();
    }
    

    @Override
    public void clientDisconnected(Client c, DisconnectInfo info) {
        System.out.println(info);
        System.exit(0);
    }



    


}

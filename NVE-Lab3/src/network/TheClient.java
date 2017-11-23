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
public class TheClient extends SimpleApplication implements ClientStateListener{

    private Ask ask = new Ask();
    private Game game = new Game();
    private float time = 0f;
    private boolean running = true;
    private ClientNetworkMessageListener clientListener;
    
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
        new TheClient(Util.HOSTNAME, Util.PORT).start(JmeContext.Type.Display);
    }

    @Override
    public void simpleInitApp() {
        setDisplayStatView(false);
        setDisplayFps(false);
        
        try {
            serverConnection = Network.connectToServer(hostname, port);            
            addAskInputs();
      
            // this make the client react on messages when they arrive by
            // calling messageReceived in ClientNetworkMessageListener
            clientListener = new ClientNetworkMessageListener(serverConnection, this);
            clientListener.run();
            
            // finally start the communication channel to the server
            serverConnection.start();
            if(serverConnection.isConnected())
                System.out.println("Connected to server");
            
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
                    OpenConnectionMessage msg = new OpenConnectionMessage(1, "Player1");
                    serverConnection.send(msg);
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
                running = false;
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
    
    public void addAskInputs(){
        inputManager.addMapping("Restart", new KeyTrigger(KeyInput.KEY_P));
        inputManager.addMapping("Exit", new KeyTrigger(KeyInput.KEY_E));
        inputManager.addListener(actionListener, "Restart", "Exit");
    }
    @Override
    public void clientConnected(Client c) {
        createGame();
    }

    @Override
    public void clientDisconnected(Client c, DisconnectInfo info) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }



    


}

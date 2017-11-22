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
import com.jme3.network.Network;
import java.io.IOException;
import mygame.Ask;
import mygame.Game;
import mygame.Main;

/**
 *
 * @author Quentin
 */
public class TheClient extends SimpleApplication{

    private Ask ask = new Ask();
    private Game game = new Game();
    private float time = 0f;
    private boolean running = true;
    
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
        //TODO Change connection parameters
        TheClient app = new TheClient("test", 789456);
        app.start();
    }

    @Override
    public void simpleInitApp() {
        setDisplayStatView(false);
        setDisplayFps(false);
        
        Util.print("Initializing");
        try {
            Util.print("Opening server connection");
            serverConnection = Network.connectToServer(hostname, port);
            Util.print("Server is starting networking");
            
            addAskInputs();
      
            Util.print("Adding network listener");
            // this make the client react on messages when they arrive by
            // calling messageReceived in ClientNetworkMessageListener
//TODO ADD PACKET TYPE
            serverConnection.addMessageListener(new ClientNetworkMessageListener());
            
            // finally start the communication channel to the server
            serverConnection.start();
            Util.print("Client communication back to server started");
            
        }catch (IOException ex) {
            ex.printStackTrace();
            this.destroy();
            this.stop();
        }
        
    }
    
    private ActionListener actionListener = new ActionListener() {
        @Override
        public void onAction(String name, boolean isPressed, float tpf) {
            if (isPressed) {
                if (name.equals("Exit")) {
                    TheClient.this.stop();
                } else if (name.equals("Restart")) {
                    //TODO SEND NEW  MESSAGE
                    //serverConnection.send(message);
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
        running = true;
        inputManager.deleteMapping("Restart");
        inputManager.deleteMapping("Exit");
    }
    
    public void startGame(){
        game.startGame();
    }
    
    public void addAskInputs(){
        inputManager.addMapping("Restart", new KeyTrigger(KeyInput.KEY_P));
        inputManager.addMapping("Exit", new KeyTrigger(KeyInput.KEY_E));
        inputManager.addListener(actionListener, "Restart", "Exit");
    }
}

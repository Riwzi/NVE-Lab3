/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package network;

import com.jme3.app.SimpleApplication;
import com.jme3.network.Filters;
import com.jme3.network.HostedConnection;
import com.jme3.network.Message;
import com.jme3.network.MessageListener;
import com.jme3.network.ConnectionListener;
import com.jme3.network.Network;
import com.jme3.network.Server;
import com.jme3.renderer.RenderManager;
import com.jme3.system.JmeContext;
import java.io.IOException;
import java.util.concurrent.ConcurrentLinkedQueue;
import mygame.Ask;
import mygame.Game;

/**
 * This program demonstrates networking in JMonkeyEngine using SpiderMonkey, and
 * contains the server.
 *
 *
 * @author hj
 */
public class TheServer extends SimpleApplication {

    private Server server;
    private final int port;
    
    private ConcurrentLinkedQueue<Util.MyAbstractMessage> incoming;
    private ConcurrentLinkedQueue<Util.MyAbstractMessage> outgoing;

    private Ask ask = new Ask();
    private Game game = new Game(); //Modify game to take the outgoing/incoming queues as arguments, or do i just send incoming as enqueued Callables?
    private float countdown = 15f;
    private float countdownRemaining = 0f;
    
    public static void main(String[] args) {
        System.out.println("Server initializing");
        Util.initialiseSerializables();
        //new TheServer(Util.PORT).start(JmeContext.Type.Headless);
        new TheServer(8001).start(JmeContext.Type.Headless);
    }

    public TheServer(int port) {
        this.port = port;
        
        ask.setEnabled(false);
        game.setEnabled(true);
        stateManager.attach(game);
        stateManager.attach(ask);
    }

    @Override
    @SuppressWarnings("CallToPrintStackTrace")
    public void simpleInitApp() {
        // In a game server, the server builds and maintains a perfect 
        // copy of the game and makes use of that copy to make descisions 

        try {
            System.out.println("Using port " + port);
            // create the server by opening a port
            server = Network.createServer(port);
            server.start(); // start the server, so it starts using the port
        } catch (IOException ex) {
            ex.printStackTrace();
            destroy();
            this.stop();
        }
        System.out.println("Server started");
        
        // add a listener that reacts on incoming network packets
        //server.addMessageListener(new ServerListener());
        server.addConnectionListener(new MyConnectionListener()); // ?
        System.out.println("ServerListener aktivated and added to server");
    }

    @Override
    public void simpleUpdate(float tpf) {
        if (game.isEnabled()) {
            if (Game.getRemainingTime() <= 0) {
                game.setEnabled(false);
                ask.setEnabled(true);
            }
        }
    }

    @Override
    public void simpleRender(RenderManager rm) {
    }

    @Override
    public void destroy() {
        System.out.println("Server going down");
        server.close();
        super.destroy();
        System.out.println("Server down");
    }

    // this class provides a handler for incoming network packets
    private class ServerListener implements MessageListener<HostedConnection> {
        @Override
        public void messageReceived(HostedConnection source, Message m) {
            
        }
    }
    
    // Should we have this class at all? perhaps just handle everything in the ServerListener
    // this class provides a handler for incoming HostedConnections
    private class MyConnectionListener implements ConnectionListener {
        @Override
        public void connectionAdded(Server s, HostedConnection c) {
            // Add player to game, assign unique id
            System.out.println("Server knows that client #"+c.getId() + " is ready");
            
        }
        @Override
        public void connectionRemoved(Server s, HostedConnection c) {
            // Remove player from game
            System.out.println("Server knows that client #"+c.getId() + " has left");
        }
        
    }
    
    /**
     * Sends out updates to all clients every time theres a new message in the outgoing queue.
     */
    private class MessageSender implements Runnable {

        @Override
        @SuppressWarnings("SleepWhileInLoop")
        public void run() {
            System.out.println("MesssageSender thread running");
            while (true) {
                System.out.println("Sending one update to each client");
                //server.broadcast(new HeartMessage()); // ... send ...
            }
        }
    }
}
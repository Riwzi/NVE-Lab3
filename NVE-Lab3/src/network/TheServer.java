/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package network;

import com.jme3.app.SimpleApplication;
import com.jme3.math.Vector2f;
import com.jme3.network.Filters;
import com.jme3.network.HostedConnection;
import com.jme3.network.Message;
import com.jme3.network.MessageListener;
import com.jme3.network.ConnectionListener;
import com.jme3.network.Network;
import com.jme3.network.Server;
import com.jme3.renderer.RenderManager;
import com.jme3.system.JmeContext;
import disk.Disk;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;
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
    private LinkedBlockingQueue<Util.MyAbstractMessage> outgoing;
    private BiMap<Integer,Integer> connPlayerMap; //Maps the connectionId to a playerId (need to have this as a map if lots of clients connect and disconnect, as we only have 9 playerids).
    
    private Ask ask = new Ask();
    private Game game = new Game(); //Modify game to take the outgoing/incoming queues as arguments, or do i just send incoming as enqueued Callables?
    private float countdown = 15f;
    private float countdownRemaining = 0f;
    
    public static void main(String[] args) {
        System.out.println("Server initializing");
        Util.initialiseSerializables();
        new TheServer(Util.PORT).start(JmeContext.Type.Headless);
    }

    public TheServer(int port) {
        this.port = port;
        this.connPlayerMap = new BiMap();
        this.outgoing = new LinkedBlockingQueue();
        
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
        server.addMessageListener(new ServerListener(), 
                Util.GameOverMessage.class); //TODO: fix messages
        System.out.println("ServerListener activated and added to server");
        // add a listener that reacts on connections
        server.addConnectionListener(new MyConnectionListener());
        System.out.println("ConnectionListener activated and added to server");
        // add a packet sender that takes messages from the blockingqueue
        new Thread(new MessageSender()).start();
    }

    @Override
    public void simpleUpdate(float tpf) {
        if (game.isEnabled()) {
            if (Game.getRemainingTime() <= 0) {
                game.setEnabled(false);
                ask.setEnabled(true);
            } else {
                ArrayList<Disk> diskStore = game.getDisks();
                for (Disk disk: diskStore) {
                    //Collision detection with frame
                    float boundary = game.getFreeAreaWidth()/2;
                    if (disk.frameCollision(-boundary, boundary, -boundary, boundary, tpf)) {
                        //If there was a collision with the frame, queue a update package
                        try {
                            outgoing.put(new Util.PositionAndVelocityChangeMessage(1, disk.getId(), disk.getPosition(), disk.getVelocity()));
                        } catch(InterruptedException ex) {
                            Logger.getLogger(TheServer.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                    //Collision detection with other disks
                    for (Disk otherDisk: diskStore) {
                        if (!(disk.getId() == (otherDisk.getId()))) {
                            if (disk.diskCollision(otherDisk, tpf)) {
                                //If there was a collision between the 2 disks, queue update packages
                                try {
                                    outgoing.put(new Util.PositionAndVelocityChangeMessage(1, disk.getId(), disk.getPosition(), disk.getVelocity()));
                                    outgoing.put(new Util.PositionAndVelocityChangeMessage(1, otherDisk.getId(), otherDisk.getPosition(), otherDisk.getVelocity()));
                                } catch(InterruptedException ex) {
                                    Logger.getLogger(TheServer.class.getName()).log(Level.SEVERE, null, ex);
                                }
                            }
                        }
                    }
                }
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
            if (m instanceof Util.GameOverMessage) { //TODO replace with the actual messages the server should listen for
                System.out.println("GameOverMessage from client #" + source.getId());
            } else {
                // This should only happen if the clients sends messages they shouldn't
                // Programming error
                throw new RuntimeException("Unknown message.");
            }
        }
    }
    
    // this class provides a handler for incoming HostedConnections
    private class MyConnectionListener implements ConnectionListener {
        @Override
        public void connectionAdded(Server s, HostedConnection c) {
            System.out.println("Client #"+c.getId() + " has connected to the server");
            
            if (TheServer.this.game.isEnabled()) {
                c.close("Try again later, the game is in progress.");
            } else if (TheServer.this.connPlayerMap.size() >= 10) {
                c.close("Try again later, the game is full");
            } else {
                //Assign playerID
                boolean assigned = false;
                for (int i = 1; i<10; i++) {
                    //If there is a free playerID, assign it to the new player
                    if (!TheServer.this.connPlayerMap.containsValue(i)) {
                        TheServer.this.connPlayerMap.put(c.getId(), i);
                        assigned = true;
                        break;
                    }
                }
                if (!assigned) {
                    throw new RuntimeException("No playerID was available even though there should be at least one");
                }
                //Add player to game
                Future result = TheServer.this.enqueue(new Callable() {
                    @Override
                    public Object call() {
                        // Need method from game
                        // Add player to a list that will be initialized when the game starts, can't just add a player from here since positions need to be randomized
                        return true;
                    }
                });
            }
        }
        @Override
        public void connectionRemoved(Server s, HostedConnection c) {
            //IMPORTANT: will this method run if i close the connection in connectionAdded? in that case i shouldn't enqueue calls to main thread
            System.out.println("Client #"+c.getId() + " has disconnected from the server");
            
            //This removes the player from the list of used playerIDs
            TheServer.this.connPlayerMap.remove(c.getId());
            
            Future result = TheServer.this.enqueue(new Callable() {
                @Override
                public Object call() {
                    //Need method in game to remove a player from the active players
                    //THIS SHOULD NOT REMOVE THEIR DISK FROM THE GAME, as we would need to send packets to all clients to remove the disk
                    //Just let the disk slide around without any further movement controls
                    //Method should just ensure that the disconnected client wont participate in the NEXT game and that we won't send any more updates
                    
                    return true;
                }
            });
        }
        
    }
    
    /**
     * Sends out updates to all clients every time theres a new message in the outgoing queue.
     */
    private class MessageSender implements Runnable {

        @Override
        public void run() {
            System.out.println("MesssageSender thread running");
            try {
                while (true) {
                    Util.MyAbstractMessage message = outgoing.take();
                    server.broadcast(message);
                }
            } catch (InterruptedException ex) {
                Logger.getLogger(MessageSender.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    // A bidirectional ConcurrentHashMap
    private class BiMap<K,V> {
        ConcurrentHashMap<K,V> map = new ConcurrentHashMap<>();
        ConcurrentHashMap<V,K> inversedMap = new ConcurrentHashMap<>();
        
        public void put(K k, V v) {
            map.put(k, v);
            inversedMap.put(v, k);
        }
        
        public V get(K k) {
            return map.get(k);
        }
        
        public K getKey(V v) {
            return inversedMap.get(v);
        }
        
        public int size() {
            return map.size();
        }
        
        public boolean containsKey(K k) {
            return map.containsKey(k);
        }
        
        public boolean containsValue(V v) {
            return map.containsValue(v);
        }
        
        public V remove(K k) {
            V v = map.remove(k);
            inversedMap.remove(v);
            return v;
        }
    }
}
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
import disk.Player;
import disk.PositiveDisk;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
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
    private LinkedBlockingQueue<Callable> outgoing;
    private BiMap<Integer,Integer> connPlayerMap; //Maps the connectionId to a playerId (need to have this as a map if lots of clients connect and disconnect, as we only have 9 playerids).
    
    private Ask ask = new Ask();
    private Game game = new Game(); //Modify game to take the outgoing/incoming queues as arguments, or do i just send incoming as enqueued Callables?
    private float countdown = 12f;
    private float countdownRemaining = 0f;
    private long delayUntilStart = 2000; //ms
    private final ArrayList<Integer> winners;
    
    private static final float SERVER_TIME_SEND_RATE = 30f;
    private static float time_since_last_update = 0;
    
    public static void main(String[] args) {
        System.out.println("Server initializing");
        Util.initialiseSerializables();
        new TheServer(Util.PORT).start(JmeContext.Type.Headless);
        //new TheServer(Util.PORT).start();
    }

    public TheServer(int port) {
        this.port = port;
        this.connPlayerMap = new BiMap();
        this.outgoing = new LinkedBlockingQueue();
        
        ask.setEnabled(true);
        game.setEnabled(false);
        stateManager.attach(game);
        stateManager.attach(ask);
        winners = new ArrayList<>();
        
        this.countdownRemaining = this.countdown;
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
        
        // add a listeners
        server.addMessageListener(new ServerListener(), 
                Util.MoveMessage.class);
        server.addConnectionListener(new MyConnectionListener());
        
        // add a packet sender that takes messages from the blockingqueue
        new Thread(new MessageSender()).start();
    }
    
    public void putConfig(ArrayList<Util.PlayerLight> playersList){
        for (Util.PlayerLight player : playersList){
            game.addPlayer(player.getID(), player.getName(), player.getPosition());
        }
    }
    
    public void gameStart() {
        System.out.println("GAME START!");
        game.setEnabled(true);
        ask.setEnabled(false);

        Enumeration<Integer> values = this.connPlayerMap.values();
        ArrayList<Util.PlayerLight> players = new ArrayList();
        
        List<Vector2f> positions = Arrays.asList(game.getPlayerPositions());
        Collections.shuffle(positions);
        //Generate all players
        while (values.hasMoreElements()) {
            int value = values.nextElement();
            players.add(new Util.PlayerLight(game.getNextID(), value, positions.get(value), new Vector2f(), 0));
        }
        putConfig(players); //Add all players to the game
        
        try {
            final ArrayList<Util.PlayerLight> playerList = players;
            // Send different GameSetupMessages to each player (the difference is the playerIDs)
            for (final Util.PlayerLight player: players) {
                int connectionID = this.connPlayerMap.getKey(player.getName());
                final HostedConnection conn = TheServer.this.server.getConnection(connectionID);
                outgoing.put(new Callable() {
                    @Override
                    public Object call() throws Exception {
                        Util.MyAbstractMessage msg = new Util.GameSetupMessage(player.getID(), playerList);
                        msg.setReliable(true);
                        TheServer.this.server.broadcast(Filters.equalTo(conn),msg);
                        return true;
                    }
                });
            }
            System.out.println("TIME BEFORE "+Game.getRemainingTime());
            Thread.sleep(delayUntilStart);
            outgoing.put(new Callable() {
                @Override
                public Object call() throws Exception {
                    Util.MyAbstractMessage msg = new Util.GameStartMessage();
                    msg.setReliable(true);
                    TheServer.this.server.broadcast(msg);
                    return true;
                }
            });
            game.startGame();
            Game.increaseGameLength(delayUntilStart/1000);
        } catch(InterruptedException ex) {
            Logger.getLogger(TheServer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void simpleUpdate(float tpf) {
        if (game.isEnabled()) {
            if (Game.getRemainingTime() <= 0) {
                
                game.resetIDs();
                game.setEnabled(false);
                ask.setEnabled(true);
                this.countdownRemaining = this.countdown;
                winners.clear();
                int highestScore = 0;
                Enumeration<Integer> values = this.connPlayerMap.values();
                while (values.hasMoreElements()){
                    int playerID = values.nextElement();
                    System.out.println("Player: "+playerID);
                    Disk player = game.getPlayer(playerID);
                    if (player.getScore() > highestScore || highestScore == 0) {
                        highestScore = player.getScore();
                    }
                }
                values = this.connPlayerMap.values();
                while (values.hasMoreElements()){
                    int playerID = values.nextElement();
                    Player player = game.getPlayer(playerID);
                    if (player.getScore() == highestScore) {
                        winners.add(playerID);
                    }
                }
                try {
                    outgoing.put(new Callable() {
                        @Override
                        public Object call() throws Exception {
                            Util.MyAbstractMessage msg = new Util.GameOverMessage(winners);
                            msg.setReliable(true);
                            TheServer.this.server.broadcast(msg);
                            return true;
                        }
                    });
                } catch(InterruptedException ex) {
                    Logger.getLogger(TheServer.class.getName()).log(Level.SEVERE, null, ex);
                }

            } else {
                SendTimeMessage(tpf);
                ArrayList<Disk> diskStore = game.getDisks();
                for (Disk disk: diskStore) {
                    //Collision detection with frame
                    float boundary = game.getFreeAreaWidth()/2;
                    if (disk.frameCollision(-boundary, boundary, -boundary, boundary, tpf)) {
                        //If there was a collision with the frame, queue a update package
                        final Disk theDisk = disk;
                        try {
                            outgoing.put(new Callable() {
                                @Override
                                public Object call() throws Exception {
                                    Util.MyAbstractMessage msg = new Util.PositionAndVelocityChangeMessage(theDisk.getId(), theDisk.getPosition(), theDisk.getVelocity());
                                    TheServer.this.server.broadcast(msg);
                                    return true;
                                }
                            });
                        } catch(InterruptedException ex) {
                            Logger.getLogger(TheServer.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                    //Collision detection with other disks
                    for (Disk otherDisk: diskStore) {
                        if (!(disk.getId() == (otherDisk.getId()))) {
                            final Disk theDisk = disk;
                            if (disk.diskCollision(otherDisk, tpf)) {
                                //If there was a collision between the 2 disks, queue update packages
                                final Disk theOtherDisk = otherDisk;
                                PositionAndVelocityMessage(theDisk.getId(), theDisk.getPosition(), theDisk.getVelocity());
                                PositionAndVelocityMessage(theOtherDisk.getId(), theOtherDisk.getPosition(), theOtherDisk.getVelocity());
                                
                                if (theDisk instanceof Player) {
                                    ScoreChangeMessage(theDisk.getId(), theDisk.getScore());
                                }
                                if (theOtherDisk instanceof Player) {
                                    ScoreChangeMessage(theOtherDisk.getId(), theOtherDisk.getScore());
                                }
                                if (theDisk instanceof PositiveDisk && theOtherDisk instanceof Player) {
                                    RemovePointMessage(theDisk.getId());
                                }
                                if (theOtherDisk instanceof PositiveDisk && theDisk instanceof Player) {
                                    RemovePointMessage(theOtherDisk.getId());
                                }
                            }
                        }
                    }
                }
            }
        } else if (ask.isEnabled()) {
            this.countdownRemaining -= tpf;
            if (this.countdownRemaining <= 0) {
                gameStart();
            }
            
        }
    }
    
    
    public void SendTimeMessage(float tpf) {
        TheServer.time_since_last_update += tpf;
        if (time_since_last_update >= 1/TheServer.SERVER_TIME_SEND_RATE); {
            TheServer.time_since_last_update = 0f;
            try {
                outgoing.put(new Callable() {
                    @Override
                    public Object call() throws Exception {
                        Util.MyAbstractMessage msg = new Util.TimeUpdateMessage(Game.getRemainingTime());
                        TheServer.this.server.broadcast(msg);
                        return true;
                    }
                });
            } catch (InterruptedException ex) {
                Logger.getLogger(TheServer.class.getName()).log(Level.SEVERE, null, ex);
            }
         }
    }
    
    public void ScoreChangeMessage(int diskId, int diskScore) {
        final int _diskId = diskId;
        final int _diskScore = diskScore;
        try {
            outgoing.put(new Callable() {
                @Override
                public Object call() throws Exception {
                    Util.MyAbstractMessage msg = new Util.ScoreChange(_diskId, _diskScore);
                    msg.setReliable(true);
                    TheServer.this.server.broadcast(msg);
                    return true;
                }
            });
        } catch (InterruptedException ex) {
            Logger.getLogger(TheServer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void PositionAndVelocityMessage(int diskId, Vector2f position, Vector2f velocity) {
        final int _diskId = diskId;
        final Vector2f _position = position;
        final Vector2f _velocity = velocity;
        try {
            outgoing.put(new Callable() {
                @Override
                public Object call() throws Exception {
                    Util.MyAbstractMessage msg = new Util.PositionAndVelocityChangeMessage(_diskId, _position, _velocity);
                    msg.setReliable(true);
                    TheServer.this.server.broadcast(msg);
                    return true;
                }
            });
        } catch (InterruptedException ex) {
            Logger.getLogger(TheServer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void RemovePointMessage(int diskId) {
        final int _diskId = diskId;
        try {
            outgoing.put(new Callable() {
                @Override
                public Object call() throws Exception {
                    Util.MyAbstractMessage msg = new Util.RemovePointMessage(_diskId);
                    msg.setReliable(true);
                    TheServer.this.server.broadcast(msg);
                    return true;
                }
            });
        } catch (InterruptedException ex) {
            Logger.getLogger(TheServer.class.getName()).log(Level.SEVERE, null, ex);
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
            if (m instanceof Util.MoveMessage) {
                final int connectionId = source.getId();
                final Util.MoveMessage msg = ((Util.MoveMessage) m);
                //final int direction = ((Util.MoveMessage) m).getDirection();
                
                Future result = TheServer.this.enqueue(new Callable() {
                    @Override
                    public Object call() {
                        //Get the player disk
                        Player player = game.getPlayer(connPlayerMap.get(connectionId));

                        //Increase the velocity in the given direction
                        player.addVelocity(msg.getAcceleration());
                        return true;
                    }
                });
                
                try {
                    outgoing.put(new Callable() {
                        @Override
                        public Object call() throws Exception {
                            Player player = game.getPlayer(connPlayerMap.get(connectionId));
                            Vector2f velocity = player.getVelocity();
                            int id = player.getId();
                            Util.MyAbstractMessage msg = new Util.VelocityChangeMessage(id, velocity);
                            TheServer.this.server.broadcast(msg);
                            return true;
                        }
                    });
                } catch(InterruptedException ex) {
                    Logger.getLogger(TheServer.class.getName()).log(Level.SEVERE, null, ex);
                }
                
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
            }
        }
        @Override
        public void connectionRemoved(Server s, HostedConnection c) {
            System.out.println("Client #"+c.getId() + " has disconnected from the server");
            //This removes the player from the list of used playerIDs
            if (TheServer.this.connPlayerMap.get(c.getId()) != null) {
                TheServer.this.connPlayerMap.remove(c.getId());
            }
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
                    outgoing.take().call();
                }
            } catch (Exception ex) {
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
        
        public Enumeration<V> values() {
            return inversedMap.keys();
        }
        
        public Enumeration<K> keys() {
            return map.keys();
        }
        
        public int mappingCount() {
            return (int)map.mappingCount();
        }
    }
}
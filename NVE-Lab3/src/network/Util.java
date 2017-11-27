
package network;

import com.jme3.math.Vector2f;
import com.jme3.network.AbstractMessage;
import com.jme3.network.serializing.Serializable;
import com.jme3.network.serializing.Serializer;
import java.util.ArrayList;


public class Util {
    
    public static final String HOSTNAME = "127.0.0.1";
    public static final int PORT = 7006;
    public static final int UP = 0;
    public static final int DOWN = 1;
    public static final int LEFT = 2;
    public static final int RIGHT = 3;    
    
    public static void initialiseSerializables() {
        Serializer.registerClasses(
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
    }
    
    abstract public static class MyAbstractMessage extends AbstractMessage {


        protected int messageID;
        protected static int globalCounter = 1000;

        public MyAbstractMessage() {
            this.messageID = globalCounter++; // default messageID
        }

        public int getMessageID() {
            return messageID;
        }

    }
    
    /**
     * Used for now to send a collection of data for a player
     */
    public static class PlayerLight{
        private int id;
        private int name;
        private Vector2f position;
        private Vector2f velocity;
        private int score;
        
        public PlayerLight(int id, int name, Vector2f position, Vector2f velocity, int score) {
            this.id = id;
            this.name = name;
            this.position = position;
            this.velocity = velocity;
            this.score = score;
        }
        
        public int getID() {
            return id;
        }
        
        public int getName() {
            return name;
        }
        
        public Vector2f getPosition() {
            return position;
        }
        
        public Vector2f getVelocity() {
            return velocity;
        }
        
        public int getScore() {
            return score;
        }
    }
    
    @Serializable
    public static class GameSetupMessage extends MyAbstractMessage {
        private ArrayList<PlayerLight> players;
        private int myId;
        
        public GameSetupMessage() {
        }
        
        public GameSetupMessage(int myId, ArrayList<PlayerLight> players) {
            this.players = players;
        }
        
        public ArrayList<PlayerLight> getPlayers() {
            return players;
        }
        
        public int getMyId(){
            return myId;
        }
        
    }
    
    @Serializable
    public static class GameStartMessage extends MyAbstractMessage {
        
        public GameStartMessage() {
        }
        
        
        public String getMessage() {
            return "Game has started.";
        }
    }
    
    @Serializable
    public static class GameOverMessage extends MyAbstractMessage {
        private ArrayList<Integer> winners;
        
        public GameOverMessage() {
        }
        
        public GameOverMessage(ArrayList<Integer> winners) {
            this.winners = winners;
        }
        
        public ArrayList<Integer> getWinners() {
            return winners;
        }
    }
    
    @Serializable
    public static class VelocityChangeMessage extends MyAbstractMessage {
        private Vector2f newVelocity;
        private int diskID;
        
        public VelocityChangeMessage() {
        }
        
        public VelocityChangeMessage(int diskID, Vector2f newVelocity) {
            this.diskID = diskID;
            this.newVelocity = newVelocity;
        }
        
        public int getDiskID() {
            return diskID;
        }
        
        public Vector2f getNewVelocity() {
            return newVelocity;
        }
    }
    
    // The MoveMessage informs us that the sender wishes to move his/her disk in a given direction
    // Direction usage: 0 for 'up', 
    //                  1 for 'down', 
    //                  2 for 'left', 
    //                  3 for 'right', 
    @Serializable
    public static class MoveMessage extends MyAbstractMessage {
        private int direction;
        
        public MoveMessage() {
        }
        
        public MoveMessage(int direction) {
            this.direction = direction;
        }
        
        public int getDirection() {
            return direction;
        }
    }
    
    @Serializable
    public static class PositionChangeMessage extends MyAbstractMessage {
        private Vector2f newPosition;
        private int diskID;
        
        public PositionChangeMessage() {
        }
        
        public PositionChangeMessage(int diskID, Vector2f newPosition) {
            this.diskID = diskID;
            this.newPosition = newPosition;
        }
        
        public int getDiskID() {
            return diskID;
        }
        
        public Vector2f getNewPosition() {
            return newPosition;
        }
    }
    
    @Serializable
    public static class PositionAndVelocityChangeMessage extends MyAbstractMessage {
        private Vector2f newPosition;
        private Vector2f newVelocity;
        private int diskID;
        
        public PositionAndVelocityChangeMessage() {
        }
        
        public PositionAndVelocityChangeMessage( 
                int diskID, 
                Vector2f newPosition, 
                Vector2f newVelocity) {
            this.diskID = diskID;
            this.newPosition = newPosition;
            this.newVelocity = newVelocity;
        }
        
        public int getDiskID() {
            return diskID;
        }
        
        public Vector2f getNewPosition() {
            return newPosition;
        }
        
        public Vector2f getNewVelocity() {
            return newVelocity;
        }
    }
    
    @Serializable
    public static class ScoreChange extends MyAbstractMessage {
        private int newScore;
        private int playerID;
        
        public ScoreChange() {
        }
        
        public ScoreChange(int playerID, int newScore) {
            this.playerID = playerID;
            this.newScore = newScore;
        }
        
        public int getPlayerID() {
            return playerID;
        }
        
        public int getNewScore() {
            return newScore;
        }
    }
    
    /*
    * Use this for periodic updates of player positions
    * These period updates currently send a PlayerLight object (might change)
    */
    @Serializable
    public static class PositionsUpdateMessage extends MyAbstractMessage {
        private ArrayList<PlayerLight> players;
        
        public PositionsUpdateMessage() {
        }
        
        public PositionsUpdateMessage(ArrayList<PlayerLight> players) {
            this.players = players;
        }
        
        public ArrayList<PlayerLight> getPlayers() {
            return players;
        }
    }
    
    /*
    * Use this for periodic updates of player scores
    */
    @Serializable
    public static class ScoreUpdateMessage extends MyAbstractMessage {
        private ArrayList<PlayerLight> players;
        
        public ScoreUpdateMessage() {
        }
        
        public ScoreUpdateMessage(ArrayList<PlayerLight> players) {
            this.players = players;
        }
        
        public ArrayList<PlayerLight> getPlayers() {
            return players;
        }
    }
    
    /*
    * Use this for periodic updates of game time
    */
    @Serializable
    public static class TimeUpdateMessage extends MyAbstractMessage {
        private float time;
        
        public TimeUpdateMessage() {
        }
        
        public TimeUpdateMessage(float time) {
            this.time = time;
        }
        
        public float getTime() {
            return time;
        }
    }
    
}


package network;

import com.jme3.math.Vector2f;
import com.jme3.network.AbstractMessage;
import com.jme3.network.serializing.Serializable;
import com.jme3.network.serializing.Serializer;
import java.util.ArrayList;


public class Util {
    
    public static void initialiseSerializables() {
        Serializer.registerClasses(
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
    
    abstract public static class MyAbstractMessage extends AbstractMessage {

        protected int senderID;

        protected int messageID;
        protected static int globalCounter = 1000;

        public MyAbstractMessage() {
            this.messageID = globalCounter++; // default messageID
        }

        public int getSenderID() {
            return senderID;
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
        private String name;
        private Vector2f position;
        private Vector2f velocity;
        private int score;
        
        public PlayerLight(int id, String name, Vector2f position, Vector2f velocity, int score) {
            this.id = id;
            this.name = name;
            this.position = position;
            this.velocity = velocity;
            this.score = score;
        }
        
        public int getID() {
            return id;
        }
        
        public String getName() {
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
    public static class OpenConnectionMessage extends MyAbstractMessage {
        private String playerName;
        
        public OpenConnectionMessage() {
        }

        public OpenConnectionMessage(int senderID, String playerName) {
            this.senderID = senderID;
            this.playerName = playerName;
        }
        
        public String getPlayerName() {
            return playerName;
        }

    }
    
    @Serializable
    public static class WelcomeClientMessage extends MyAbstractMessage {
        private String playerName;
        
        public WelcomeClientMessage() {
        }
        
        public WelcomeClientMessage(int senderID, String playerName) {
            this.senderID = senderID;
            this.playerName = playerName;
        }
        
        public String getMessage() {
            return playerName + " has connected to the game.";
        }
    }
    
    @Serializable
    public static class NameTakenMessage extends MyAbstractMessage {
        private String playerName;
        
        public NameTakenMessage() {
        }
        
        public NameTakenMessage(int senderID, String playerName) {
            this.senderID = senderID;
            this.playerName = playerName;
        }
        
        public String getMessage() {
            return "Name " + playerName + " is already taken.";
        }
    }
    
    @Serializable
    public static class GameActiveMessage extends MyAbstractMessage {
        
        public GameActiveMessage() {
        }
        
        public GameActiveMessage(int senderID) {
            this.senderID = senderID;
        }
        
        public String getMessage() {
            return "Can't connect, game is currently ongoing.";
        }
    }
    
    @Serializable
    public static class DisconnectMessage extends MyAbstractMessage {
        private String playerName;
        
        public DisconnectMessage() {
        }
        
        public DisconnectMessage(int senderID, String playerName) {
            this.senderID = senderID;
            this.playerName = playerName;
        }
        
        public String getMessage() {
            return playerName + "has left the game.";
        }
    }
    
    @Serializable
    public static class GameSetupMessage extends MyAbstractMessage {
        private ArrayList<PlayerLight> players;
        
        public GameSetupMessage() {
        }
        
        public GameSetupMessage(int senderID, ArrayList<PlayerLight> players) {
            this.senderID = senderID;
            this.players = players;
        }
        
        public ArrayList<PlayerLight> getPlayers() {
            return players;
        }
        
    }
    
    @Serializable
    public static class GameStartMessage extends MyAbstractMessage {
        
        public GameStartMessage() {
        }
        
        public GameStartMessage(int senderID) {
            this.senderID = senderID;
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
        
        public GameOverMessage(int senderID, ArrayList<Integer> winners) {
            this.senderID = senderID;
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
        
        public VelocityChangeMessage(int senderID, int diskID, Vector2f newVelocity) {
            this.senderID = senderID;
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
    
    @Serializable
    public static class PositionChangeMessage extends MyAbstractMessage {
        private Vector2f newPosition;
        private int diskID;
        
        public PositionChangeMessage() {
        }
        
        public PositionChangeMessage(int senderID, int diskID, Vector2f newPosition) {
            this.senderID = senderID;
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
        
        public PositionAndVelocityChangeMessage(int senderID, 
                int diskID, 
                Vector2f newPosition, 
                Vector2f newVelocity) {
            this.senderID = senderID;
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
        
        public ScoreChange(int senderID, int playerID, int newScore) {
            this.senderID = senderID;
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
        
        public PositionsUpdateMessage(int senderID, ArrayList<PlayerLight> players) {
            this.senderID = senderID;
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
        
        public ScoreUpdateMessage(int senderID, ArrayList<PlayerLight> players) {
            this.senderID = senderID;
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
        
        public TimeUpdateMessage(int senderID, float time) {
            this.senderID = senderID;
            this.time = time;
        }
        
        public float getTime() {
            return time;
        }
    }
    
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package network;

import com.jme3.network.Client;
import com.jme3.network.Message;
import com.jme3.network.MessageListener;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

/**
 *
 * @author Quentin
 */
 // This class is a packet handler
public class ClientNetworkMessageListener
            implements MessageListener<Client> {
    
    private Client serverConnection;


    // this method is called whenever network packets arrive
    @Override
    public void messageReceived(Client source, Message m) {
        // these if statements is a clumsy but simple (and working) 
        // solution; better would be to code behavour in the message 
        // classes and call them on the message
        if (m instanceof ChangeMessage) {
            // 1) carry out the change and 2) send back an ack to sender
            ChangeMessage msg = (ChangeMessage) m;
            int originalSender = msg.getSenderID();
            int messageID = msg.getMessageID();
            Util.print("Getting ChangeMessage from "
                    + originalSender
                    + " with messageID "
                    + messageID);
            // Enqueue a callable to main thread that changes box color

            // NB! ALL CHANGES TO THE SCENE GRAPH MUST BE DONE IN THE 
            // MAIN THREAD! THE TECHNIQUE IS TO SEND OVER A PIECE OF CODE 
            // (A "CALLABLE") FROM THE NETWORKING THREAD (THIS THREAD) TO 
            // THE MAIN THREAD (THE ONE WITH THE SCENE GRAPH) AND HAVE 
            // THE MAIN THREAD EXECUTE IT. (This is part of how threads 
            // communicate in Java and NOT something specific to 
            // JMonkeyEngine)

            Future result = TheClient.this.enqueue(new Callable() {
                @Override
                public Object call() throws Exception {
                    return true;
                }
            });

            // Send ack to original sender via the server
            int thisClient = serverConnection.getId();
            serverConnection.send(new AckMessage(originalSender,
                    thisClient, messageID));
            Util.print("Sending AckMessage back via server to "
                    + originalSender
                    + " regarding their messageID "
                    + messageID);
        } else if (m instanceof AckMessage) {
            // no need to do anything
            AckMessage msg = (AckMessage) m;
            Util.print("Getting AckMessage from "
                    + msg.getSenderID()
                    + " regarding my messageID "
                    + msg.getMessageID());
        } else if (m instanceof HeartMessage) {
            // send back an ack to server
            HeartMessage msg = (HeartMessage) m;
            Util.print("Getting HeartMessage from the server "
                    + "- sending HeartAckMessage back");
            serverConnection.
                    send(new HeartAckMessage(serverConnection.getId()));
        } else if (m instanceof HeartAckMessage) {
            // must be a programming error(!)
            throw new RuntimeException("Client got HeartAckMessage "
                    + "- should not be possible!");
        } else {
            // must be a programming error(!)
            throw new RuntimeException("Unknown message.");
        }
    }
}
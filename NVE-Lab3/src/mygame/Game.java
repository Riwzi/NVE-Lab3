/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame;

import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.BaseAppState;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.AnalogListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.math.Vector2f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Sphere;
import disk.*;
import java.util.Collections;
import java.util.List;
import java.util.Arrays;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Random;

/**
 *
 * @author Rickard
 */
public class Game extends BaseAppState {
    
    private SimpleApplication sapp;
    private boolean needCleaning = false;
    
    private Frame Frame;
    
    // thickness of the sides of the frame
    private static final float FRAME_THICKNESS = 24f; 
    // width (and height) of the free area inside the frame, where disks move
    private static final float FREE_AREA_WIDTH = 492f; 
    // total outer width (and height) of the frame
    private static final float FRAME_SIZE = FREE_AREA_WIDTH + 2f * FRAME_THICKNESS; 

    // next three constants define initial positions for disks
    private static final float PLAYER_COORD = FREE_AREA_WIDTH / 6;
    private static final float POSNEG_MAX_COORD = FREE_AREA_WIDTH / 3;
    private static final float POSNEG_BETWEEN_COORD = PLAYER_COORD;

    private static final float PLAYER_R = 20f; // radius of a player's disk
    private static final float POSDISK_R = 16f; // radius of a positive disk
    private static final float NEGDISK_R = 16f; // radius of a negative disk
    private static final int N_POSITIVE = 8; // number of positive disks
    private static final int N_NEGATIVE = 8; // number of negative disks
    private static final int N_PLAYERS = 3; // number of player disks
    
    private static final float START_VELOCITY_MIN = -15;
    private static final float START_VELOCITY_MAX = 15;
    private static final int INITIAL_POSITIVE = 5;
    private static final int INITIAL_NEGATIVE = -3;
    
    private static final Vector2f[] NEGATIVE_POSITIONS = {
        new Vector2f(POSNEG_BETWEEN_COORD,  POSNEG_MAX_COORD),
        new Vector2f(POSNEG_BETWEEN_COORD, -POSNEG_MAX_COORD),
        new Vector2f(-POSNEG_BETWEEN_COORD, POSNEG_MAX_COORD),
        new Vector2f(-POSNEG_BETWEEN_COORD,-POSNEG_MAX_COORD),
        new Vector2f(POSNEG_MAX_COORD,  POSNEG_BETWEEN_COORD),
        new Vector2f(POSNEG_MAX_COORD, -POSNEG_BETWEEN_COORD),
        new Vector2f(-POSNEG_MAX_COORD, POSNEG_BETWEEN_COORD),
        new Vector2f(-POSNEG_MAX_COORD,-POSNEG_BETWEEN_COORD),
    };
    private static final Vector2f[] POSITIVE_POSITIONS = {
        new Vector2f(POSNEG_MAX_COORD,   POSNEG_MAX_COORD),
        new Vector2f(POSNEG_MAX_COORD,  -POSNEG_MAX_COORD),
        new Vector2f(POSNEG_MAX_COORD,   0),
        new Vector2f(-POSNEG_MAX_COORD,  POSNEG_MAX_COORD),
        new Vector2f(-POSNEG_MAX_COORD, -POSNEG_MAX_COORD),
        new Vector2f(-POSNEG_MAX_COORD,  0),
        new Vector2f(0,  POSNEG_MAX_COORD),
        new Vector2f(0,  -POSNEG_MAX_COORD),
    };
    private static final Vector2f[] PLAYER_POSITIONS = {
        new Vector2f(POSNEG_BETWEEN_COORD,  POSNEG_BETWEEN_COORD),
        new Vector2f(POSNEG_BETWEEN_COORD,  -POSNEG_BETWEEN_COORD),
        new Vector2f(POSNEG_BETWEEN_COORD,  0),
        new Vector2f(-POSNEG_BETWEEN_COORD, POSNEG_BETWEEN_COORD),
        new Vector2f(-POSNEG_BETWEEN_COORD, -POSNEG_BETWEEN_COORD),
        new Vector2f(-POSNEG_BETWEEN_COORD, 0),
        new Vector2f(0, POSNEG_BETWEEN_COORD),
        new Vector2f(0, -POSNEG_BETWEEN_COORD),
        new Vector2f(0, 0)
    };
    
    private static final int[][] KEY_BINDINGS = new int[][] {
        {KeyInput.KEY_A, KeyInput.KEY_S, KeyInput.KEY_D, KeyInput.KEY_W},
        {KeyInput.KEY_G, KeyInput.KEY_H, KeyInput.KEY_J, KeyInput.KEY_Y},
        {KeyInput.KEY_LEFT, KeyInput.KEY_DOWN, KeyInput.KEY_RIGHT, KeyInput.KEY_UP}
    };
    
    
    private final float acceleration = 70f;
    private final float friction = 4f;
    
    private int nextID = 0;
    
    private static final float START_TIME = 30f;
    private static float time = 0;
    
    private BitmapText hudText;
    
    private ArrayList<String> mappings;
    private ArrayList<Disk> diskStore;
    
    //A hash table mapping player id's to Player instances. Used to allow any number of players
    private HashMap<String, Player> playerMap;
    private Map<Integer, String> playerName;
    
    @Override
    protected void initialize(Application app) {
        System.out.println("Game: initialize");
        sapp = (SimpleApplication) app;
    }
   
    @Override
    protected void cleanup(Application app) {
        System.out.println("Game: cleanup");
    }
    
    @Override
    public void onEnable() {
        System.out.println("Game: onEnable");
        if (needCleaning) {
            System.out.println("Game: Cleaning up");
            time = 0;
            sapp.getRootNode().detachAllChildren();
            needCleaning = false;
        }
        
       game.startGame();

        diskStore = new ArrayList();
        //Set up HUD
        initHud();
        //Set up camera
        initCam();
        //Set up frame
        initFrame();
        //Set up players
        initPlayers();
        //Set up disks
        initDisks();
    }
    
    @Override
    public void onDisable() {
        System.out.println("Game: onDisable");
        for (String map: mappings) {
            sapp.getInputManager().deleteMapping(map);
        }
        needCleaning = true;
    }
    
    private void initHud() {
        BitmapFont myFont = sapp.getAssetManager().loadFont("Interface/Fonts/Console.fnt");
        hudText = new BitmapText(myFont, false);
        hudText.setSize(myFont.getCharSet().getRenderedSize() * 3);
        hudText.setColor(ColorRGBA.White);
        hudText.setLocalTranslation(60, sapp.getContext().getSettings().getHeight()-hudText.getLineHeight(), 0);
        sapp.getGuiNode().attachChild(hudText);
    }
    
    private void initCam() {
        sapp.getCamera().setFrustumFar(1000);
        sapp.getCamera().onFrameChange();
        sapp.getCamera().setLocation(new Vector3f(-84f, 0.0f, 720f));
        sapp.getCamera().setRotation(new Quaternion(0.0f, 1.0f, 0.0f, 0.0f));
        sapp.getFlyByCamera().setMoveSpeed(0);
        sapp.getFlyByCamera().setRotationSpeed(0);
    }
    
    private void initFrame() {
        this.Frame = new Frame(sapp.getAssetManager(), FRAME_THICKNESS, FREE_AREA_WIDTH, FRAME_SIZE);
        this.Frame.init();
        sapp.getRootNode().attachChild(Frame);
    }
    
    private void initPlayers() {
        List<Vector2f> list = Arrays.asList(PLAYER_POSITIONS);
        Collections.shuffle(list);
        mappings = new ArrayList();
        playerMap = new HashMap();
        playerName = new HashMap<>();
        playerName.put(1, "I");
        playerName.put(2, "II");
        playerName.put(3, "III");
        playerName.put(4, "IV");
        playerName.put(5, "V");
        playerName.put(6, "VI");
        playerName.put(7, "VII");
        playerName.put(8, "VIII");
        playerName.put(9, "IX");
        for (int i = 0; i<N_PLAYERS; i++) {
            addLocalPlayer(i, list.get(i));
        }
        /*
        for (int i = N_PLAYERS; i<9; i++) {
            addPlayer(i, list.get(i));
        }
        */
    }
    
    private void initDisks() {
        Random r = new Random();
        for (int i = 0; i<N_NEGATIVE; i++) {
            Vector2f velocity = randomInitialVelocity(r);
            addNegative(NEGATIVE_POSITIONS[i], velocity);
        }
        for (int i = 0; i<N_POSITIVE; i++) {
            Vector2f velocity = randomInitialVelocity(r);
            addPositive(POSITIVE_POSITIONS[i], velocity);
        }
    }
    
    // Returns a Vector2f whose values are are between START_VELOCITY_MIN and START_VELOCITY_MAX
    private Vector2f randomInitialVelocity(Random r) {
        float x = r.nextFloat() * (START_VELOCITY_MAX - START_VELOCITY_MIN) + START_VELOCITY_MIN;
        float y = r.nextFloat() * (START_VELOCITY_MAX - START_VELOCITY_MIN) + START_VELOCITY_MIN;
        return new Vector2f(x,y);
    }
    
    // Adds a negative disk to the game at the given position
    private void addNegative(Vector2f position, Vector2f velocity) {
        NegativeDisk negative = new NegativeDisk(sapp.getAssetManager(), NEGDISK_R, getNextID(), INITIAL_NEGATIVE);
        Geometry negativeGeometry = negative.createGeometry(NEGDISK_R, FRAME_THICKNESS);
        sapp.getRootNode().attachChild(negative);
        negative.attachChild(negativeGeometry);
        negative.move(new Vector3f(0f, 0f, -FRAME_THICKNESS));
        
        diskStore.add(negative);
        
        negative.move(position);
        negative.setVelocity(velocity);
    }
    
    // Adds a positive disk to the game at the given position
    private void addPositive(Vector2f position, Vector2f velocity) {
        PositiveDisk positive = new PositiveDisk(sapp.getAssetManager(), POSDISK_R, getNextID(), INITIAL_POSITIVE);
        Geometry positiveGeometry = positive.createGeometry(POSDISK_R, FRAME_THICKNESS);
        sapp.getRootNode().attachChild(positive);
        positive.attachChild(positiveGeometry);
        
        //Setup value markers on the disk
        float MarkerSize = 3;
        Sphere c = new Sphere(5, 5, MarkerSize);
        Material mat = new Material(sapp.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setColor("Color", ColorRGBA.White);
        Geometry marker5 = new Geometry("Marker", c);
        Geometry marker4 = new Geometry("Marker", c);
        Geometry marker3 = new Geometry("Marker", c);
        Geometry marker2 = new Geometry("Marker", c);
        Geometry marker1 = new Geometry("Marker", c);
        marker5.setMaterial(mat);
        marker4.setMaterial(mat);
        marker3.setMaterial(mat);
        marker2.setMaterial(mat);
        marker1.setMaterial(mat);
        
        positive.attachChild(marker5);
        positive.attachChild(marker4);
        positive.attachChild(marker3);
        positive.attachChild(marker2);
        positive.attachChild(marker1);
        
        float MarkerDistance = MarkerSize * 2;
        marker5.setLocalTranslation(0, 0, FRAME_THICKNESS/2);
        marker4.setLocalTranslation(MarkerDistance, MarkerDistance, FRAME_THICKNESS/2);
        marker3.setLocalTranslation(MarkerDistance, -MarkerDistance, FRAME_THICKNESS/2);
        marker2.setLocalTranslation(-MarkerDistance, MarkerDistance, FRAME_THICKNESS/2);
        marker1.setLocalTranslation(-MarkerDistance, -MarkerDistance, FRAME_THICKNESS/2);
        
        positive.move(new Vector3f(0f, 0f, -FRAME_THICKNESS));        
        diskStore.add(positive);
        
        positive.move(position);
        positive.setVelocity(velocity);
    }
    
    //Adds a local player to the Game. A local player has KeyInputs
    private void addLocalPlayer(int player_id, Vector2f position) {
        String name = playerName.get(player_id+1);
        String disk_id = getNextID();
        Player player = new Player(sapp.getAssetManager(), PLAYER_R, disk_id, name);
        Geometry playerGeometry = player.createGeometry(PLAYER_R, FRAME_THICKNESS);
        sapp.getRootNode().attachChild(player);
        player.attachChild(playerGeometry);
        player.attachChild(createDescription(player_id+1));
        player.move(new Vector3f(0f, 0f, -FRAME_THICKNESS));
        
        diskStore.add(player);
        playerMap.put(disk_id, player);
        
        player.move(position);
        
        String mapU = "U:"+disk_id;
        String mapD = "D:"+disk_id;
        String mapL = "L:"+disk_id;
        String mapR = "R:"+disk_id;
        this.mappings.add(mapU);
        this.mappings.add(mapD);
        this.mappings.add(mapL);
        this.mappings.add(mapR);
        
        sapp.getInputManager().addMapping(mapL, new KeyTrigger(KEY_BINDINGS[player_id][0]));
        sapp.getInputManager().addMapping(mapD, new KeyTrigger(KEY_BINDINGS[player_id][1]));
        sapp.getInputManager().addMapping(mapR, new KeyTrigger(KEY_BINDINGS[player_id][2]));
        sapp.getInputManager().addMapping(mapU, new KeyTrigger(KEY_BINDINGS[player_id][3]));
        sapp.getInputManager().addListener(analogListener, mapU, mapD, mapL, mapR);
    }
    
    //Adds a non-local player (no keyboard inputs)
    private void addPlayer(int player_id, Vector2f position) {
        String name = playerName.get(player_id+1);
        String disk_id = getNextID();
        Player player = new Player(sapp.getAssetManager(), PLAYER_R, disk_id, name);
        Geometry playerGeometry = player.createGeometry(PLAYER_R, FRAME_THICKNESS);
        sapp.getRootNode().attachChild(player);
        player.attachChild(playerGeometry);
        player.attachChild(createDescription(player_id+1));
        player.move(new Vector3f(0f, 0f, -FRAME_THICKNESS));
        
        diskStore.add(player);
        playerMap.put(disk_id, player);
        
        player.move(position);
    }
    
    private String getNextID() {
        return Integer.toString(this.nextID++);
    }
    
    // Returns the time left until the game is over
    public static float getRemainingTime() {
        float remaining = START_TIME - time;
        if (remaining < 0f) {
            return 0f;
        } else {
            return START_TIME - time;
        }
    }
    
    private AnalogListener analogListener = new AnalogListener() {
        public void onAnalog(String name, float value, float tpf) {
            String sub = name.substring(0, 2);
            if (sub.equals("U:")) {
                Player player = playerMap.get(name.substring(2));
                player.addVelocity(new Vector2f(0,acceleration*tpf));
            }
            if (sub.equals("D:")) {
                Player player = playerMap.get(name.substring(2));
                player.addVelocity(new Vector2f(0,-acceleration*tpf));
            }
            if (sub.equals("L:")) {
                Player player = playerMap.get(name.substring(2));
                player.addVelocity(new Vector2f(-acceleration*tpf,0));
            }
            if (sub.equals("R:")) {
                Player player = playerMap.get(name.substring(2));
                player.addVelocity(new Vector2f(acceleration*tpf,0));
            }
        }
    };

    @Override
    public void update(float tpf) {
        time += tpf;
        String text = "Time: " + getRemainingTime() + "\n";
        
        //Apply change for data
        
        
        for (Disk d: diskStore) {
            //Move the disk
            d.move(d.getVelocity().mult(tpf));
            
            //Apply friction
            float friction_tpf = friction * tpf;
            Vector2f velocity = d.getVelocity();
            float newX;
            float newY;
            float oldX = velocity.getX();
            float oldY = velocity.getY();
            if (oldX < -friction_tpf) {
                newX = oldX + friction_tpf;
            } else if (oldX > friction_tpf) {
                newX = oldX - friction_tpf;
            } else {
                newX = 0;
            }
            if (oldY < -friction_tpf) {
                newY = oldY + friction_tpf;
            } else if (oldY > friction_tpf) {
                newY = oldY - friction_tpf;
            } else {
                newY = 0;
            }
            d.setVelocity(new Vector2f(newX, newY));
            
           /* //Collision detection with frame
            float boundary = FREE_AREA_WIDTH/2;
            d.frameCollision(-boundary, boundary, -boundary, boundary, tpf);
            
            //Collision detection with other disks
            for (Disk otherDisk: diskStore) {
                if (!d.getId().equals(otherDisk.getId())) {
                    d.diskCollision(otherDisk, tpf);
                }
            }
            
            if (d instanceof Player) {
                text += ((Player) d).getName() + ": " + d.getScore() + "\n";
            }*/
        }
        hudText.setText(text);
    }
    
    // Returns a node with geometry "stripes" (boxes) in the form of Roman numbers
    private Node createDescription(int number) {
        Node description = new Node("Description");
        float stripe_length = 8;
        float stripe_width = 2;
        float stripe_thickness = 2;
        
        if (number == 1) {
            Geometry stripe1 = getStripe(stripe_length, stripe_width, stripe_thickness);
            description.attachChild(stripe1);
            stripe1.setLocalTranslation(0f, 0f, FRAME_THICKNESS/2);
        }
        if (number == 2) {
            Geometry stripe1 = getStripe(stripe_length, stripe_width, stripe_thickness);
            Geometry stripe2 = getStripe(stripe_length, stripe_width, stripe_thickness);
            
            description.attachChild(stripe1);
            description.attachChild(stripe2);
            
            stripe1.setLocalTranslation(stripe_width*2, 0f, FRAME_THICKNESS/2);
            stripe2.setLocalTranslation(-stripe_width*2, 0f, FRAME_THICKNESS/2);
        }
        if (number == 3) {
            Geometry stripe1 = getStripe(stripe_length, stripe_width, stripe_thickness);
            Geometry stripe2 = getStripe(stripe_length, stripe_width, stripe_thickness);
            Geometry stripe3 = getStripe(stripe_length, stripe_width, stripe_thickness);
            
            description.attachChild(stripe1);
            description.attachChild(stripe2);
            description.attachChild(stripe3);
            
            stripe1.setLocalTranslation(0f, 0f, FRAME_THICKNESS/2);
            stripe2.setLocalTranslation(stripe_width*3, 0f, FRAME_THICKNESS/2);
            stripe3.setLocalTranslation(-stripe_width*3, 0f, FRAME_THICKNESS/2);
        }
        if (number == 4) {
            Geometry stripe1 = getStripe(stripe_length, stripe_width, stripe_thickness);
            Geometry stripe2 = getStripe(stripe_length, stripe_width, stripe_thickness);
            Geometry stripe3 = getStripe(stripe_length, stripe_width, stripe_thickness);
            
            description.attachChild(stripe1);
            description.attachChild(stripe2);
            description.attachChild(stripe3);
            
            stripe1.setLocalTranslation(0f, 0f, FRAME_THICKNESS/2);
            stripe2.setLocalTranslation(stripe_width*3, 0f, FRAME_THICKNESS/2);
            stripe3.setLocalTranslation(-stripe_width*3, 0f, FRAME_THICKNESS/2);
            
            stripe1.rotate(0f, 0f, 20*FastMath.DEG_TO_RAD);
            stripe2.rotate(0f, 0f, -20*FastMath.DEG_TO_RAD);
        }
        if (number == 5) {
            Geometry stripe1 = getStripe(stripe_length, stripe_width, stripe_thickness);
            Geometry stripe2 = getStripe(stripe_length, stripe_width, stripe_thickness);
            
            description.attachChild(stripe1);
            description.attachChild(stripe2);
            
            stripe1.setLocalTranslation(stripe_width*1.5f, 0f, FRAME_THICKNESS/2);
            stripe2.setLocalTranslation(-stripe_width*1.5f, 0f, FRAME_THICKNESS/2);
            
            stripe1.rotate(0f, 0f, -20*FastMath.DEG_TO_RAD);
            stripe2.rotate(0f, 0f, 20*FastMath.DEG_TO_RAD);
        }
        if (number == 6) {
            Geometry stripe1 = getStripe(stripe_length, stripe_width, stripe_thickness);
            Geometry stripe2 = getStripe(stripe_length, stripe_width, stripe_thickness);
            Geometry stripe3 = getStripe(stripe_length, stripe_width, stripe_thickness);
            
            description.attachChild(stripe1);
            description.attachChild(stripe2);
            description.attachChild(stripe3);
            
            stripe1.setLocalTranslation(0f, 0f, FRAME_THICKNESS/2);
            stripe2.setLocalTranslation(stripe_width*3, 0f, FRAME_THICKNESS/2);
            stripe3.setLocalTranslation(-stripe_width*3, 0f, FRAME_THICKNESS/2);
            
            stripe1.rotate(0f, 0f, -20*FastMath.DEG_TO_RAD);
            stripe3.rotate(0f, 0f, 20*FastMath.DEG_TO_RAD);
        }
        if (number == 7) {
            Geometry stripe1 = getStripe(stripe_length, stripe_width, stripe_thickness);
            Geometry stripe2 = getStripe(stripe_length, stripe_width, stripe_thickness);
            Geometry stripe3 = getStripe(stripe_length, stripe_width, stripe_thickness);
            Geometry stripe4 = getStripe(stripe_length, stripe_width, stripe_thickness);
            
            description.attachChild(stripe1);
            description.attachChild(stripe2);
            description.attachChild(stripe3);
            description.attachChild(stripe4);
            
            stripe1.setLocalTranslation(-stripe_width*1, 0f, FRAME_THICKNESS/2);
            stripe2.setLocalTranslation(stripe_width*2, 0f, FRAME_THICKNESS/2);
            stripe3.setLocalTranslation(-stripe_width*4, 0f, FRAME_THICKNESS/2);
            stripe4.setLocalTranslation(stripe_width*5, 0f, FRAME_THICKNESS/2);
            
            stripe1.rotate(0f, 0f, -20*FastMath.DEG_TO_RAD);
            stripe3.rotate(0f, 0f, 20*FastMath.DEG_TO_RAD);
        }
        if (number == 8) {
            Geometry stripe1 = getStripe(stripe_length, stripe_width, stripe_thickness);
            Geometry stripe2 = getStripe(stripe_length, stripe_width, stripe_thickness);
            Geometry stripe3 = getStripe(stripe_length, stripe_width, stripe_thickness);
            Geometry stripe4 = getStripe(stripe_length, stripe_width, stripe_thickness);
            Geometry stripe5 = getStripe(stripe_length, stripe_width, stripe_thickness);
            
            description.attachChild(stripe1);
            description.attachChild(stripe2);
            description.attachChild(stripe3);
            description.attachChild(stripe4);
            description.attachChild(stripe5);
            
            stripe1.setLocalTranslation(-stripe_width*3, 0f, FRAME_THICKNESS/2);
            stripe2.setLocalTranslation(stripe_width*0, 0f, FRAME_THICKNESS/2);
            stripe3.setLocalTranslation(-stripe_width*6, 0f, FRAME_THICKNESS/2);
            stripe4.setLocalTranslation(stripe_width*3, 0f, FRAME_THICKNESS/2);
            stripe5.setLocalTranslation(stripe_width*6, 0f, FRAME_THICKNESS/2);
            
            stripe1.rotate(0f, 0f, -20*FastMath.DEG_TO_RAD);
            stripe3.rotate(0f, 0f, 20*FastMath.DEG_TO_RAD);
        }
        if (number == 9) {
            Geometry stripe1 = getStripe(stripe_length, stripe_width, stripe_thickness);
            Geometry stripe2 = getStripe(stripe_length*1.1f, stripe_width, stripe_thickness);
            Geometry stripe3 = getStripe(stripe_length*1.1f, stripe_width, stripe_thickness);
            
            description.attachChild(stripe1);
            description.attachChild(stripe2);
            description.attachChild(stripe3);
            
            stripe1.setLocalTranslation(-stripe_width*3, 0f, FRAME_THICKNESS/2);
            stripe2.setLocalTranslation(stripe_width*2, 0f, FRAME_THICKNESS/2);
            stripe3.setLocalTranslation(stripe_width*2, 0f, FRAME_THICKNESS/2);
            
            stripe2.rotate(0f, 0f, -45*FastMath.DEG_TO_RAD);
            stripe3.rotate(0f, 0f, 45*FastMath.DEG_TO_RAD);
        }
        return description;
    }
    
    private Geometry getStripe(float length, float width, float thickness) {
        Box s = new Box(width, length, thickness);
        Material mat = new Material(sapp.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setColor("Color", ColorRGBA.White);
        Geometry stripe = new Geometry("Stripe", s);
        stripe.setMaterial(mat);
        return stripe;
    }
}

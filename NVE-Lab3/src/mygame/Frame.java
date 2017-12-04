package mygame;

import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.asset.AssetManager;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Box;
/**
 * 
 * Class for the frame of the gamearea
 *
 * @author Rickard
 * implementation
 */
public class Frame extends Node {
    AssetManager assetManager;
    
    private float FRAME_THICKNESS;
    private float FREE_AREA_SIZE;
    private float FRAME_SIZE;
    
    public Frame(AssetManager assetManager, float thickness, float freeArea, float size) {
        this.assetManager = assetManager;
        this.FRAME_THICKNESS = thickness;
        this.FREE_AREA_SIZE = freeArea;
        this.FRAME_SIZE = size;
    }
    
    //Constructs the frame and attaches it to the Frame node
    public void init() {
        // Note that the boxes actually are 2*SIZE long/wide, since they expand in both directions. So we cut the size in half
        String horizontalName = "FRAME_HORIZONTAL";
        Box horizontal = new Box(FREE_AREA_SIZE/2 + FRAME_THICKNESS, FRAME_THICKNESS/2, FRAME_THICKNESS/2);
        Geometry northWall = new Geometry(horizontalName, horizontal);
        Geometry southWall = new Geometry(horizontalName, horizontal);
        String verticalName = "FRAME_VERTICAL";
        Box vertical = new Box(FRAME_THICKNESS/2, FREE_AREA_SIZE/2 + FRAME_THICKNESS, FRAME_THICKNESS/2);
        Geometry westWall = new Geometry(verticalName, vertical);
        Geometry eastWall = new Geometry(verticalName, vertical);
        
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setColor("Color", ColorRGBA.Brown);
        northWall.setMaterial(mat);
        southWall.setMaterial(mat);
        westWall.setMaterial(mat);
        eastWall.setMaterial(mat);
        
        this.attachChild(northWall);
        this.attachChild(southWall);
        this.attachChild(westWall);
        this.attachChild(eastWall);
        
        northWall.setLocalTranslation(0, FREE_AREA_SIZE/2 + FRAME_THICKNESS/2, 0);
        southWall.setLocalTranslation(0, -FREE_AREA_SIZE/2 - FRAME_THICKNESS/2 ,0);
        westWall.setLocalTranslation(-FREE_AREA_SIZE/2 - FRAME_THICKNESS/2, 0, 0);
        eastWall.setLocalTranslation(FREE_AREA_SIZE/2 + FRAME_THICKNESS/2, 0, 0);
        
        // The floor beneath the frame
        Box f = new Box(FREE_AREA_SIZE/2 + FRAME_THICKNESS, FREE_AREA_SIZE/2 + FRAME_THICKNESS, 1);
        Geometry floor = new Geometry("Floor", f);
        Material floorMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        floorMat.setColor("Color", ColorRGBA.White);
        floor.setMaterial(floorMat);
        this.attachChild(floor);
        floor.setLocalTranslation(0, 0, -FRAME_THICKNESS);
        
    }
}

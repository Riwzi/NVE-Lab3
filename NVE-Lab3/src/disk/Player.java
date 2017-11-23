package disk;

import com.jme3.math.ColorRGBA;
import com.jme3.scene.Geometry;
import com.jme3.asset.AssetManager;

/**
 *
 * @author Rickard
 */
public class Player extends Disk{
    private String name;
    
    public Player(AssetManager assetManager, float radius, String id, String name) {
        super(assetManager, radius, id);
        this.name = name;
    }
    
    public Geometry createGeometry(float radius, float height, ColorRGBA color) {
        Geometry disk = super.createGeometry(radius, height, color);
        return disk;
    }
    
    @Override
    public int reward(Disk otherDisk) {
            return 0;
    }
    
    public String getName() {
        return this.name;
    }
}

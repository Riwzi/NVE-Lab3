package disk;

import com.jme3.math.ColorRGBA;
import com.jme3.scene.Geometry;
import com.jme3.asset.AssetManager;

/**
 * Class for negative disks
 * 
 * 
 * @author Rickard
 * implemented class
 */
public class NegativeDisk extends Disk {
    private int value;
    
    public NegativeDisk(AssetManager assetManager, float radius, int id, int initialValue) {
        super(assetManager, radius, id);
        this.value = initialValue;
    }
    
    public Geometry createGeometry(float radius, float height) {
        Geometry disk = super.createGeometry(radius, height, ColorRGBA.Red);
        return disk;
    }
    
    @Override
    public int reward(Disk d) {
        if (d instanceof Player) {
            return value;
        } else {
            return 0;
        }
    }
}

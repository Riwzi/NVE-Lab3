package disk;

import com.jme3.math.ColorRGBA;
import com.jme3.scene.Geometry;
import com.jme3.asset.AssetManager;
import com.jme3.scene.Spatial;

/**
 *
 * @author Rickard
 */
public class PositiveDisk extends Disk {
    private int value;
    
    public PositiveDisk(AssetManager assetManager, float radius, int id, int initialValue) {
        super(assetManager, radius, id);
        this.value = initialValue;
    }
    
    public Geometry createGeometry(float radius, float height) {
        Geometry disk = super.createGeometry(radius, height, ColorRGBA.Green);
        return disk;
    }
    
    @Override
    public int reward(Disk d) {
        if (d instanceof Player) {
            int reward = this.value;
            if (this.value >= 1) {
                this.value -= 1;
                removeMarker();
            }
            return reward;
        } else {
            return 0;
        }
    }
    
    public void removeMarker() {
        Spatial marker = this.getChild("Marker");
        if (marker != null) {
            this.detachChild(marker);
        }
    }
}

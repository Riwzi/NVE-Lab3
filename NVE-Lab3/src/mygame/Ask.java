package mygame;

import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.BaseAppState;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.math.ColorRGBA;

/**
 *
 * @author Rickard
 */
public class Ask extends BaseAppState {
    
    private SimpleApplication sapp;
    
    @Override
    public void initialize(Application app) {
        System.out.println("Ask: initialize");
        sapp = (SimpleApplication) app;
    }
    
    @Override
    protected void cleanup(Application app) {
        System.out.println("Ask: cleanup");
    }
    
    @Override
    protected void onEnable() {
        System.out.println("Ask: onEnable");
        BitmapFont myFont = sapp.getAssetManager().loadFont("Interface/Fonts/Console.fnt");
        BitmapText hudText = new BitmapText(myFont, false);
        hudText.setSize(myFont.getCharSet().getRenderedSize() * 3);
        hudText.setColor(ColorRGBA.White);
        hudText.setText("PRESS P TO START A NEW GAME AND\nE TO EXIT");
        hudText.setLocalTranslation(60, hudText.getLineHeight()*10, 0);
        sapp.getGuiNode().attachChild(hudText);
    }
    
    @Override
    protected void onDisable() {
        System.out.println("Ask: onDisable (user pressed P)");
        sapp.getGuiNode().detachAllChildren();
    }
    
}

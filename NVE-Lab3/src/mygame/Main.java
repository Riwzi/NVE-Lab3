package mygame;

import com.jme3.app.SimpleApplication;
import com.jme3.renderer.RenderManager;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;

public class Main extends SimpleApplication {
    
    private Ask ask = new Ask();
    private Game game = new Game();
    private float time = 0f;
    private boolean running = true;
    
    public Main() {
        ask.setEnabled(false);
        game.setEnabled(true);
        stateManager.attach(game);
        stateManager.attach(ask);
    }
    
    public static void main(String[] args) {
        Main app = new Main();
        app.start();
    }

    @Override
    public void simpleInitApp() {
        setDisplayStatView(false);
        setDisplayFps(false);
    }
    
    private ActionListener actionListener = new ActionListener() {
        @Override
        public void onAction(String name, boolean isPressed, float tpf) {
            if (isPressed) {
                if (name.equals("Exit")) {
                    Main.this.stop();
                } else if (name.equals("Restart")) {
                    ask.setEnabled(false);
                    game.setEnabled(true);
                    running = true;
                    inputManager.deleteMapping("Restart");
                    inputManager.deleteMapping("Exit");
                }
            }
        }
    };

    @Override
    public void simpleUpdate(float tpf) {
        if (running) {
            if (Game.getRemainingTime() <= 0) {
                game.setEnabled(false);
                inputManager.addMapping("Restart", new KeyTrigger(KeyInput.KEY_P));
                inputManager.addMapping("Exit", new KeyTrigger(KeyInput.KEY_E));
                inputManager.addListener(actionListener, "Restart", "Exit");
                ask.setEnabled(true);
                time = 0f;
                running = false;
            }
        }
    }

    @Override
    public void simpleRender(RenderManager rm) {
        //TODO: add render code
    }
}

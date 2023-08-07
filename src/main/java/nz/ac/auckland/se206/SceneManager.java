package nz.ac.auckland.se206;

import java.util.HashMap;

import javafx.scene.Parent;

public class SceneManager {
    
    public enum AppUi {
        CHAT, ROOM, END
    }

    private static HashMap<AppUi, Parent> sceneMap = new HashMap<AppUi, Parent>();

    public static void addScene(AppUi appUi, Parent uiRoot) {
        sceneMap.put(appUi, uiRoot);
    }

    public static Parent getUiRoot(AppUi appUi) {
        return sceneMap.get(appUi);
    }
}

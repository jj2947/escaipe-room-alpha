package nz.ac.auckland.se206.controllers;

import java.io.IOException;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import nz.ac.auckland.se206.GameState;
import nz.ac.auckland.se206.SceneManager;
import nz.ac.auckland.se206.SceneManager.AppUi;
import nz.ac.auckland.se206.gpt.openai.ApiProxyException;

public class EndController {
  @FXML private Button restartButton;

  @FXML
  public void initialize() throws ApiProxyException {}

  @FXML
  private void clickRestart(ActionEvent event) throws ApiProxyException, IOException {
    GameState.isGameStarted = false;
    GameState.isRiddleResolved = false;
    GameState.isKeyFound = false;
    GameState.isTimeReached = false;
    Button button = (Button) event.getSource();
    Scene sceneButtonIsIn = button.getScene();
    sceneButtonIsIn.setRoot(SceneManager.getUiRoot(AppUi.ROOM));
  }
}

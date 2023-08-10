package nz.ac.auckland.se206.controllers;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import nz.ac.auckland.se206.GameState;
import nz.ac.auckland.se206.SceneManager;
import nz.ac.auckland.se206.SceneManager.AppUi;
import nz.ac.auckland.se206.gpt.openai.ChatCompletionRequest;
import nz.ac.auckland.se206.speech.TextToSpeech;

public class LivingRoomController {

  @FXML private Button hintButton;
  @FXML private Button backButton;
  @FXML private Label timerLabel;
  @FXML private Label chatLabel;
  @FXML private Label codeLabel;
  private TextToSpeech textToSpeech;
  private ChatCompletionRequest chatCompletionRequest;
  private Timer timer;

  public void initialize() {
    // Initialization code goes here
    chatCompletionRequest =
        new ChatCompletionRequest().setN(1).setTemperature(1.5).setTopP(1).setMaxTokens(25);

    textToSpeech = new TextToSpeech();
    timer = GameState.timer;

    // Update the timer label every second
    Task<Void> updateLabelTask =
        new Task<Void>() {
          @Override
          protected Void call() throws Exception {
            while (!GameState.isTimeReached) {
              Thread.sleep(1000); // Wait for 1 second
              Platform.runLater(() -> updateLabel());
            }
            return null;
          }
        };

    // Create a new thread for the update task and start it
    Thread updateThread = new Thread(updateLabelTask);
    updateThread.setDaemon(true);
    updateThread.start();
  }
  

  @FXML
  public void onHint(ActionEvent event) {
    // Hint button action code goes here
    
  }

  @FXML
  public void onBack(ActionEvent event) {
    // Back button action code goes here
    Button button = (Button) event.getSource();
    Scene sceneButtonIsIn = button.getScene();
    GameState.isInRoom = true;
    sceneButtonIsIn.setRoot(SceneManager.getUiRoot(AppUi.ROOM));
    sceneButtonIsIn.getWindow().sizeToScene();
  }

  /**
   * Handles the click event on the controller.
   *
   * @param event the mouse event
   */
  @FXML
  public void clickRocket(MouseEvent event) {
    System.out.println("controller clicked");

    String message = "Remember this code!";
    Task<Void> speakTask =
        new Task<Void>() {
          @Override
          protected Void call() throws Exception {
            textToSpeech.speak(message);
            return null;
          }
        };
    Thread speakThread = new Thread(speakTask);
    speakThread.setDaemon(true);
    speakThread.start();
    chatLabel.setText(message);
    GameState.Code = "4 9 1 6";
    codeLabel.setText("4916");
    GameState.isKeyFound = true;
    speakThread.interrupt();
  }

  private void updateLabel() {
    timerLabel.setText(
        String.format("%02d:%02d", timer.getCounter() / 60, timer.getCounter() % 60));

    if (GameState.isTimeReached) {
      // Timer has reached zero, switch to the desired scene
      switchToGameOverScene();
    }
  }


private void switchToGameOverScene() {
    textToSpeech.terminate();

    Platform.runLater(
        () -> {
          Scene currentScene = timerLabel.getScene();
          if (currentScene != null) {
            currentScene.setRoot(SceneManager.getUiRoot(AppUi.LOST));
            chatLabel.setText("");
          }
        });
  }
}

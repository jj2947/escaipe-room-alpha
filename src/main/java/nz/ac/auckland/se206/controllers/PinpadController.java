package nz.ac.auckland.se206.controllers;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import nz.ac.auckland.se206.GameState;
import nz.ac.auckland.se206.SceneManager;
import nz.ac.auckland.se206.SceneManager.AppUi;
import nz.ac.auckland.se206.gpt.openai.ChatCompletionRequest;

public class PinpadController {

  @FXML private Button oneButton;
  @FXML private Button twoButton;
  @FXML private Button threeButton;
  @FXML private Button fourButton;
  @FXML private Button fiveButton;
  @FXML private Button sixButton;
  @FXML private Button sevenButton;
  @FXML private Button eightButton;
  @FXML private Button nineButton;
  @FXML private Button enterButton;
  @FXML private TextField pinpadTextField;
  @FXML private Label timerLabel3;
  private Timer timer;
  private ChatCompletionRequest chatCompletionRequest;

  /** Initializes the room view, it is called when the room loads. */
  public void initialize() {
    // Initialization code goes here
    chatCompletionRequest =
        new ChatCompletionRequest().setN(1).setTemperature(1).setTopP(1).setMaxTokens(25);
    // Start the timer
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

    // Set up and start the chatLabel task
    Task<Void> chatTask =
        new Task<Void>() {
          @Override
          protected Void call() throws Exception {
            int currentSentenceIndex = 0;
            while (!Thread.currentThread().isInterrupted()) {
              String sentence = chatSentences[currentSentenceIndex];

              Platform.runLater(
                  () -> {
                    pinpadTextField.setText(sentence);
                  });

              if (currentSentenceIndex < chatSentences.length - 1) {
                currentSentenceIndex++;
                Thread.sleep(3 * 1000);
              } else {
                Thread.currentThread().interrupt();
              }
            }
            return null;
          }
        };

    Thread chatThread = new Thread(chatTask);
    chatThread.setDaemon(true);
    chatThread.start();
  }

  private void updateLabel() {
    timerLabel3.setText(
        String.format("%02d:%02d", timer.getCounter() / 60, timer.getCounter() % 60));

    if (GameState.isTimeReached) {
      // Timer has reached zero, switch to the desired scene
      switchToGameOverScene();
    }
  }

  private void switchToGameOverScene() {
    Scene currentScene = timerLabel3.getScene();
    Platform.runLater(
        () -> {
          currentScene.setRoot(SceneManager.getUiRoot(AppUi.LOST));
        });
  }

  private String[] chatSentences = {
    "Enter the code to escape the room.",
    "If you get stuck, use the Help button!",
    ""
  };
}

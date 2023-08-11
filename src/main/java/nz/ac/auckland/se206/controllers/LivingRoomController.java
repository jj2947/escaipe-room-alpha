package nz.ac.auckland.se206.controllers;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.shape.Rectangle;
import nz.ac.auckland.se206.GameState;
import nz.ac.auckland.se206.SceneManager;
import nz.ac.auckland.se206.SceneManager.AppUi;
import nz.ac.auckland.se206.gpt.ChatMessage;
import nz.ac.auckland.se206.gpt.GptPromptEngineering;
import nz.ac.auckland.se206.gpt.openai.ApiProxyException;
import nz.ac.auckland.se206.gpt.openai.ChatCompletionRequest;
import nz.ac.auckland.se206.gpt.openai.ChatCompletionResult;
import nz.ac.auckland.se206.gpt.openai.ChatCompletionResult.Choice;
import nz.ac.auckland.se206.speech.TextToSpeech;

public class LivingRoomController {

  @FXML private Button hintButton;
  @FXML private Button backButton;
  @FXML private Label timerLabel;
  @FXML private Label chatLabel;
  @FXML private Label codeLabel;
  @FXML private Rectangle rocket;
  @FXML private Rectangle door;
  @FXML private ImageView pinpad;
  private TextToSpeech textToSpeech;
  private ChatCompletionRequest chatCompletionRequest;
  private Timer timer;

  public void initialize() {
    // Initialization code goes here
    chatCompletionRequest =
        new ChatCompletionRequest().setN(1).setTemperature(1).setTopP(0.5).setMaxTokens(25);

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
    System.out.println("help button clicked");

    // Disable the help button temporarily to prevent multiple clicks
    hintButton.setDisable(true);

    // Set the chat label to loading message
    chatLabel.setText("Loading...");

    // Run the AI chat in the background
    Task<Void> aiChatTask =
        new Task<Void>() {
          @Override
          protected Void call() throws Exception {
            try {
              String helpMessage = getAIHelpMessage(); // Get AI-generated help message
              Platform.runLater(
                  () -> {
                    chatLabel.setText(helpMessage);
                  });
            } catch (Exception e) {
              e.printStackTrace();
            } finally {
              // Re-enable the help button
              Platform.runLater(() -> hintButton.setDisable(false));
            }
            return null;
          }
        };

    // Start the AI chat task in a new thread
    Thread aiChatThread = new Thread(aiChatTask);
    aiChatThread.setDaemon(true);
    aiChatThread.start();
    aiChatThread.interrupt();
  }

  private String getAIHelpMessage() {
    try {
      // Run the AI chat and get the response
      if (!GameState.isKeyFound) {
        System.out.println("riddle resolved, key not found");
        chatCompletionRequest.addMessage(
            new ChatMessage("user", GptPromptEngineering.getKeyHint()));
      } else {
        System.out.println("key found");
        chatCompletionRequest.addMessage(
            new ChatMessage("user", GptPromptEngineering.getDoorHint()));
      }

      ChatCompletionResult chatCompletionResult = chatCompletionRequest.execute();
      Choice result = chatCompletionResult.getChoices().iterator().next();
      String helpMessage = result.getChatMessage().getContent();
      chatCompletionRequest.addMessage(result.getChatMessage());
      return helpMessage;
    } catch (ApiProxyException e) {
      e.printStackTrace();
      return "Sorry, I couldn't retrieve the help message.";
    }
  }

  @FXML
  public void onGoBack(ActionEvent event) {
    // Back button action code goes here
    Button button = (Button) event.getSource();
    Scene sceneButtonIsIn = button.getScene();
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
    GameState.Code = Integer.toString((int) (Math.random() * 10000));
    while (Integer.parseInt(GameState.Code) > 9000) {
      GameState.Code = Integer.toString((int) (Math.random() * 10000));
    }
    codeLabel.setText(GameState.Code);
    GameState.isKeyFound = true;
    pinpad.setVisible(true);
    speakThread.interrupt();
  }

  private void updateLabel() {
    timerLabel.setText(
        String.format("%02d:%02d", timer.getCounter() / 60, timer.getCounter() % 60));

    if (GameState.isTimeReached) {
      // Timer has reached zero, switch to the desired scene
      switchToGameOverScene();
    }

    if (GameState.isInLivingRoom && GameState.isFirstTimeInLivingRoom) {
      updateChatLabel();
      GameState.isInLivingRoom = false;
        GameState.isFirstTimeInLivingRoom = false;
    }
  }

  private void switchToGameOverScene() {
    textToSpeech.terminate();

    Platform.runLater(
        () -> {
          Scene currentScene = timerLabel.getScene();
          if (currentScene != null) {
            currentScene.setRoot(SceneManager.getUiRoot(AppUi.LOST));
            currentScene.getWindow().sizeToScene();
          }
        });
  }
}

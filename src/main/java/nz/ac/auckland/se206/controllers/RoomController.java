package nz.ac.auckland.se206.controllers;

import java.io.IOException;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyEvent;
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

/** Controller class for the room view. */
public class RoomController {

  @FXML private Rectangle door;
  @FXML private Rectangle window;
  @FXML private Rectangle computer;
  @FXML private Label timerLabel;
  @FXML private Label chatLabel;
  @FXML private Button helpButton;
  private Timer timer;
  private ChatCompletionRequest chatCompletionRequest;
  Thread chatThread;

  /** Initializes the room view, it is called when the room loads. */
  public void initialize() {
    // Initialization code goes here
    chatCompletionRequest =
        new ChatCompletionRequest().setN(1).setTemperature(1).setTopP(1).setMaxTokens(25);
    // Start the timer
    timer = new Timer(timerLabel);
    GameState.timer = timer;
    GameState.isGameStarted = true;

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
              Platform.runLater(() -> chatLabel.setText(sentence));

              if (currentSentenceIndex < chatSentences.length - 1) {
                currentSentenceIndex++;
              } else {
                Thread.sleep(120000);
              }

              Thread.sleep(4 * 1000);
            }
            return null;
          }
        };

    chatThread = new Thread(chatTask);
    chatThread.setDaemon(true);
    chatThread.start();
  }

  /**
   * Handles the key pressed event.
   *
   * @param event the key event
   */
  @FXML
  public void onKeyPressed(KeyEvent event) {
    System.out.println("key " + event.getCode() + " pressed");
  }

  /**
   * Handles the key released event.
   *
   * @param event the key event
   */
  @FXML
  public void onKeyReleased(KeyEvent event) {
    System.out.println("key " + event.getCode() + " released");
  }

  /**
   * Displays a dialog box with the given title, header text, and message.
   *
   * @param title the title of the dialog box
   * @param headerText the header text of the dialog box
   * @param message the message content of the dialog box
   */
  private void showDialog(String title, String headerText, String message) {
    Alert alert = new Alert(Alert.AlertType.INFORMATION);
    alert.setTitle(title);
    alert.setHeaderText(headerText);
    alert.setContentText(message);
    alert.showAndWait();
  }

  /**
   * Handles the click event on the door.
   *
   * @param event the mouse event
   * @throws IOException if there is an error loading the chat view
   */
  @FXML
  public void clickDoor(MouseEvent event) throws IOException {
    System.out.println("door clicked");

    if (!GameState.isRiddleResolved) {
      // showDialog("Info", "Riddle", "You need to resolve the riddle!");
      Rectangle rectangle = (Rectangle) event.getSource();
      Scene sceneRectangleIsIn = rectangle.getScene();
      chatLabel.setText("");
      sceneRectangleIsIn.setRoot(SceneManager.getUiRoot(AppUi.CHAT));
      return;
    }

    if (!GameState.isKeyFound) {
      showDialog("Info", "Find the key!", "You solved the riddle, now you know where the key is.");
    } else {
      GameState.isTimeReached = true;
      GameState.isGameStarted = false;
      Rectangle rectangle = (Rectangle) event.getSource();
      Scene sceneRectangleIsIn = rectangle.getScene();
      sceneRectangleIsIn.setRoot(SceneManager.getUiRoot(AppUi.ESCAPED));
    }
  }

  /**
   * Handles the click event on the computer.
   *
   * @param event the mouse event
   */
  @FXML
  public void clickComputer(MouseEvent event) {
    System.out.println("computer clicked");
    if (GameState.isRiddleResolved && !GameState.isKeyFound) {
      showDialog("Info", "Key Found", "You found a key under the computer!");
      GameState.isKeyFound = true;
    }
  }

  /**
   * Handles the click event on the window.
   *
   * @param event the mouse event
   */
  @FXML
  public void clickWindow(MouseEvent event) {
    System.out.println("window clicked");
  }

  private void updateLabel() {
    timerLabel.setText(
        String.format("%02d:%02d", timer.getCounter() / 60, timer.getCounter() % 60));

    if (GameState.isTimeReached) {
      // Timer has reached zero, switch to the desired scene
      switchToGameOverScene();
    }
  }

  @FXML
  public void clickHelpButton(ActionEvent event) {
    System.out.println("help button clicked");

    // Disable the help button temporarily to prevent multiple clicks
    helpButton.setDisable(true);

    // Set the chat label to loading message
    chatLabel.setText("Loading...");

    // Run the AI chat in the background
    Task<Void> aiChatTask =
        new Task<Void>() {
          @Override
          protected Void call() throws Exception {
            try {
              String helpMessage = getAIHelpMessage(); // Get AI-generated help message
              Platform.runLater(() -> chatLabel.setText(helpMessage));
            } catch (Exception e) {
              e.printStackTrace();
            } finally {
              // Re-enable the help button
              Platform.runLater(() -> helpButton.setDisable(false));
            }
            return null;
          }
        };

    // Start the AI chat task in a new thread
    Thread aiChatThread = new Thread(aiChatTask);
    aiChatThread.setDaemon(true);
    aiChatThread.start();
  }

  private void switchToGameOverScene() {

    Scene currentScene = timerLabel.getScene();
    Platform.runLater(
        () -> {
          currentScene.setRoot(SceneManager.getUiRoot(AppUi.LOST));
        });
  }

  private String[] chatSentences = {
    "Welcome to the escape room!", "You have 2 minutes to escape the room.", "If you get stuck, use the Help button!", ""
  };

  private String getAIHelpMessage() {
    try {
      // Run the AI chat and get the response
      if (!GameState.isKeyFound && GameState.isRiddleResolved) {
        chatCompletionRequest.addMessage(
            new ChatMessage("user", GptPromptEngineering.getKeyHint()));
      } else if (GameState.isRiddleResolved && GameState.isKeyFound) {
        chatCompletionRequest.addMessage(
            new ChatMessage("user", GptPromptEngineering.getDoorHint()));
      } else {
        chatCompletionRequest.addMessage(
            new ChatMessage("user", GptPromptEngineering.getStartHint()));
      }

      ChatCompletionResult chatCompletionResult = chatCompletionRequest.execute();
      Choice result = chatCompletionResult.getChoices().iterator().next();
      String helpMessage = result.getChatMessage().getContent();
      return helpMessage;
    } catch (ApiProxyException e) {
      e.printStackTrace();
      return "Sorry, I couldn't retrieve the help message.";
    }
  }
}

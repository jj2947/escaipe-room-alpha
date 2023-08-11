package nz.ac.auckland.se206.controllers;

import java.io.IOException;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Scene;
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
import nz.ac.auckland.se206.speech.TextToSpeech;

/** Controller class for the room view. */
public class RoomController {

  @FXML private Rectangle couch;
  @FXML private Rectangle computer;
  @FXML private Label timerLabel;
  @FXML private Label chatLabel;
  @FXML private Button helpButton;
  @FXML private Label codeLabel;
  private Timer timer;
  private ChatCompletionRequest chatCompletionRequest;
  private Thread chatThread;
  private Thread updateThread;
  private TextToSpeech textToSpeech;
  private Thread aiChatThread;

  /** Initializes the room view, it is called when the room loads. */
  public void initialize() {
    // Initialization code goes here
    chatCompletionRequest =
        new ChatCompletionRequest().setN(1).setTemperature(1).setTopP(0.5).setMaxTokens(25);
    // Start the timer
    timer = new Timer(timerLabel);
    // Initialize the TextToSpeech instance
    textToSpeech = new TextToSpeech();
    GameState.timer = timer;
    GameState.isGameStarted = true;
    GameState.isInRoom = true;

    // Schedule the updateLabelTask to start after the room view is initialized
    Platform.runLater(() -> startUpdateLabelTask());
    // Schedule the chat task to start after the room view is initialized
    Platform.runLater(() -> startChatTask());
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
   * Handles the click event on the computer.
   *
   * @param event the mouse event
   */
  @FXML
  public void clickComputer(MouseEvent event) {
    System.out.println("computer clicked");

    Rectangle rectangle = (Rectangle) event.getSource();
    Scene sceneRectangleIsIn = rectangle.getScene();
    GameState.isInRoom = false;
    chatLabel.setText("");
    sceneRectangleIsIn.setRoot(SceneManager.getUiRoot(AppUi.CHAT));
    // Resizing the window so the larger scene fits
    sceneRectangleIsIn.getWindow().sizeToScene();
  }

  /**
   * Handles the click event on the couch.
   *
   * @param event the mouse event
   * @throws IOException if there is an error loading the chat view
   */
  @FXML
  public void clickCouch(MouseEvent event) throws IOException {
    System.out.println("couch clicked");

    if (GameState.isRiddleResolved) {
      Rectangle rectangle = (Rectangle) event.getSource();
      Scene sceneRectangleIsIn = rectangle.getScene();
      GameState.isInRoom = false;
      GameState.isInLivingRoom = true;
      sceneRectangleIsIn.setRoot(SceneManager.getUiRoot(AppUi.LIVING_ROOM));
      sceneRectangleIsIn.getWindow().sizeToScene();
      return;
    }
  }

  @FXML
  private void onClickHelpButton(ActionEvent event) {
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
              String helpMessage = getAiHelpMessage(); // Get AI-generated help message
              // Start text to speech
              Task<Void> speakTask =
                  new Task<Void>() {
                    @Override
                    protected Void call() throws Exception {

                      textToSpeech.speak(helpMessage);

                      return null;
                    }
                  };
              // Start textToSpeech task in new thread
              Thread speakThread = new Thread(speakTask);
              speakThread.setDaemon(true);
              speakThread.start();
              Platform.runLater(
                  () -> {
                    chatLabel.setText(helpMessage);
                  });
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
    aiChatThread = new Thread(aiChatTask);
    aiChatThread.setDaemon(true);
    aiChatThread.start();
  }

  private void updateLabel() {
    timerLabel.setText(
        String.format("%02d:%02d", timer.getCounter() / 60, timer.getCounter() % 60));

    // If the player just solved the riddle, get the couch message
    if (GameState.isInRoom && GameState.isRiddleResolved && GameState.isFirstTimeInLivingRoom) {
      String sentence = getAiHelpMessage();
      GameState.isInRoom = false;
      // Start text to speech
      Task<Void> speakTask =
          new Task<Void>() {
            @Override
            protected Void call() throws Exception {
              textToSpeech.speak(sentence);
              return null;
            }
          };
      // Start textToSpeech task in new thread
      Thread speakThread = new Thread(speakTask);
      speakThread.setDaemon(true);
      speakThread.start();
      speakThread.interrupt();
      // Set the chat label to the couch message
      Platform.runLater(
          () -> {
            chatLabel.setText(sentence);
          });
    }
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
            currentScene.getWindow().sizeToScene();
            chatLabel.setText("");
          }
        });
  }

  private String[] chatSentences = {
    "Welcome to the escape room!",
    "You have 2 minutes to escape the room.",
    "If you get stuck, use the Hint button to ask the game master for a clue!",
    ""
  };

  private String getAiHelpMessage() {
    try {
      // Run the AI chat and get the response
      if (GameState.isRiddleResolved) {
        chatCompletionRequest.addMessage(
            new ChatMessage("user", GptPromptEngineering.getCouchHint()));
        System.out.println("riddle resolved");
      } else {
        System.out.println("key not found, riddle not resolved");
        chatCompletionRequest.addMessage(
            new ChatMessage("user", GptPromptEngineering.getStartHint()));
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

  private void startChatTask() {
    // Run the AI chat in the background
    Task<Void> chatTask =
        new Task<Void>() {
          @Override
          protected Void call() throws Exception {
            int currentSentenceIndex = 0;
            while (!Thread.currentThread().isInterrupted()) {
              // Get the sentence to speak
              String sentence = chatSentences[currentSentenceIndex];
              // Start text to speech
              Task<Void> speakTask =
                  new Task<Void>() {
                    @Override
                    protected Void call() throws Exception {

                      textToSpeech.speak(sentence);

                      return null;
                    }
                  };
              // Start textToSpeech task in new thread
              Thread speakThread = new Thread(speakTask);
              speakThread.setDaemon(true);
              speakThread.start();
              speakThread.interrupt();
              // Set the chat label to the sentence
              Platform.runLater(
                  () -> {
                    chatLabel.setText(sentence);
                  });

              // Get next sentence
              if (currentSentenceIndex < chatSentences.length - 1) {
                currentSentenceIndex++;
                if (currentSentenceIndex == 3) {
                  Thread.sleep(5 * 1000);
                } else {
                  Thread.sleep(3 * 1000);
                }
              } else {
                // Once all sentences have been spoken, stop the thread
                Thread.currentThread().interrupt();
              }
            }
            return null;
          }
        };
    // Start the AI chat task in a new thread
    chatThread = new Thread(chatTask);
    chatThread.setDaemon(true);
    chatThread.start();
  }

  private void startUpdateLabelTask() {
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
    updateThread = new Thread(updateLabelTask);
    updateThread.setDaemon(true);
    updateThread.start();
  }
}

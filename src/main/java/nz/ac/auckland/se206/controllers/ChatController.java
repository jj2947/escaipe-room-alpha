package nz.ac.auckland.se206.controllers;

import java.io.IOException;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import nz.ac.auckland.se206.GameState;
import nz.ac.auckland.se206.SceneManager;
import nz.ac.auckland.se206.SceneManager.AppUi;
import nz.ac.auckland.se206.gpt.ChatMessage;
import nz.ac.auckland.se206.gpt.GptPromptEngineering;
import nz.ac.auckland.se206.gpt.openai.ApiProxyException;
import nz.ac.auckland.se206.gpt.openai.ChatCompletionRequest;
import nz.ac.auckland.se206.gpt.openai.ChatCompletionResult;
import nz.ac.auckland.se206.gpt.openai.ChatCompletionResult.Choice;

/** Controller class for the chat view. */
public class ChatController {
  @FXML private TextArea chatTextArea;
  @FXML private TextField inputText;
  @FXML private Button sendButton;
  @FXML private Label timerLabel2;
  @FXML private Button backButton;  
  private Thread updateThread;
  private Thread initializeThread;
  private Thread runGptThread;

  private Timer timer;

  private ChatCompletionRequest chatCompletionRequest;

  /**
   * Initializes the chat view, loading the riddle.
   *
   * @throws ApiProxyException if there is an error communicating with the API proxy
   */
  @FXML
  public void initialize() throws ApiProxyException {
    chatCompletionRequest =
        new ChatCompletionRequest().setN(1).setTemperature(0.6).setTopP(0.4).setMaxTokens(50);
    appendChatMessage(
        new ChatMessage("assistant", "Solve the riddle to find the key and escape the room!"));
    Task<Void> initializeTask =
        new Task<Void>() {
          @Override
          protected Void call() throws Exception {
            // Your initialization code here
            runGpt(
                new ChatMessage("user", GptPromptEngineering.getRiddleWithGivenWord("rocket")));
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
            updateThread = new Thread(updateLabelTask);
            updateThread.setDaemon(true);
            updateThread.start();
            return null;
          }
        };

    // Create a new thread for the initialization task and start it
    initializeThread = new Thread(initializeTask);
    initializeThread.setDaemon(true);
    initializeThread.start();
  }

  /**
   * Appends a chat message to the chat text area.
   *
   * @param msg the chat message to append
   */
  private void appendChatMessage(ChatMessage msg) {
    chatTextArea.appendText(msg.getContent() + "\n\n");
  }

  /**
   * Runs the GPT model with a given chat message.
   *
   * @param msg the chat message to process
   * @return the response chat message
   * @throws ApiProxyException if there is an error communicating with the API proxy
   */
  private void runGpt(ChatMessage msg) {
    Platform.runLater(
        () -> {
          appendChatMessage(new ChatMessage("assistant", "Loading..."));
        });

    Task<Void> backgroundTask =
        new Task<Void>() {
          @Override
          protected Void call() throws Exception {
            chatCompletionRequest.addMessage(msg);
            try {
              ChatCompletionResult chatCompletionResult = chatCompletionRequest.execute();
              Choice result = chatCompletionResult.getChoices().iterator().next();
              chatCompletionRequest.addMessage(result.getChatMessage());
              // Replace "Loading..." message with GPT response
              Platform.runLater(
                  () -> {
                    replaceLoadingMessageWithResponse(result.getChatMessage().getContent());
                  });
              // Check if the assistant's response contains "Correct"
              if (result.getChatMessage().getRole().equals("assistant")
                  && result.getChatMessage().getContent().startsWith("Correct")) {
                GameState.isRiddleResolved = true;
              }
            } catch (ApiProxyException e) {
              // TODO handle exception appropriately
              e.printStackTrace();
            }
            return null;
          }
        };

    // Execute the background task in a new thread
     runGptThread = new Thread(backgroundTask);
    runGptThread.setDaemon(true);
    runGptThread.start();
  }

  /**
   * Sends a message to the GPT model.
   *
   * @param event the action event triggered by the send button
   * @throws ApiProxyException if there is an error communicating with the API proxy
   * @throws IOException if there is an I/O error
   */
  @FXML
  private void onSendMessage(ActionEvent event) throws ApiProxyException, IOException {
    String message = inputText.getText();
    if (message.trim().isEmpty()) {
      return;
    }
    inputText.clear();
    ChatMessage msg = new ChatMessage("user", message);
    appendChatMessage(msg);
    runGpt(msg);
  }

  /**
   * Navigates back to the previous view.
   *
   * @param event the action event triggered by the go back button
   * @throws ApiProxyException if there is an error communicating with the API proxy
   * @throws IOException if there is an I/O error
   */
  @FXML
  private void onGoBack(ActionEvent event) throws ApiProxyException, IOException {
    Button button = (Button) event.getSource();
    Scene sceneButtonIsIn = button.getScene();
    GameState.isInRoom = true;
    sceneButtonIsIn.setRoot(SceneManager.getUiRoot(AppUi.ROOM));
    sceneButtonIsIn.getWindow().sizeToScene();
  }

  private void updateLabel() {
    Platform.runLater(
        () -> {
          timerLabel2.setText(
              String.format("%02d:%02d", timer.getCounter() / 60, timer.getCounter() % 60));

          if (GameState.isTimeReached) {
            // Timer has reached zero, switch to the desired scene
            switchToGameOverScene();
          }
        });
  }

  private void switchToGameOverScene() {
    // Get the root of the game over scene and set it as the new root
    Platform.runLater(
        () -> {
          Scene currentScene = timerLabel2.getScene();
          if (currentScene != null) {
            currentScene.setRoot(SceneManager.getUiRoot(AppUi.LOST));
          }
        });
  }

  private void replaceLoadingMessageWithResponse(String response) {
    String loadingMessage = "Loading...";
    String content = chatTextArea.getText();

    // Find the index of the last occurrence of "Loading..." in the chatTextArea
    int lastLoadingIndex = content.lastIndexOf(loadingMessage);

    // If "Loading..." is found, replace it with the GPT response
    if (lastLoadingIndex != -1) {
      chatTextArea.replaceText(
          lastLoadingIndex, lastLoadingIndex + loadingMessage.length(), response);
    }
  }
}

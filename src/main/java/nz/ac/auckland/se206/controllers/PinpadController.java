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
import nz.ac.auckland.se206.gpt.ChatMessage;
import nz.ac.auckland.se206.gpt.GptPromptEngineering;
import nz.ac.auckland.se206.gpt.openai.ApiProxyException;
import nz.ac.auckland.se206.gpt.openai.ChatCompletionRequest;
import nz.ac.auckland.se206.gpt.openai.ChatCompletionResult;
import nz.ac.auckland.se206.gpt.openai.ChatCompletionResult.Choice;

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
  @FXML private TextField textField;
  @FXML private Label timerLabel3;
  @FXML private Button helpButton;
  @FXML private Button backButton;
  @FXML private Label chatLabel;
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
                    textField.setText("");
                    chatLabel.setText(sentence);
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
    "Enter the code to escape the room.", "If you get stuck, use the Help button!", ""
  };

  @FXML
  public void onClickHelp() {
    System.out.println("help button clicked");

    // Disable the help button temporarily to prevent multiple clicks
    helpButton.setDisable(true);

    textField.setText("");
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
              Platform.runLater(() -> helpButton.setDisable(false));
            }
            return null;
          }

          private String getAIHelpMessage() {
            try {
              chatCompletionRequest.addMessage(
                  new ChatMessage("user", GptPromptEngineering.getCodeHint()));

              ChatCompletionResult chatCompletionResult = chatCompletionRequest.execute();
              Choice result = chatCompletionResult.getChoices().iterator().next();
              String helpMessage = result.getChatMessage().getContent();
              return helpMessage;
            } catch (ApiProxyException e) {
              e.printStackTrace();
              return "Sorry, I couldn't retrieve the help message.";
            }
          }
        };

    // Start the AI chat task in a new thread
    Thread aiChatThread = new Thread(aiChatTask);
    aiChatThread.setDaemon(true);
    aiChatThread.start();
  }

  @FXML
  public void onClickOne() {
    chatLabel.setText("");
    textField.setText(textField.getText() + "1");
  }

  @FXML
  public void onClickTwo() {
    chatLabel.setText("");
    textField.setText(textField.getText() + "2");
  }

  @FXML
  public void onClickThree() {
    chatLabel.setText("");
    textField.setText(textField.getText() + "3");
  }

  @FXML
  public void onClickFour() {
    chatLabel.setText("");
    textField.setText(textField.getText() + "4");
  }

  @FXML
  public void onClickFive() {
    chatLabel.setText("");
    textField.setText(textField.getText() + "5");
  }

  @FXML
  public void onClickSix() {
    chatLabel.setText("");
    textField.setText(textField.getText() + "6");
  }

  @FXML
  public void onClickSeven() {
    chatLabel.setText("");
    textField.setText(textField.getText() + "7");
  }

  @FXML
  public void onClickEight() {
    chatLabel.setText("");
    textField.setText(textField.getText() + "8");
  }

  @FXML
  public void onClickNine() {
    chatLabel.setText("");
    textField.setText(textField.getText() + "9");
  }

  @FXML
  public void onEnter() {
    if (textField.getText().equals(GameState.Code)) {
      GameState.isTimeReached = true;
      Scene currentScene = timerLabel3.getScene();
      Platform.runLater(
          () -> {
            currentScene.setRoot(SceneManager.getUiRoot(AppUi.ESCAPED));
          });
    } else {
      textField.setText("Incorrect");
    }
  }

  @FXML
  public void onBack() {
    Scene currentScene = timerLabel3.getScene();
    Platform.runLater(
        () -> {
          currentScene.setRoot(SceneManager.getUiRoot(AppUi.ROOM));
        });
  }
}

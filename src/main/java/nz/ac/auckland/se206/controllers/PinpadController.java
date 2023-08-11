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
import nz.ac.auckland.se206.speech.TextToSpeech;

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
  @FXML private Button zeroButton;
  @FXML private Button enterButton;
  @FXML private TextField textField;
  @FXML private Label timerLabel3;
  @FXML private Button helpButton;
  @FXML private Button backButton;
  @FXML private Button clearButton;
  @FXML private Label chatLabel;
  private Timer timer;
  private int numsEntered = 0;
  private Thread updateThread;
  private int randNum;
  private TextToSpeech textToSpeech;

  public void initialize() {
    System.out.println("pinpad controller initialized");
    textToSpeech = new TextToSpeech();
    // Start the timer
    timer = GameState.timer;
    enterButton.disableProperty().setValue(true);

    randNum = (int) (Math.random() * 100);
    chatLabel.setText("What is the code + " + randNum + "?");

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

  @FXML
  private void onClickHelp() {
    System.out.println("help button clicked");

    textField.setText("");
    numsEntered = 0;
    String sentence = "What is the code + " + randNum + "?";
    chatLabel.setText(sentence);
    Task<Void> speakTask =
        new Task<Void>() {
          @Override
          protected Void call() throws Exception {

            textToSpeech.speak(sentence);

            return null;
          }
        };
    Thread speakThread = new Thread(speakTask);
    speakThread.setDaemon(true);
    speakThread.start();
    speakThread.interrupt();
  }

  @FXML
  private void onClickOne() {
    updateTextField("1");
  }

  @FXML
  private void onClickTwo() {
    updateTextField("2");
  }

  @FXML
  private void onClickThree() {
    updateTextField("3");
  }

  @FXML
  private void onClickFour() {
    updateTextField("4");
  }

  @FXML
  private void onClickFive() {
    updateTextField("5");
  }

  @FXML
  private void onClickSix() {
    updateTextField("6");
  }

  @FXML
  private void onClickSeven() {
    updateTextField("7");
  }

  @FXML
  private void onClickEight() {
    updateTextField("8");
  }

  @FXML
  private void onClickNine() {
    updateTextField("9");
  }

  @FXML
  private void onClickZero() {
    updateTextField("0");
  }

  private void updateTextField(String number) {
    chatLabel.setText("");

    if (number == null) {
      enterButton.disableProperty().setValue(true);
      oneButton.disableProperty().setValue(false);
      twoButton.disableProperty().setValue(false);
      threeButton.disableProperty().setValue(false);
      fourButton.disableProperty().setValue(false);
      fiveButton.disableProperty().setValue(false);
      sixButton.disableProperty().setValue(false);
      sevenButton.disableProperty().setValue(false);
      eightButton.disableProperty().setValue(false);
      nineButton.disableProperty().setValue(false);
      zeroButton.disableProperty().setValue(false);
      return;
    }
    numsEntered++;

    if (numsEntered == 1) {
      textField.setText(number + " _ _ _");
    } else if (numsEntered == 2) {
      textField.setText(textField.getText().charAt(0) + " " + number + " _ _");
    } else if (numsEntered == 3) {
      textField.setText(textField.getText().substring(0, 4) + number + " _");
    } else if (numsEntered == 4) {
      enterButton.disableProperty().setValue(false);
      enterButton.requestFocus();
      textField.setText(textField.getText().substring(0, 6) + number);
      oneButton.disableProperty().setValue(true);
      twoButton.disableProperty().setValue(true);
      threeButton.disableProperty().setValue(true);
      fourButton.disableProperty().setValue(true);
      fiveButton.disableProperty().setValue(true);
      sixButton.disableProperty().setValue(true);
      sevenButton.disableProperty().setValue(true);
      eightButton.disableProperty().setValue(true);
      nineButton.disableProperty().setValue(true);
      zeroButton.disableProperty().setValue(true);
    } else {
      numsEntered = 0;
      enterButton.disableProperty().setValue(true);
      oneButton.disableProperty().setValue(false);
      twoButton.disableProperty().setValue(false);
      threeButton.disableProperty().setValue(false);
      fourButton.disableProperty().setValue(false);
      fiveButton.disableProperty().setValue(false);
      sixButton.disableProperty().setValue(false);
      sevenButton.disableProperty().setValue(false);
      eightButton.disableProperty().setValue(false);
      nineButton.disableProperty().setValue(false);
      zeroButton.disableProperty().setValue(false);
    }
  }

  @FXML
  private void onEnter() {
    int answer = Integer.parseInt(GameState.Code) + randNum;
    String answerString = Integer.toString(answer);
    String input = stripString(textField.getText());
    if (input.equals(answerString)) {
      textToSpeech.terminate();
      GameState.isTimeReached = true;
      Platform.runLater(
          () -> {
            Scene currentScene = timerLabel3.getScene();
            if (currentScene != null) {
              currentScene.setRoot(SceneManager.getUiRoot(AppUi.ESCAPED));
            }
          });
    } else {
      numsEntered = 0;
      textField.setText("Incorrect");
      updateTextField(null);
      Task<Void> speakTask =
          new Task<Void>() {
            @Override
            protected Void call() throws Exception {

              textToSpeech.speak("Incorrect");

              return null;
            }
          };
      Thread speakThread = new Thread(speakTask);
      speakThread.setDaemon(true);
      speakThread.start();
      speakThread.interrupt();
    }
  }

  @FXML
  private void onBack() {
    Scene currentScene = timerLabel3.getScene();
    Platform.runLater(
        () -> {
          currentScene.setRoot(SceneManager.getUiRoot(AppUi.LIVING_ROOM));
          currentScene.getWindow().sizeToScene();
        });
  }

  @FXML
  private void onClear() {
    numsEntered = 0;
    textField.setText("_ _ _ _");
    updateTextField(null);
  }

  private String stripString(String str) {
    return str.replaceAll("[^a-zA-Z0-9]", "");
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
    updateThread.interrupt();
    textToSpeech.terminate();
    Platform.runLater(
        () -> {
          Scene currentScene = timerLabel3.getScene();
          if (currentScene != null) {
            currentScene.setRoot(SceneManager.getUiRoot(AppUi.LOST));
            currentScene.getWindow().sizeToScene();
          }
        });
  }
}

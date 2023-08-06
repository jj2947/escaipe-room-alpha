package nz.ac.auckland.se206.controllers;

import javafx.concurrent.Task;
import javafx.scene.control.Label;

public class Timer {
  private int counter;
  private Label timerLabel;

  public Timer(Label timerLabel) {
    this.timerLabel = timerLabel;
    startTimer();
  }

  private void startTimer() {
    counter = 120; // Set the initial value of the counter to 2 minutes (120 seconds)
    updateLabel();

    Task<Void> backgroundTask =
        new Task<Void>() {

          @Override
          protected Void call() throws Exception {

            while (counter > 0) {
              Thread.sleep(1000); // Wait for 1 second
              counter--;
            }

            return null;
          }
        };

    // Create a new thread for the countdown and start it
    Thread countdownThread = new Thread(backgroundTask);
    countdownThread.start();
  }

  private void updateLabel() {
    timerLabel.setText(String.format("%02d:%02d", counter / 60, counter % 60));
  }

  public int getCounter() {
    return counter;
  }

  public Timer getTimer() {
    return this;
  }
}

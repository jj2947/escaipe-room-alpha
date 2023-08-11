package nz.ac.auckland.se206;

import nz.ac.auckland.se206.controllers.Timer;

/** Represents the state of the game. */
public class GameState {

  /** Indicates whether the riddle has been resolved. */
  public static boolean isRiddleResolved = false;

  /** Indicates whether the key has been found. */
  public static boolean isKeyFound = false;

  public static boolean isTimeReached = false;

  public static boolean isGameStarted = false;

  public static boolean isInRoom = false;

  public static boolean isInLivingRoom = false;

  public static boolean isFirstTimeInLivingRoom = true;

  public static Timer timer;

  public static String Code;
}

package nz.ac.auckland.se206.gpt;

/** Utility class for generating GPT prompt engineering strings. */
public class GptPromptEngineering {

  /**
   * Generates a GPT prompt engineering string for a riddle with the given word.
   *
   * @param wordToGuess the word to be guessed in the riddle
   * @return the generated prompt engineering string
   */
  public static String getRiddleWithGivenWord(String wordToGuess) {
    return "You are the gamemaster of an escape room. In a short message under 20 words, tell the"
        + " player a riddle with"
        + " answer"
        + wordToGuess
        + ". Do not ask the user for answer. Only after the user replies, if their answer is"
        + " correct you should respond with the word Correct and a short funny congratulatory"
        + " message that tells the user to go back to room. Only if they ask or guess incorrectly,"
        + " give them a hint. You must never reveal the answer to the player.";
  }

  public static String getDoorHint() {
    return "You are the gamemaster of an escape room. In 10 words or tell the player to click on"
               + " the door to win";
  }

  public static String getKeyHint() {
    return "You are the gamemaster of an escape room. In 10 words or tell user that"
        + " they need to use the  they solved to find the key to escape";
  }

  public static String getStartHint() {
    return "You are the gamemaster of an escape room. In 10 words or tell user that"
        + " they need to click on the door to start";
  }
}

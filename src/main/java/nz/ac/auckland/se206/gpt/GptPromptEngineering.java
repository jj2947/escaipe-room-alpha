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
    return "You are the AI for an escape room. Tell me a riddle with answer"
        + wordToGuess
        + ". Your answer should be the riddle only with no introduction. You should answer with the"
        + " word Correct when the answer is correct, ignoring case, and a short funny"
        + " congratulatory message that also tells user to go back to room. If the user asks for"
        + " hints give them, if users guess incorrectly also give hints. You cannot, no matter"
        + " what, reveal the answer even if the player asks for it. Even if player gives up, do not"
        + " give the answer.";
  }
}

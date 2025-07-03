package me.blvckbytes.syllables_matcher;

public class Syllables {

  public static final char DELIMITER_SEARCH_PATTERN = '-';
  public static final char DELIMITER_FREE_TEXT      = ' ';

  private static final char PATTERN_WILDCARD_CHAR_EXCLUDING_EXACT = '?';
  private static final char PATTERN_WILDCARD_CHAR_INCLUDING_EXACT = '*';
  private static final char PATTERN_NEGATION_CHAR = '!';

  private static final int INITIAL_CAPACITY = 32;
  private static final int BIT_IS_NEGATED   = (1 << 1);
  private static final int START_END_MASK   = 32768 - 1;

  /*
    <15b start><15b end><1b is_negated><1b unused>
   */
  private int[] syllables;
  private int size;

  public String container;
  private WildcardMode wildcardMode;

  public Syllables(String container) {
    this.container = container;
    this.wildcardMode = WildcardMode.NONE;
    this.syllables = new int[INITIAL_CAPACITY];
  }

  public Syllables add(int start, int end, boolean isNegated) {
    if (syllables.length == size) {
      var newArray = new int[syllables.length * 2];
      System.arraycopy(syllables, 0, newArray, 0, syllables.length);
      syllables = newArray;
    }

    syllables[size++] = (
      (isNegated ? BIT_IS_NEGATED : 0) |
      ((end & START_END_MASK) << 2) |
      ((start & START_END_MASK) << (2 + 15))
    );

    return this;
  }

  public WildcardMode getWildcardMode() {
    return this.wildcardMode;
  }

  public int getSyllable(int index) {
    return syllables[index];
  }

  public int size() {
    return size;
  }

  public int capacity() {
    return syllables.length;
  }

  public void clear() {
    this.size = 0;
  }

  public static int getStartIndex(int syllable) {
    return (syllable >> (15 + 2)) & START_END_MASK;
  }

  public static int getEndIndex(int syllable) {
    return (syllable >> 2) & START_END_MASK;
  }

  public static int getLength(int syllable) {
    return (getEndIndex(syllable) - getStartIndex(syllable)) + 1;
  }

  public static boolean isNegated(int syllable) {
    return (syllable & BIT_IS_NEGATED) != 0;
  }

  public static Syllables forString(String input, char delimiter) {
    return forString(input, delimiter, false).syllables();
  }

  public static SyllablesAndCounters forStringWithWildcardSupport(String input, char delimiter) {
    return forString(input, delimiter, true);
  }

  private static SyllablesAndCounters forString(String input, char delimiter, boolean supportsWildcard) {
    var result = new Syllables(input);
    var inputLength = input.length();

    int nextPartBeginning = 0;

    int numberOfWildcardSyllables = 0;
    int numberOfNonWildcardSyllables = 0;

    boolean encounteredNonDelimiter = false;

    for (int i = 0; i < inputLength; ++i) {
      var currentChar = input.charAt(i);
      var isDelimiter = currentChar == delimiter;

      if (!isDelimiter) {
        encounteredNonDelimiter = true;

        if (i != inputLength - 1)
          continue;
      }

      if (encounteredNonDelimiter) {
        var firstChar = input.charAt(nextPartBeginning);
        var partEnd = isDelimiter ? i - 1 : i;

        if (nextPartBeginning != partEnd && firstChar == PATTERN_NEGATION_CHAR) {
          result.add(nextPartBeginning + 1, partEnd, true);
          ++numberOfNonWildcardSyllables;
        }

        else if (supportsWildcard && nextPartBeginning == partEnd && firstChar == PATTERN_WILDCARD_CHAR_EXCLUDING_EXACT) {
          ++numberOfWildcardSyllables;
          result.wildcardMode = WildcardMode.EXCLUDING_EXACT_MATCH;
        }

        else if (supportsWildcard && nextPartBeginning == partEnd && firstChar == PATTERN_WILDCARD_CHAR_INCLUDING_EXACT) {
          ++numberOfWildcardSyllables;
          result.wildcardMode = WildcardMode.INCLUDING_EXACT_MATCH;
        }

        else {
          result.add(nextPartBeginning, partEnd, false);
          ++numberOfNonWildcardSyllables;
        }
      }

      nextPartBeginning = i + 1;
      encounteredNonDelimiter = false;
    }

    return new SyllablesAndCounters(result, numberOfWildcardSyllables, numberOfNonWildcardSyllables);
  }
}

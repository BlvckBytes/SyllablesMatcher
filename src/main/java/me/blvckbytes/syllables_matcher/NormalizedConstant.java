package me.blvckbytes.syllables_matcher;

import java.util.Objects;

public class NormalizedConstant<T extends Enum<?>> {

  public final T constant;
  public final String initialNormalizedName;

  private String normalizedName;
  private Syllables syllables;

  public NormalizedConstant(T constant) {
    this.constant = constant;
    this.normalizedName = normalizeName(constant.name());
    this.initialNormalizedName = this.normalizedName;
    this.syllables = Syllables.forString(this.normalizedName, Syllables.DELIMITER_SEARCH_PATTERN);
  }

  public void setName(String name) {
    this.normalizedName = normalizeName(name);
    this.syllables = Syllables.forString(this.normalizedName, Syllables.DELIMITER_SEARCH_PATTERN);
  }

  public String getNormalizedName() {
    return normalizedName;
  }

  public Syllables getSyllables() {
    return syllables;
  }

  private static String normalizeName(String name) {
    var result = new StringBuilder();
    char previousChar = 0;

    for (var charIndex = 0; charIndex < name.length(); ++charIndex) {
      var currentChar = name.charAt(charIndex);

      if (currentChar == '_')
        result.append('-');
      else if (charIndex == 0 || previousChar == '-' || previousChar == '_')
        result.append(Character.toUpperCase(currentChar));
      else
        result.append(Character.toLowerCase(currentChar));

      previousChar = currentChar;
    }

    return result.toString();
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(constant);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof NormalizedConstant<?> that)) return false;
    return Objects.equals(constant, that.constant);
  }
}

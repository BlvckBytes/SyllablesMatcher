package me.blvckbytes.syllables_matcher;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class EnumMatcherTests {

  enum TestEnum {
    FIRST_CONSTANT,
    SECOND_CONSTANT,
    THIRD_CONSTANT,
    FOURTH_CONSTANT,
    FIFTH,
    A,
    AA,
    AB,
    AC,
    AD,
    AAC
    ;

    static final EnumMatcher<TestEnum> matcher = new EnumMatcher<>(values());
  }

  @Test
  public void shouldCreateUnfilteredCompletions() {
    assertEquals(
      sortedStrings(
        "First-Constant",
        "Second-Constant",
        "Third-Constant",
        "Fourth-Constant",
        "Fifth",
        "A",
        "Aa",
        "Ab",
        "Ac",
        "Ad",
        "Aac"
      ),
      TestEnum.matcher.createCompletions(null)
    );

    assertEquals(
      sortedStrings(
        "First-Constant",
        "Second-Constant",
        "Third-Constant",
        "Fourth-Constant"
      ),
      TestEnum.matcher.createCompletions("cons")
    );

    assertEquals(
      sortedStrings(
        "First-Constant"
      ),
      TestEnum.matcher.createCompletions("firs")
    );
  }

  @Test
  public void shouldCreateFilteredCompletions() {
    EnumPredicate<TestEnum> thirdForthNegativeFilter = normalizedConstant -> !(
      normalizedConstant.constant == TestEnum.THIRD_CONSTANT ||
      normalizedConstant.constant == TestEnum.FOURTH_CONSTANT
    );

    assertEquals(
      sortedStrings(
        "First-Constant",
        "Second-Constant",
        "Fifth",
        "A",
        "Aa",
        "Ab",
        "Ac",
        "Ad",
        "Aac"
      ),
      TestEnum.matcher.createCompletions(null, thirdForthNegativeFilter)
    );

    assertEquals(
      sortedStrings(
        "First-Constant",
        "Second-Constant"
      ),
      TestEnum.matcher.createCompletions("cons", thirdForthNegativeFilter)
    );

    assertEquals(
      sortedStrings(
        "First-Constant"
      ),
      TestEnum.matcher.createCompletions("firs", thirdForthNegativeFilter)
    );
  }

  @Test
  public void shouldGetNormalizedNames() {
    assertEquals("First-Constant", TestEnum.matcher.getNormalizedName(TestEnum.FIRST_CONSTANT));
    assertEquals("Second-Constant", TestEnum.matcher.getNormalizedName(TestEnum.SECOND_CONSTANT));
    assertEquals("Third-Constant", TestEnum.matcher.getNormalizedName(TestEnum.THIRD_CONSTANT));
    assertEquals("Fourth-Constant", TestEnum.matcher.getNormalizedName(TestEnum.FOURTH_CONSTANT));
    assertEquals("Fifth", TestEnum.matcher.getNormalizedName(TestEnum.FIFTH));
  }

  @Test
  public void shouldMatchFirstUnfiltered() {
    assertEquals(TestEnum.FIRST_CONSTANT, Objects.requireNonNull(TestEnum.matcher.matchFirst("const")).constant);
    assertEquals(TestEnum.A, Objects.requireNonNull(TestEnum.matcher.matchFirst("a")).constant);
    assertEquals(TestEnum.AA, Objects.requireNonNull(TestEnum.matcher.matchFirst("aa")).constant);
  }

  @Test
  public void shouldMatchFirstFiltered() {
    var result = TestEnum.matcher.matchFirst(
      "a",
      normalizedConstant -> !(
        normalizedConstant.constant == TestEnum.A ||
        normalizedConstant.constant == TestEnum.AA
      )
    );
    assertEquals(TestEnum.AAC, Objects.requireNonNull(result).constant);
  }

  private List<String> sortedStrings(String... values) {
    return Arrays.stream(values).sorted().toList();
  }
}

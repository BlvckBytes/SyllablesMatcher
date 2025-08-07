package me.blvckbytes.syllables_matcher;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class EnumMatcherTests {

  enum TestEnum implements MatchableEnum {
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
    assertEquals(TestEnum.AB, Objects.requireNonNull(result).constant);
  }

  enum MyEnum implements MatchableEnum {
    PRICE,
    MIN_PRICE,
    MAX_PRICE
    ;

    private static final EnumMatcher<MyEnum> matcher = new EnumMatcher<>(values());
  }

  @Test
  public void shouldChooseExactMatch() {
    var result = MyEnum.matcher.matchFirst("Price");

    assertNotNull(result);
    assertEquals(MyEnum.PRICE, result.constant);
  }

  private List<String> sortedStrings(String... values) {
    return Arrays.stream(values).sorted(
      Comparator
        .comparingInt(String::length)
        .thenComparing(value -> value)
    ).toList();
  }
}

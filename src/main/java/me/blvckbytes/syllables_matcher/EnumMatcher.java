package me.blvckbytes.syllables_matcher;

import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;

public class EnumMatcher<T extends Enum<?>> {

  private final NormalizedConstant<T>[] normalizedConstants;
  private final Map<T, NormalizedConstant<T>> normalizedConstantByEnumConstant;

  @SuppressWarnings("unchecked")
  public EnumMatcher(T[] values) {
    this.normalizedConstants = new NormalizedConstant[values.length];
    this.normalizedConstantByEnumConstant = new HashMap<>();

    for (var i = 0; i < values.length; ++i) {
      var enumConstant = values[i];
      var normalizedConstant = new NormalizedConstant<>(enumConstant);

      this.normalizedConstants[i] = normalizedConstant;
      this.normalizedConstantByEnumConstant.put(enumConstant, normalizedConstant);
    }

    // Sort just like the client would, so that the first match is equal to
    // the first entry in the suggestion-list displayed to the user
    Arrays.sort(this.normalizedConstants, Comparator.comparing(NormalizedConstant::getNormalizedName));
  }

  public String getNormalizedName(T enumConstant) {
    return getNormalizedConstant(enumConstant).getNormalizedName();
  }

  public NormalizedConstant<T> getNormalizedConstant(T enumConstant) {
    return normalizedConstantByEnumConstant.get(enumConstant);
  }

  public List<String> createCompletions(@Nullable String input) {
    return createCompletions(input, null);
  }

  public List<String> createCompletions(@Nullable String input, @Nullable EnumPredicate<T> filter) {
    var result = new ArrayList<String>();

    forEachMatch(input, filter, match -> result.add(match.getNormalizedName()));

    return result;
  }

  public @Nullable NormalizedConstant<T> matchFirst(@Nullable String input) {
    return matchFirst(input, null);
  }

  public @Nullable NormalizedConstant<T> matchFirst(@Nullable String input, @Nullable EnumPredicate<T> filter) {
    return forEachMatch(input, filter, match -> false);
  }

  private @Nullable NormalizedConstant<T> forEachMatch(
    @Nullable String input,
    @Nullable EnumPredicate<T> filter,
    Function<NormalizedConstant<T>, Boolean> matchHandler
  ) {
    if (input == null) {
      for (var translationLanguage : normalizedConstants) {
        if (filter != null && !filter.test(translationLanguage))
          continue;

        if (!matchHandler.apply(translationLanguage))
          return translationLanguage;
      }

      return null;
    }

    var inputSyllables = Syllables.forString(input, Syllables.DELIMITER_SEARCH_PATTERN);

    var matcher = new SyllablesMatcher();
    matcher.setQuery(inputSyllables);

    for (var constantIndex = 0; constantIndex < normalizedConstants.length; ++constantIndex) {
      var constant = normalizedConstants[constantIndex];

      if (filter != null && !filter.test(constant))
        continue;

      if (constantIndex != 0)
        matcher.resetQueryMatches();

      matcher.setTarget(constant.getSyllables());
      matcher.match();

      if (matcher.hasUnmatchedQuerySyllables())
        continue;

      if (!matchHandler.apply(constant))
        return constant;
    }

    return null;
  }
}

package me.blvckbytes.syllables_matcher;

import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;

public class EnumMatcher<T extends MatchableEnum> {

  private final NormalizedConstant<T>[] normalizedConstants;
  private final Map<T, NormalizedConstant<T>> normalizedConstantByEnumConstant;

  @SuppressWarnings("unchecked")
  public EnumMatcher(Collection<T> values) {
    this.normalizedConstants = new NormalizedConstant[values.size()];
    this.normalizedConstantByEnumConstant = new HashMap<>();

    var normalizedConstantsIndex = 0;

    for (var enumConstant : values) {
      var normalizedConstant = new NormalizedConstant<>(enumConstant);

      this.normalizedConstants[normalizedConstantsIndex++] = normalizedConstant;
      this.normalizedConstantByEnumConstant.put(enumConstant, normalizedConstant);
    }

    // Sort just like the client would, so that the first match is equal to
    // the first entry in the suggestion-list displayed to the user
    Arrays.sort(this.normalizedConstants, Comparator.comparing(NormalizedConstant::getNormalizedName));
  }

  public EnumMatcher(T[] values) {
    this(Arrays.asList(values));
  }

  public String getNormalizedName(T enumConstant) {
    return getNormalizedConstant(enumConstant).getNormalizedName();
  }

  public NormalizedConstant<T> getNormalizedConstant(T enumConstant) {
    return normalizedConstantByEnumConstant.get(enumConstant);
  }

  public List<String> createCompletions(@Nullable String input) {
    return createCompletions(input, null, null, null);
  }

  public List<String> createCompletions(@Nullable String input, @Nullable String prefix, @Nullable String suffix) {
    return createCompletions(input, null, prefix, suffix);
  }

  public List<String> createCompletions(@Nullable String input, @Nullable EnumPredicate<T> filter) {
    return createCompletions(input, filter, null, null);
  }

  public List<String> createCompletions(@Nullable String input, @Nullable EnumPredicate<T> filter, String prefix, String suffix) {
    var result = new ArrayList<String>();

    forEachMatch(input, filter, match -> {
      var name = match.getNormalizedName();

      if (prefix != null)
        name = prefix + name;

      if (suffix != null)
        name = name + suffix;

      result.add(name);
      return true;
    });

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
      for (var normalizedConstant : normalizedConstants) {
        if (filter != null && !filter.test(normalizedConstant))
          continue;

        if (!matchHandler.apply(normalizedConstant))
          return normalizedConstant;
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

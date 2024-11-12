package me.blvckbytes.syllables_matcher;

public record SyllablesAndCounters(
  Syllables syllables,
  int numberOfWildcardSyllables,
  int numberOfNonWildcardSyllables
) {}

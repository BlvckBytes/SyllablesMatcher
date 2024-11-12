package me.blvckbytes.syllables_matcher;

@FunctionalInterface
public interface UnmatchedSyllableConsumer {

  void accept(Syllables holder, int syllable);

}

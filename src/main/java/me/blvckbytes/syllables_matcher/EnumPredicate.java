package me.blvckbytes.syllables_matcher;

import java.util.function.Predicate;

public interface EnumPredicate<T extends Enum<?>> extends Predicate<NormalizedConstant<T>> {}

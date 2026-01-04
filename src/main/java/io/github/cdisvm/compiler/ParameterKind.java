package io.github.cdisvm.compiler;

public enum ParameterKind {
    POSITIONAL_OR_KEYWORD,
    POSITIONAL_ONLY,
    KEYWORD_ONLY,
    VARGS,
    KWARGS;
}

package lpcCarpetAddition.Utils;

@SuppressWarnings("unused")
public record BracketPair(char left, char right) {
    public static final BracketPair PARENTHESES = new BracketPair('(', ')');
    public static final BracketPair SQUARE_BRACKETS = new BracketPair('[', ']');
    public static final BracketPair CURLY_BRACES = new BracketPair('{', '}');
}

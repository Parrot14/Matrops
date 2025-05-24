package matrops;

public enum Symbol {
    Sign,
    Number,
    Dot,
    Fraction,
    Multiplication,
    Literal,
    OpenParentheses,
    CloseParentheses,
    SpecialOperation;
    
    public static Symbol getSymbol(char c) {
        if (c == '$')
            return SpecialOperation;
        if (c == '+' || c == '-')
            return Sign;
        if (Character.isDigit(c))
            return Number;
        if (c == '.')
            return Dot;
        if (c == '/')
            return Fraction;
        if (c == '*')
            return Multiplication;
        if (c == '(')
            return OpenParentheses;
        if (c == ')')
            return CloseParentheses;
        if ("abcdefghijklmnopqrstuvwxyz".contains(Character.toLowerCase(c)+""))
            return Literal;
        return null;
    }
}

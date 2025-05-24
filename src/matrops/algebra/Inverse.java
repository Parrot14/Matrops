package matrops.algebra;

import matrops.Rational;

public class Inverse extends Expression{
    char literal;

    public Inverse(Rational coefficient, char literal){
        setCoefficient(coefficient);
        this.literal = literal;
    }

    public char getLiteral() {
        return literal;
    }

    public String toString() {
        return "Inverse("+getCoefficient().toString()+")("+literal+")";
    }
}
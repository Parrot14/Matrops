package matrops.algebra;

import matrops.Rational;

public class Monomial extends Expression{
    char literal;

    public Monomial(Rational coefficient, char literal){
        setCoefficient(coefficient);
        this.literal = literal;
    }

    public char getLiteral() {
        return literal;
    }

    public String toString() {
        return getCoefficient().toString()+" "+literal;
    }
}
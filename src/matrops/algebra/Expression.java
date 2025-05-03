package matrops.algebra;

import matrops.Rational;

public abstract class Expression{
    private Rational coefficient = new Rational(1);

    public void setCoefficient(Rational coefficient) {
        this.coefficient = coefficient;
    }
    public void multiplyCoefficient(Rational coefficient){
        this.coefficient.multiply(coefficient);
    }
    public Rational getCoefficient() {
        return coefficient;
    }
}

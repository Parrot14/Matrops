package matrops.algebra;

import java.util.Arrays;

public class Polinomial extends Expression{
    Expression[] terms;

    public Polinomial(Expression[] terms){
        this.terms = terms;
    }

    public Expression getTerm(int i) {
        return terms[i];
    }

    public int size(){
        return terms.length;
    }

    public String toString() {
        return "Polinomial( "+getCoefficient()+" )"+Arrays.toString(terms);
    }
}
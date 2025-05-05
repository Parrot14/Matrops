package matrops.algebra;

import java.util.Arrays;

public class Multiplication extends Expression{
    Expression[] factors;

    public Multiplication(Expression[] factors){
        this.factors = factors;
    }

    public Expression getFactor(int i){
        return factors[i];
    }

    public int size(){
        return factors.length;
    }

    public String toString() {
        return "Mul( "+getCoefficient()+" )"+Arrays.toString(factors);
    }
}
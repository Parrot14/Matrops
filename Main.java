package Matrops;

import Matrops.Rational;

public class Main {
    public static void main(String[] args) {
        Rational a = new Rational(10);
        Rational b = new Rational(-5);

        System.out.print(a+" * "+b+" = ");

        a.multiply(b);

        System.out.println(a);


    }
}

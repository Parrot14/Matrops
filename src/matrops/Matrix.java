package matrops;

import java.util.Scanner;

public class Matrix{
    private Rational[][] matrix;
    int f, c;

    public void fillData(){
        System.out.print("Filas: ");
        f = Util.getNumber();
        System.out.print("Columnas: ");
        c = Util.getNumber();

        matrix = new Rational[f][c];

        Scanner sc = new Scanner(System.in);
        System.out.println("Introduzca los valores...{int|float}[/int]");
        for (int i = 0; i < f; i++) {
            String line = "\t[ ";
            for (int j = 0; j < c; j++) {
                while(true){
                    System.out.print(line);
                    String input = sc.nextLine().trim();
                    Rational rational = Util.toRational(input);
                    if(rational == null){
                        System.out.println("Numero mal formado, intentelo de nuevo.");
                        continue;
                    }

                    line += input+" ] [ ";
                    matrix[i][j] = rational;
                    System.out.print("\u001B[A");
                    break;
                }
            }
            System.out.print(line.substring(0, line.length()-2)+"\n");
        }
    }

    public void copyData(Matrix m){
        if(!(m.f == f && m.c == c)){
            matrix = new Rational[m.f][m.c];
            f = m.f;
            c = m.c;
        }

        for (int i = 0; i < f; i++)
            for (int j = 0; j < c; j++){
                if (matrix[i][j] == null)
                    matrix[i][j] = new Rational(0);
                m.matrix[i][j].copyTo(matrix[i][j]);
            }
                
    }

    public String toString(){
        if (matrix == null)
            return "NULL";

        String[][] print = new String[f][c];
        int maxSize = 0;

        for (int i = 0; i < f; i++) {
            for (int j = 0; j < c; j++) {
                String str = matrix[i][j].toString();
                if (str.length() > maxSize) 
                    maxSize = str.length();
                print[i][j] = str;
            }
        }

        String string = ""; 

        for (int i = 0; i < f; i++) {
            for (int j = 0; j < c; j++)
                print[i][j] = ("%"+maxSize+"s").formatted(print[i][j]);
            string += "     | "+String.join(", ", print[i])+" |\n";
        }

        return string;
    }

    public void multiply(long n){
        for (Rational[] rationals : matrix) {
            for (Rational rational : rationals) {
                rational.multiply(n);
            }
        }
    }

    public void multiply(Rational r){
        for (Rational[] rationals : matrix) {
            for (Rational rational : rationals) {
                rational.multiply(r);
            }
        }
    }

    public boolean multiply(Matrix m){
        if (this.c != m.f)
            return false;
        
        Rational[][] product = new Rational[this.f][m.c];
        Rational aux = new Rational(1);
        for (int pf = 0; pf < this.f; pf++) {
            for (int pc = 0; pc < m.c; pc++) {
                Rational acc = new Rational(0);
                for (int i = 0; i < this.c; i++) {
                    acc.add(matrix[pf][i].copyTo(aux).multiply(m.matrix[i][pc]));
                }
                product[pf][pc] = acc;
            }
        }
        this.matrix = product;
        this.c = m.c;
        return true;
    }

    public boolean sum(Matrix m){
        if (!(m.c == c && m.f == f))
            return false;
        for (int i = 0; i < f; i++)
            for (int j = 0; j < c; j++)
                matrix[i][j].add(m.matrix[i][j]);
        return true;
    }
}

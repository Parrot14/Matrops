package matrops;

import java.util.Scanner;

public class Matrix{
    private Rational[][] matrix;
    int f, c;

    public int getRows(){
        return f;
    }

    public int getColumns(){
        return c;
    }

    public void setRows(int rows) {
        this.f = rows;
    }

    public void setColumns(int columns) {
        this.c = columns;
    }

    public boolean isSquare(){
        return c == f;
    }

    public void fillRows(){
        System.out.print("Filas: ");
        f = Util.getNumber();
    }

    public void fillColumns(){
        System.out.print("Columnas: ");
        c = Util.getNumber();
    }

    public void fillMatrix(){
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

    public void fillData(){
        fillRows();
        fillColumns();
        fillMatrix();
    }

    // Copy data from m
    public Matrix copyData(Matrix m){
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
        return this;
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

    public Rational getRational(int row, int column){
        return matrix[row][column];
    }

    public void mulSum(Rational mul, int row_mul, int row_sum){
        Rational[] rowMul = matrix[row_mul];
        Rational[] rowSum = matrix[row_sum];
        Rational aux = new Rational(0);

        for (int i = 0; i < c; i++) {
            rowMul[i].copyTo(aux).multiply(mul);
            rowSum[i].add(aux);
        }
    }

    public static Matrix identity(int size){
        if (size <= 0)
            return null;
        Matrix identity = new Matrix();
        identity.c = size;
        identity.f = size;

        identity.matrix = new Rational[size][size];

        for (int i = 0; i < size; i++)
            for (int j = 0; j < size; j++)
                if (i == j)
                    identity.matrix[i][j] = new Rational(1);
                else
                    identity.matrix[i][j] = new Rational(0);

        return identity;
    }

    public static Matrix elemental(int size, Rational mul, int row_mul, int row_sum ){
        if (row_mul >= size || row_mul < 0 ||
            row_sum >= size || row_sum < 0 ||
            row_mul == row_sum)
            return null;

        Matrix elemental = identity(size);

        elemental.matrix[row_sum][row_mul] = mul.copy();

        return elemental;
    }

    public static Matrix elemental(int size, Rational mul, int row_mul){
        if (row_mul >= size || row_mul < 0)
            return null;


        Matrix elemental = identity(size);
        
        elemental.matrix[row_mul][row_mul].multiply(mul);
    
        return elemental;
    }

    public static Matrix elemental(int size, int row_switch, int row_switch2){
        if (row_switch >= size || row_switch < 0 ||
            row_switch2 >= size || row_switch2 < 0 ||
            row_switch == row_switch2)
            return null;

        Matrix elemental = identity(size);

        Rational[] aux = elemental.matrix[row_switch];
        elemental.matrix[row_switch] = elemental.matrix[row_switch2];
        elemental.matrix[row_switch2] = aux;

        return elemental;
    }
}

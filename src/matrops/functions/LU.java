package matrops.functions;

import matrops.Matrix;
import matrops.Rational;

public class LU {
    Matrix matrix;
    Matrix lower;
    Matrix b;
    int size;

    private LU(){}
    
    public static LU createLU(Matrix a, Matrix b){
        LU lu = new LU();

        lu.matrix = new Matrix().copyData(a);
        lu.size = lu.matrix.getRows();
        lu.lower = Matrix.identity(lu.size);

        lu.b = b;

        return lu;
    }

    public void computeLU(){
        for (int col = 0; col < matrix.getColumns()-1; col++) {
            Rational dia = matrix.getRational(col, col);
            if (dia.isZero()) {
                System.out.println("Sin solución.");
                return;
            }

            System.out.println(matrix.toString());

            dia = dia.copy().mInverse();
            for (int row = col+1; row < matrix.getRows(); row++) {
                Rational zeroRational = matrix.getRational(row, col);
                if (!zeroRational.isZero()){
                    zeroRational = zeroRational.copy().multiply(dia);
                    lower.multiply(Matrix.elemental(size, zeroRational, col, row));
                    matrix.mulSum(zeroRational.aInverse(), col, row);
                    System.out.println(zeroRational+"R"+(col+1)+"+R"+(row+1));
                    System.out.println(matrix);
                }
            }
        }

        System.out.println("Upper");
        System.out.println(matrix.toString());
        System.out.println("Lower");
        System.out.println(lower.toString());
        System.out.println("\t\tL * y = B");

        for (int f = 0; f < lower.getRows(); f++) {
            for (int c = 0; c < f; c++)
                System.out.print(lower.getRational(f, c).toSignedString()+" y"+(c+1));
            System.out.print(lower.getRational(f, f).toSignedString()+" y"+(f+1));
            System.out.print(" = "+b.getRational(f, 0).toString()+" ...("+(f+1)+")"+"\n");
        }

        Rational[] y_result = new Rational[size];
        Rational[] aux_matrix = new Rational[size];

        for (int f = 0; f < lower.getRows(); f++) {
            System.out.println("\n\t("+(f+1)+")");
            for (int i = 0; i < f; i++) {
                Rational aux = lower.getRational(f, i).copy();

                System.out.print(aux.toSignedString()+"("+y_result[i]+")");

                aux.multiply(y_result[i]);
                aux_matrix[i] = aux;
            }

            Rational b_value = b.getRational(f, 0).copy();
            Rational lower_value = lower.getRational(f, f).copy();

            System.out.print(lower_value.toSignedString()+" y"+(f+1)+" = "+b_value.toString());

            System.out.print("\n");

            for (int i = 0; i < f; i++) {
                System.out.print(aux_matrix[i].toSignedString());
            }

            System.out.print(lower_value.toSignedString()+" y"+(f+1)+" = "+b_value.toString());

            System.out.print("\n");

            System.out.print((lower_value.isOne()?"":lower_value.toString())+" y"+(f+1)+" = "+b_value.toString());

            for (int i = 0; i < f; i++) {
                b_value.add(aux_matrix[i].aInverse());
                System.out.print(aux_matrix[i].toSignedString());
            }

            System.out.print("\n");
            System.out.print(lower_value.toString()+" y"+(f+1)+" = "+b_value.toString());

            if (!lower_value.isOne()) {
                System.out.print("\n");
                System.out.print("y"+(f+1)+" = "+b_value.toString()+"("+lower_value.mInverse().toString()+")");
                b_value.multiply(lower_value);

                System.out.print("\n");
                System.out.println("y"+(f+1)+" = "+b_value.toString());
            }

            System.out.print("\n");

            y_result[f] = b_value;
        }

        System.out.print("\n");

        System.out.println("y =");
        for (int i=0; i < size;i++){
            System.out.println("|"+y_result[i].toString()+"|");
        }

        System.out.print("\n");

        System.out.println("\t\tU * x = y");

        for (int f = 0; f < matrix.getRows(); f++) {
            System.out.print(matrix.getRational(f, f).toString()+" x"+(f+1));
            for (int c = f+1; c < size; c++)
                System.out.print(matrix.getRational(f, c).toSignedString()+" x"+(c+1));
            System.out.print("="+y_result[f].toString()+" ...("+(f+1)+")"+"\n");
        }

        Rational[] x_result = new Rational[size];

        for (int f = size-1; f >= 0; f--) {
            System.out.println("\n\t("+(f+1)+")");

            Rational result_value = y_result[f].copy();
            Rational upper_value = matrix.getRational(f, f).copy();

            System.out.print(upper_value.toString()+" x"+(f+1));

            for (int i = f+1; i < size; i++) {
                Rational aux = matrix.getRational(f, i).copy();

                System.out.print(aux.toSignedString()+"("+x_result[i]+")");

                aux.multiply(x_result[i]);
                aux_matrix[i] = aux;
            }

            System.out.print(" = "+result_value.toString());

            System.out.print("\n");

            System.out.print(upper_value.toString()+" x"+(f+1));

            for (int i = f+1; i < size; i++) {
                System.out.print(aux_matrix[i].toSignedString());
            }

            System.out.print(" = "+result_value.toString());

            System.out.print("\n");

            System.out.print((upper_value.isOne()?"":upper_value.toString())+" x"+(f+1)+" = "+result_value.toString());

            for (int i = f+1; i < size; i++) {
                result_value.add(aux_matrix[i].aInverse());
                System.out.print(aux_matrix[i].toSignedString());
            }

            System.out.print("\n");
            System.out.print(upper_value.toString()+" x"+(f+1)+" = "+result_value.toString());

            if (!upper_value.isOne()) {
                System.out.print("\n");
                System.out.print("x"+(f+1)+" = "+result_value.toString()+"("+upper_value.mInverse().toString()+")");
                result_value.multiply(upper_value);

                System.out.print("\n");
                System.out.println("x"+(f+1)+" = "+result_value.toString());
            }

            System.out.print("\n");

            x_result[f] = result_value;
        }

        for (int i=0; i < size;i++){
            System.out.println("x"+(i+1)+" = "+x_result[i].toString());
        }

        System.out.print("\n");
    }
}

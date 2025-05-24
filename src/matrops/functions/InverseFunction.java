package matrops.functions;

import matrops.Matrix;
import matrops.Rational;

public class InverseFunction {
    private Matrix matrix, identity;
    private Rational aux = new Rational(0);
    private Rational aux2 = new Rational(0);
    private int order;
    private String[] ops;

    public InverseFunction(Matrix matrix){
        this.matrix = matrix;
        this.order = matrix.getRows();
        
        this.identity = Matrix.identity(order);

        ops = new String[order];

        for (int i = 0; i < order; i++) {
            ops[i] = "";
        }
    }

    public Matrix getInverse(){
        return identity;
    }

    public boolean compute(){
        if (!matrix.isSquare())
            return false;
        if(!computeGauss())
            return false;
        computeJordan();
        return true;
    }

    private void computeJordan(){
        for (int i = order-1; i >= 0; i--) {
            boolean print = false;
            for (int j = i-1; j >= 0; j--) {
                if(!matrix.getRational(j, i).equals(Rational.ZERO)){
                    matrix.getRational(j, i).copyTo(aux).aInverse();
                    mulSum(aux, i, j);
                    print = true;
                }
            }
            if (print)
                System.out.println(this);
        }
        System.out.println("----------------------------------------------------");
    }

    private boolean computeGauss(){
        System.out.println("------------- Inverso por eliminacion GAUSSIANA ----");
        System.out.println(this);
        for (int i = 0; i < order; i++) {
            // See if there is a single variable equation(shortcut)
            int row_ready = findReadyRow(i);
            if(row_ready != -1){
                // Prevent switching row with itselft
                if(row_ready != i){
                    switchRows(i, row_ready);
                    System.out.println(this);
                }
                // Ensure coefficient equals to one
                if(!matrix.getRational(i, i).equals(Rational.ONE)){
                    matrix.getRational(i, i).copyTo(aux).mInverse();
                    multiplyRow(aux, i);
                    System.out.println(this);
                }
            // Ensure coefficient equals to one
            }else if(!matrix.getRational(i, i).equals(Rational.ONE)){
                // See if there is a row with coefficient equals to one
                int findone = findBelow(i, Rational.ONE);
                if(findone != -1){
                    switchRows(i, findone);
                }else{
                    if(!ensureNotZero(i))
                        return false;
                    matrix.getRational(i, i).copyTo(aux).mInverse();
                    multiplyRow(aux, i);
                }
                System.out.println(this);
            }

            boolean print = false;
            for (int j = i+1; j < order; j++) {
                if(!matrix.getRational(j, i).equals(Rational.ZERO)){
                    matrix.getRational(j, i).copyTo(aux).aInverse();
                    mulSum(aux, i, j);
                    print = true;
                }
            }
            if (print)
                System.out.println(this);
        }
        return true;
    }

    private boolean ensureNotZero(int index){
        // Ensure coefficient is not zero
        if(matrix.getRational(index, index).equals(Rational.ZERO)){
            int findany = findFirstDiferentBelow(index, Rational.ZERO);
            if(findany != -1){
                switchRows(index, findany);
                System.out.println(this);
            }else{
                System.out.println("\t\t\t----- SIN SOLUCIÓN UNICA -----");
                return false;
            }
        }
        return true;
    }

    public int findReadyRow(int index){
        for (int i = index; i < order; i++)
            if(isRowReady(i, index))
                return i;
        return -1;
    }
    
    public boolean isRowReady(int nindex, int mindex){
        if(matrix.getRational(nindex, mindex).isZero())
            return false;
        for(int i = mindex+1; i < order; i++)
            if(!matrix.getRational(nindex, i).isZero())
                return false;
        return true;
    }

    // public boolean isRowZero(int index){
    //     for (Rational rational : extendedMatrix[index])
    //         if(!rational.isZero())
    //             return false;
    //     return true;
    // }

    public int findFirstDiferentBelow(int dindex, Rational not){
        for (int i = dindex+1; i < this.order; i++) {
            if(!matrix.getRational(i, dindex).equals(not)){
                return i;
            }
        }
        return -1;
    }

    public int findBelow(int dindex, Rational num){
        for (int i = dindex+1; i < this.order; i++) {
            if(matrix.getRational(i, dindex).equals(num)){
                return i;
            }
        }
        return -1;
    }

    public void switchRows(int a, int b){
        matrix.switchRows(a, b);
        identity.switchRows(a, b);
        ops[a]="R"+(a+1)+","+(b+1)+"\t";
    }

    public void multiplyRow(Rational mul, int index){
        matrix.multiplyRow(mul, index);
        identity.multiplyRow(mul, index);
        ops[index]=mul+" R"+(index+1)+"\t";
    }

    public void mulSum(Rational mul, int indexA, int indexB){
        matrix.mulSum(mul, indexA, indexB);
        identity.mulSum(mul, indexA, indexB);
        ops[indexB]=mul+" R"+(indexA+1)+"+R"+(indexB+1)+"\t";
    }

    @Override
    public String toString() {
        String str = "";
        for (int i = 0; i < order; i++) {
            str+= "%20s  ".formatted(ops[i].trim());
            ops[i] = "";
            str+= "%-3s |".formatted("R"+(i+1));
            for (int j = 0; j < order; j++) {
                str += "%14s |" .formatted(matrix.getRational(i,j));
            }
            str += '|';
            for (int j = 0; j < order; j++) {
                str += "%14s |" .formatted(identity.getRational(i,j));
            }
            str += '\n';
        }
        return str;
    }
}

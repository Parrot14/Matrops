package matrops;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import matrops.algebra.Expression;
import matrops.algebra.Inverse;
import matrops.algebra.Monomial;
import matrops.algebra.Multiplication;
import matrops.algebra.Polinomial;
import matrops.functions.InverseFunction;
import matrops.functions.LU;

public class Terminal {
    private HashMap<Character,Matrix> data = new HashMap<Character,Matrix>();
    private final Pattern equalPattern = Pattern.compile("^(\\w=)?(.+)?$");
    private final Pattern opsPattern = Pattern.compile("\\G(^|\\+|-)(?:(\\d+)(?:\\.(\\d+))?(?:\\/(\\d+))?)?([a-zA-Z])");//"\\G(^|\\+|-)(\\d+)*([a-zA-Z])+");
    private final char accumulator = ' ', helper = '?';
    private boolean show_expr = false;

    public Terminal(){
        data.put(accumulator, new Matrix());
        data.put(helper, new Matrix());
    }

    public ExitCode print(){
        System.out.print(" Variables: ");
        for (Character matrix : data.keySet()) {
            if (matrix == accumulator||matrix == helper)
                continue;
            System.out.print(" "+matrix);
        }
        System.out.print("\n > ");

        Scanner sc = new Scanner(System.in);
        String input = sc.nextLine().trim();

        if (input.startsWith("/")) {
            String cmd[] = input.split(" ");
            switch (cmd[0]) {
                case "/help" -> {
                    System.out.println( " \tCOMANDOS\n"+
                                        " /help: imprime esta pagina\n"+
                                        " /exit: salir del programa(sin confirmacion, todos los datos se perderan)\n"+
                                        " /lu (a) (b): resolver sistema lineal por factorización LU\n"+
                                        " /showexpr: deja de resolver formulas y solo mostrar representación logica interna\n"+
                                        " \n\tOPERACIONES\n"+
                                        " $inv/(a)$: obtener inversa por eliminacion Gaussiana");
                    return ExitCode.OK;}
                case "/exit" -> {return ExitCode.EXIT;}
                case "/lu" -> {
                    if (cmd.length != 3) {
                        System.out.println("Comando mal formado");
                        return ExitCode.BAD;
                    }

                    if (cmd[1].length() != 1 || cmd[2].length() != 1) {
                        System.out.println("Identificador de matriz invalido");
                    }

                    Character a_id = cmd[1].charAt(0);
                    Character b_id = cmd[2].charAt(0);
                    Matrix a_matrix = data.get(a_id);
                    Matrix b_matrix = data.get(b_id);

                    if (a_matrix == null || b_matrix == null){
                        System.out.println("Matriz no registrada");
                        return ExitCode.BAD;
                    }

                    if (!a_matrix.isSquare()) {
                        System.out.println("Matriz no cuadrada");
                        return ExitCode.BAD;
                    }

                    if (b_matrix.getColumns() != 1 || b_matrix.getRows() != a_matrix.getRows()) {
                        System.out.println("Matriz b no valida");
                        return ExitCode.BAD;
                    }
                
                    LU lu = LU.createLU(a_matrix,b_matrix);
                    lu.computeLU();
                    return ExitCode.OK;
                }
                case "/showexpr" ->{
                    show_expr = !show_expr;
                    if (show_expr)
                        System.out.println("Mostrando expresiones en lugar de resultados");
                    else
                        System.out.println("Mostrando resultados");
                    return ExitCode.OK;
                }
                default -> {
                    System.out.println("Comando invalido");
                    return ExitCode.BAD;
                }
            }
        }
        
        Matcher equalMatcher = equalPattern.matcher(input);
        if (!equalMatcher.matches())
            return ExitCode.BAD;
        String equal = equalMatcher.group(1);
        String ops = equalMatcher.group(2);
        Character saveTo = accumulator;// Default accumulator

        if (!(equal != null || ops != null))
            return ExitCode.BAD;

        if (equal != null)
            saveTo = equal.charAt(0);

        Matrix equalMatrix = getMatrix(saveTo);

        if (ops == null){
            equalMatrix.fillData();
        }else{

            Expression expr = extractAlgebraicExpression(ops);

            if (expr == null){
                System.out.println("ERR: Error al computar operación (expresión mal formada)");
                return ExitCode.BAD;
            }

            if (!show_expr) {
                Matrix result = computeExpression(expr);

                if (result == null){
                    System.out.println("ERR: Error al computar operación (suma de matrices de orden distinto, multiplicacion incompatible o uso de matriz no registrada)");
                    return ExitCode.BAD;
                }

                equalMatrix.copyData(result);
            }else
                System.out.println(expr);
        }

        if (!equalMatrix.isNull())
            System.out.println(equalMatrix.toString().replaceFirst("    ", " "+saveTo+" ="));
        else
            data.remove(saveTo);

        return ExitCode.OK;
    }

    private Matrix computeExpression(Expression expr){
        if (expr instanceof Monomial) {
            Monomial mono = (Monomial) expr;
            if (!data.containsKey(mono.getLiteral())){
                System.out.println("La matriz '"+mono.getLiteral()+"' no existe");
                return null;
            }
            Matrix res = new Matrix();
            res.copyData(data.get(mono.getLiteral()));
            res.multiply(mono.getCoefficient());
            return res;
        }else if (expr instanceof Multiplication) {
            Multiplication mul = (Multiplication) expr;
            Matrix res = computeExpression(mul.getFactor(0));
            if (res == null)
                    return null;
            for (int i = 1; i < mul.size(); i++) {
                Matrix temp = computeExpression(mul.getFactor(i));
                if (temp == null)
                    return null;
                if (!res.multiply(temp))
                    return null;
            }
            res.multiply(mul.getCoefficient());
            return res;
        }else if (expr instanceof Polinomial) {
            Polinomial pol = (Polinomial) expr;
            Matrix res = computeExpression(pol.getTerm(0));
            if (res == null)
                    return null;
            for (int i = 1; i < pol.size(); i++) {
                Matrix temp = computeExpression(pol.getTerm(i));
                if (temp == null)
                    return null;
                if (!res.sum(temp))
                    return null;
            }
            res.multiply(pol.getCoefficient());
            return res;
        }else if (expr instanceof Inverse){
            Inverse inv = (Inverse) expr;
            if (!data.containsKey(inv.getLiteral())){
                System.out.println("La matriz '"+inv.getLiteral()+"' no existe");
                return null;
            }
            Matrix res = new Matrix();
            res.copyData(data.get(inv.getLiteral()));

            InverseFunction func = new InverseFunction(res);

            if (!func.compute()) {
                System.out.println("La matriz '"+inv.getLiteral()+"' no es invertible");
                return null;
            }

            Matrix inv_mat = func.getInverse();

            inv_mat.multiply(inv.getCoefficient());

            return inv_mat;
        }
        return null;
    }

    private final Step start = op();

    public Expression extractAlgebraicExpression(String str){
        EnumMap<Register, String> registers = new EnumMap<>(Register.class);
        registers.put(Register.Sign, "+");
        Register numberRegister = Register.Integer;
        String number = "";
        boolean endOfTerm = false;
        Step step = this.start;

        EnumMap<Operation, ArrayList<Expression>> opsMap = new EnumMap<>(Operation.class);
        opsMap.put(Operation.Multiplication, new ArrayList<>());
        opsMap.put(Operation.Sum, new ArrayList<>());

        Operation operation = Operation.Sum;

        

        for (int i = 0; i < str.length(); i++) {
            char ch = str.charAt(i);
            Symbol symbol = Symbol.getSymbol(ch);

            step = step.path.get(symbol);
            if (step == null)
                return null;
            if (step.chReg)
                numberRegister = step.reg;
            if (step.chOp && operation != step.op){
                operation = step.op;
                switch (operation) {
                    case Sum ->{
                        ArrayList<Expression> multiplication = opsMap.get(Operation.Multiplication);
                        Expression[] factors = new Expression[multiplication.size()];
                        multiplication.toArray(factors);
                        multiplication.clear();
                        opsMap.get(Operation.Sum).add(new Multiplication(factors));
                    }
                    case Multiplication ->{
                        ArrayList<Expression> sum = opsMap.get(Operation.Sum);
                        opsMap.get(Operation.Multiplication).add(sum.remove(sum.size()-1));
                    }
                }
            }
            if (step.reset){
                registers.clear();
                registers.put(Register.Sign, "+");
                registers.remove(Register.Integer);
                numberRegister = Register.Integer;
                number = "";
            }
            endOfTerm = step.endOfTerm;

            if (symbol == Symbol.Sign){
                registers.put(Register.Sign, ch+"");
            }else if (symbol == Symbol.SpecialOperation){
                boolean end = true;
                String op = "";
                for (int j = i+1; j < str.length(); j++) {
                    char d = str.charAt(j);
                    if(d=='$'){
                        i = j;
                        end = false;
                        break;
                    }
                    op += d;
                }
                if (end)
                    return null;
                
                String op_opt[] = op.split("/");

                Expression expr;

                String numerator_str = registers.remove(Register.Integer);
                numerator_str = registers.remove(Register.Sign)+(numerator_str==null?"":numerator_str);
                String decimal_str = registers.remove(Register.Decimal);
                String denominator_str = registers.remove(Register.Denominator);

                Rational rational = Util.toRational(numerator_str, decimal_str, denominator_str);
                
                if (rational == null)
                    return null;

                switch (op_opt[0]) {
                    case "inv"->{
                        if (op_opt.length!=2 ||
                            op_opt[1].length()!=1) {
                            System.out.println("La operacion especial '"+op+"' malformada");
                            return null;
                        }
                        expr = new Inverse(rational,op_opt[1].charAt(0));
                    }
                    default->{
                        System.out.println("La operacion especial '"+op_opt+"' no existe");
                        return null;
                    }
                }

                opsMap.get(operation).add(expr);
            }else if (symbol == Symbol.Number){
                boolean end = true;
                for (int j = i; j < str.length(); j++) {
                    char d = str.charAt(j);
                    if(!Character.isDigit(d)){
                        i = j-1;
                        end = false;
                        break;
                    }
                    number += d;
                }
                if (end)
                    return null;
                registers.put(numberRegister, number);
                number = "";
            }else if (symbol == Symbol.OpenParentheses){
                int level = 1;
                int close = -1;
                for (int j = i+1; j < str.length(); j++) {
                    char d = str.charAt(j);
                    if (d == '(')
                        level++;
                    else if(d == ')'){
                        level--;
                        if (level == 0) {
                            close = j;
                            break;
                        }
                    }
                }
                if (close == -1)
                    return null;
                System.out.println(str.substring(i+1, close));
                Expression expr = extractAlgebraicExpression(str.substring(i+1, close));

                if (expr == null)
                    return null;
                String numerator_str = registers.remove(Register.Integer);
                numerator_str = registers.remove(Register.Sign)+(numerator_str==null?"":numerator_str);
                String decimal_str = registers.remove(Register.Decimal);
                String denominator_str = registers.remove(Register.Denominator);

                Rational rational = Util.toRational(numerator_str, decimal_str, denominator_str);
                
                if (rational == null)
                    return null;

                expr.multiplyCoefficient(rational);

                opsMap.get(operation).add(expr);
                i = close-1;
            }else if (symbol == Symbol.Literal) {
                String numerator_str = registers.remove(Register.Integer);
                numerator_str = registers.remove(Register.Sign)+(numerator_str==null?"":numerator_str);
                String decimal_str = registers.remove(Register.Decimal);
                String denominator_str = registers.remove(Register.Denominator);

                Rational rational = Util.toRational(numerator_str, decimal_str, denominator_str);

                if (rational == null)
                    return null;

                Expression expr = new Monomial(rational, ch);
                opsMap.get(operation).add(expr);
            }
        }

        if (!endOfTerm) {
            return null;
        }

        ArrayList<Expression> multiplication = opsMap.get(Operation.Multiplication);
        ArrayList<Expression> sum = opsMap.get(Operation.Sum);

        if (!multiplication.isEmpty()){
            Expression[] factors = new Expression[multiplication.size()];
            multiplication.toArray(factors);
            sum.add(new Multiplication(factors));
        }

        if (sum.isEmpty())
            return null;

        if (sum.size() == 1)
            return sum.get(0);

        Expression[] terms = new Expression[sum.size()];
        sum.toArray(terms);
        return new Polinomial(terms);
    }

    private enum Operation{
        Sum,
        Multiplication;
    }

    private enum Register{
        Sign,
        Integer,
        Decimal,
        Denominator;

    }

    private class Step{
        EnumMap<Symbol, Step> path = new EnumMap<>(Symbol.class);
        boolean endOfTerm = false;
        boolean chReg = false;
        Register reg;
        boolean reset = false;

        boolean chOp = false;
        Operation op;

        void changeRegister(Register reg){
            chReg = true;
            this.reg = reg;
        }

        void changeOperation(Operation op){
            chOp = true;
            this.op = op;
        }

        void reset(){
            reset = true;
        }

        void endOfTerm(){
            endOfTerm = true;
        }
    }

    private Step op(){
        Step start = new Step();
        Step sign = new Step();
        Step adition = new Step();
        Step integer = new Step();
        Step fraction = new Step();
        Step numerator_decimal = new Step();
        Step dot = new Step();
        //Step decimal = new Step();
        Step openParentheses = new Step();
        Step closeParentheses = new Step(); /// END
        closeParentheses.endOfTerm();
        Step literal = new Step();          /// END
        literal.endOfTerm();
        Step multiplication = new Step();
        Step specialOperation = new Step();
        specialOperation.endOfTerm();

        start.path.put(Symbol.Sign, sign);
        start.path.put(Symbol.Number, integer);
        start.path.put(Symbol.Dot, dot);                                  /// Adition         -> Dot
        start.path.put(Symbol.OpenParentheses, openParentheses);          /// Adition         -> OpenParentheses
        start.path.put(Symbol.Literal, literal);
        start.path.put(Symbol.SpecialOperation, specialOperation);

        adition.reset();
        adition.changeOperation(Operation.Sum);
        adition.path.put(Symbol.Number, integer);                           /// Adition         -> Integer 
        adition.path.put(Symbol.Dot, dot);                                  /// Adition         -> Dot
        adition.path.put(Symbol.OpenParentheses, openParentheses);          /// Adition         -> OpenParentheses
        adition.path.put(Symbol.Literal, literal);                          /// Adition         -> Literal
        adition.path.put(Symbol.SpecialOperation, specialOperation);        /// Adition         -> SpecialOperation

        integer.path.put(Symbol.Dot, dot);                                  /// Integer         -> Dot
        integer.path.put(Symbol.Fraction, fraction);                        /// Integer         -> Fraction
        integer.path.put(Symbol.OpenParentheses, openParentheses);          /// Integer         -> OpenParentheses
        integer.path.put(Symbol.Literal, literal);                          /// Integer         -> Literal
        integer.path.put(Symbol.SpecialOperation, specialOperation);        /// Integer         -> SpecialOperation

        fraction.changeRegister(Register.Denominator);
        fraction.path.put(Symbol.Number, numerator_decimal);                /// Fraction        -> Numerator

        numerator_decimal.path.put(Symbol.Literal, literal);                /// Numerator/Decimal-> Literal
        numerator_decimal.path.put(Symbol.OpenParentheses, openParentheses);/// Numerator/Decimal-> OpenParentheses
        numerator_decimal.path.put(Symbol.SpecialOperation, specialOperation);/// Numerator/Decimal-> SpecialOperation

        dot.changeRegister(Register.Decimal);
        dot.path.put(Symbol.Number, numerator_decimal);                                  /// Dot             -> Decimal

        openParentheses.path.put(Symbol.CloseParentheses, closeParentheses);/// OpenParentheses -> CloseParentheses

        closeParentheses.path.put(Symbol.Multiplication, multiplication);
        closeParentheses.path.put(Symbol.Sign, adition);

        multiplication.reset();
        multiplication.changeOperation(Operation.Multiplication);
        multiplication.path.put(Symbol.Sign, sign);                         /// Multiplication  -> Integer 
        multiplication.path.put(Symbol.Number, integer);                    /// Multiplication  -> Integer 
        multiplication.path.put(Symbol.Dot, dot);                           /// Multiplication  -> Dot
        multiplication.path.put(Symbol.OpenParentheses, openParentheses);   /// Multiplication  -> OpenParentheses
        multiplication.path.put(Symbol.Literal, literal);
        multiplication.path.put(Symbol.SpecialOperation, specialOperation);

        sign.path.put(Symbol.Number, integer);                              /// Sign            -> Integer 
        sign.path.put(Symbol.Dot, dot);                                     /// Sign            -> Dot
        sign.path.put(Symbol.OpenParentheses, openParentheses);             /// Sign            -> OpenParentheses
        sign.path.put(Symbol.Literal, literal);                             /// Sign            -> Literal
        sign.path.put(Symbol.SpecialOperation, specialOperation);                             /// Sign            -> Literal

        literal.path.put(Symbol.Sign, adition);
        literal.path.put(Symbol.Multiplication, multiplication);

        specialOperation.path.put(Symbol.Sign, adition);
        specialOperation.path.put(Symbol.Multiplication, multiplication);

        return start;
    }

    private Matrix getMatrix(Character ch){
        if (data.containsKey(ch)){
            return data.get(ch);
        }else{
            Matrix temp = new Matrix();
            data.put(ch, temp);
            return temp;
        }
    }

    public enum ExitCode{
        OK,
        BAD,
        EXIT;
    }
}
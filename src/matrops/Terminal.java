package matrops;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import matrops.algebra.Expression;
import matrops.algebra.Monomial;
import matrops.algebra.Multiplication;
import matrops.algebra.Polinomial;

public class Terminal {
    private HashMap<Character,Matrix> data = new HashMap<Character,Matrix>();
    private final Pattern equalPattern = Pattern.compile("^(\\w=)?(.+)?$");
    private final Pattern opsPattern = Pattern.compile("\\G(^|\\+|-)(?:(\\d+)(?:\\.(\\d+))?(?:\\/(\\d+))?)?([a-zA-Z])");//"\\G(^|\\+|-)(\\d+)*([a-zA-Z])+");
    private final char accumulator = ' ', helper = '?';

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

        switch (input.toLowerCase()) {
            case "help" -> System.out.println(" help: print this page\n exit: exit the program(no confirmation, all data will be loss)");
            case "exit" -> {return ExitCode.EXIT;}
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
            System.out.println(extractAlgebraicExpression(ops));

            Matcher opsMatcher = opsPattern.matcher(ops);
            int lastMatchPos = 0;
            if (opsMatcher.find()) {
                char matrix = opsMatcher.group(5).charAt(0);
                if (data.containsKey(matrix)){
                    equalMatrix.copyData(data.get(matrix));
                    Rational r = Util.toRational(opsMatcher.group(1)+(opsMatcher.group(2)==null?"1":opsMatcher.group(2)), opsMatcher.group(3), opsMatcher.group(4));
                    if (r == null) {
                        System.out.println("ERR: Numero malformado");
                        return ExitCode.BAD;    
                    }
                    equalMatrix.multiply(r);
                }else{
                    System.out.println("ERR: La matriz "+matrix+" no esta registrada");
                    return ExitCode.BAD;
                }
                lastMatchPos = opsMatcher.end();
            }
            Matrix temp = new Matrix();
            while (opsMatcher.find()) {
                char matrix = opsMatcher.group(5).charAt(0);
                if (data.containsKey(matrix)){
                    temp.copyData(data.get(matrix));
                    Rational r = Util.toRational(opsMatcher.group(1)+opsMatcher.group(2), opsMatcher.group(3), opsMatcher.group(4));
                    if (r == null) {
                        System.out.println("ERR: Numero malformado");
                        return ExitCode.BAD;    
                    }
                    temp.multiply(r);
                    if (!equalMatrix.sum(temp)) {
                        System.out.println("ERR: No puede sumarse matrices de tamaño diferente");
                        return ExitCode.BAD;
                    }
                }else{
                    System.out.println("ERR: La matriz "+matrix+" no esta registrada");
                    return ExitCode.BAD;
                }
                lastMatchPos = opsMatcher.end();
            }
            if (lastMatchPos < ops.length()) {
                System.out.println("ERR: Operaciones Malformadas");
                return ExitCode.BAD;
            }
        }

        System.out.println(equalMatrix.toString().replaceFirst("    ", " "+saveTo+" ="));

        return ExitCode.OK;
    }

    private final Step start = op();

    public Expression extractAlgebraicExpression(String str){
        EnumMap<Register, String> registers = new EnumMap<>(Register.class);
        registers.put(Register.Sign, "+");
        registers.put(Register.Integer, "1");
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
                registers.put(Register.Integer, "1");
                numberRegister = Register.Integer;
                number = "";
            }
            endOfTerm = step.endOfTerm;

            if (symbol == Symbol.Sign){
                registers.put(Register.Sign, ch+"");
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
                String numerator_str = registers.remove(Register.Sign)+registers.remove(Register.Integer);
                String decimal_str = registers.remove(Register.Decimal);
                String denominator_str = registers.remove(Register.Denominator);

                Rational rational = Util.toRational(numerator_str, decimal_str, denominator_str);
                
                if (rational == null)
                    return null;

                expr.multiplyCoefficient(rational);

                opsMap.get(operation).add(expr);
                i = close-1;
            }else if (symbol == Symbol.Literal) {
                String numerator_str = registers.remove(Register.Sign)+registers.remove(Register.Integer);
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
        Step decimal = new Step();
        Step openParentheses = new Step();
        Step closeParentheses = new Step(); /// END
        closeParentheses.endOfTerm();
        Step literal = new Step();          /// END
        literal.endOfTerm();
        Step multiplication = new Step();

        start.path.put(Symbol.Sign, sign);
        start.path.put(Symbol.Number, adition);

        adition.reset();
        adition.changeOperation(Operation.Sum);
        adition.path.put(Symbol.Number, integer);                           /// Adition         -> Integer 
        adition.path.put(Symbol.Dot, dot);                                  /// Adition         -> Dot
        adition.path.put(Symbol.OpenParentheses, openParentheses);          /// Adition         -> OpenParentheses
        adition.path.put(Symbol.Literal, literal);                          /// Adition         -> Literal

        integer.path.put(Symbol.Dot, dot);                                  /// Integer         -> Dot
        integer.path.put(Symbol.Fraction, fraction);                        /// Integer         -> Fraction
        integer.path.put(Symbol.OpenParentheses, openParentheses);          /// Integer         -> OpenParentheses
        integer.path.put(Symbol.Literal, literal);                          /// Integer         -> Literal

        fraction.changeRegister(Register.Denominator);
        fraction.path.put(Symbol.Number, numerator_decimal);                /// Fraction        -> Numerator

        numerator_decimal.path.put(Symbol.Literal, literal);                /// Numerator/Decimal-> Literal
        numerator_decimal.path.put(Symbol.OpenParentheses, openParentheses);/// Numerator/Decimal-> OpenParentheses

        dot.changeRegister(Register.Decimal);
        dot.path.put(Symbol.Dot, decimal);                                  /// Dot             -> Decimal

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

        sign.path.put(Symbol.Number, integer);                              /// Sign            -> Integer 
        sign.path.put(Symbol.Dot, dot);                                     /// Sign            -> Dot
        sign.path.put(Symbol.OpenParentheses, openParentheses);             /// Sign            -> OpenParentheses
        sign.path.put(Symbol.Literal, literal);                             /// Sign            -> Literal

        literal.path.put(Symbol.Sign, adition);
        literal.path.put(Symbol.Multiplication, multiplication);

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
package Matrops;

import java.util.HashMap;
import java.util.Scanner;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import Matrops.Matrix;
import Matrops.Util;

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

    private void op(){

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
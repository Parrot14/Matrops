package matrops;

import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import matrops.Rational;

public class Util {

    private Util(){}

    public static int getNumber(){
        Scanner sc = new Scanner(System.in);
        int n;
        while (true) {
            String num = sc.nextLine();
            if(num.matches("^\\d+$")){
                n = Integer.parseInt(num);
                break;
            }
            System.out.println("Numero mal formado, intentelo de nuevo.");
        }
        return n;
    }

    private static Pattern rationalPattern = Pattern.compile("(^(?:\\+|-)?\\d+)(?:\\.(\\d+))?(?:\\/(\\d+))?$");

    public static Rational toRational(String str){
        Matcher match = rationalPattern.matcher(str);
        if(match.matches())
            return toRational(match.group(1), match.group(2), match.group(3));
        return null;
    }

    public static Rational toRational(String numerator_str, String decimal_str, String denominator_str){
        long numerator = Long.parseLong(numerator_str+(decimal_str!=null?decimal_str:""));
        long denominator = 1;

        if(denominator_str!=null){
            denominator = Long.parseLong(denominator_str);
            if(denominator == 0){
                return null;
            }
        }
        if(decimal_str != null)
            for (int i = 0; i < decimal_str.length(); i++)
                denominator *= 10;
        return new Rational(numerator, denominator);
    }
}

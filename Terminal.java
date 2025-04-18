package Matrops;

import java.util.HashMap;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Terminal {
    HashMap<Character,Matrix> data = new HashMap<Character,Matrix>();

    public ExitCode print(){
        for (Character matrix : data.keySet()) {
            System.out.print(" "+matrix);
        }
        System.out.print("\n > ");

        Scanner sc = new Scanner(System.in);
        String input = sc.nextLine();

        switch (input.toLowerCase()) {
            case "help" -> System.err.println("TODO!!");
            case "exit" -> {return ExitCode.EXIT;}
        }
        System.err.println("TODO!!");
        System.out.println(input);
        return ExitCode.OK;
    }

    public enum ExitCode{
        OK,
        EXIT;
    }
}
package matrops;

public class Main {
    public static void main(String[] args) {
        Terminal term = new Terminal();
        System.out.println( "\t- /help para ver lista de comandos y operaciones\n"+
                            "\t- [letra]= para asignar una matriz\n"+
                            "\t- [letra]={expr} para asignar el resultado de una expresion a una matriz");
        loop: while (true) {
            switch( term.print() ){
                case OK, BAD -> {continue loop;}
                case EXIT -> {break loop;}
            }
        }
    }
}

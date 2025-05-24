package matrops;

public class Main {
    public static void main(String[] args) {
        Terminal term = new Terminal();
        System.out.println("Use /help to see help page");
        loop: while (true) {
            switch( term.print() ){
                case OK -> {continue loop;}
                case EXIT -> {break loop;}
                case BAD -> System.out.println("Bad");
            }
        }
    }
}

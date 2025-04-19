package Matrops;

public class Main {
    public static void main(String[] args) {
        Terminal term = new Terminal();
        loop: while (true) {
            switch( term.print() ){
                case OK -> {continue loop;}
                case EXIT -> {break loop;}
            }
        }
    }
}

public class P {
    public Python toPython() {
        return new Python();
    }

    public void pythonCall() {
        new Python().hi();
    }

    public static void main(String[] args) {
        System.out.println("Hey there, what's this Python thing ?");
        new Python().hi();
        System.out.println("See you !");
    }
}
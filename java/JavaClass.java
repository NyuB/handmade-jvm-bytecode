public class JavaClass {
    public static void main(String[] args) {
        System.out.println("Hey there, what's this Crafted thing ?");
        Crafted crafted = new Crafted();
        System.out.println(crafted.hi());
        System.out.println("Crafted id field is : " + crafted.craftedId);
        crafted.setCraftedId(42);
        int id = crafted.craftedId;
        System.out.println("Crafted id field after setting it to 42 is : " + id);
        assert id == 42;
        System.out.println("See you !");
    }
}
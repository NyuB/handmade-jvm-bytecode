import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class TestClassLoader extends ClassLoader {
    public Class<?> findClass(String name) {
        byte[] b = readCustomClassFile(name);
        return defineClass(name, b, 0, b.length);
    }

    public byte[] readCustomClassFile(String name) {
        try {
            return Files.readAllBytes(Paths.get(String.format("target/classes/%s.class", name)));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, SecurityException {
        Class<?> craftedClass = new TestClassLoader().findClass(args[0]);
        System.out.println(craftedClass);
        try {
            Object instance = craftedClass.getConstructors()[0].newInstance();
            System.out.println(instance);
        } catch(VerifyError e) {
            System.out.println("Invalid class constructor bytecode");
            throw e;
        } catch (InstantiationException e) {
            System.out.println("default Constructor of the class cannot be invoked");
            throw e;
        } catch (InvocationTargetException e) {
            System.out.println("Error thrown by constructor method");
            throw e;
        } 
    }

}

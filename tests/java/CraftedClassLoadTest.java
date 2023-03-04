import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;

public class CraftedClassLoadTest {
    
    private class TestClassLoader extends ClassLoader {
        private final Path classDir;

        private TestClassLoader(Path classDir) {
            this.classDir = classDir;
        }
    
        @Override
        public Class<?> findClass(String name) {
            byte[] b = readCustomClassFile(name);
            return defineClass(name, b, 0, b.length);
        }
    
        public byte[] readCustomClassFile(String name) {
            try {
                return Files.readAllBytes(this.classDir.resolve(name + ".class"));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }


    public void test(String[] args) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, SecurityException {
        Class<?> craftedClass = new TestClassLoader(Paths.get(args[0])).findClass(args[1]);
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

    public static void main(String [] args) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, SecurityException {
        new CraftedClassLoadTest().test(args);
    }

}

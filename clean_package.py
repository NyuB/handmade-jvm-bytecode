import os
import sys
import jvm_class

classes = "target/classes"
class_name = "Python.class"
jar ="target/python.jar"
java_source = "java/P.java"

def clean():
    if os.path.exists(classes):
        for f in os.listdir(classes):
            os.remove(os.path.join(classes, f))
    else:
        os.makedirs(classes)

    if(os.path.exists(jar)):
        os.remove(jar)

def package():
    with open(os.path.join(classes, class_name), 'ab') as classfile:
        for chunk in jvm_class.jvm_class_bytes():
            classfile.write(chunk)

    os.system(f"jar --create --file {jar} -C {classes} {class_name}")
    os.system(f"javac -cp {jar} -d {classes} java/P.java")

def run():
    os.system(f"java -cp {classes} P")

def help():
    print("Usage: clean_package <goal> where goal is in { clean; package; run; help }")

if __name__ == "__main__":
    goal = sys.argv[1] if len(sys.argv) > 1 else "help"

    if goal == "clean":
        clean()
    elif goal == "package":
        clean()
        package()
    elif goal == "run":
        clean()
        package()
        run()
    else:
        help()
    

    
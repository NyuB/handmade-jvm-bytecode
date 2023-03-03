import os
import sys
import jvm_class

classes = "target/classes"
class_name = "Crafted.class"
jar ="target/crafted.jar"

def remove_force(dir: str):
    if os.path.isdir(dir):
        for f in os.listdir(dir):
            full_path = os.path.join(dir, f)
            if os.path.isdir(full_path):
                remove_force(full_path)
            else:
                os.remove(full_path)
        os.removedirs(dir)
    else:
        os.remove(dir)

def clean():
    if os.path.exists(classes):
        for f in os.listdir(classes):
            remove_force(os.path.join(classes, f))
    else:
        os.makedirs(classes)

    if(os.path.exists(jar)):
        os.remove(jar)

def package():
    with open(os.path.join(classes, class_name), 'ab') as classfile:
        for chunk in jvm_class.jvm_class_bytes():
            classfile.write(chunk)

    os.system(f"jar --verbose --create --file {jar} -C {classes} {class_name}")
def help():
    print("Usage: build <goal> where goal is in { clean; package; run; help }")

if __name__ == "__main__":
    goal = sys.argv[1] if len(sys.argv) > 1 else "help"

    if goal == "clean":
        clean()
    elif goal == "package":
        clean()
        package()
    else:
        help()
    

    
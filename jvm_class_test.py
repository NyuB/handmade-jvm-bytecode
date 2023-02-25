import unittest
import jvm_class
import os
import shutil

class PythonJvmClassTest(unittest.TestCase):
    output_classes = "plasses"
    output_class = os.path.join(output_classes, "Python.class")

    def setUp(self):
        try:
            os.makedirs("plasses")
        except FileExistsError:
            pass
    def tearDown(self):
        try:
            shutil.rmtree("plasses")
        except FileNotFoundError:
            pass

    def assert_command_success(self, cmd: str) -> None:
        exit_code = os.system(cmd)
        self.assertEqual(exit_code, 0, f"Command ${cmd} failed with exit code {exit_code}")

    def test_valid_class(self):
        write_bytes(self.output_class, jvm_class.jvm_class_bytes())
        javap = f"javap -c -v {self.output_class}"
        self.assert_command_success(javap)

    def test_compile_with_java(self):
        write_bytes(self.output_class, jvm_class.jvm_class_bytes())
        javac = f"javac -cp {self.output_classes} -d {self.output_classes} java/P.java"
        self.assert_command_success(javac)

    def test_run_with_java(self):
        write_bytes(self.output_class, jvm_class.jvm_class_bytes())
        javac = f"javac -cp {self.output_classes} -d {self.output_classes} java/P.java"
        self.assert_command_success(javac)
        java = f"java -cp {self.output_classes} P"
        self.assert_command_success(java)

def rm(f: str) -> None:
    try:
        os.remove(f)
    except:
        pass

def write_bytes(f: str, b: list[bytes]):
    with open(f, 'ab') as file:
        for chunk in b:
            file.write(chunk)

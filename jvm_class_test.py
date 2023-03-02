import unittest
import jvm_class
import os
import shutil

class BaseTestClass(unittest.TestCase):

    java_class_name = "JavaClass"
    java_source = f"java/{java_class_name}.java"
    java_class_loader_name = "TestClassLoader"
    java_class_loader_source = f"java/{java_class_loader_name}.java"

    def __init__(self, methodName: str):
        super().__init__(methodName)
        self.output_classes = self.get_output_classes()
        self.output_class = os.path.join(self.output_classes, f"{self.get_output_class_name()}.class")

    def get_output_classes(self) -> str:
        raise NotImplementedError(self.get_output_classes.__name__ + " must be implemented by actual test case")
    
    def get_output_class_name(self) -> str:
        raise NotImplementedError(self.get_output_class_name.__name__ + " must be implemented by actual test case")

    def assert_command_success(self, cmd: str) -> None:
        exit_code = os.system(cmd)
        self.assertEqual(exit_code, 0, f"Command ${cmd} failed with exit code {exit_code}")
    
    def write_class_file(self) -> None:
        raise NotImplementedError(self.write_class_file.__name__ + " must be implemented by actual test case")
    
    def setUp(self) -> None:
        try:
            os.makedirs(self.output_classes)
        except FileExistsError:
            pass
        self.write_class_file()
    
    def tearDown(self) -> None:
        try:
            shutil.rmtree(self.output_classes)
        except FileNotFoundError:
            pass

class PythonJvmClassTest(BaseTestClass):

    def get_output_classes(self) -> str:
        return "py_test_classes"
    
    def get_output_class_name(self) -> str:
        return jvm_class.jvm_class_name

    def write_class_file(self) -> None:
        write_bytes(self.output_class, jvm_class.jvm_class_bytes())

    def test_valid_class(self):
        javap = f"javap -c -v {self.output_class}"
        self.assert_command_success(javap)

    def test_compile_with_java(self):
        javac = f"javac -cp {self.output_classes} -d {self.output_classes} {self.java_source}"
        self.assert_command_success(javac)

    def test_run_with_java(self):
        javac = f"javac -cp {self.output_classes} -d {self.output_classes} {self.java_source}"
        self.assert_command_success(javac)
        java = f"java -ea -cp {self.output_classes} {self.java_class_name}"
        self.assert_command_success(java)
    
    def test_run_with_java_class_loader(self):
        javac = f"javac -cp {self.output_classes} -d {self.output_classes} {self.java_class_loader_source}"
        self.assert_command_success(javac)
        java = f"java -ea -cp {self.output_classes} {self.java_class_loader_name} {self.output_classes} {jvm_class.jvm_class_name}"
        self.assert_command_success(java)

class ScalaJvmClassTest(BaseTestClass):

    def get_output_classes(self) -> str:
        return "scala_test_classes"
    
    def get_output_class_name(self) -> str:
        return "Crafted"

    def write_class_file(self) -> None:
        scalac = f"scalac -d {self.output_classes} JvmClass.scala"
        self.assert_command_success(scalac)
        writeBytes = f"scala -cp {self.output_classes} writeBytes {self.output_class}"
        self.assert_command_success(writeBytes)

    def test_valid_class(self):
        javap = f"javap -c -v {self.output_class}"
        self.assert_command_success(javap)

def rm(f: str) -> None:
    try:
        os.remove(f)
    except:
        pass

def write_bytes(f: str, b: list[bytes]):
    with open(f, 'ab') as file:
        for chunk in b:
            file.write(chunk)

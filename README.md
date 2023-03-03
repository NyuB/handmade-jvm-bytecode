# Toy project to explore .class format and java compilation process

All the .class layout generation takes place in the method **jvm_class_bytes** in **jvm_class.py**

## Prerequisites
+ `java`, `javac`, `jar` binaries in **PATH** with java version > = 8 (the project CI uses java 8)
+ `python3` (further examples in this documentation assume python3 command to be aliased by `py`)
+ (optionnal) `scala`, `scalac` binaries in **PATH** to be able to run the scala tests

## Running the tests

```
py -m unittest tests/class_generation_tests.py
```

code

## Generating, compiling and jar-ing .class files

### Using the python class generator
```bash
py build.py package
```

Ths will generate 'manually' a **Crafted.class** in **target/classes**, and a jar **target/crafted.jar** containing this generated .class. It uses **jvm_class.py** to generate the bytecodeof Crafted.class.

### Using the scala class generator

Transition to scala generation with JvmClass.scala is on the way and the build.py script does not support it yet. To generate and jar the Crafted.class with the JvmClass.scala generator, run 

```bash
scala JvmClass.scala target/classes/Crafted.class
jar --verbose --create --file target/crafted.jar -C target/classes Crafted.class
```

## Clean target folder

```bash
py build.py clean
```

**NB:** This will clean and recompile the sources before actually running
## Include generated classes to your IDE scope

### VsCode

In **Java Projects** extension settings(for local workspace), add **target/crafted.jar** in the **Referenced Libraries** section

### IntelliJ

Add **target/crafted.jar** as an external dependency of the project

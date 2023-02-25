# Toy project to explore .class format and java compilation process

All the .class layout generation takes place in the method **jvm_class_bytes** in **jvm_class.py**

## Prerequisites
+ `java`, `javac`, `jar` binaries in **PATH**
+ `python3` (further examples in this documentation assume python3 command to be aliased by `py`)

## Running the tests

```bash
py -m unittest jvm_class_test.py
```

## Generating and compiling .class files

```bash
py clean_package.py package
```

Ths will generate 'manually' a **Python.class** in **target/classes**, a jar **target/python.jar** containing this generated .class, and a class **target/classes/P.class** compiled from source **java/P.java** depending on **python.jar**

## Clean target folder

```bash
py clean_package.py clean
```

## Running compiled classes

To run P.main(args) { ... } with `java`

```bash
py clean_package.py run
```

**NB:** This will clean and recompile the sources before actually running
## Include generated classes to your IDE scope

### VsCode

In **Java Projects** extension settings(for local workspace), add **target/python.jar** in the **Referenced Libraries** section

### IntelliJ

Add **target/python.jar** as an external dependency of the project
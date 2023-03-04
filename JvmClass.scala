object ClassFileBinaryEncoding:
    opaque type Bytes = IArray[Byte]
    extension(b: Bytes)
        def toIterable: Iterable[Byte] = b
    
    def byteLength(bytesArray: IArray[Bytes]): Int =
        bytesArray.foldLeft(0)((total, bytes) => total + bytes.size)

    val MAGIC_NUMBER: Bytes = u4(0xCAFEBABE)
    val VERSION_JAVA_8_MAJOR: Bytes = u2(52)
    val VERSION_JAVA_8_MINOR: Bytes = u2(0)

    val CLASSFILE_STRING_TAG: Bytes = u1(1)
    val CLASSFILE_CLASS_TAG: Bytes = u1(7)
    val CLASSFILE_METHOD_REF_TAG: Bytes = u1(10)
    val CLASSFILE_NAME_AND_TYPE_DESCRIPTOR_TAG: Bytes = u1(12)

    val EMPTY_TABLE: Bytes = elementCount(0) // [0 elements, []]

    val PUBLIC_FLAG = 0x1
    val FINAL_FLAG = 0x10

    def flags(flagList: List[Int]) = u2(flagList.foldLeft(0)((acc, i) => acc | i))

    def stringUtf8(s: String): Bytes =
        val stringBytes = IArray.from(s.getBytes(java.nio.charset.StandardCharsets.UTF_8))
        IArray(CLASSFILE_STRING_TAG, u2(stringBytes.length), stringBytes).flatten

    def classNameReference(classNameIndex: Int): Bytes =
        IArray(CLASSFILE_CLASS_TAG, constantPoolIndex(classNameIndex)).flatten

    def elementCount(count: Int): Bytes = u2(count)

    def constantPoolSection(constantPool: IArray[Bytes]): Bytes =
        IArray(
            elementCount(constantPool.size + 1), // constant pool size = len(constant pool) + 1
            constantPool.flatten
        ).flatten

    def constantPoolIndex(i: Int) = u2(i)

    def u1(i: Int): Bytes = IArray(i.toByte)

    def u2(i: Int): Bytes =
        val left = (i  & 0xFF00) >> 8
        val right = i & 0xFF
        IArray(left.toByte, right.toByte)

    def u4(i: Int): Bytes =
        val a = (i & 0xFF000000) >> 24
        val b = (i & 0x00FF0000) >> 16
        val c = (i & 0x0000FF00) >> 8
        val d = (i & 0x000000FF)
        IArray(a, b, c, d).map(_.toByte)

    val aload_0 = u1(0x2A)
    val invokespecial = u1(0xB7)
    val _return = u1(0xB1)

    def method(codeAttributeIndex: Int, stackSize: Int, localSize: Int)(operations: IArray[Bytes]): Bytes =
        val body = IArray(
            u2(stackSize),
            u2(localSize),
            u4(byteLength(operations)),
            operations.flatten,
            EMPTY_TABLE,
            EMPTY_TABLE,
        )

        IArray(
            constantPoolIndex(codeAttributeIndex),
            u4(byteLength(body)),
            body.flatten,
        ).flatten
    
    def constructor(codeAttributeIndex: Int, superInitIndex: Int): Bytes = method(codeAttributeIndex, 1, 1) {
        IArray(
            aload_0,
            IArray(invokespecial, constantPoolIndex(superInitIndex)).flatten,
            _return 
        )
    }

    def methodReference(receiverClassIndex: Int, nameAndTypeDescriptorIndex: Int): Bytes =
        IArray(CLASSFILE_METHOD_REF_TAG, constantPoolIndex(receiverClassIndex), constantPoolIndex(nameAndTypeDescriptorIndex)).flatten
    
    def methodNameAndTypeDescriptor(nameIndex: Int, typeDescriptorIndex: Int): Bytes = 
        IArray(CLASSFILE_NAME_AND_TYPE_DESCRIPTOR_TAG, constantPoolIndex(nameIndex), constantPoolIndex(typeDescriptorIndex)).flatten
    
    def methodTypeDescriptor(args: List[String], returnType: String): Bytes =
        val joinedArgs = args.foldLeft("")((s, a) => s ++ a)
        stringUtf8(s"(${joinedArgs})${returnType}")

    def customJvmClassBytes(thisName: String, superName: String): Bytes = IArray(
        MAGIC_NUMBER,
        VERSION_JAVA_8_MINOR,
        VERSION_JAVA_8_MAJOR,
        constantPoolSection(IArray(
            classNameReference(2), // 1
            stringUtf8(thisName), // 2
            classNameReference(4), // 3
            stringUtf8(superName), // 4
            stringUtf8("<init>"), // 5
            stringUtf8("Code"), // 6
            methodTypeDescriptor(List.empty, "V"), // 7
            methodReference(3, 9), // 8
            methodNameAndTypeDescriptor(5, 7), // 9
        )),
        flags(List(PUBLIC_FLAG, FINAL_FLAG)),
        constantPoolIndex(1), // this
        constantPoolIndex(3), // super
        EMPTY_TABLE, // no interface
        EMPTY_TABLE, // no field
        u2(1),
        IArray(
            flags(List(PUBLIC_FLAG)),
            constantPoolIndex(5),
            constantPoolIndex(7),
            elementCount(1),
            constructor(6, 8),
        ).flatten,
        EMPTY_TABLE, // no attribute
    ).flatten


@main def writeBytes(path: String) =
    val file = java.io.File(path)
    val target = java.io.BufferedOutputStream(java.io.FileOutputStream(file))
    try
        ClassFileBinaryEncoding.customJvmClassBytes("Crafted", "java/lang/Object")
        .toIterable.foreach( target.write(_) )
    finally 
        target.close
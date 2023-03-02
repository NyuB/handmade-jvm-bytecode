object ClassFileBinaryEncoding:
    opaque type Bytes = IArray[Byte]
    extension(b: Bytes)
        def toIterable: Iterable[Byte] = b

    val MAGIC_NUMBER: Bytes = u4(0xCAFEBABE)
    val VERSION_JAVA_8_MAJOR: Bytes = u2(52)
    val VERSION_JAVA_8_MINOR: Bytes = u2(0)

    val CLASSFILE_STRING_TAG: Bytes = u1(1)
    val CLASSFILE_CLASS_TAG: Bytes = u1(7)

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

    val customJvmClassBytes: Bytes = IArray(
        MAGIC_NUMBER,
        VERSION_JAVA_8_MINOR,
        VERSION_JAVA_8_MAJOR,
        constantPoolSection(IArray(
            classNameReference(2),
            stringUtf8("Crafted"),
            classNameReference(4),
            stringUtf8("java/lang/Object")
        )),
        flags(List(PUBLIC_FLAG, FINAL_FLAG)),
        constantPoolIndex(1), // this
        constantPoolIndex(3), // super
        EMPTY_TABLE, // no interface
        EMPTY_TABLE, // no field
        EMPTY_TABLE, // no method
        EMPTY_TABLE, // no attribute
    ).flatten


@main def writeBytes(path: String) =
    val file = java.io.File(path)
    val target = java.io.BufferedOutputStream(java.io.FileOutputStream(file))
    try
        ClassFileBinaryEncoding.customJvmClassBytes
        .toIterable.foreach( target.write(_) )
    finally 
        target.close
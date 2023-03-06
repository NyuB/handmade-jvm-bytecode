object ClassBytecode:
  type Bytes = IArray[Byte]

  opaque type ConstantPoolIndex = Int
  object ConstantPoolIndex:
    def fromZeroBasedIndex(zeroBasedIndex: Int): ConstantPoolIndex =
      zeroBasedIndex + 1

  extension (cpi: ConstantPoolIndex) def toBytes: Bytes = u2(cpi)

  def byteLength(bytesArray: IArray[Bytes]): Int =
    bytesArray.foldLeft(0)((total, bytes) => total + bytes.size)

  val MAGIC_NUMBER: Bytes = u4(0xcafebabe)
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
    val stringBytes =
      IArray.from(s.getBytes(java.nio.charset.StandardCharsets.UTF_8))
    IArray(CLASSFILE_STRING_TAG, u2(stringBytes.length), stringBytes).flatten

  def classNameReference(classNameIndex: ConstantPoolIndex): Bytes =
    IArray(CLASSFILE_CLASS_TAG, classNameIndex.toBytes).flatten

  def elementCount(count: Int): Bytes = u2(count)

  def constantPoolSection(constantPool: IArray[Bytes]): Bytes =
    IArray(
      elementCount(
        constantPool.size + 1
      ), // constant pool size = len(constant pool) + 1
      constantPool.flatten
    ).flatten

  def u1(i: Int): Bytes = IArray(i.toByte)

  def u2(i: Int): Bytes =
    val left = (i & 0xff00) >> 8
    val right = i & 0xff
    IArray(left.toByte, right.toByte)

  def u4(i: Int): Bytes =
    val a = (i & 0xff000000) >> 24
    val b = (i & 0x00ff0000) >> 16
    val c = (i & 0x0000ff00) >> 8
    val d = (i & 0x000000ff)
    IArray(a, b, c, d).map(_.toByte)

  val aload_0 = u1(0x2a)
  val invokespecial = u1(0xb7)
  val _return = u1(0xb1)

  def method(
      codeAttributeIndex: ConstantPoolIndex,
      stackSize: Int,
      localSize: Int
  )(operations: IArray[Bytes]): Bytes =
    val body = IArray(
      u2(stackSize),
      u2(localSize),
      u4(byteLength(operations)),
      operations.flatten,
      EMPTY_TABLE,
      EMPTY_TABLE
    )

    IArray(
      codeAttributeIndex.toBytes,
      u4(byteLength(body)),
      body.flatten
    ).flatten

  def constructor(
      codeAttributeIndex: ConstantPoolIndex,
      superInitIndex: ConstantPoolIndex
  ): Bytes = method(codeAttributeIndex, 1, 1) {
    IArray(
      aload_0,
      IArray(invokespecial, superInitIndex.toBytes).flatten,
      _return
    )
  }

  def methodReference(
      receiverClassIndex: ConstantPoolIndex,
      nameAndTypeDescriptorIndex: ConstantPoolIndex
  ): Bytes =
    IArray(
      CLASSFILE_METHOD_REF_TAG,
      receiverClassIndex.toBytes,
      nameAndTypeDescriptorIndex.toBytes
    ).flatten

  def methodNameAndTypeDescriptor(
      nameIndex: ConstantPoolIndex,
      typeDescriptorIndex: ConstantPoolIndex
  ): Bytes =
    IArray(
      CLASSFILE_NAME_AND_TYPE_DESCRIPTOR_TAG,
      nameIndex.toBytes,
      typeDescriptorIndex.toBytes
    ).flatten

  def methodTypeDescriptor(args: List[String], returnType: String): Bytes =
    val joinedArgs = args.foldLeft("")((s, a) => s ++ a)
    stringUtf8(s"(${joinedArgs})${returnType}")

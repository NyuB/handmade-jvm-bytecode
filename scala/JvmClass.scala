import ClassBytecode._

sealed trait ConstantPoolEntry
object ConstantPoolEntry:
  case class RawString(s: String) extends ConstantPoolEntry
  case class ClassNameReference(s: String) extends ConstantPoolEntry
  case class MethodTypeDescriptor(paramsTypes: List[String], returnType: String)
      extends ConstantPoolEntry
  case class MethodReference(
      receiverClass: ClassNameReference,
      nameAndTypeDescriptor: MethodNameAndTypeDescriptor
  ) extends ConstantPoolEntry
  case class MethodNameAndTypeDescriptor(
      name: String,
      methodTypeDescriptor: MethodTypeDescriptor
  ) extends ConstantPoolEntry

case class ConstantPool(entries: List[ConstantPoolEntry]):
  def indexOf(entry: ConstantPoolEntry): ConstantPoolIndex =
    val index = entries.indexOf(entry)
    if index < 0 then
      throw IllegalStateException(s"Unknown entry in constant pool ${entry}")
    else ConstantPoolIndex.fromZeroBasedIndex(index)

  def toBytesArray: IArray[Bytes] =
    IArray.from(entries.map(bytesFromEntry(_)))

  private def bytesFromEntry(entry: ConstantPoolEntry): Bytes =
    entry match
      case ConstantPoolEntry.RawString(s) =>
        ClassBytecode.stringUtf8(s)

      case ConstantPoolEntry.ClassNameReference(className) =>
        val index = this.indexOf(ConstantPoolEntry.RawString(className))
        ClassBytecode.classNameReference(index)

      case ConstantPoolEntry.MethodTypeDescriptor(params, returnType) =>
        ClassBytecode.methodTypeDescriptor(params, returnType)

      case ConstantPoolEntry.MethodReference(
            receiverClass,
            nameAndTypeDescriptor
          ) =>
        val receiverIndex = this.indexOf(receiverClass)
        val nameAndDescriptorIndex = this.indexOf(nameAndTypeDescriptor)
        ClassBytecode.methodReference(receiverIndex, nameAndDescriptorIndex)

      case ConstantPoolEntry.MethodNameAndTypeDescriptor(
            name,
            typeDescriptor
          ) =>
        val nameIndex = this.indexOf(ConstantPoolEntry.RawString(name))
        val typeDescriptorIndex = this.indexOf(typeDescriptor)
        ClassBytecode.methodNameAndTypeDescriptor(
          nameIndex,
          typeDescriptorIndex
        )

case class JvmClass private (
    thisName: String,
    superName: String,
    constantPool: ConstantPool,
    methods: List[ClassBytecode.Bytes]
):
  class MutablePool(constantPool: ConstantPool):
    val entries =
      scala.collection.mutable.ArrayBuffer.from(constantPool.entries)
    def getOrAppend(entry: ConstantPoolEntry): ConstantPoolIndex =
      val index = entries.indexOf(entry)
      if index < 0 then
        entries += entry
        ConstantPoolIndex.fromZeroBasedIndex(entries.size - 1)
      else ConstantPoolIndex.fromZeroBasedIndex(index)
    def toConstantPool: ConstantPool = ConstantPool(List.from(entries))

  def withMethod(methodDeclaration: (MutablePool) => (Bytes)): JvmClass =
    val mutablePool = MutablePool(constantPool)
    val bytes = methodDeclaration(mutablePool)
    this.copy(
      constantPool = mutablePool.toConstantPool,
      methods = this.methods :+ bytes
    )

  def toBytes: Bytes =
    val thisIndex =
      constantPool.indexOf(ConstantPoolEntry.ClassNameReference(thisName))
    val superIndex =
      constantPool.indexOf(ConstantPoolEntry.ClassNameReference(superName))
    val cp = constantPool.toBytesArray
    IArray(
      MAGIC_NUMBER,
      VERSION_JAVA_8_MINOR,
      VERSION_JAVA_8_MAJOR,
      ClassBytecode.constantPoolSection(cp),
      flags(List(PUBLIC_FLAG, FINAL_FLAG)),
      thisIndex.toBytes, // this
      superIndex.toBytes, // super
      EMPTY_TABLE, // no interface
      EMPTY_TABLE, // no field
      u2(methods.size),
      IArray.from(methods).flatten,
      EMPTY_TABLE // no attribute
    ).flatten

object JvmClass:
  def apply(thisName: String, superName: String): JvmClass =
    JvmClass(
      thisName,
      superName,
      ConstantPool(
        List(
          ConstantPoolEntry.RawString(thisName),
          ConstantPoolEntry.ClassNameReference(thisName),
          ConstantPoolEntry.RawString(superName),
          ConstantPoolEntry.ClassNameReference(superName)
        )
      ),
      List.empty
    )

val customJvmClass =
  JvmClass("Crafted", "java/lang/Object")
    .withMethod { cp =>
      val name = ConstantPoolEntry.RawString("<init>")
      val codeAttribute = ConstantPoolEntry.RawString("Code")
      val descriptor = ConstantPoolEntry.MethodTypeDescriptor(List.empty, "V")
      val nameAndTypeDescriptor =
        ConstantPoolEntry.MethodNameAndTypeDescriptor("<init>", descriptor)
      val objectClassReference =
        ConstantPoolEntry.ClassNameReference("java/lang/Object")
      val objectInit = ConstantPoolEntry.MethodReference(
        objectClassReference,
        nameAndTypeDescriptor
      )

      val nameIndex = cp.getOrAppend(name)
      val codeIndex = cp.getOrAppend(codeAttribute)
      val descriptorIndex = cp.getOrAppend(descriptor)
      val nameAndDescriptorIndex = cp.getOrAppend(nameAndTypeDescriptor)
      val superInitIndex = cp.getOrAppend(objectInit)

      IArray(
        flags(List(PUBLIC_FLAG)),
        nameIndex.toBytes,
        descriptorIndex.toBytes,
        elementCount(1),
        constructor(codeIndex, superInitIndex)
      ).flatten
    }

@main def writeBytes(path: String) =
  val file = java.io.File(path)
  val target = java.io.BufferedOutputStream(java.io.FileOutputStream(file))
  try
    customJvmClass.toBytes.foreach(target.write(_))
  finally
    target.close

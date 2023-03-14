import JvmClass._
import ClassBytecode._

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

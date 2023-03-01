"""
Custom class file bytecode generation
jvm_class_bytes defines the actual layout of the generated 'Crafted' class file
"""

import functools

jvm_class_name = "Crafted"

def jvm_class_bytes() -> list[bytes]:
    return [
        U4_MAGIC_NUMBER,
        u2(0), # Minor version (0)
        VERSION_JAVA_8, # Major version
        u2(16), # constant pool size = len(constant pool) + 1
        *[ # constant pool
            *classfile_class(2), # classname at index 2
            *classfile_string(jvm_class_name),
            *classfile_class(4), # classname at index 4
            *classfile_string("java/lang/Object"),
            *METHOD_INIT,
            *classfile_void_method_param_descriptor("V"), # () -> ()
            *classfile_string("hi"),
            *classfile_string("Code"), # Code attribute name
            *classfile_name_and_type_descriptor(5, 6), # void <init>()
            *classfile_method_reference(3, 9), # void Object.<init>()
            *classfile_string("Hello java, you don't know me but i do know you ;)"),
            *classfile_void_method_param_descriptor("Ljava/lang/String;"), # () -> java.lang.String
            *classfile_string_reference(11),
            *classfile_string("craftedId"),
            *classfile_string("I"), # int
        ], 
        CLASSFILE_ACCESS_PUBLIC, # Public class
        cpool_index(1), # this => point to the first entry in constant pool
        cpool_index(3), # super => point to the third entry in constant pool
        u2(0), # implemented interfaces count = 0
        U_EMPTY_TABLE, # empty interface table
        u2(1), # fields count = 1
        *[
            CLASSFILE_ACCESS_PUBLIC_FINAL, # public final field
            cpool_index(14), # name index in constant pool
            cpool_index(15), # type descriptor in constant pool
            u2(0), # 0 attributes
            U_EMPTY_TABLE, # no attributes

        ],
        U_EMPTY_TABLE, # empty field table
        u2(2), # methods count = 2
        *[ # method table
            *[ # constructor
                CLASSFILE_ACCESS_PUBLIC, # Public method
                cpool_index(5), # Name index in constant pool
                cpool_index(6), # Method descriptor index,
                u2(1), # Attributes count = 1
                *empty_constructor_code(8, 10),
            ],
            *[ # String hi() { }
                CLASSFILE_ACCESS_PUBLIC,
                cpool_index(7),
                cpool_index(12),
                u2(1), # Attributes count = 1
                *string_empty_method_code(8, 13),
            ]
        ],
        u2(0), # attributes count = 0
        U_EMPTY_TABLE, # empty attribute table
    ]

def empty_constructor_code(code_index: int, super_init_index: int) -> list[bytes]:
    return method_code(code_index, [
        aload_0, # place super instance from local variable 0 on top of the stack
        *[invokespecial, cpool_index(super_init_index)], # call super.<init>() on the super object
        _return,
    ])

def string_empty_method_code(code_index: int, string_index: int) -> list[bytes]:
    return method_code(code_index, [
        *[ldc_w, cpool_index(string_index)], # load string on top of the stack
        areturn, # return the reference on top of the stack
    ])

def method_code(code_index: int, operations: list[bytes]) -> list[bytes]:
    body = [
        u2(1), # stack length = 1
        u2(1), # locals length = 1
        u4(byte_length(operations)), # code length in bytes
        *operations,
        u2(0), # exception table length
        U_EMPTY_TABLE, #exception
        u2(0), # attributes count
        U_EMPTY_TABLE, # attribute table  
    ]

    header = [
        cpool_index(code_index), # Code string at index 8
        u4(byte_length(body)), # attr length in bytes
    ]

    return [
        *header,
        *body
    ]

def u1(i: int) -> bytes:
    return i.to_bytes(1, 'big')
def u2(i: int) -> bytes:
    return i.to_bytes(2, 'big')
def u4(i: int) -> bytes:
    return i.to_bytes(4, 'big')

def classfile_string(s: str) -> list[bytes]:
    return [CLASSFILE_STRING_TAG, u2(len(s)), s.encode(encoding = "utf-8")]

def classfile_string_reference(str_index: int) -> list[bytes]:
    return [CLASSFILE_STRING_REF_TAG, cpool_index(str_index)]

def classfile_class(name_index: int) -> list[bytes]:
    return [CLASSFILE_CLASS_TAG, cpool_index(name_index)]

def cpool_index(index: int) -> bytes:
    return u2(index)

def classfile_void_method_param_descriptor(returnType: str) -> list[bytes]:
    descriptor = f"(){returnType}"
    return classfile_string(descriptor)

def classfile_name_and_type_descriptor(name_index: int, type_index: int) -> list[bytes]:
    return [u1(12), cpool_index(name_index), cpool_index(type_index)]

def classfile_method_reference(class_index: int, name_and_type_index: int) -> list[bytes]:
    return [u1(10), cpool_index(class_index), cpool_index(name_and_type_index)]

def byte_length(bytes_list: list[bytes]) -> int:
    return sum([len(b) for b in bytes_list])

def combine_access_flags(flags: list[bytes]) -> bytes:
    return functools.reduce(lambda a, b: a + b, flags, b"\x00\x00")

U4_MAGIC_NUMBER: bytes = b"\xCA\xFE\xBA\xBE"
VERSION_JAVA_8: bytes = u2(52)
CLASSFILE_STRING_TAG = u1(1)
CLASSFILE_STRING_REF_TAG = u1(8)
CLASSFILE_CLASS_TAG = u1(7)
CLASSFILE_ACCESS_PUBLIC = u2(1)
CLASSFILE_ACCESS_FINAL = u2(16)
CLASSFILE_ACCESS_PUBLIC_FINAL = u2(17)
METHOD_INIT = classfile_string("<init>")
U_EMPTY_TABLE: bytes = b""


# opcodes
aload_0 = b"\x2a"
areturn = b"\xb0"
invokespecial = b"\xb7"
ldc_w = b"\x13"
_return = b"\xb1"

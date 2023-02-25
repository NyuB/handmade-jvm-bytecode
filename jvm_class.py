"""
Custom class file bytecode generation
jvm_class_bytes defines the actual layout of the generated 'Python' class file
"""

def jvm_class_bytes() -> list[bytes]:
    return [
        U4_MAGIC_NUMBER,
        u2(1), # Minor version (1)
        VERSION_JAVA_8, # Major version
        u2(11), # constant pool size = len(constant pool) + 1
        *[ # constant pool
            *classfile_class(2), # classname at index 2
            *classfile_string("Python"),
            *classfile_class(4), # classname at index 4
            *classfile_string("java/lang/Object"),
            *METHOD_INIT,
            *classfile_void_method_param_descriptor("V"), # V for void return type
            *classfile_string("hi"),
            *classfile_string("Code"), # Code attribute name
            *classfile_name_and_type_descriptor(5, 6), # void <init>()
            *classfile_method_reference(3, 9),
        ], 
        CLASSFILE_ACCESS_PUBLIC, # Public class
        cpool_index(1), # this => point to the first entry in constant pool
        cpool_index(3), # super => point to the third entry in constant pool
        u2(0), # implemented interfaces count = 0
        U_EMPTY_TABLE, # empty interface table
        u2(0), # fields count = 0
        U_EMPTY_TABLE, # empty field table
        u2(2), # methods count = 2
        *[ # method table
            *[ # constructor
                CLASSFILE_ACCESS_PUBLIC, # Public method
                cpool_index(5), # Name index in constant pool
                cpool_index(6), # Method descriptor index,
                u2(1), # Attributes count = 1
                *[ # attribute table
                    cpool_index(8), # Code string at index 8
                    u4(17), # attr length in bytes
                    u2(1), # stack length = 1
                    u2(1), # locals length = 1
                    u4(5), # code length in bytes
                    *[ # code
                        b"\x2A", #aload_0
                        *[b"\xb7", cpool_index(10)], #invokespecial Object.<init>
                        b"\xb1", # return
                    ],
                    u2(0), # exception table length
                    U_EMPTY_TABLE, #exception
                    u2(0), # attributes count
                    U_EMPTY_TABLE, # attribute table
                ],
            ],
            *[ # void hi() { }
                CLASSFILE_ACCESS_PUBLIC,
                cpool_index(7),
                cpool_index(6),
                u2(1), # Attributes count = 1
                *[ # attribute table
                    cpool_index(8), # Code string at index 8
                    u4(13), # attr length in bytes
                    u2(1), # stack length = 1
                    u2(1), # locals length = 1
                    u4(1), # code length in bytes
                    *[b"\xB1"], # code = return
                    u2(0), # exception table length
                    U_EMPTY_TABLE, #exception
                    u2(0), # attributes count
                    U_EMPTY_TABLE, # attribute table
                ],
            ]
        ],
        u2(0), # attributes count = 0
        U_EMPTY_TABLE, # empty attribute table
    ]

def u1(i: int) -> bytes:
    return i.to_bytes(1, 'big')
def u2(i: int) -> bytes:
    return i.to_bytes(2, 'big')
def u4(i: int) -> bytes:
    return i.to_bytes(4, 'big')

def classfile_string(s: str) -> list[bytes]:
    return [CLASSFILE_STRING_TAG, u2(len(s)), s.encode(encoding = "utf-8")]

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

U4_MAGIC_NUMBER: bytes = b"\xCA\xFE\xBA\xBE"
VERSION_JAVA_8: bytes = u2(52)
CLASSFILE_STRING_TAG = u1(1)
CLASSFILE_CLASS_TAG = u1(7)
CLASSFILE_ACCESS_PUBLIC = u2(1)
METHOD_INIT = classfile_string("<init>")
U_EMPTY_TABLE: bytes = b""

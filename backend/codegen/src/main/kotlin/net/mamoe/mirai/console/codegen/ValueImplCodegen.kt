/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.console.codegen

import java.io.File


fun main() {
    println(File("").absolutePath) // default project base dir

    File("backend/mirai-console/src/main/kotlin/net/mamoe/mirai/console/setting/_ValueImpl.kt").apply {
        createNewFile()
    }.writeText(buildString {
        appendln(COPYRIGHT)
        appendln()
        appendln(PACKAGE)
        appendln()
        appendln(IMPORTS)
        appendln()
        appendln()
        appendln(DO_NOT_MODIFY)
        appendln()
        appendln()
        appendln(genAllValueImpl())
    })
}

private val DO_NOT_MODIFY = """
/**
 * !!! This file is auto-generated by backend/codegen/src/kotlin/net.mamoe.mirai.console.codegen.ValueImplCodegen.kt
 * !!! DO NOT MODIFY THIS FILE MANUALLY
 */
""".trimIndent()

private val PACKAGE = """
package net.mamoe.mirai.console.setting
""".trimIndent()

private val IMPORTS = """
import kotlinx.serialization.builtins.*
""".trimIndent()

fun genAllValueImpl(): String = buildString {
    // PRIMITIVE
    for (number in NUMBERS + OTHER_PRIMITIVES) {
        appendln(genValueImpl(number, number, "$number.serializer()", false))
    }

    // PRIMITIVE ARRAYS
    for (number in NUMBERS + OTHER_PRIMITIVES.filterNot { it == "String" }) {
        appendln(genValueImpl("${number}Array", "${number}Array", "${number}ArraySerializer()", true))
    }

    // TYPED ARRAYS
    for (number in NUMBERS + OTHER_PRIMITIVES) {
        appendln(genValueImpl("Array<${number}>", "Typed${number}Array", "ArraySerializer(${number}.serializer())", true))
    }

    // PRIMITIVE LISTS
    for (number in NUMBERS + OTHER_PRIMITIVES) {
        appendln(genValueImpl("List<${number}>", "${number}List", "ListSerializer(${number}.serializer())", false))
    }
}

fun genValueImpl(kotlinTypeName: String, miraiValueName: String, serializer: String, isArray: Boolean): String =
    """
        internal fun Setting.valueImpl(default: ${kotlinTypeName}): ${miraiValueName}Value {
            return object : ${miraiValueName}Value() {
                private var internalValue: $kotlinTypeName = default
                override var value: $kotlinTypeName
                    get() = internalValue
                    set(new) {
                        ${
    if (isArray) """
                        if (!new.contentEquals(internalValue)) {
                            internalValue = new
                            onElementChanged(this)
                        }
    """.trim()
    else """
                        if (new != internalValue) {
                            internalValue = new
                            onElementChanged(this)
                        }
    """.trim()
    }
                    }
                override val serializer = ${serializer}.bind(
                    getter = { internalValue },
                    setter = { internalValue = it }
                )
            }
        }
    """.trimIndent() + "\n"


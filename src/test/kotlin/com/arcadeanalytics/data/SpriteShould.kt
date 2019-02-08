/*-
 * #%L
 * Arcade Data
 * %%
 * Copyright (C) 2018 - 2019 ArcadeAnalytics
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package com.arcadeanalytics.data

import org.assertj.core.api.Assertions
import org.assertj.core.util.Lists
import org.assertj.guava.api.Assertions.assertThat
import org.assertj.guava.api.Assertions.entry
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.regex.Pattern

class SpriteShould {

    private lateinit var sprite: Sprite

    @BeforeEach
    internal fun setUp() {
        sprite = Sprite()
    }

    @Test
    internal fun `add values of different types`() {
        sprite.add("field1", "value1")
                .add("field2", 10)
                .add("field3", false)

        Assertions.assertThat(sprite.entries()).contains(
                entry("field1", "value1"),
                entry("field2", 10),
                entry("field3", false)
        )

    }

    @Test
    internal fun `flat collection value`() {

        val values = mutableListOf<String>("value1", "value2", "value3")

        sprite.add("field1", values)
                .add("field2", 10)
                .add("field3", false)

        Assertions.assertThat(sprite.rawValuesOf<String>("field1")).isNotEmpty
                .hasSize(3)

        Assertions.assertThat(sprite.entries()).contains(
                entry("field1", "value1"),
                entry("field1", "value2"),
                entry("field1", "value3"),
                entry("field2", 10),
                entry("field3", false)
        )


    }

    @Test
    internal fun `return single value as string`() {
        sprite.add("field1", "value1")
                .add("field2", 10)
                .add("field3", false)

        Assertions.assertThat(sprite.valueOf("field1")).isEqualTo("value1")
        Assertions.assertThat(sprite.valueOf("field2")).isEqualTo("10")
        Assertions.assertThat(sprite.valueOf("field3")).isEqualTo("false")

    }

    @Test
    internal fun `return multiValue as string`() {
        sprite.add("field1", 10)
                .add("field1", 20)
                .add("field1", 30)


        Assertions.assertThat(sprite.valuesOf("field1")).contains("10", "20", "30")

    }

    @Test
    internal fun `return multi value as type`() {
        sprite.add("field1", 10)
                .add("field1", 20)
                .add("field1", 30)


        Assertions.assertThat(sprite.rawValuesOf<Int>("field1")).contains(10, 20, 30)

    }

    @Test
    internal fun `return single value as type`() {
        sprite.add("field1", "value1")
                .add("field2", 10)
                .add("field3", false)

        Assertions.assertThat(sprite.rawValueOf<String>("field1")).isEqualTo("value1")
        Assertions.assertThat(sprite.rawValueOf<Int>("field2")).isEqualTo(10)
        Assertions.assertThat(sprite.rawValueOf<Boolean>("field3")).isEqualTo(false)

    }

    @Test
    internal fun shouldCopySingleValueField() {
        sprite.add("field", "value")
                .copy("field", "copyOfField")

        Assertions.assertThat(sprite.entries()).contains(
                entry("field", "value"),
                entry("copyOfField", "value")

        )
    }

    @Test
    internal fun shouldCopyMultiValuesField() {
        sprite.add("field", "value1")
                .add("field", "value2")
                .add("field", "value3")
                .add("field", "value4")
                .copy("field", "copyOfField")

        Assertions.assertThat(sprite.entries()).isNotEmpty

        Assertions.assertThat(sprite.entries()).contains(
                entry("field", "value1"),
                entry("field", "value2"),
                entry("field", "value3"),
                entry("field", "value4"),
                entry("copyOfField", "value1"),
                entry("copyOfField", "value2"),
                entry("copyOfField", "value3"),
                entry("copyOfField", "value4")
        )

    }

    @Test
    internal fun shouldNotAddsEmptyValues() {
        sprite.addAll("field", Lists.emptyList())

        assertThat(sprite.data).isEmpty()

        sprite.add("field", "")

        assertThat(sprite.data).isEmpty()

    }


    @Test
    internal fun shouldRenameField() {
        sprite.add("field", "value1")
                .add("field", "value2")
                .add("field", "value3")
                .add("field", "value4")
                .rename("field", "renamed")

        assertThat(sprite.data).contains(
                entry("renamed", "value1"),
                entry("renamed", "value2"),
                entry("renamed", "value3"),
                entry("renamed", "value4")
        )
    }


    @Test
    internal fun shouldRenameWithLambda() {
        sprite.add("a_field", "value1")
                .rename(Pattern.compile("a_.*"), { v: String -> v.removePrefix("a_") })

        assertThat(sprite.data)
                .contains(entry("field", "value1"))
                .hasSize(1)


    }

    @Test
    internal fun shouldJoinFieldValues() {
        sprite.add("field", "value1")
                .add("field", "value2")
                .add("field", "value3")
                .add("field", "value4")
                .joinValuesOf("field", "\n", "VALUES\n", "\nEND")

        Assertions.assertThat(sprite.isSingleValue("field")).isTrue()
        Assertions.assertThat(sprite.valueOf("field"))
                .isEqualTo("""VALUES
                    |value1
                    |value2
                    |value3
                    |value4
                    |END""".trimMargin())

    }

    @Test
    internal fun shouldSplitFieldValue() {
        sprite.add("field", "value1 value2 value3 value4")
                .splitValues("field", " ")

        Assertions.assertThat(sprite.isMultiValue("field")).isTrue()
        Assertions.assertThat(sprite.valuesOf("field"))
                .contains("value1", "value2", "value3", "value4")
    }


    @Test
    internal fun shouldBeIdempotentOnRenamingNotPresentField() {
        sprite.rename("notPresent", "renamed")

        assertThat(sprite.data).isEmpty()

    }

    @Test
    internal fun shouldRetrieveFieldNames() {
        sprite.add("field", "value")
                .add("field2", "value2")
                .add("field3", "value3")

        Assertions.assertThat(sprite.fields()).contains("field", "field2", "field3")

    }

    @Test
    internal fun `say if value exeists`() {
        sprite.add("field", "value")

        Assertions.assertThat(sprite.hasValue("value")).isTrue()
        Assertions.assertThat(sprite.hasValue("field", "value")).isTrue()

        Assertions.assertThat(sprite.hasNotValue("notPresent")).isTrue()
        Assertions.assertThat(sprite.hasNotValue("field", "NotPresent")).isTrue()

    }


    @Test
    internal fun shouldRetrieveFieldNamesWithPattern() {
        sprite.add("field1", "value")
                .add("field2", "value2")
                .add("field3", "value3")

        Assertions.assertThat(sprite.fields(Pattern.compile("field.*")))
                .contains("field1", "field2", "field3")

    }

    @Test
    internal fun shouldRemoveField() {
        sprite.add("field", "value")

        sprite.remove("field")

        assertThat(sprite.data).isEmpty()
    }

    @Test
    internal fun shouldRemoveFields() {
        sprite.add("field", "value")
                .add("field", "value2")
                .add("field2", "value2")

        sprite.remove(listOf("field", "field2"))

        assertThat(sprite.data).isEmpty()
    }

    @Test
    internal fun shouldRemoveFieldsWithPattern() {
        sprite.add("field", "value")
                .add("field", "value2")
                .add("field2", "value2")

        sprite.remove(Pattern.compile("field.*"))

        assertThat(sprite.data).isEmpty()
    }

    @Test
    internal fun shouldLoadFromMap() {
        val now = LocalDate.now()
        val input = mapOf(
                "field1" to "value1",
                "field2" to "value2",
                "field3" to now)

        sprite.load(input)

        Assertions.assertThat(sprite.hasField("field1")).isTrue()
        Assertions.assertThat(sprite.hasField("field2")).isTrue()

        Assertions.assertThat(sprite.hasField("field3")).isTrue()
        Assertions.assertThat(sprite.valueOf("field3")).isEqualTo(now.toString())


    }

    @Test
    internal fun shouldApplyLambdaToAllFieldValues() {
        sprite.add("field", "value")
                .add("field", "value2")

        sprite.apply("field", String::toUpperCase)

        assertThat(sprite.data).isNotEmpty()

        Assertions.assertThat(sprite.valuesOf("field")).contains("VALUE", "VALUE2")
    }

    @Test
    internal fun shouldApplyLambdaToAllFieldMatchingPattern() {
        sprite.add("firstField", "value")
                .add("secondField", "value2")

        sprite.apply(Pattern.compile(".*Field"), String::toUpperCase)

        assertThat(sprite.data).isNotEmpty()

        Assertions.assertThat(sprite.valuesOf("firstField")).contains("VALUE")
        Assertions.assertThat(sprite.valuesOf("secondField")).contains("VALUE2")
    }

    @Test
    internal fun shouldApplyLambdaToAllFieldValuesAndStoreOnAnotherField() {
        sprite.add("field", "value")
                .add("field", "value2")

        sprite.apply("field", String::toUpperCase, "to")


        assertThat(sprite.data).isNotEmpty()

        Assertions.assertThat(sprite.valuesOf("field")).contains("value", "value2")
        Assertions.assertThat(sprite.valuesOf("to")).contains("VALUE", "VALUE2")
    }


    @Test
    internal fun shouldReturnMapWithSingleValue() {
        val now = LocalDate.now()
        val map = sprite.add("field", "value")
                .add("field", "value2")
                .add("field", "value3")
                .add("dateField", now)
                .asSingleLevelMap()

        Assertions.assertThat(map?.get("field")).isEqualTo("value")
        Assertions.assertThat(map?.get("dateField")).isEqualTo(now)
    }

    @Test
    internal fun shouldReturnMapWithSingleValueAsString() {
        val now = LocalDate.now()
        val map = sprite.add("field", "value")
                .add("field", "value2")
                .add("field", "value3")
                .add("dateField", now)
                .asSingleLevelStringMap()

        Assertions.assertThat(map.get("field")).isEqualTo("value")


        Assertions.assertThat(map.get("dateField")).isEqualTo(now.toString())


    }

    @Test
    internal fun shouldReturnMapWithCollections() {
        val map = sprite.add("field", "value")
                .add("field", "value2")
                .add("field", "value3")
                .asRawMap()

        Assertions.assertThat(map?.get("field"))
                .hasSize(3)
                .contains("value", "value2", "value3")
    }

    @Test
    internal fun shouldReturnMap() {
        val map = sprite.add("field", "value")
                .add("field", "value2")
                .add("field", "value3")
                .add("single", "singleValue")
                .asMap()

        Assertions.assertThat(map.get("field") as List<String>)
                .hasSize(3)
                .contains("value", "value2", "value3")

        Assertions.assertThat(map.get("single") as String)
                .isEqualTo("singleValue")


    }


    @Test
    internal fun shouldAnswerOnFieldSize() {

        sprite.add("field", "value")
                .add("field", "value2")
                .add("field", "value3")
                .add("dateField", LocalDate.now())

        //field
        Assertions.assertThat(sprite.isMultiValue("field")).isTrue()
        Assertions.assertThat(sprite.isSingleValue("field")).isFalse()
        Assertions.assertThat(sprite.sizeOf("field")).isEqualTo(3)


        //dateField
        Assertions.assertThat(sprite.isMultiValue("dateField")).isFalse()
        Assertions.assertThat(sprite.isSingleValue("dateField")).isTrue()
        Assertions.assertThat(sprite.sizeOf("dateField")).isEqualTo(1)

    }

    @Test
    internal fun shouldBeEquals() {
        sprite.add("f", "v")

        val other = Sprite().add("f", "v")

        Assertions.assertThat(sprite).isEqualTo(other)
    }

    @Test
    internal fun testToString() {
        sprite.add("text", "the text")
                .add("text", "another text")
                .add("date", LocalDate.parse("20180901", DateTimeFormatter.ofPattern("yyyyMMdd")))
                .add("age", 10)


        Assertions.assertThat(sprite.toString()).isEqualTo("{date=[2018-09-01], text=[the text, another text], age=[10]}")
    }


    @Test
    internal fun article() {

        val data = Sprite()
                .add("age", 90)
                .add("name", "rob")
                .add("text", "first phrase")
                .add("text", "second phrase")
                .add("text", "third phrase")
                .add("text", "fourth phrase")
                .rename("age", "weight")
                .apply("weight", { v: Float -> v * 2.2 })
                .apply("name", String::toUpperCase)
                .joinValuesOf("text")


        println("""name  :: ${data.valueOf("name")}""")
        println("""weight:: ${data.valueOf("weight")}""")
        println("""text  :: ${data.valueOf("text")}""")

    }


    @Test
    internal fun shouldApply() {
        sprite.add("f1", "value1")

    }


}

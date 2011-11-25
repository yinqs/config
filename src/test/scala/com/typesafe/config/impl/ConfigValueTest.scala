/**
 *   Copyright (C) 2011 Typesafe Inc. <http://typesafe.com>
 */
package com.typesafe.config.impl

import org.junit.Assert._
import org.junit._
import com.typesafe.config.ConfigValue
import java.util.Collections
import scala.collection.JavaConverters._
import com.typesafe.config.ConfigObject
import com.typesafe.config.ConfigList
import com.typesafe.config.ConfigException
import com.typesafe.config.ConfigValueType
import com.typesafe.config.ConfigOrigin

class ConfigValueTest extends TestUtils {

    @Test
    def configOriginEquality() {
        val a = new SimpleConfigOrigin("foo")
        val sameAsA = new SimpleConfigOrigin("foo")
        val b = new SimpleConfigOrigin("bar")

        checkEqualObjects(a, a)
        checkEqualObjects(a, sameAsA)
        checkNotEqualObjects(a, b)
    }

    @Test
    def configIntEquality() {
        val a = intValue(42)
        val sameAsA = intValue(42)
        val b = intValue(43)

        checkEqualObjects(a, a)
        checkEqualObjects(a, sameAsA)
        checkNotEqualObjects(a, b)
    }

    @Test
    def configLongEquality() {
        val a = longValue(Integer.MAX_VALUE + 42L)
        val sameAsA = longValue(Integer.MAX_VALUE + 42L)
        val b = longValue(Integer.MAX_VALUE + 43L)

        checkEqualObjects(a, a)
        checkEqualObjects(a, sameAsA)
        checkNotEqualObjects(a, b)
    }

    @Test
    def configIntAndLongEquality() {
        val longVal = longValue(42L)
        val intValue = longValue(42)
        val longValueB = longValue(43L)
        val intValueB = longValue(43)

        checkEqualObjects(intValue, longVal)
        checkEqualObjects(intValueB, longValueB)
        checkNotEqualObjects(intValue, longValueB)
        checkNotEqualObjects(intValueB, longVal)
    }

    private def configMap(pairs: (String, Int)*): java.util.Map[String, AbstractConfigValue] = {
        val m = new java.util.HashMap[String, AbstractConfigValue]()
        for (p <- pairs) {
            m.put(p._1, intValue(p._2))
        }
        m
    }

    @Test
    def configObjectEquality() {
        val aMap = configMap("a" -> 1, "b" -> 2, "c" -> 3)
        val sameAsAMap = configMap("a" -> 1, "b" -> 2, "c" -> 3)
        val bMap = configMap("a" -> 3, "b" -> 4, "c" -> 5)
        // different keys is a different case in the equals implementation
        val cMap = configMap("x" -> 3, "y" -> 4, "z" -> 5)
        val a = new SimpleConfigObject(fakeOrigin(), aMap)
        val sameAsA = new SimpleConfigObject(fakeOrigin(), sameAsAMap)
        val b = new SimpleConfigObject(fakeOrigin(), bMap)
        val c = new SimpleConfigObject(fakeOrigin(), cMap)

        checkEqualObjects(a, a)
        checkEqualObjects(a, sameAsA)
        checkEqualObjects(b, b)
        checkEqualObjects(c, c)
        checkNotEqualObjects(a, b)
        checkNotEqualObjects(a, c)
        checkNotEqualObjects(b, c)

        // the config for an equal object is also equal
        val config = a.toConfig()
        checkEqualObjects(config, config)
        checkEqualObjects(config, sameAsA.toConfig())
        checkEqualObjects(a.toConfig(), config)
        checkNotEqualObjects(config, b.toConfig())
        checkNotEqualObjects(config, c.toConfig())

        // configs are not equal to objects
        checkNotEqualObjects(a, a.toConfig())
        checkNotEqualObjects(b, b.toConfig())
    }

    @Test
    def configListEquality() {
        val aScalaSeq = Seq(1, 2, 3) map { intValue(_): AbstractConfigValue }
        val aList = new SimpleConfigList(fakeOrigin(), aScalaSeq.asJava)
        val sameAsAList = new SimpleConfigList(fakeOrigin(), aScalaSeq.asJava)
        val bScalaSeq = Seq(4, 5, 6) map { intValue(_): AbstractConfigValue }
        val bList = new SimpleConfigList(fakeOrigin(), bScalaSeq.asJava)

        checkEqualObjects(aList, aList)
        checkEqualObjects(aList, sameAsAList)
        checkNotEqualObjects(aList, bList)
    }

    @Test
    def configSubstitutionEquality() {
        val a = subst("foo")
        val sameAsA = subst("foo")
        val b = subst("bar")

        checkEqualObjects(a, a)
        checkEqualObjects(a, sameAsA)
        checkNotEqualObjects(a, b)
    }

    @Test
    def configDelayedMergeEquality() {
        val s1 = subst("foo")
        val s2 = subst("bar")
        val a = new ConfigDelayedMerge(fakeOrigin(), List[AbstractConfigValue](s1, s2).asJava)
        val sameAsA = new ConfigDelayedMerge(fakeOrigin(), List[AbstractConfigValue](s1, s2).asJava)
        val b = new ConfigDelayedMerge(fakeOrigin(), List[AbstractConfigValue](s2, s1).asJava)

        checkEqualObjects(a, a)
        checkEqualObjects(a, sameAsA)
        checkNotEqualObjects(a, b)
    }

    @Test
    def configDelayedMergeObjectEquality() {
        val empty = SimpleConfigObject.empty()
        val s1 = subst("foo")
        val s2 = subst("bar")
        val a = new ConfigDelayedMergeObject(fakeOrigin(), List[AbstractConfigValue](empty, s1, s2).asJava)
        val sameAsA = new ConfigDelayedMergeObject(fakeOrigin(), List[AbstractConfigValue](empty, s1, s2).asJava)
        val b = new ConfigDelayedMergeObject(fakeOrigin(), List[AbstractConfigValue](empty, s2, s1).asJava)

        checkEqualObjects(a, a)
        checkEqualObjects(a, sameAsA)
        checkNotEqualObjects(a, b)
    }

    @Test
    def valuesToString() {
        // just check that these don't throw, the exact output
        // isn't super important since it's just for debugging
        intValue(10).toString()
        longValue(11).toString()
        doubleValue(3.14).toString()
        stringValue("hi").toString()
        nullValue().toString()
        boolValue(true).toString()
        val emptyObj = SimpleConfigObject.empty()
        emptyObj.toString()
        (new SimpleConfigList(fakeOrigin(), Collections.emptyList[AbstractConfigValue]())).toString()
        subst("a").toString()
        substInString("b").toString()
        val dm = new ConfigDelayedMerge(fakeOrigin(), List[AbstractConfigValue](subst("a"), subst("b")).asJava)
        dm.toString()
        val dmo = new ConfigDelayedMergeObject(fakeOrigin(), List[AbstractConfigValue](emptyObj, subst("a"), subst("b")).asJava)
        dmo.toString()

        fakeOrigin().toString()
    }

    private def unsupported(body: => Unit) {
        intercept[UnsupportedOperationException] {
            body
        }
    }

    @Test
    def configObjectUnwraps() {
        val m = new SimpleConfigObject(fakeOrigin(),
            configMap("a" -> 1, "b" -> 2, "c" -> 3))
        assertEquals(Map("a" -> 1, "b" -> 2, "c" -> 3), m.unwrapped().asScala)
    }

    @Test
    def configObjectImplementsMap() {
        val m: ConfigObject = new SimpleConfigObject(fakeOrigin(),
            configMap("a" -> 1, "b" -> 2, "c" -> 3))

        assertEquals(intValue(1), m.get("a"))
        assertEquals(intValue(2), m.get("b"))
        assertEquals(intValue(3), m.get("c"))
        assertNull(m.get("d"))
        // get can take a non-string
        assertNull(m.get(new Object()))

        assertTrue(m.containsKey("a"))
        assertFalse(m.containsKey("z"))
        // containsKey can take a non-string
        assertFalse(m.containsKey(new Object()))

        assertTrue(m.containsValue(intValue(1)))
        assertFalse(m.containsValue(intValue(10)))

        // can take a non-ConfigValue
        assertFalse(m.containsValue(new Object()))

        assertFalse(m.isEmpty())

        assertEquals(3, m.size())

        val values = Set(intValue(1), intValue(2), intValue(3))
        assertEquals(values, m.values().asScala.toSet)
        assertEquals(values, m.entrySet().asScala map { _.getValue() } toSet)

        val keys = Set("a", "b", "c")
        assertEquals(keys, m.keySet().asScala.toSet)
        assertEquals(keys, m.entrySet().asScala map { _.getKey() } toSet)

        unsupported { m.clear() }
        unsupported { m.put("hello", intValue(42)) }
        unsupported { m.putAll(Collections.emptyMap[String, AbstractConfigValue]()) }
        unsupported { m.remove("a") }
    }

    @Test
    def configListImplementsList() {
        val scalaSeq = Seq[AbstractConfigValue](stringValue("a"), stringValue("b"), stringValue("c"))
        val l: ConfigList = new SimpleConfigList(fakeOrigin(),
            scalaSeq.asJava)

        assertEquals(scalaSeq(0), l.get(0))
        assertEquals(scalaSeq(1), l.get(1))
        assertEquals(scalaSeq(2), l.get(2))

        assertTrue(l.contains(stringValue("a")))

        assertTrue(l.containsAll(List[AbstractConfigValue](stringValue("b")).asJava))
        assertFalse(l.containsAll(List[AbstractConfigValue](stringValue("d")).asJava))

        assertEquals(1, l.indexOf(scalaSeq(1)))

        assertFalse(l.isEmpty());

        assertEquals(scalaSeq, l.iterator().asScala.toSeq)

        unsupported { l.iterator().remove() }

        assertEquals(1, l.lastIndexOf(scalaSeq(1)))

        val li = l.listIterator()
        var i = 0
        while (li.hasNext()) {
            assertEquals(i > 0, li.hasPrevious())
            assertEquals(i, li.nextIndex())
            assertEquals(i - 1, li.previousIndex())

            unsupported { li.remove() }
            unsupported { li.add(intValue(3)) }
            unsupported { li.set(stringValue("foo")) }

            val v = li.next()
            assertEquals(l.get(i), v)

            if (li.hasPrevious()) {
                // go backward
                assertEquals(scalaSeq(i), li.previous())
                // go back forward
                li.next()
            }

            i += 1
        }

        l.listIterator(1) // doesn't throw!

        assertEquals(3, l.size())

        assertEquals(scalaSeq.tail, l.subList(1, l.size()).asScala)

        assertEquals(scalaSeq, l.toArray.toList)

        assertEquals(scalaSeq, l.toArray(new Array[ConfigValue](l.size())).toList)

        unsupported { l.add(intValue(3)) }
        unsupported { l.add(1, intValue(4)) }
        unsupported { l.addAll(List[ConfigValue]().asJava) }
        unsupported { l.addAll(1, List[ConfigValue]().asJava) }
        unsupported { l.clear() }
        unsupported { l.remove(intValue(2)) }
        unsupported { l.remove(1) }
        unsupported { l.removeAll(List[ConfigValue](intValue(1)).asJava) }
        unsupported { l.retainAll(List[ConfigValue](intValue(1)).asJava) }
        unsupported { l.set(0, intValue(42)) }
    }

    private def unresolved(body: => Unit) {
        intercept[ConfigException.NotResolved] {
            body
        }
    }

    @Test
    def notResolvedThrown() {
        // ConfigSubstitution
        unresolved { subst("foo").valueType() }
        unresolved { subst("foo").unwrapped() }

        // ConfigDelayedMerge
        val dm = new ConfigDelayedMerge(fakeOrigin(), List[AbstractConfigValue](subst("a"), subst("b")).asJava)
        unresolved { dm.valueType() }
        unresolved { dm.unwrapped() }

        // ConfigDelayedMergeObject
        val emptyObj = SimpleConfigObject.empty()
        val dmo = new ConfigDelayedMergeObject(fakeOrigin(),
            List[AbstractConfigValue](emptyObj, subst("a"), subst("b")).asJava)
        assertEquals(ConfigValueType.OBJECT, dmo.valueType())
        unresolved { dmo.unwrapped() }
        unresolved { dmo.containsKey(null) }
        unresolved { dmo.containsValue(null) }
        unresolved { dmo.entrySet() }
        unresolved { dmo.isEmpty() }
        unresolved { dmo.keySet() }
        unresolved { dmo.size() }
        unresolved { dmo.values() }
        unresolved { dmo.toConfig.getInt("foo") }
    }

    @Test
    def roundTripNumbersThroughString() {
        // formats rounded off with E notation
        val a = "132454454354353245.3254652656454808909932874873298473298472"
        // formats as 100000.0
        val b = "1e6"
        // formats as 5.0E-5
        val c = "0.00005"
        // formats as 1E100 (capital E)
        val d = "1e100"

        val obj = parseConfig("{ a : " + a + ", b : " + b + ", c : " + c + ", d : " + d + "}")
        assertEquals(Seq(a, b, c, d),
            Seq("a", "b", "c", "d") map { obj.getString(_) })

        // make sure it still works if we're doing concatenation
        val obj2 = parseConfig("{ a : xx " + a + " yy, b : xx " + b + " yy, c : xx " + c + " yy, d : xx " + d + " yy}")
        assertEquals(Seq(a, b, c, d) map { "xx " + _ + " yy" },
            Seq("a", "b", "c", "d") map { obj2.getString(_) })
    }

    @Test
    def mergeOriginsWorks() {
        def o(desc: String, empty: Boolean) = {
            val values = new java.util.HashMap[String, AbstractConfigValue]()
            if (!empty)
                values.put("hello", intValue(37))
            new SimpleConfigObject(new SimpleConfigOrigin(desc), values);
        }
        def m(values: AbstractConfigObject*) = {
            AbstractConfigObject.mergeOrigins(values: _*).description()
        }

        // simplest case
        assertEquals("merge of a,b", m(o("a", false), o("b", false)))
        // combine duplicate "merge of"
        assertEquals("merge of a,x,y", m(o("a", false), o("merge of x,y", false)))
        assertEquals("merge of a,b,x,y", m(o("merge of a,b", false), o("merge of x,y", false)))
        // ignore empty objects
        assertEquals("a", m(o("foo", true), o("a", false)))
        // unless they are all empty, pick the first one
        assertEquals("foo", m(o("foo", true), o("a", true)))
        // merge just one
        assertEquals("foo", m(o("foo", false)))
        // merge three
        assertEquals("merge of a,b,c", m(o("a", false), o("b", false), o("c", false)))
    }

    @Test
    def hasPathWorks() {
        val empty = parseConfig("{}")

        assertFalse(empty.hasPath("foo"))

        val obj = parseConfig("a=null, b.c.d=11, foo=bar")

        // returns true for the non-null values
        assertTrue(obj.hasPath("foo"))
        assertTrue(obj.hasPath("b.c.d"))
        assertTrue(obj.hasPath("b.c"))
        assertTrue(obj.hasPath("b"))

        // hasPath() is false for null values but containsKey is true
        assertEquals(nullValue(), obj.toObject.get("a"))
        assertTrue(obj.toObject.containsKey("a"))
        assertFalse(obj.hasPath("a"))

        // false for totally absent values
        assertFalse(obj.toObject.containsKey("notinhere"))
        assertFalse(obj.hasPath("notinhere"))

        // throws proper exceptions
        intercept[ConfigException.BadPath] {
            empty.hasPath("a.")
        }

        intercept[ConfigException.BadPath] {
            empty.hasPath("..")
        }

    }

    @Test
    def newNumberWorks() {
        def nL(v: Long) = ConfigNumber.newNumber(fakeOrigin(), v, null)
        def nD(v: Double) = ConfigNumber.newNumber(fakeOrigin(), v, null)

        // the general idea is that the destination type should depend
        // only on the actual numeric value, not on the type of the source
        // value.
        assertEquals(3.14, nD(3.14).unwrapped())
        assertEquals(1, nL(1).unwrapped())
        assertEquals(1, nD(1.0).unwrapped())
        assertEquals(Int.MaxValue + 1L, nL(Int.MaxValue + 1L).unwrapped())
        assertEquals(Int.MinValue - 1L, nL(Int.MinValue - 1L).unwrapped())
        assertEquals(Int.MaxValue + 1L, nD(Int.MaxValue + 1.0).unwrapped())
        assertEquals(Int.MinValue - 1L, nD(Int.MinValue - 1.0).unwrapped())
    }

    @Test
    def automaticBooleanConversions() {
        val trues = parseObject("{ a=true, b=yes, c=on }").toConfig
        assertEquals(true, trues.getBoolean("a"))
        assertEquals(true, trues.getBoolean("b"))
        assertEquals(true, trues.getBoolean("c"))

        val falses = parseObject("{ a=false, b=no, c=off }").toConfig
        assertEquals(false, falses.getBoolean("a"))
        assertEquals(false, falses.getBoolean("b"))
        assertEquals(false, falses.getBoolean("c"))
    }
}

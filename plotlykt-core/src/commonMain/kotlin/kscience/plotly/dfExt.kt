package kscience.plotly

import hep.dataforge.meta.*
import hep.dataforge.names.Name
import hep.dataforge.names.asName
import hep.dataforge.values.Value
import hep.dataforge.values.asValue
import hep.dataforge.values.long
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.milliseconds
import kotlin.time.toDuration

//extensions for DataForge

/**
 * A delegate for list of objects with specification
 * TODO move to DataForge core
 */
internal fun <T : Scheme> Configurable.list(
    spec: Specification<T>, key: Name? = null
): ReadWriteProperty<Any?, List<T>> = object : ReadWriteProperty<Any?, List<T>> {
    override fun getValue(thisRef: Any?, property: KProperty<*>): List<T> {
        val name = key ?: property.name.asName()
        return config.getIndexed(name).values.mapNotNull { item -> item.node?.let { spec.wrap(it) } }
    }

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: List<T>) {
        val name = key ?: property.name.asName()
        config.setIndexed(name, value.map { it.config })
    }
}

/**
 * List of values delegate
 */
internal fun Configurable.list(
    key: Name? = null
): ReadWriteProperty<Any?, List<Value>> = object : ReadWriteProperty<Any?, List<Value>> {
    override fun getValue(thisRef: Any?, property: KProperty<*>): List<Value> {
        val name = key ?: property.name.asName()
        return config[name].value?.list ?: emptyList()
    }

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: List<Value>) {
        val name = key ?: property.name.asName()
        config[name] = value.asValue()
    }
}

/**
 * A variation of [spec] extension with lazy initialization of empty specified nod in case it is missing
 */
internal fun <T : Scheme> Configurable.lazySpec(
    spec: Specification<T>, key: Name? = null
): ReadWriteProperty<Any?, T> = object : ReadWriteProperty<Any?, T> {
    override fun getValue(thisRef: Any?, property: KProperty<*>): T {
        val name = key ?: property.name.asName()
        return config[name].node?.let { spec.wrap(it) }
            ?: spec.empty().also { config[name] = it.config }
    }

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        val name = key ?: property.name.asName()
        config[name] = value.config
    }
}

/**
 * A safe [Double] range
 */
internal fun Configurable.doubleInRange(
    range: ClosedFloatingPointRange<Double>,
    key: Name? = null
): ReadWriteProperty<Any?, Double> = object : ReadWriteProperty<Any?, Double> {
    override fun getValue(thisRef: Any?, property: KProperty<*>): Double {
        val name = key ?: property.name.asName()
        return config[name].double ?: Double.NaN
    }

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: Double) {
        val name = key ?: property.name.asName()
        if (value in range) {
            config[name] = value
        } else {
            error("$value not in range $range")
        }
    }
}

/**
 * A safe [Double] ray
 */
internal fun Configurable.doubleGreaterThan(
        minValue: Double,
        key: Name? = null
): ReadWriteProperty<Any?, Double> = object : ReadWriteProperty<Any?, Double> {
    override fun getValue(thisRef: Any?, property: KProperty<*>): Double {
        val name = key ?: property.name.asName()
        return config[name].double ?: Double.NaN
    }

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: Double) {
        val name = key ?: property.name.asName()
        if (value >= minValue) {
            config[name] = value
        } else {
            error("$value less than $minValue")
        }
    }
}


/**
 * A safe [Int] ray
 */
internal fun Configurable.intGreaterThan(
        minValue: Int,
        key: Name? = null
): ReadWriteProperty<Any?, Int> = object : ReadWriteProperty<Any?, Int> {
    override fun getValue(thisRef: Any?, property: KProperty<*>): Int {
        val name = key ?: property.name.asName()
        return config[name].int ?: minValue
    }

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: Int) {
        val name = key ?: property.name.asName()
        if (value >= minValue) {
            config[name] = value
        } else {
            error("$value less than $minValue")
        }
    }
}

/**
 * A safe [Int] range
 */
internal fun Configurable.intInRange(
        range: ClosedRange<Int>,
        key: Name? = null
): ReadWriteProperty<Any?, Int> = object : ReadWriteProperty<Any?, Int> {
    override fun getValue(thisRef: Any?, property: KProperty<*>): Int {
        val name = key ?: property.name.asName()
        return config[name].int ?: 0
    }

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: Int) {
        val name = key ?: property.name.asName()
        if (value in range) {
            config[name] = value
        } else {
            error("$value not in range $range")
        }
    }
}

/**
 * A safe [Number] ray
 */
internal fun Configurable.numberGreaterThan(
        minValue: Number,
        key: Name? = null
): ReadWriteProperty<Any?, Number> = object : ReadWriteProperty<Any?, Number> {
    override fun getValue(thisRef: Any?, property: KProperty<*>): Number {
        val name = key ?: property.name.asName()
        return config[name].number ?: minValue
    }

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: Number) {
        val name = key ?: property.name.asName()
        if (value.toDouble() >= minValue.toDouble()) {
            config[name] = value
        } else {
            error("$value less than $minValue")
        }
    }
}

/**
 * A safe [Number] range
 */
internal fun Configurable.numberInRange(
        range: ClosedRange<Double>,
        key: Name? = null
): ReadWriteProperty<Any?, Number> = object : ReadWriteProperty<Any?, Number> {
    override fun getValue(thisRef: Any?, property: KProperty<*>): Number {
        val name = key ?: property.name.asName()
        return config[name].int ?: 0
    }

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: Number) {
        val name = key ?: property.name.asName()
        if (value.toDouble() in range) {
            config[name] = value
        } else {
            error("$value not in range $range")
        }
    }
}

@OptIn(DFExperimental::class)
internal fun Configurable.duration(
    default: Duration? = null,
    key: Name? = null
): ReadWriteProperty<Any?, Duration?> = object : ReadWriteProperty<Any?, Duration?> {
    override fun getValue(thisRef: Any?, property: KProperty<*>): Duration? {
        val name = key ?: property.name.asName()
        return when (val item = config[name]) {
            null -> default
            is MetaItem.ValueItem -> item.value.long.milliseconds
            is MetaItem.NodeItem<*> -> {
                val value = item.node["value"].long ?: error("Duration value is not defined")
                val unit = item.node["unit"].enum<DurationUnit>() ?: DurationUnit.MILLISECONDS
                value.toDuration(unit)
            }
        }
    }

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: Duration?) {
        val name = key ?: property.name.asName()
        if (value == null) {
            config.remove(name)
        } else {
            config.edit(name) {
                set("value", value.inMilliseconds)
                set("unit", DurationUnit.MILLISECONDS)
            }
        }
    }
}
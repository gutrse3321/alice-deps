package red.asuka.alice.db.jpa

import lombok.experimental.UtilityClass
import org.slf4j.LoggerFactory
import org.springframework.beans.BeanUtils
import org.springframework.beans.BeanWrapper
import org.springframework.beans.BeanWrapperImpl
import org.springframework.util.Assert
import java.beans.PropertyDescriptor
import java.lang.reflect.Field
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method
import java.math.BigDecimal
import java.sql.Date
import java.sql.Time
import java.sql.Timestamp
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import javax.persistence.EmbeddedId
import javax.persistence.Id

/**
 * @author Tomonori
 * @mail gutrse3321@live.com
 * @data 2020-12-12 16:32
 */
@UtilityClass
class JpaEntityPropertyUtility : BeanUtils() {

    private val cacheBeanPropertyDes: MutableMap<Class<*>, Array<PropertyDescriptor?>> = ConcurrentHashMap()
    private val cacheEntityPropertyDes: MutableMap<Class<*>, Array<PropertyDescriptor?>> = ConcurrentHashMap()
    private val excludes: MutableList<String> = ArrayList()
    private val notNullProperty: MutableList<Class<*>> = ArrayList()
    private val allProperty: MutableList<Class<*>> = ArrayList()
    private val cacheNotNullPropertyInfo: MutableMap<Class<*>, EntityPropertyInfo?> = ConcurrentHashMap<Class<*>, EntityPropertyInfo?>()
    private val cacheAllPropertyInfo: MutableMap<Class<*>, EntityPropertyInfo?> = ConcurrentHashMap<Class<*>, EntityPropertyInfo?>()

    companion object {
        protected val log = LoggerFactory.getLogger(JpaEntityPropertyUtility::class.java)
        private val cacheBeanProDes: Map<Class<*>, Array<PropertyDescriptor>> = ConcurrentHashMap()
    }

    init {
        excludes.add("class")
        notNullProperty.add(Int::class.java)
        notNullProperty.add(Short::class.java)
        notNullProperty.add(Byte::class.java)
        notNullProperty.add(Float::class.java)
        notNullProperty.add(Double::class.java)
        notNullProperty.add(Long::class.java)
        notNullProperty.add(Boolean::class.java)
        notNullProperty.add(Char::class.java)
        notNullProperty.add(String::class.java)
        notNullProperty.add(Timestamp::class.java)
        notNullProperty.add(Date::class.java)
        notNullProperty.add(Time::class.java)
        notNullProperty.add(BigDecimal::class.java)
        Int::class.javaPrimitiveType?.let { allProperty.add(it) }
        allProperty.add(Int::class.java)
        Short::class.javaPrimitiveType?.let { allProperty.add(it) }
        allProperty.add(Short::class.java)
        Byte::class.javaPrimitiveType?.let { allProperty.add(it) }
        allProperty.add(Byte::class.java)
        Float::class.javaPrimitiveType?.let { allProperty.add(it) }
        allProperty.add(Float::class.java)
        Double::class.javaPrimitiveType?.let { allProperty.add(it) }
        allProperty.add(Double::class.java)
        Long::class.javaPrimitiveType?.let { allProperty.add(it) }
        allProperty.add(Long::class.java)
        Boolean::class.javaPrimitiveType?.let { allProperty.add(it) }
        allProperty.add(Boolean::class.java)
        Char::class.javaPrimitiveType?.let { allProperty.add(it) }
        allProperty.add(Char::class.java)
        allProperty.add(String::class.java)
        allProperty.add(Timestamp::class.java)
        allProperty.add(Date::class.java)
        allProperty.add(Time::class.java)
        allProperty.add(BigDecimal::class.java)
    }

    fun getProperty(clazz: Class<*>, all: Boolean): EntityPropertyInfo? {
        Assert.notNull(clazz, "Class required")
        var entityPropertyInfo: EntityPropertyInfo = if (all) cacheAllPropertyInfo[clazz] else cacheNotNullPropertyInfo[clazz]
        if (null != entityPropertyInfo) return entityPropertyInfo
        entityPropertyInfo = getEntityPropertyInfo(clazz, if (all) allProperty else notNullProperty)
        if (all) {
            cacheAllPropertyInfo[clazz] = entityPropertyInfo
        } else {
            cacheNotNullPropertyInfo[clazz] = entityPropertyInfo
        }
        return entityPropertyInfo
    }

    fun getEntityPropertyInfo(clazz: Class<*>, include: List<Class<*>>?): EntityPropertyInfo {
        var include = include
        Assert.notNull(clazz, "class required")
        if (null == include) include = ArrayList()
        var primaryKey: PropertyDescriptor? = null
        val otherKeys: MutableList<PropertyDescriptor?> = ArrayList()
        val propertyDescriptors = getEntityPropertyDescriptor(clazz)
        for (descriptor in propertyDescriptors) {
            if (checkAnnotation(clazz, descriptor, Id::class.java) || checkAnnotation(clazz, descriptor, EmbeddedId::class.java)) {
                primaryKey = descriptor
                continue
            }
            if (!include.contains(descriptor!!.propertyType)) continue
            otherKeys.add(descriptor)
        }
        Assert.notNull(primaryKey, clazz.name + " cant find the primary key annotation")
        return EntityPropertyInfo(primaryKey, otherKeys.toTypedArray())
    }

    fun getEntityPropertyDescriptor(clazz: Class<*>): Array<PropertyDescriptor?> {
        var propertyDescriptors = cacheEntityPropertyDes[clazz]
        if (null == propertyDescriptors) {
            val propertyDesList = getPropertyDescriptors(clazz)
            val list: MutableList<PropertyDescriptor> = ArrayList()
            for (propertyDes in propertyDesList) {
                if ("class" == propertyDes.name) continue
                list.add(propertyDes)
            }
            propertyDescriptors = arrayOfNulls(list.size)
            cacheEntityPropertyDes[clazz] = list.toTypedArray()
        }
        return propertyDescriptors
    }

    fun checkAnnotation(clazz: Class<*>, proDes: PropertyDescriptor?, annotationClass: Class<out Annotation?>?): Boolean {
        Assert.notNull(clazz, " Class required")
        Assert.notNull(proDes, " PropertyDescriptor required")
        Assert.notNull(annotationClass, " Annotation class required")
        val readMethod = proDes!!.readMethod
        var method: Method?
        try {
            method = clazz.getMethod(readMethod.name, *readMethod.parameterTypes)
            if (method?.getAnnotation(annotationClass) != null) return true
        } catch (e: NoSuchMethodException) {
        }
        try {
            method = clazz.getDeclaredMethod(readMethod.name, *readMethod.parameterTypes)
            if (method?.getAnnotation(annotationClass) != null) return true
        } catch (e: NoSuchMethodException) {
        }
        var field: Field?
        try {
            field = clazz.getField(proDes.name)
            if (field?.getAnnotation(annotationClass) != null) return true
        } catch (e: NoSuchFieldException) {
        }
        try {
            field = clazz.getDeclaredField(proDes.name)
            if (field?.getAnnotation(annotationClass) != null) return true
        } catch (e: NoSuchFieldException) {
        }
        val superClass = clazz.superclass ?: return false
        return checkAnnotation(superClass, proDes, annotationClass)
    }

    @Throws(InvocationTargetException::class, IllegalAccessException::class)
    fun getValueByAnnotationId(entity: Any): Any {
        Assert.notNull(entity, " entity must be not null")
        val allProperty: EntityPropertyInfo? = getProperty(entity.javaClass, true)
        val readMethod: Method = allProperty.getPrimaryKey().getReadMethod()
        return readMethod.invoke(entity)
    }

    fun copyNotNull(origin: Any, target: Any) {
        Assert.notNull(origin, " Origin required")
        Assert.notNull(target, " Target required")
        if (origin.javaClass != target.javaClass && !origin.javaClass.isInstance(target)) {
            copyProperties(origin, target, *getNullPropertyNames(origin))
        } else {
            val beanPropertyDescriptor = getBeanPropertyDescriptor(origin.javaClass)
            for (descriptor in beanPropertyDescriptor) {
                val readMethod = descriptor!!.readMethod
                val writeMethod = descriptor.writeMethod
                val returnType = readMethod.returnType
                if (returnType.isPrimitive) continue
                try {
                    val invoke = readMethod.invoke(origin)
                    if (null != invoke) writeMethod.invoke(target, invoke)
                } catch (e: IllegalAccessException) {
                } catch (e: InvocationTargetException) {
                }
            }
        }
    }

    fun getNullPropertyNames(source: Any?): Array<String> {
        val src: BeanWrapper = BeanWrapperImpl(source!!)
        val pds = src.propertyDescriptors
        val emptyNames: MutableSet<String> = HashSet()
        for (pd in pds) {
            val srcValue = src.getPropertyValue(pd.name)
            if (srcValue == null) {
                emptyNames.add(pd.name)
            }
        }
        val result = arrayOfNulls<String>(emptyNames.size)
        return emptyNames.toTypedArray()
    }

    fun getBeanPropertyDescriptor(clazz: Class<*>): Array<PropertyDescriptor?> {
        var propertyDescriptors = cacheBeanPropertyDes[clazz]
        if (null == propertyDescriptors) {
            val propertyDesList = getPropertyDescriptors(clazz)
            val list: MutableList<PropertyDescriptor> = ArrayList()
            for (propertyDes in propertyDesList) {
                if (excludes.contains(propertyDes.name)) continue
                list.add(propertyDes)
            }
            propertyDescriptors = arrayOfNulls(list.size)
            cacheBeanPropertyDes[clazz] = list.toTypedArray()
        }
        return propertyDescriptors
    }
}
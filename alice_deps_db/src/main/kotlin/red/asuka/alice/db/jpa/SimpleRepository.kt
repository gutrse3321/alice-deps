package red.asuka.alice.db.jpa

import org.hibernate.SQLQuery
import org.hibernate.Session
import org.hibernate.query.criteria.internal.CriteriaBuilderImpl
import org.hibernate.transform.Transformers
import org.hibernate.type.*
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.support.JpaEntityInformation
import org.springframework.data.jpa.repository.support.JpaEntityInformationSupport
import org.springframework.data.jpa.repository.support.SimpleJpaRepository
import org.springframework.transaction.annotation.Transactional
import org.springframework.util.Assert
import org.springframework.util.CollectionUtils
import red.asuka.alice.db.jpa.entity.BaseEntity
import java.beans.PropertyDescriptor
import java.io.Serializable
import java.lang.reflect.InvocationTargetException
import java.math.BigDecimal
import java.sql.Date
import java.util.*
import javax.persistence.EntityManager
import javax.persistence.criteria.CriteriaBuilder
import javax.persistence.criteria.CriteriaDelete
import javax.persistence.criteria.CriteriaQuery
import javax.persistence.criteria.CriteriaUpdate

/**
 * @author Tomonori
 * @mail gutrse3321@live.com
 * @data 2020-12-12 16:18
 */
@Transactional
class SimpleRepository<T, ID : Serializable?> : SimpleJpaRepository<T, ID>, ISimpleRepository<T, ID> {

    protected val em: EntityManager
    protected val information: JpaEntityInformation<*, *>
    protected val session: Session
    /*******************************
     * constructor methods
     */
    constructor(domainClass: Class<T>?, em: EntityManager) : super(domainClass!!, em) {
        this.em = em
        //使用JpaEntityInformationSupport从其类中获取实体信息
        information = JpaEntityInformationSupport.getEntityInformation(domainClass, em)
        session = em.delegate as Session
    }

    constructor(entityInformation: JpaEntityInformation<T, *>, entityManager: EntityManager) : super(entityInformation, entityManager) {
        this.em = entityManager
        information = entityInformation
        session = entityManager.delegate as Session
    }

    private fun fillTime(entity: T) {
        val time = System.currentTimeMillis()
        val baseEntity: BaseEntity = entity as BaseEntity
        if (baseEntity.created_time == null) {
            baseEntity.created_time = time
            baseEntity.updated_time = time
        }
        baseEntity.updated_time = time
    }

    override fun <S : T?> save(entity: S): S {
        (entity as? BaseEntity)?.let { fillTime(it) }
        return super.save(entity)
    }

    override fun <S : T?> saveAll(entities: Iterable<S>): List<S> {
        val result: ArrayList<*> = ArrayList<Any?>()
        if (entities != null) {
            val var3: Iterator<*> = entities.iterator()
            while (var3.hasNext()) {
                val entity = var3.next() as S
                result.add(save(entity))
            }
        }
        return result
    }

    @Throws(Exception::class)
    override fun <S : T?> extSaveAllNot(entities: Iterable<S>?): List<S> {
        val result: ArrayList<*> = ArrayList<Any?>()
        if (entities != null) {
            val var3: Iterator<*> = entities.iterator()
            while (var3.hasNext()) {
                val entity = var3.next() as S
                result.add(extSaveNotNull(entity))
            }
        }
        return result
    }

    override fun <S : T?> saveAndFlush(entity: S): S {
        (entity as? BaseEntity)?.let { fillTime(it) }
        return super.saveAndFlush(entity)
    }

    override fun <S : T?> saveEntity(entity: S): S {
        return super.save(entity)
    }

    @Throws(Exception::class)
    override fun <S : T?> extSaveFull(entity: S): S {
        (entity as? BaseEntity)?.let { fillTime(it) }

        return if (information.isNew(entity)) {
            em.persist(entity)
            entity
        } else {
            val i = extUpdateFull(entity)
            if (i == 0) throw EXPF.E404(this.javaClass.simpleName, true)
            entity
        }
    }

    @Throws(Exception::class)
    override fun <S : T?> extSaveNotNull(entity: S): S {
        (entity as? BaseEntity)?.let { fillTime(it) }

        return if (information.isNew(entity)) {
            //创建实体持久化
            em.persist(entity)
            entity
        } else {
            val i = extUpdateNotNull(entity)
            if (i == 0) throw EXPF.E404(this.javaClass.simpleName, true)
            entity
        }
    }

    override fun extBuilder(): CriteriaBuilderImpl {
        return em.criteriaBuilder as CriteriaBuilderImpl
    }

    override fun <D> extExecuteCQ(cq: CriteriaQuery<D>?): List<D> {
        return em.createQuery(cq).resultList
    }

    override fun <D> extExecuteCQ(cq: CriteriaQuery<D>?, pageable: Pageable?): List<D> {
        val query = em.createQuery(cq)
        if (null != pageable) {
            query.firstResult = pageable.offset.toInt()
            query.maxResults = pageable.pageSize
        }
        return query.resultList
    }

    override fun <D> extExecuteCQ(cq: CriteriaQuery<D>?, page: Int, size: Int): List<D> {
        val query = em.createQuery(cq)
        query.firstResult = page * size
        query.maxResults = page
        return query.resultList
    }

    override fun <D> extExecuteCU(cu: CriteriaUpdate<D>?): Int {
        return em.createQuery(cu).executeUpdate()
    }

    override fun <D> extExecuteCD(cd: CriteriaDelete<D>?): Int {
        return em.createQuery(cd).executeUpdate()
    }

    override fun extDeleteBySoft(id: ID, vararg account: String?): Int {
        val builder = extBuilder()
        val cu = builder.createCriteriaUpdate(domainClass)
        val from = cu.from(domainClass)

        cu["data_state"] = Constant.DataState.Invalid.ordinal()
        cu["updated_time"] = System.currentTimeMillis()
        if (account.size > 0) {
            cu["updated_by"] = account[0]
        }

        val propertyInfo: EntityPropertyInfo = JpaEntityPropertyUtility.getProperty(domainClass, true)
        val where = builder.equal(from.get(propertyInfo.getPrimaryKey().getDisplayName()), id)
        cu.where(where)

        return extExecuteCU(cu)
    }

    override fun extDeleteByPhysically(id: ID): Int {
        val builder = extBuilder()
        val cd = builder.createCriteriaDelete(domainClass)
        val from = cd.from(domainClass)

        val propertyInfo: EntityPropertyInfo = JpaEntityPropertyUtility.getProperty(domainClass, true)
        val where = builder.equal(from.get(propertyInfo.getPrimaryKey().getDisplayName()), id)
        cd.where(where)

        return extExecuteCD(cd)
    }

    override fun extDisable(id: ID, account: String?): Int {
        val builder = extBuilder()
        val cu = builder.createCriteriaUpdate(domainClass)
        val from = cu.from(domainClass)

        cu["data_state"] = Constant.DataState.Disable.ordinal()
        cu["updated_time"] = System.currentTimeMillis()
        cu["updated_by"] = account

        val propertyInfo: EntityPropertyInfo = JpaEntityPropertyUtility.getProperty(domainClass, true)
        val where = builder.equal(from.get(propertyInfo.getPrimaryKey().getDisplayName()), id)
        cu.where(where)

        return extExecuteCU(cu)
    }

    override fun extAvailable(id: ID, account: String?): Int {
        val builder = extBuilder()
        val cu = builder.createCriteriaUpdate(domainClass)
        val from = cu.from(domainClass)

        cu["data_state"] = Constant.DataState.Available.ordinal()
        cu["updated_time"] = System.currentTimeMillis()
        cu["updated_by"] = account

        val propertyInfo: EntityPropertyInfo = JpaEntityPropertyUtility.getProperty(domainClass, true)
        val where = builder.equal(from.get(propertyInfo.getPrimaryKey().getDisplayName()), id)
        cu.where(where)

        return extExecuteCU(cu)
    }

    override fun extRefreshUpdateTime(id: ID, time: Long?): Int {
        val builder = extBuilder()
        val cu = builder.createCriteriaUpdate(domainClass)
        val from = cu.from(domainClass)

        cu["update_time"] = time

        val propertyInfo: EntityPropertyInfo = JpaEntityPropertyUtility.getProperty(domainClass, true)
        val where = builder.equal(from.get(propertyInfo.getPrimaryKey().getDisplayName()), id)
        cu.where(where)

        //执行修改方法
        return extExecuteCU(cu)
    }

    override fun extRefreshUpdateTime(id: ID, time: String?): Int {
        return this.extRefreshUpdateTime(id, java.lang.Long.valueOf(time))
    }

    @Throws(InvocationTargetException::class, IllegalAccessException::class)
    override fun <S : T?> extUpdateFull(entity: S): Int {
        if (entity is BaseEntity) {
            fillTime(entity)
        }

        val clazz = entity!!.javaClass
        val builder: CriteriaBuilder = extBuilder()
        val cu = builder.createCriteriaUpdate(clazz)
        val from = cu.from(clazz)

        val propertyInfo: EntityPropertyInfo = JpaEntityPropertyUtility.getProperty(clazz, true)
        Assert.notNull(propertyInfo, "PropertyDescriptor is not null")
        val otherKey: Array<PropertyDescriptor> = propertyInfo.getOtherKey()
        for (pro in otherKey) {
            val readMethod = pro.readMethod
            val invoke = readMethod.invoke(entity)
            cu[pro.name] = invoke
        }
        val primaryKey: PropertyDescriptor = propertyInfo.getPrimaryKey()
        val readMethod = primaryKey.readMethod
        val invoke = readMethod.invoke(entity)
        Assert.notNull(invoke, "Modify the object [id] is empty")
        val where = builder.equal(from.get<Any>(primaryKey.name), invoke)
        cu.where(where)
        return extExecuteCU(cu)
    }

    @Throws(InvocationTargetException::class, IllegalAccessException::class)
    override fun <S : T?> extUpdateNotNull(entity: S): Int {
        if (entity is BaseEntity) {
            fillTime(entity)
        }

        val clazz = entity!!.javaClass
        val builder: CriteriaBuilder = extBuilder()
        val cu = builder.createCriteriaUpdate(clazz)
        val from = cu.from(clazz)

        val propertyInfo: EntityPropertyInfo = JpaEntityPropertyUtility.getProperty(clazz, false)
        Assert.notNull(propertyInfo, "PropertyDescriptor is not null")
        val otherKey: Array<PropertyDescriptor> = propertyInfo.getOtherKey()
        for (pro in otherKey) {
            val readMethod = pro.readMethod
            val invoke = readMethod.invoke(entity)
            if (null != invoke) cu[pro.name] = invoke
        }
        val primaryKey: PropertyDescriptor = propertyInfo.getPrimaryKey()
        val readMethod = primaryKey.readMethod
        val invoke = readMethod.invoke(entity)
        Assert.notNull(invoke, "Modify the object [id] is empty")
        val where = builder.equal(from.get<Any>(primaryKey.name), invoke)
        cu.where(where)
        return extExecuteCU(cu)
    }

    /**
     * 查询单个
     * 一次执行多次查询来统计某些信息，这时为了保证数据整体的一致性，要用只读事务（Transactional(readOnly = true)）
     * @param id
     * @return
     */
    @Transactional(readOnly = true)
    override fun extFindOne(id: ID): T {
        //2.0之前 使用 findOne(ID)
        return super.findById(id).get()
    }

    override fun extFindOne(id: ID, state: Constant.DataState): T? {
        //创建标准查询构造器实例
        val builder = extBuilder()
        //获取主类实体属性信息（全部
        val propertyInfo: EntityPropertyInfo = JpaEntityPropertyUtility.getProperty(domainClass, true)
        //创建批量查询的CriteriaQuery接口
        val query = builder.createQuery(domainClass)
        //创建并添加与作为查询目标的实体相对应的查询根
        val from = query.from(domainClass)

        val whereDataState = builder.equal(from["data_state"], state.ordinal())
        val whereId = builder.equal(from.get(propertyInfo.getPrimaryKey().getName()), id)
        query.where(whereId, whereDataState)

        val ts = this.extExecuteCQ(query)
        return if (CollectionUtils.isEmpty(ts)) null else ts[0]
    }

    override fun <D> extFindByNativeSQL(sql: String?, clazz: Class<D>?, parameter: List<Any?>?): List<D>? {
        Assert.notNull(sql, "NativeSql is not null")
        val cq: SQLQuery<*> = session.createSQLQuery(sql)
        clazz?.let { addSclar(cq, it) }
        for ((i, obj) in parameter.withIndex()) {
            cq.setParameter(i, obj)
        }
        return cq.list() as List<D>
    }

    companion object {
        /***********************
         * static methods
         * @param query
         * @param clazz
         */
        fun addSclar(query: SQLQuery<*>, clazz: Class<*>?) {
            Assert.notNull(query, "Query sql required")
            Assert.notNull(clazz, "Entity class required")
            val propertyDescriptor: Array<PropertyDescriptor> = JpaEntityPropertyUtility.getEntityPropertyDescriptor(clazz)
            for (descriptor in propertyDescriptor) {
                val name = descriptor.name
                val propertyType = descriptor.propertyType
                if (propertyType == Long::class.javaPrimitiveType || propertyType == Long::class.java) {
                    query.addScalar(name, LongType.INSTANCE)
                } else if (propertyType == Int::class.javaPrimitiveType || propertyType == Int::class.java) {
                    query.addScalar(name, IntegerType.INSTANCE)
                } else if (propertyType == Char::class.javaPrimitiveType || propertyType == Char::class.java) {
                    query.addScalar(name, CharacterType.INSTANCE)
                } else if (propertyType == Short::class.javaPrimitiveType || propertyType == Short::class.java) {
                    query.addScalar(name, ShortType.INSTANCE)
                } else if (propertyType == Double::class.javaPrimitiveType || propertyType == Double::class.java) {
                    query.addScalar(name, DoubleType.INSTANCE)
                } else if (propertyType == Float::class.javaPrimitiveType || propertyType == Float::class.java) {
                    query.addScalar(name, FloatType.INSTANCE)
                } else if (propertyType == Boolean::class.javaPrimitiveType || propertyType == Boolean::class.java) {
                    query.addScalar(name, BooleanType.INSTANCE)
                } else if (propertyType == String::class.java) {
                    query.addScalar(name, StringType.INSTANCE)
                } else if (propertyType == Date::class.java) {
                    query.addScalar(name, DateType.INSTANCE)
                } else if (propertyType == BigDecimal::class.java) {
                    query.addScalar(name, BigDecimalType.INSTANCE)
                }
            }
            query.setResultTransformer(Transformers.aliasToBean(clazz))
        }
    }
}
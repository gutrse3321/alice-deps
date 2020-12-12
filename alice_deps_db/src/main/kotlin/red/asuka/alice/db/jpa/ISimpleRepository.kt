package red.asuka.alice.db.jpa

import org.hibernate.query.criteria.internal.CriteriaBuilderImpl
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.repository.NoRepositoryBean
import java.io.Serializable
import java.lang.reflect.InvocationTargetException
import javax.persistence.criteria.CriteriaDelete
import javax.persistence.criteria.CriteriaQuery
import javax.persistence.criteria.CriteriaUpdate

/**
 * @author Tomonori
 * @mail gutrse3321@live.com
 * @data 2020-12-12 16:29
 */
@NoRepositoryBean
interface ISimpleRepository<T, ID : Serializable?> : JpaRepository<T, ID>, JpaSpecificationExecutor<T> {

    fun extBuilder(): CriteriaBuilderImpl?
    fun <D> extExecuteCQ(cq: CriteriaQuery<D>?): List<D>?
    fun <D> extExecuteCQ(cq: CriteriaQuery<D>?, pageable: Pageable?): List<D>?
    fun <D> extExecuteCQ(cq: CriteriaQuery<D>?, page: Int, size: Int): List<D>?

    fun <D> extExecuteCU(cu: CriteriaUpdate<D>?): Int

    fun <D> extExecuteCD(cd: CriteriaDelete<D>?): Int

    @Throws(Exception::class)
    fun <S : T?> extSaveFull(entity: S): S

    @Throws(Exception::class)
    fun <S : T?> extSaveNotNull(entity: S): S

    @Throws(Exception::class)
    fun <S : T?> extSaveAllNot(entities: Iterable<S>?): List<S>?

    fun extDeleteBySoft(id: ID, vararg account: String?): Int

    fun extDeleteByPhysically(id: ID): Int

    fun extDisable(id: ID, update_by: String?): Int

    fun extAvailable(id: ID, update_by: String?): Int

    fun extRefreshUpdateTime(id: ID, time: Long?): Int
    fun extRefreshUpdateTime(id: ID, time: String?): Int

    @Throws(InvocationTargetException::class, IllegalAccessException::class)
    fun <S : T?> extUpdateFull(entity: S): Int

    @Throws(InvocationTargetException::class, IllegalAccessException::class)
    fun <S : T?> extUpdateNotNull(entity: S): Int
    fun extFindOne(id: ID): T
    fun extFindOne(id: ID, state: Constant.DataState?): T

    fun <D> extFindByNativeSQL(sql: String?, clazz: Class<D>?, parameter: List<Any?>?): List<D>?

    fun <S : T?> saveEntity(entity: S): S
}
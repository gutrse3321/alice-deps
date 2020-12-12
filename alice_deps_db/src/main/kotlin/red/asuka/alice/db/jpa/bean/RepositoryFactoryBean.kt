package red.asuka.alice.db.jpa.bean

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.support.JpaRepositoryFactoryBean
import org.springframework.data.repository.core.support.RepositoryFactorySupport
import java.io.Serializable
import javax.persistence.EntityManager

/**
 * @author Tomonori
 * @mail gutrse3321@live.com
 * @data 2020-12-12 16:17
 */
class RepositoryFactoryBean<T : JpaRepository<S, ID>?, S, ID : Serializable?>

(repositoryInterface: Class<out T>?) : JpaRepositoryFactoryBean<T, S, ID>(repositoryInterface!!) {

    override fun createRepositoryFactory(entityManager: EntityManager): RepositoryFactorySupport {
        return RepositoryFactory(entityManager)
    }
}
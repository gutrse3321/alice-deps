package red.asuka.alice.db.jpa.bean

import org.springframework.data.jpa.repository.support.JpaRepositoryFactory
import org.springframework.data.repository.core.RepositoryMetadata
import red.asuka.alice.db.jpa.SimpleRepository
import javax.persistence.EntityManager

/**
 * @author Tomonori
 * @mail gutrse3321@live.com
 * @data 2020-12-12 16:17
 */
class RepositoryFactory(entityManager: EntityManager?) : JpaRepositoryFactory(entityManager!!) {
    override fun getRepositoryBaseClass(metadata: RepositoryMetadata): Class<*> {
        return SimpleRepository::class.java
    }
}
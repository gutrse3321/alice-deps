package red.asuka.alice.db.jpa

import org.springframework.boot.autoconfigure.AutoConfigureAfter
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.orm.jpa.JpaTransactionManager
import org.springframework.orm.jpa.JpaVendorAdapter
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean
import org.springframework.orm.jpa.vendor.Database
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.annotation.EnableTransactionManagement
import red.asuka.alice.db.jdbc.JdbcAutoConfiguration
import red.asuka.alice.db.jpa.bean.RepositoryFactoryBean
import java.util.*
import javax.sql.DataSource

/**
 * @author Tomonori
 * @mail gutrse3321@live.com
 * @data 2020-12-12 16:15
 */
@Configuration
@EnableConfigurationProperties(JpaProperties::class)
@ConditionalOnProperty(value = ["alice.jpa.enabled"], havingValue = "true")
//JdbcAutoConfiguration配置类加载完再加载JpaAutoConfiguration配置类
@AutoConfigureAfter(JdbcAutoConfiguration::class)
@EnableJpaRepositories(basePackages = ["red.alice.alice.server.persist.repository"], repositoryFactoryBeanClass = RepositoryFactoryBean::class)
@EnableTransactionManagement
class JpaAutoConfiguration {
    @Bean
    fun jpaVendorAdapter(): JpaVendorAdapter {
        val hibernateJpaVendorAdapter = HibernateJpaVendorAdapter()
        hibernateJpaVendorAdapter.setShowSql(true) //设置是否在日志（或控制台）中显示SQL
        hibernateJpaVendorAdapter.setGenerateDdl(false) //设置是否在初始化EntityManagerFactory后创建/更新所有相关表来生成DDL
        hibernateJpaVendorAdapter.setDatabase(Database.MYSQL) //指定要操作的目标数据库，作为Database枚举
        return hibernateJpaVendorAdapter
    }

    @Bean(name = ["entityManagerFactory"])
    fun managerFactoryBean(builder: EntityManagerFactoryBuilder, dataSource: DataSource?): LocalContainerEntityManagerFactoryBean {
        val entityManager = builder.dataSource(dataSource)
                .packages("red.asuka.alice.server.persist.entity") //扫描@Entity实体
                .persistenceUnit("entityManager")
                .build()
        entityManager.jpaVendorAdapter = jpaVendorAdapter()
        val map: MutableMap<String, String?> = HashMap()
        map["javax.persistence.validation.mode"] = "none"
        entityManager.setJpaPropertyMap(map)
        return entityManager
    }

    @Bean(name = ["transactionManager"]) //自动化配置
    @ConditionalOnProperty(value = ["alice.jpa.enable-transaction"], havingValue = "true")
    fun businessTransactionManager(managerFactoryBean: LocalContainerEntityManagerFactoryBean): PlatformTransactionManager {
        return JpaTransactionManager(managerFactoryBean.getObject()!!)
    }
}
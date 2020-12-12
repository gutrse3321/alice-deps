package red.asuka.alice.db.jdbc

import com.zaxxer.hikari.HikariDataSource
import org.springframework.beans.BeansException
import org.springframework.beans.factory.InitializingBean
import org.springframework.beans.factory.support.GenericBeanDefinition
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.jdbc.DataSourceBuilder
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.context.support.GenericApplicationContext
import org.springframework.jdbc.datasource.LazyConnectionDataSourceProxy
import org.springframework.jdbc.datasource.lookup.BeanFactoryDataSourceLookup
import red.asuka.alice.db.aop.DynamicDataSourceAspect
import red.asuka.alice.db.jdbc.multiData.DynamicDataSourceRouter
import java.beans.PropertyDescriptor
import java.util.*
import javax.sql.DataSource

/**
 * @author Tomonori
 * @mail gutrse3321@live.com
 * @data 2020-12-12 15:50
 */
@Configuration
@EnableConfigurationProperties(JdbcProperties::class)
class JdbcAutoConfiguration(jdbcProperties: JdbcProperties) : ApplicationContextAware, InitializingBean {

    private var jdbcProperties: JdbcProperties = jdbcProperties
    private var applicationContext: GenericApplicationContext? = null
    private fun makeDataSourceBeanName(name: String): String {
        return "asuka_alice_db_" + name.replace("-", "_").replace(".", "_").replace(" ", "_").toLowerCase()
    }

    @Bean
    fun beanFactoryDataSourceLookup(): BeanFactoryDataSourceLookup {
        return BeanFactoryDataSourceLookup()
    }

    @Bean
    @Primary
    fun dataSource(): DataSource {
        val router = DynamicDataSourceRouter()
        router.setDataSourceLookup(beanFactoryDataSourceLookup())
        val targetDataSources: MutableMap<Any, Any> = LinkedHashMap()
        jdbcProperties.dataSource.forEach { dataSourceProperties -> targetDataSources[dataSourceProperties.name] = makeDataSourceBeanName(dataSourceProperties.name) }
        router.setTargetDataSources(targetDataSources)
        router.setDefaultTargetDataSource(makeDataSourceBeanName(jdbcProperties.defaultDataSource.name))
        return router
    }

    @Bean //自动化配置
    @ConditionalOnProperty(value = ["alice.jdbc.enable-dynamic-switch"], havingValue = "true")
    fun dynamicDataSourceAspect(): DynamicDataSourceAspect {
        return DynamicDataSourceAspect()
    }

    @Throws(BeansException::class)
    override fun setApplicationContext(applicationContext: ApplicationContext) {
        if (applicationContext is GenericApplicationContext) {
            this.applicationContext = applicationContext
        }
    }

    @Throws(Exception::class)
    override fun afterPropertiesSet() {
        registerDataSource(jdbcProperties.defaultDataSource)
        jdbcProperties.dataSource.forEach { dataSourceProperties: DataSourceProperties -> registerDataSource(dataSourceProperties) }
    }

    private fun registerDataSource(dataSourceProperties: DataSourceProperties) {
        var dataSourceProperties: DataSourceProperties = dataSourceProperties
        val defaultSource = DataSourceProperties()
        EntityPropertyUtility.copyNotNull(jdbcProperties.defaultDataSource, defaultSource)
        EntityPropertyUtility.copyNotNull(dataSourceProperties, defaultSource)
        dataSourceProperties = defaultSource

        val propertyDescriptors: Array<PropertyDescriptor> = EntityPropertyUtility.getPropertyDescriptors(DataSourceProperties::class.java)
        val map: MutableMap<String?, Any?> = HashMap()

        for (descriptor in propertyDescriptors) {
            if (dataSourceProperties.ignore().contains(descriptor.name)) continue
            val value: Any = ReflectionUtility.invokeMethod(descriptor.readMethod, dataSourceProperties)
                    ?: continue
            map[descriptor.name] = value
        }

        val beanDefinition = GenericBeanDefinition()
        if (jdbcProperties.isEnableLazyProxy()) {
            beanDefinition.setBeanClass(LazyConnectionDataSourceProxy::class.java)
            val dataSource: DataSource = DataSourceBuilder.create().type(HikariDataSource::class.java).build()
            for ((key, value) in map) {
                val propertyDescriptor: PropertyDescriptor = EntityPropertyUtility.getPropertyDescriptor(dataSource.javaClass, key)
                ReflectionUtility.invokeMethod(propertyDescriptor.writeMethod, dataSource, value)
            }
            beanDefinition.propertyValues.addPropertyValue("targetDataSource", dataSource)
        } else {
//            beanDefinition.setBeanClass(DataSourceBuilder.create().type(org.apache.tomcat.jdbc.pool.DataSource.class).findType(null));
            beanDefinition.setBeanClass(DataSourceBuilder.findType(null))
            beanDefinition.propertyValues.addPropertyValues(map)
        }
        beanDefinition.isSynthetic = true

        applicationContext!!.registerBeanDefinition(makeDataSourceBeanName(dataSourceProperties.name), beanDefinition)
    }
}
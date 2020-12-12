package red.asuka.alice.db.mybatis

import org.apache.ibatis.session.SqlSessionFactory
import org.mybatis.spring.SqlSessionFactoryBean
import org.springframework.boot.autoconfigure.AutoConfigureAfter
import org.springframework.boot.autoconfigure.AutoConfigureBefore
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.Resource
import org.springframework.core.io.support.PathMatchingResourcePatternResolver
import red.asuka.alice.db.jdbc.JdbcAutoConfiguration
import red.asuka.alice.db.jpa.JpaAutoConfiguration
import java.util.*
import javax.sql.DataSource

/**
 * @author Tomonori
 * @mail gutrse3321@live.com
 * @data 2020-12-12 15:44
 */
@Configuration
@EnableConfigurationProperties(MybatisProperties::class) //自动化配置
@ConditionalOnProperty(value = ["alice.mybatis.enabled"], havingValue = "true")
@AutoConfigureAfter(JdbcAutoConfiguration::class)
@AutoConfigureBefore(JpaAutoConfiguration::class)
class MybatisAutoConfiguration(mybatisProperties: MybatisProperties) {

    private val mybatisProperties: MybatisProperties = mybatisProperties

    @Bean
    @Throws(Exception::class)
    fun sqlSessionFactory(dataSource: DataSource?): SqlSessionFactory? {
        val sessionFactoryBean = SqlSessionFactoryBean()
        //数据源，由jdbcAutoConfiguration中注入的bean，装配到这里
        sessionFactoryBean.setDataSource(dataSource)
        //资源服务
        sessionFactoryBean.vfs = MybatisVFS::class.java
        if (StringUtility.hasText(mybatisProperties.getMapperLocations())) {
            val resourceList: List<Resource> = ArrayList()
            val split: Array<String> = mybatisProperties.getMapperLocations().split(",")
            val resolver = PathMatchingResourcePatternResolver()

            for (s in split) {
                Collections.addAll(resourceList, *resolver.getResources(s))
            }

            sessionFactoryBean.setMapperLocations(*resourceList.toTypedArray())
        }

        if (StringUtility.hasText(mybatisProperties.getTypeAliasesPackage())) {
            sessionFactoryBean.setTypeAliasesPackage(mybatisProperties.getTypeAliasesPackage())
        }

        if (StringUtility.hasText(mybatisProperties.getTypeHandlersPackage())) {
            sessionFactoryBean.setTypeHandlersPackage(mybatisProperties.getTypeHandlersPackage())
        }

        return sessionFactoryBean.getObject()
    }
}
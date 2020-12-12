package red.asuka.alice.db.mybatis

import lombok.Data
import org.springframework.boot.context.properties.ConfigurationProperties

/**
 * @author Tomonori
 * @mail gutrse3321@live.com
 * @data 2020-12-12 15:48
 */
@Data
@ConfigurationProperties(prefix = "alice.mybatis")
class MybatisProperties {
    private val enabled = false
    private val typeAliasesPackage: String? = null
    private val typeHandlersPackage: String? = null
    private val mapperLocations: String? = null
}
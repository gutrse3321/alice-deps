package red.asuka.alice.db.jdbc

import lombok.Data
import org.springframework.boot.context.properties.ConfigurationProperties
import java.util.*

/**
 * @author Tomonori
 * @mail gutrse3321@live.com
 * @data 2020-12-12 15:51
 */
@Data
@ConfigurationProperties(prefix = "alice.jdbc")
class JdbcProperties {

    val enableLazyProxy = false
    val enableDynamicSwitching = false
    val defaultDataSource: DataSourceProperties = DataSourceProperties()
    val dataSource: List<DataSourceProperties> = ArrayList<DataSourceProperties>()

    fun isEnableLazyProxy(): Boolean {
        return this.enableLazyProxy
    }
}
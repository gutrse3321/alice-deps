package red.asuka.alice.db.jdbc

import lombok.Data
import java.util.*

/**
 * @author Tomonori
 * @mail gutrse3321@live.com
 * @data 2020-12-12 15:52
 */
@Data
class DataSourceProperties {

    val name: String = ""
    val db: String? = null
    val baseUrl: String? = null
    val searchUrl: String? = null
    val driverClassName: String? = null
    var jdbcUrl: String? = null
        get() {
            if (null == field) {
                field = makeUrl()
            }
            return field
        }
        private set
    val username: String? = null
    val password: String? = null

    //TODO NEW
    val minimumIdle: Int? = null
    val maximumPoolSize: Int? = null
    val connectionTimeout: Long? = null
    val validationTimeout: Long? = null
    val connectionInitSql: String? = null
    val readOnly: Boolean? = null
    val autoCommit: Boolean? = null
    //FIXME OLD
    //private Integer maxActive;
    //private Integer initialSize;
    //private Integer minIdle;
    //private Integer maxIdle;
    //private Boolean testWhileIdle;
    //private Boolean testOnBorrom;
    //private Integer minEvictableIdleTimeMillis;
    //private Integer timeBetweenEvictionRunsMillis;
    //private Integer maxWait;
    //private Integer removeAbandonedTimeout;
    //private Boolean removeAbandoned;
    //private Integer validationInterval;
    //private String  validationQuery;
    //private Boolean defaultAutoCommit;
    //private Boolean defaultReadOnly;

    fun makeUrl(): String {
        val builder = StringBuilder()
        builder.append(this.baseUrl)
        if (StringUtility.hasText(this.db)) {
            builder.append("/").append(this.db)
        }
        //连接地址参数
        if (StringUtility.hasText(this.searchUrl)) {
            builder.append("?").append(this.searchUrl)
        }
        return builder.toString()
    }

    fun ignore(): List<String> {
        val list: MutableList<String> = ArrayList()
        list.add("name")
        list.add("db")
        list.add("baseUrl")
        list.add("searchUrl")
        list.add("class")
        return list
    }
}
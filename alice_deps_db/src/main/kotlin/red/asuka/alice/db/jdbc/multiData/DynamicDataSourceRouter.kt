package red.asuka.alice.db.jdbc.multiData

import org.slf4j.LoggerFactory
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource

/**
 * @author Tomonori
 * @mail gutrse3321@live.com
 * @data 2020-12-12 15:54
 */
class DynamicDataSourceRouter : AbstractRoutingDataSource() {

    companion object {
        private val log = LoggerFactory.getLogger(DynamicDataSourceRouter::class.java)
        private val dataSource = ThreadLocal<String>()
    }

    override fun determineCurrentLookupKey(): Any? {
        return dataSource
    }

    fun getDataSource(): String {
        return dataSource.get()
    }

    fun clearDataSource() {
        dataSource.remove()
    }

    fun setDataSource(customerType: String) {
        dataSource.set(customerType)
    }
}
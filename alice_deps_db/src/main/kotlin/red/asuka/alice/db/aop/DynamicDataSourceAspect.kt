package red.asuka.alice.db.aop

import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.annotation.Order
import red.asuka.alice.db.jdbc.annotiation.DataSource
import red.asuka.alice.db.jdbc.multiData.DynamicDataSourceRouter

/**
 * @author Tomonori
 * @mail gutrse3321@live.com
 * @data 2020-12-12 16:06
 */
@Aspect
@Order(3)
class DynamicDataSourceAspect {
    @Autowired
    private val dataSourceRouter: DynamicDataSourceRouter? = null

    @Around("@annotation(dataSource)")
    @Throws(Throwable::class)
    fun around(pjp: ProceedingJoinPoint, dataSource: DataSource): Any {
        dataSourceRouter?.setDataSource(dataSource.key)
        return try {
            pjp.proceed()
        } finally {
            dataSourceRouter?.clearDataSource()
        }
    }
}
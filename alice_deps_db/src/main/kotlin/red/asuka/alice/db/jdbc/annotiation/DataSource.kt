package red.asuka.alice.db.jdbc.annotiation

import java.lang.annotation.Documented
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy

/**
 * @author Tomonori
 * @mail gutrse3321@live.com
 * @data 2020-12-12 16:07
 */
@Target(AnnotationTarget.FUNCTION,
        AnnotationTarget.PROPERTY_GETTER,
        AnnotationTarget.PROPERTY_SETTER,
        AnnotationTarget.ANNOTATION_CLASS,
        AnnotationTarget.CLASS)
@Retention(RetentionPolicy.RUNTIME)
@Documented
annotation class DataSource(val value: String = "default", val key: String = "default")
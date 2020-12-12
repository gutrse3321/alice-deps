package red.asuka.alice.db.jpa.entity

import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer
import lombok.Data
import java.io.Serializable
import javax.persistence.Inheritance
import javax.persistence.InheritanceType
import javax.persistence.MappedSuperclass

/**
 * @author Tomonori
 * @mail gutrse3321@live.com
 * @data 2020-12-12 16:50
 */
@Data
@MappedSuperclass
@Inheritance(strategy = InheritanceType.JOINED)
abstract class BaseEntity : Serializable {
    var data_state: Int = Constant.DataState.Available.ordinal()

    @JsonSerialize(using = ToStringSerializer::class)
    var created_time: Long? = null
    var created_by: String? = null

    @JsonSerialize(using = ToStringSerializer::class)
    var updated_time: Long? = null
    var updated_by: String? = null
}
package cc.cb.entity

import jakarta.persistence.*

/**
 * @author Moyuyanli
 * @Date 2024/7/21 13:55
 */
@Entity
@Table
class MyUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int? = null

    var name: String? = null

    var sex: Int? = null

    override fun toString(): String {
        return "MyUser(id=$id, name=$name, sex=$sex)"
    }
}


package entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/**
 * @author Moyuyanli
 * @date 2024/7/18 14:58
 */
@Getter
@Setter
@Entity(name = "myUser")
@Table(name = "myUser")
public class myUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String name;
    private Integer sex;

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", sex=" + sex +
                '}';
    }
}

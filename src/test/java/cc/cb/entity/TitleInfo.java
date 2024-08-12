package cc.cb.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

/**
 * 称号信息<p>
 *
 * @author Moyuyanli
 * @date 2022/12/5 17:01
 */
@Getter
@Setter
@Entity(name = "TitleInfo")
@Table
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class TitleInfo implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;
    /**
     * 所属者用户id
     */
    private long userId;
    /**
     * 称号类型
     */
    private String code;
    /**
     * 称号名称
     */
    private String name;
    /**
     * 使用状态
     */
    private boolean status;
    /**
     * 称号
     */
    private String title;
    /**
     * 是否影响名称
     */
    private boolean impactName;
    /**
     * 是否渐变
     */
    private boolean gradient;
    /**
     * 称号颜色
     */
    private String sColor;
    /**
     * 称号颜色
     */
    private String eColor;
    /**
     * 称号到期时间
     */
    private Date dueTime;

    @Override
    public String toString() {
        return "TitleInfo{" +
                "id=" + id +
                ", userId=" + userId +
                ", code='" + code + '\'' +
                ", name='" + name + '\'' +
                ", status=" + status +
                ", title='" + title + '\'' +
                ", impactName=" + impactName +
                ", gradient=" + gradient +
                ", sColor='" + sColor + '\'' +
                ", eColor='" + eColor + '\'' +
                ", dueTime=" + dueTime +
                '}';
    }
}

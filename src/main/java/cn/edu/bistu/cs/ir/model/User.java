package cn.edu.bistu.cs.ir.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import javax.persistence.*;

@Data
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(unique = true)
    private String email;

    @JsonProperty("password")
    private String password;

    private String avatar;

    private String realName;

    private String gender;

    private String birthDate;

    private boolean enabled = true;

    // 用于JSON序列化时排除密码字段
    @JsonIgnore
    public String getPassword() {
        return this.password;
    }
}
package org.ukdw.entity;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import java.util.HashSet;
import java.util.Set;


@Entity
@Table(name = "users", uniqueConstraints = {
        @UniqueConstraint(columnNames = "email"),
        @UniqueConstraint(columnNames = "username")
})
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class UserEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    @Setter
    @Getter
    private Integer id;

    @Column(name = "username", length = 50, unique = true)
    @Setter
    @Getter
    private String username;

    @Column(name = "password", length = 40)
    @Setter
    @Getter
    private String password;

    @ManyToMany(cascade = { CascadeType.ALL })
    @JoinTable(
            name = "user_group",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "group_id")
    )
    @Setter
    @Getter
    @JsonManagedReference // Prevent recursion by indicating this is the "forward" side of the relationship
    private Set<GroupEntity> groups= new HashSet<>();

    @Column(name = "fullname", length = 255, nullable = false)
    @Setter
    @Getter
    private String fullname;

    // Foreign key relationship with google_users.google_email
    @ManyToOne
    @JoinColumn(name = "email", referencedColumnName = "google_email",
            foreignKey = @ForeignKey(name = "fk_users_google_email", value = ConstraintMode.CONSTRAINT))
    @Setter
    @Getter
    private GoogleUserEntity googleUser;

    @Column(name = "nim", length = 15)
    @Setter
    @Getter
    private String nim;

    @Column(name = "modified", nullable = false)
    @Setter
    @Getter
    private String modified;

    @Column(name = "telpno", length = 15, nullable = false)
    @Setter
    @Getter
    private String telpNo;

    @Column(name = "active", nullable = false)
    @Setter
    @Getter
    private Boolean active;

    @Column(name = "hash_validation", length = 512, nullable = false)
    @Setter
    @Getter
    private String hashValidation;

    @Column(name = "foto")
    @Setter
    @Getter
    private String foto;

    @Column(name = "dir")
    @Setter
    @Getter
    private String dir;

    @Column(name = "mimetype")
    @Setter
    @Getter
    private String mimetype;

    @Column(name = "filesize", columnDefinition = "int(10)")
    @Setter
    @Getter
    private Integer fileSize;

    @Column(name = "hashtag", length = 256)
    @Setter
    @Getter
    private String hashtag;

    @Column(name = "jwt_token")
    @Setter
    @Getter
    private String jwtToken;

    @Column(name = "status", nullable = false, columnDefinition = "tinyint(1) default 1")
    @Setter
    @Getter
    private Boolean status = true; // 1 untuk aktif (true), 0 untuk pasif (false)
}

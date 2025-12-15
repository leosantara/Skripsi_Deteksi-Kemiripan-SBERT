/**
 * Author: dendy
 * Date:03/10/2024
 * Time:9:52
 * Description:
 */

package org.ukdw.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "groups")
public class GroupEntity implements Serializable {
    @Id
    @Setter
    @Getter
    private Long id;

    @Column(name = "groupname", nullable = false)
    @Setter
    @Getter
    private String groupname;

    @ManyToMany(mappedBy = "groups")
    @Setter
    @Getter
    @JsonBackReference // Prevent recursion by indicating this is the "back" side of the relationship
    private Set<UserEntity> users = new HashSet<>();
}

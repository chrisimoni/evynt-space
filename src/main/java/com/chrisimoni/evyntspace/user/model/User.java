package com.chrisimoni.evyntspace.user.model;

import com.chrisimoni.evyntspace.common.model.ActivatableEntity;
import com.chrisimoni.evyntspace.event.model.Event;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users")
public class User extends ActivatableEntity {
    private String firstName;
    private String lastName;
    private String email;
    private String password;
    private String company;
    private String phoneNumber;
    private String profileImageUrl;
}

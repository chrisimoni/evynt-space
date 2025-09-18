package com.chrisimoni.evyntspace.user.model;

import com.chrisimoni.evyntspace.common.model.ActivatableEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
    private String countryCode;
}

package org.sfl.models;
import jakarta.persistence.*;
import org.sfl.enums.Role;

import java.time.LocalDateTime;
import java.util.Date;

@Entity
@Table(name = "User")
public class User {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long idUser;

	private String name;

	private String email;

	private String password;

	@Enumerated(EnumType.STRING)
	private Role role;

	private Boolean active;

	private LocalDateTime createdAt;

	private Integer loginTry;

	private Date birthDate;
}

package kr.hhplus.be.server.user.domain;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Table(name = "users")
@Entity
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "create_at")
    private LocalDateTime createAt;

}

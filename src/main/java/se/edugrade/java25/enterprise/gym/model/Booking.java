package se.edugrade.java25.enterprise.gym.model;


import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)

    private Long id;
    private String participantName;
    private String email;
    private LocalDateTime bookedAt;

    @ManyToOne
    @JoinColumn(name = "gym_class_id", nullable = false)
    @JsonBackReference
    private GymClass gymClass;

    public Booking(String participantName, String email) {
        this.participantName = participantName;
        this.email = email;
    }

    @PrePersist
    protected void onCreate() {
        if (bookedAt == null) {
            this.bookedAt = LocalDateTime.now();
        }
    }
}

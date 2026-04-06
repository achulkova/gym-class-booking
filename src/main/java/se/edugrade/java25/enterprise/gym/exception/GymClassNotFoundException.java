package se.edugrade.java25.enterprise.gym.exception;

public class GymClassNotFoundException extends RuntimeException {

    public GymClassNotFoundException(Long id) {
        super("Gym class with id " + id + " not found");
    }
}

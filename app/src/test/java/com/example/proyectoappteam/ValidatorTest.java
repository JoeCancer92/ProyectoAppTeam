package com.example.proyectoappteam;

import org.junit.Test;
import static org.junit.Assert.*;

public class ValidatorTest {

    private final Validator validator = new Validator();

    @Test
    public void emailValidator_CorrectEmail_ReturnsTrue() {
        assertTrue(validator.isValidEmail("name@gmail.com"));
    }

    @Test
    public void emailValidator_InvalidEmailNoTld_ReturnsFalse() {
        assertFalse(validator.isValidEmail("name@email"));
    }

    @Test
    public void emailValidator_InvalidEmailDoubleDot_ReturnsFalse() {
        assertFalse(validator.isValidEmail("name@email..com"));
    }

    @Test
    public void emailValidator_NullEmail_ReturnsFalse() {
        assertFalse(validator.isValidEmail(null));
    }
}

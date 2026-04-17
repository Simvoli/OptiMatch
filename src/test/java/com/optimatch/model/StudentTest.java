package com.optimatch.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the Student model class.
 */
@DisplayName("Student Model Tests")
class StudentTest {

    private Student student;

    @BeforeEach
    void setUp() {
        student = new Student();
    }

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Default constructor creates student with default values")
        void defaultConstructor() {
            assertNotNull(student);
            assertEquals(0, student.getId());
            assertNull(student.getStudentId());
            assertNull(student.getName());
            assertNull(student.getEmail());
            assertEquals(0.0, student.getGpa());
            assertNull(student.getPartnerId());
        }

        @Test
        @DisplayName("Full constructor sets all fields correctly")
        void fullConstructor() {
            Student s = new Student(1, "123456", "John Doe", "john@test.com", 3.5, 2);

            assertEquals(1, s.getId());
            assertEquals("123456", s.getStudentId());
            assertEquals("John Doe", s.getName());
            assertEquals("john@test.com", s.getEmail());
            assertEquals(3.5, s.getGpa());
            assertEquals(2, s.getPartnerId());
        }
    }

    @Nested
    @DisplayName("Getter and Setter Tests")
    class GetterSetterTests {

        @Test
        @DisplayName("Set and get ID")
        void idGetterSetter() {
            student.setId(42);
            assertEquals(42, student.getId());
        }

        @Test
        @DisplayName("Set and get student ID")
        void studentIdGetterSetter() {
            student.setStudentId("ABC123");
            assertEquals("ABC123", student.getStudentId());
        }

        @Test
        @DisplayName("Set and get name")
        void nameGetterSetter() {
            student.setName("Jane Doe");
            assertEquals("Jane Doe", student.getName());
        }

        @Test
        @DisplayName("Set and get email")
        void emailGetterSetter() {
            student.setEmail("jane@test.com");
            assertEquals("jane@test.com", student.getEmail());
        }

        @Test
        @DisplayName("Set and get GPA")
        void gpaGetterSetter() {
            student.setGpa(3.75);
            assertEquals(3.75, student.getGpa());
        }

        @Test
        @DisplayName("Set and get partner ID")
        void partnerIdGetterSetter() {
            student.setPartnerId(5);
            assertEquals(5, student.getPartnerId());
        }
    }

    @Nested
    @DisplayName("Partner Tests")
    class PartnerTests {

        @Test
        @DisplayName("Student without partner returns false for hasPartner")
        void noPartner() {
            student.setPartnerId(null);
            assertFalse(student.hasPartner());
        }

        @Test
        @DisplayName("Student with partner returns true for hasPartner")
        void withPartner() {
            student.setPartnerId(10);
            assertTrue(student.hasPartner());
        }

        @Test
        @DisplayName("Partner ID 0 is considered valid partner")
        void partnerIdZero() {
            student.setPartnerId(0);
            assertTrue(student.hasPartner());
        }
    }

    @Nested
    @DisplayName("Equals and HashCode Tests")
    class EqualsHashCodeTests {

        @Test
        @DisplayName("Students with same ID are equal")
        void equalsWithSameId() {
            Student s1 = new Student(1, "123", "Name1", null, 3.0, -1);
            Student s2 = new Student(1, "456", "Name2", null, 2.0, 5);

            assertEquals(s1, s2);
            assertEquals(s1.hashCode(), s2.hashCode());
        }

        @Test
        @DisplayName("Students with different IDs are not equal")
        void notEqualsWithDifferentId() {
            Student s1 = new Student(1, "123", "Name", null, 3.0, -1);
            Student s2 = new Student(2, "123", "Name", null, 3.0, -1);

            assertNotEquals(s1, s2);
        }

        @Test
        @DisplayName("Student is not equal to null")
        void notEqualsNull() {
            student.setId(1);
            assertNotEquals(null, student);
        }

        @Test
        @DisplayName("Student is equal to itself")
        void equalsSelf() {
            student.setId(1);
            assertEquals(student, student);
        }
    }

    @Nested
    @DisplayName("toString Tests")
    class ToStringTests {

        @Test
        @DisplayName("toString contains student information")
        void toStringContainsInfo() {
            Student s = new Student(1, "123", "John", "john@test.com", 3.5, -1);
            String str = s.toString();

            assertTrue(str.contains("id=1"));
            assertTrue(str.contains("studentId='123'"));
            assertTrue(str.contains("name='John'"));
        }
    }
}

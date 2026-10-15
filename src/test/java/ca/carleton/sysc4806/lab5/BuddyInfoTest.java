package ca.carleton.sysc4806.lab5;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BuddyInfoTest {

    @Test
    void emailIsAvailableAndOldConstructorStillWorks() {
        assertEquals("daniel@example.com", new BuddyInfo("Daniel", "Esenwa", 324, 234, "daniel@example.com").getEmail());
        assertNull(new BuddyInfo("Daniel", "Esenwa", 324, 234).getEmail());
    }

    @Test
    void testConstructorAndGetFirstName() {
        BuddyInfo buddy = new BuddyInfo("Daniel", "Esenwa", 324, 234);

        assertEquals("Daniel", buddy.getFirstName());
    }

    @Test
    void testGetLastName() {
        BuddyInfo buddy = new BuddyInfo("Daniel", "Esenwa", 324, 234);

        assertEquals("Esenwa", buddy.getLastName());
    }

    @Test
    void testGetAddress() {
        BuddyInfo buddy = new BuddyInfo("Daniel", "Esenwa", 324, 234);

        assertEquals(324, buddy.getAddress());
    }

    @Test
    void testGetPhone() {
        BuddyInfo buddy = new BuddyInfo("Daniel", "Esenwa", 324, 234);

        assertEquals(234, buddy.getPhone());
    }

    @Test
    void testAllBuddyInfoValues() {
        BuddyInfo buddy = new BuddyInfo("Tom", "Carleton", 613, 2342);

        assertAll(
                () -> assertEquals("Tom", buddy.getFirstName()),
                () -> assertEquals("Carleton", buddy.getLastName()),
                () -> assertEquals(613, buddy.getAddress()),
                () -> assertEquals(2342, buddy.getPhone())
        );
    }

}

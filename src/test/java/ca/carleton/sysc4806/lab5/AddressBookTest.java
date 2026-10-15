package ca.carleton.sysc4806.lab5;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AddressBookTest {
    @Test
    void testAddressBookIsCreatedEmpty() {
        assertTrue(new AddressBook().getBuddies().isEmpty());
    }

    @Test
    void testAddBuddy() {
        AddressBook book = new AddressBook();
        BuddyInfo buddy = new BuddyInfo("Daniel", "Esenwa", 324, 234);
        book.addBuddy(buddy);
        assertEquals(1, book.getBuddies().size());
        assertSame(buddy, book.getBuddies().get(0));
    }

    @Test
    void testAddMultipleBuddies() {
        AddressBook book = new AddressBook();
        BuddyInfo first = new BuddyInfo("Tom", "Carleton", 613, 2342);
        BuddyInfo second = new BuddyInfo("Daniel", "Esenwa", 324, 234);
        book.addBuddy(first);
        book.addBuddy(second);
        assertEquals(2, book.getBuddies().size());
        assertSame(first, book.getBuddies().get(0));
        assertSame(second, book.getBuddies().get(1));
    }

    @Test
    void testRemoveBuddy() {
        AddressBook book = new AddressBook();
        BuddyInfo buddy = new BuddyInfo("Daniel", "Esenwa", 324, 234);
        book.addBuddy(buddy);
        assertTrue(book.removeBuddy(buddy));
        assertTrue(book.getBuddies().isEmpty());
        assertFalse(book.removeBuddy(buddy));
    }
}

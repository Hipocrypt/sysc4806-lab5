package ca.carleton.sysc4806.lab5;

import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class RepositoryIntegrationTest {
    @Autowired private AddressBookRepository addressBooks;
    @Autowired private BuddyInfoRepository buddies;
    @Autowired private PlatformTransactionManager transactionManager;

    @Test
    void buddyCanBeSavedQueriedAndDeleted() {
        BuddyInfo saved = buddies.save(new BuddyInfo("Daniel", "Esenwa", 324, 234, "daniel@example.com"));
        try {
            assertNotNull(saved.getId());
            BuddyInfo loaded = buddies.findById(saved.getId()).orElseThrow();
            assertNotSame(saved, loaded);
            assertAll(
                    () -> assertEquals("Daniel", loaded.getFirstName()),
                    () -> assertEquals("Esenwa", loaded.getLastName()),
                    () -> assertEquals(324, loaded.getAddress()),
                    () -> assertEquals(234, loaded.getPhone()),
                    () -> assertEquals("daniel@example.com", loaded.getEmail()));
            List<BuddyInfo> matches = buddies.findByLastName("Esenwa");
            assertEquals(1, matches.size());
            assertEquals(saved.getId(), matches.get(0).getId());
            assertTrue(buddies.findByLastName("Missing").isEmpty());
        } finally {
            buddies.deleteById(saved.getId());
        }
        assertFalse(buddies.existsById(saved.getId()));
    }

    @Test
    void addressBookCascadesPersistencePreservesOrderAndDeletesBuddies() {
        AddressBook book = new AddressBook();
        BuddyInfo first = new BuddyInfo("Tom", "Carleton", 613, 2342);
        BuddyInfo second = new BuddyInfo("Daniel", "Esenwa", 324, 234);
        book.addBuddy(first);
        book.addBuddy(second);
        addressBooks.save(book);
        try {
            assertNotNull(book.getId());
            assertNotNull(first.getId());
            assertNotNull(second.getId());
            // Each repository call uses a separate transaction, so this reads H2 again.
            AddressBook loaded = addressBooks.findById(book.getId()).orElseThrow();
            assertNotSame(book, loaded);
            assertEquals(List.of("Tom", "Daniel"), loaded.getBuddies().stream()
                    .map(BuddyInfo::getFirstName).collect(Collectors.toList()));
            assertEquals(first.getId(), loaded.getBuddies().get(0).getId());
            assertEquals(second.getId(), loaded.getBuddies().get(1).getId());
            assertEquals(613, loaded.getBuddies().get(0).getAddress());
            assertEquals(234, loaded.getBuddies().get(1).getPhone());

            new TransactionTemplate(transactionManager).executeWithoutResult(status -> {
                AddressBook managed = addressBooks.findById(book.getId()).orElseThrow();
                managed.removeBuddy(managed.getBuddies().get(0));
                addressBooks.save(managed);
            });
            assertFalse(buddies.existsById(first.getId()));
            assertEquals(1, addressBooks.findById(book.getId()).orElseThrow().getBuddies().size());
        } finally {
            addressBooks.deleteById(book.getId());
        }
        assertFalse(addressBooks.existsById(book.getId()));
        assertFalse(buddies.existsById(second.getId()));
    }

    @Test
    void emptyAddressBookCanBeSavedAndReloaded() {
        AddressBook saved = addressBooks.save(new AddressBook());
        try {
            assertTrue(addressBooks.findById(saved.getId()).orElseThrow().getBuddies().isEmpty());
        } finally {
            addressBooks.deleteById(saved.getId());
        }
    }
}

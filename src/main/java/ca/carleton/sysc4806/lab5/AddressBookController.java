package ca.carleton.sysc4806.lab5;

import java.net.URI;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping(value = "/api", produces = "application/json")
public class AddressBookController {
    private final AddressBookRepository books;
    private final BuddyInfoRepository buddies;

    public AddressBookController(AddressBookRepository books, BuddyInfoRepository buddies) {
        this.books = books;
        this.buddies = buddies;
    }

    public record CreateBuddy(String firstName, String lastName, Integer address, Integer phone, String email) {}
    public record AddBuddy(Long buddyId) {}

    @PostMapping(value = "/addressbooks", consumes = "application/json")
    public ResponseEntity<AddressBook> createBook(@RequestBody java.util.Map<String, Object> request) {
        AddressBook book = books.save(new AddressBook());
        return ResponseEntity.created(URI.create("/api/addressbooks/" + book.getId())).body(book);
    }

    @PostMapping(value = "/buddies", consumes = "application/json")
    public ResponseEntity<BuddyInfo> createBuddy(@RequestBody CreateBuddy request) {
        if (request.firstName() == null || request.firstName().isBlank()
                || request.lastName() == null || request.lastName().isBlank()
                || request.address() == null || request.phone() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Name, address, and phone are required");
        }
        String email = request.email() == null || request.email().isBlank() ? null : request.email().trim();
        if (email != null && (email.length() > 254 || !email.matches("[^\\s@]+@[^\\s@]+\\.[^\\s@]+"))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email must be a valid email address");
        }
        BuddyInfo buddy = buddies.save(new BuddyInfo(request.firstName(), request.lastName(),
                request.address(), request.phone(), email));
        return ResponseEntity.created(URI.create("/api/buddies/" + buddy.getId())).body(buddy);
    }

    @GetMapping("/addressbooks/{id}")
    public AddressBook getBook(@PathVariable Long id) {
        return books.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    @GetMapping("/buddies/{id}")
    public BuddyInfo getBuddy(@PathVariable Long id) {
        return buddies.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    @Transactional
    @PostMapping(value = "/addressbooks/{id}/buddies", consumes = "application/json")
    public AddressBook addBuddy(@PathVariable Long id, @RequestBody AddBuddy request) {
        if (request.buddyId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "buddyId is required");
        }
        AddressBook book = getBook(id);
        BuddyInfo buddy = getBuddy(request.buddyId());
        // Each buddy belongs to one book under the inherited one-to-many model.
        for (AddressBook existing : books.findAll()) {
            if (existing.getBuddies().stream().anyMatch(b -> b.getId().equals(buddy.getId()))) {
                if (existing.getId().equals(id)) return book;
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Buddy already belongs to another book");
            }
        }
        book.addBuddy(buddy);
        return books.save(book);
    }
}

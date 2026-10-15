package ca.carleton.sysc4806.lab5;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Entity
public class AddressBook {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "address_book_id")
    @OrderColumn(name = "buddy_position")
    private List<BuddyInfo> buddies;

    public AddressBook() {

        buddies = new ArrayList<>();
    }

    public void addBuddy(BuddyInfo buddy) {

        buddies.add(buddy);
    }

    public Long getId() {
        return id;
    }

    public List<BuddyInfo> getBuddies() {
        return Collections.unmodifiableList(buddies);
    }

    public boolean removeBuddy(BuddyInfo buddy) {
        return buddies.remove(buddy);
    }
}

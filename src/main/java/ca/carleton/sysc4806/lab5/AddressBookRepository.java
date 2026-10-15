package ca.carleton.sysc4806.lab5;

import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.repository.CrudRepository;

public interface AddressBookRepository extends CrudRepository<AddressBook, Long> {
    @Override
    @EntityGraph(attributePaths = "buddies")
    Optional<AddressBook> findById(Long id);
}

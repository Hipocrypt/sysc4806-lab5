package ca.carleton.sysc4806.lab5;

import java.util.List;
import org.springframework.data.repository.CrudRepository;

public interface BuddyInfoRepository extends CrudRepository<BuddyInfo, Long> {
    List<BuddyInfo> findByLastName(String lastName);
}

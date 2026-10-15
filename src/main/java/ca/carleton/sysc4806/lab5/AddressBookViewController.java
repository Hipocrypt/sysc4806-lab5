package ca.carleton.sysc4806.lab5;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

@Controller
public class AddressBookViewController {
    private final AddressBookRepository books;

    public AddressBookViewController(AddressBookRepository books) {
        this.books = books;
    }

    @GetMapping("/addressbooks/{id}")
    public String showBook(@PathVariable Long id, Model model) {
        model.addAttribute("book", books.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND)));
        return "addressbook";
    }
}

package ca.carleton.sysc4806.lab5;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class BuddyInfo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String firstName;
    private String lastName;
    private int address;
    private int phone;
    private String email;
    protected BuddyInfo() {
        // Required by JPA.
    }

    public Long getId() {
        return id;
    }
    public BuddyInfo(String firstName, String lastName, int address, int phone) {
        this(firstName, lastName, address, phone, null);
    }

    public BuddyInfo(String firstName, String lastName, int address, int phone, String email) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.address = address;
        this.phone = phone;
        this.email = email;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public int getAddress() {
        return address;
    }

    public int getPhone() {
        return phone;
    }

    public String getEmail() {
        return email;
    }

    @Override
    public String toString() {
        return "BuddyInfo[id=" + id + ", name=" + firstName + " " + lastName
                + ", address=" + address + ", phone=" + phone + ", email=" + email + "]";
    }

}

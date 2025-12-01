package dev.educore.eduxel.domain.school;

public class Teacher {
    private Long id;
    private String firstName;
    private String lastName;
    private String subject;
    private String email;
    private String phone;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    @Override
    public String toString() {
        String ln = lastName == null ? "" : lastName;
        String fn = firstName == null ? "" : firstName;
        if (ln.isBlank() && fn.isBlank()) return "";
        if (ln.isBlank()) return fn;
        if (fn.isBlank()) return ln;
        return ln + ", " + fn;
    }
}

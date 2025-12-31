package dev.educore.eduxel.domain.school;

public class Student {
    private Long id;
    private String firstName;
    private String lastName;
    private Long classId;

    // Adresse
    private String street;
    private String postalCode;
    private String city;
    private String country;

    // Schülerkontakt
    private String studentEmail;
    private String studentMobile;

    // Erziehungsberechtigte(r) 1
    private String guardian1Name;
    private String guardian1Relation;
    private String guardian1Mobile;
    private String guardian1WorkPhone;
    private String guardian1Email;

    // Erziehungsberechtigte(r) 2 (optional)
    private String guardian2Name;
    private String guardian2Relation;
    private String guardian2Mobile;
    private String guardian2Email;

    private String notes;

    private boolean isSick;
    private boolean isMissingUnexcused;

    public Student() {
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public Long getClassId() { return classId; }
    public void setClassId(Long classId) { this.classId = classId; }

    public String getStreet() { return street; }
    public void setStreet(String street) { this.street = street; }

    public String getPostalCode() { return postalCode; }
    public void setPostalCode(String postalCode) { this.postalCode = postalCode; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }

    public String getStudentEmail() { return studentEmail; }
    public void setStudentEmail(String studentEmail) { this.studentEmail = studentEmail; }

    public String getStudentMobile() { return studentMobile; }
    public void setStudentMobile(String studentMobile) { this.studentMobile = studentMobile; }

    public String getGuardian1Name() { return guardian1Name; }
    public void setGuardian1Name(String guardian1Name) { this.guardian1Name = guardian1Name; }

    public String getGuardian1Relation() { return guardian1Relation; }
    public void setGuardian1Relation(String guardian1Relation) { this.guardian1Relation = guardian1Relation; }

    public String getGuardian1Mobile() { return guardian1Mobile; }
    public void setGuardian1Mobile(String guardian1Mobile) { this.guardian1Mobile = guardian1Mobile; }

    public String getGuardian1WorkPhone() { return guardian1WorkPhone; }
    public void setGuardian1WorkPhone(String guardian1WorkPhone) { this.guardian1WorkPhone = guardian1WorkPhone; }

    public String getGuardian1Email() { return guardian1Email; }
    public void setGuardian1Email(String guardian1Email) { this.guardian1Email = guardian1Email; }

    public String getGuardian2Name() { return guardian2Name; }
    public void setGuardian2Name(String guardian2Name) { this.guardian2Name = guardian2Name; }

    public String getGuardian2Relation() { return guardian2Relation; }
    public void setGuardian2Relation(String guardian2Relation) { this.guardian2Relation = guardian2Relation; }

    public String getGuardian2Mobile() { return guardian2Mobile; }
    public void setGuardian2Mobile(String guardian2Mobile) { this.guardian2Mobile = guardian2Mobile; }

    public String getGuardian2Email() { return guardian2Email; }
    public void setGuardian2Email(String guardian2Email) { this.guardian2Email = guardian2Email; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public boolean isSick() { return isSick; }
    public void setSick(boolean sick) { isSick = sick; }

    public boolean isMissingUnexcused() { return isMissingUnexcused; }
    public void setMissingUnexcused(boolean missingUnexcused) { isMissingUnexcused = missingUnexcused; }
}

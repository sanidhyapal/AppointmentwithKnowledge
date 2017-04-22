package sanidhya.academic.com.appointmentwithknowledge.model;

/**
 * Created by Sanidhya on 01-Apr-17.
 */

public class Tutor {
    private String tutorId;
    private String name;
    private String email;
    private String experience;
    private String contactNumber;
    private String address;
    private String designation;
    private String role;

    public Tutor(String tutorId, String name, String email, String contactNumber,String experience) {

        this.tutorId = tutorId;
        this.name = name;
        this.email = email;
        this.experience = experience;
        this.contactNumber = contactNumber;
        this.role="tutor";

    }

    public String getExperience() {
        return experience;
    }

    public void setExperience(String experience) {
        this.experience = experience;
    }

    public String getTutorId() {
        return tutorId;
    }

    public void setTutorId(String tutorId) {
        this.tutorId = tutorId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getContactNumber() {
        return contactNumber;
    }

    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getDesignation() {
        return designation;
    }

    public void setDesignation(String designation) {
        this.designation = designation;
    }
}

package sanidhya.academic.com.appointmentwithknowledge.model;

/**
 * Created by Sanidhya on 23-Apr-17.
 */

public class Student {

    private String studentId;
    private String name;
    private String contactNumber;
    private String schoolName;
    private String emailId;
    private String schoolAddress;
    private String residentialAddress;
    private String division;
    private String role;

    public Student(String studentId, String name, String contactNumber, String schoolName, String emailId) {
        this.studentId = studentId;
        this.name = name;
        this.contactNumber = contactNumber;
        this.schoolName = schoolName;
        this.emailId = emailId;
        this.role="student";
    }


    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getContactNumber() {
        return contactNumber;
    }

    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
    }

    public String getSchoolName() {
        return schoolName;
    }

    public void setSchoolName(String schoolName) {
        this.schoolName = schoolName;
    }

    public String getEmailId() {
        return emailId;
    }

    public void setEmailId(String emailId) {
        this.emailId = emailId;
    }

    public String getSchoolAddress() {
        return schoolAddress;
    }

    public void setSchoolAddress(String schoolAddress) {
        this.schoolAddress = schoolAddress;
    }

    public String getResidentialAddress() {
        return residentialAddress;
    }

    public void setResidentialAddress(String residentialAddress) {
        this.residentialAddress = residentialAddress;
    }

    public String getDivision() {
        return division;
    }

    public void setDivision(String division) {
        this.division = division;
    }
}

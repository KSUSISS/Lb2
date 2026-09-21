package ru.kafpin.lb2.domain;
import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "schoolboys")

public class Schoolboy {
@Id
@GeneratedValue(strategy = GenerationType.IDENTITY)
private  Long id;

    private String surname;       // фамилия
    private String name;          // имя
    private String patronymic;    // отчество
    private String gender;        // пол
    private String nationality;   // национальность
    private Integer height;       // рост (в сантиметрах)
    private Double weight;        // вес (в килограммах)
    private LocalDate birthDate;  // дата рождения
    private String phoneNumber;   // номер телефона
    private String homePhone;

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPatronymic() {
        return patronymic;
    }

    public void setPatronymic(String patronymic) {
        this.patronymic = patronymic;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getNationality() {
        return nationality;
    }

    public void setNationality(String nationality) {
        this.nationality = nationality;
    }

    public Integer getHeight() {
        return height;
    }

    public void setHeight(Integer height) {
        this.height = height;
    }

    public Double getWeight() {
        return weight;
    }

    public void setWeight(Double weight) {
        this.weight = weight;
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(LocalDate birthDate) {
        this.birthDate = birthDate;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getHomePhone() {
        return homePhone;
    }

    public void setHomePhone(String homePhone) {
        this.homePhone = homePhone;
    }
}

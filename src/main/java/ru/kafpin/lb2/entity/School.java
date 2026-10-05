package ru.kafpin.lb2.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.CascadeType;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;


import java.util.List;

@Entity
public class School {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String name;

    @OneToMany(
            mappedBy = "school",
            cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE},
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    private List<Schoolchild> schoolchildren;

    public School() {
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Schoolchild> getSchoolchildren() {
        return schoolchildren;
    }

    public void setSchoolchildren(List<Schoolchild> schoolchildren) {
        this.schoolchildren = schoolchildren;
    }

    public void addSchoolchild(Schoolchild schoolchild) {
        schoolchildren.add(schoolchild);
        schoolchild.setSchool(this);
    }

    public void removeSchoolchild(Schoolchild schoolchild) {
        schoolchildren.remove(schoolchild);
        schoolchild.setSchool(null);
    }
}
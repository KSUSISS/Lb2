package ru.kafpin.lb2.entity;

import java.util.List;

public class School {

    private long id;

    private String name;

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
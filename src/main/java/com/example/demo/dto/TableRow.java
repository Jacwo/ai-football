package com.example.demo.dto;

public class TableRow {
    private String name;
    private int age;
    private String country;

    public TableRow(String name, int age, String country) {
        this.name = name;
        this.age = age;
        this.country = country;
    }

    @Override
    public String toString() {
        return "Name: " + name + ", Age: " + age + ", Country: " + country;
    }
}

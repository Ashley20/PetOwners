package com.owners.pet.petowners.models;

import java.util.List;

public class Pet {
    private User owner;
    private String name;
    private String age;
    private boolean wants_to_be_adopted;
    private enum TYPE {
        Cat,
        Dog
    }

    public Pet() {
    }

    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAge() {
        return age;
    }

    public void setAge(String age) {
        this.age = age;
    }

    public boolean isWants_to_be_adopted() {
        return wants_to_be_adopted;
    }

    public void setWants_to_be_adopted(boolean wants_to_be_adopted) {
        this.wants_to_be_adopted = wants_to_be_adopted;
    }
}

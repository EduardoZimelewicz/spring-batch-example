package com.github.eduardozimelewicz.batchprocessing.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Person {

  private Integer personId;

  private String lastName;

  private String firstName;

  @Override
  public String toString() {
    return "firstName: " + firstName + ", lastName: " + lastName;
  }
}

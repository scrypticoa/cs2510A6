import java.util.function.*;

interface IList<T> {
  <R> R fold(BiFunction<R, T, R> func, R val);
  
  boolean anyCompareMatches(BiPredicate<T, T> compare);
}

class MtList<T> implements IList<T> {
  public <R> R fold(BiFunction<R, T, R> func, R val) {
    return val;
  }
  
  public boolean anyCompareMatches(BiPredicate<T, T> compare) {
    return false;
  }
}

class ConsList<T> implements IList<T> {
  T first;
  IList<T> rest;

  public ConsList(T first, IList<T> rest) {
    this.first = first;
    this.rest = rest;
  }
  
  public <R> R fold(BiFunction<R, T, R> func, R val) {
    return func.apply(val, first);
  }
  
  public boolean anyCompareMatches(BiPredicate<T, T> compare) {
    boolean firstMatch = this.rest.fold(
        (res, element) -> {
          return res || compare.test(this.first, element);
        }, false);
    if (firstMatch) return true;
    return this.rest.anyCompareMatches(compare);
  }
}

class Course {
  String name;
  Instructor prof;
  ConsList<Student> students;

  Course(String name, Instructor prof) {
    this.name = name;
    this.prof = prof;
    prof.newClass(this);
  }

  void addStudent(Student s) {
    students = new ConsList<Student>(s, students);
  }
  
  boolean equals(Course other) {
    return this.name.equals(other.name);
  }
  
  boolean sameProf(Course c) {
    return this.prof.equals(c.prof);
}

class Instructor {
  String name;
  IList<Course> courses;

  Instructor(String name) {
    this.name = name;
    courses = new MtList<Course>();
  }

  void newClass(Course c) {
    courses = new ConsList<Course>(c, courses);
  }
  
  boolean dejavu(Student s) {
    return s.dejavu(this);
  }
  
  boolean equals(Instructor i) {
    return this.name.equals(i.name);
  }
}

class Student {
  String name;
  int id;
  ConsList <Course> courses;
  
  Student (String name, int id) {
    this.name = name;
    this.id = id;
  }
  
  boolean dejavu(Instructor i) {
    return this.courses.anyCompareMatches(
        (course1, course2) -> {
          return course1.sameProf(course2);
        });
  }
  
  boolean classmates(Student c) {
    return this.courses.fold(
      (res, course) -> {
        return res ||
            c.courses.fold(
                (res1, course1) -> {
                  return res1 || course.equals(course1);
                },
                false);
      }, false);
  }

  void enroll(Course c) {
    courses = new ConsList<Course>(c, courses);
    c.addStudent(this);
  }
}



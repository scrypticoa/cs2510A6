import java.util.function.*;

interface IList<T> {
  <R> R fold(BiFunction<R, T, R> func, R val);
}

class MtList<T> implements IList<T> {
  public <R> R fold(BiFunction<R, T, R> func, R val) {
    return val;
  }
}

class ConsList<T> implements IList<T>{
  T first;
  IList<T> rest;
  
  public ConsList(T first, IList<T> rest) {
    this.first = first;
    this.rest = rest;
  }
  
  public <R> R fold(BiFunction<R, T, R> func, R val) {
    return func.apply(val, first);
  }
}

class Course{
  String name;
  Instructor prof;
  ConsList <Student> students;
  
  Course(String name, Instructor prof){
    this.name = name;
    this.prof = prof;
  }
  
  boolean equals(Course other) {
    return this.name.equals(other.name);
  }
}

class Instructor{
  String name;
  IList <Course> courses;
  
  Instructor(String name){
    this.name = name;
    courses = new MtList <Course> ();
  }
  
  void newClass(Course c) {
    courses = new ConsList <Course> (c, courses);
  }
}

class Student{
  String name;
  int id;
  ConsList <Course> courses;
  
  Student (String name, int id){
    this.name = name;
    this.id = id;
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
}


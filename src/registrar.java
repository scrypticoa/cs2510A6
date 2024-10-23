interface IList<T> {
}

class MtList<T> implements IList<T> {

}

class ConsList<T> implements IList<T> {
  T first;
  IList<T> rest;

  public ConsList(T first, IList<T> rest) {
    this.first = first;
    this.rest = rest;
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
}

class Student {
  String name;
  int id;
  ConsList<Course> courses;

  Student(String name, int id) {
    this.name = name;
    this.id = id;
  }

  void enroll(Course c) {
    courses = new ConsList<Course>(c, courses);
    c.addStudent(this);
  }
}
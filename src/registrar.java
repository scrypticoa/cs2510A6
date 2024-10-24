import java.util.function.*;
import tester.Tester;

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
    boolean firstMatch = this.rest.fold((res, element) -> {
      return res || compare.test(this.first, element);
    }, false);
    if (firstMatch)
      return true;
    return this.rest.anyCompareMatches(compare);
  }
}

class Course {
  String name;
  Instructor prof;
  IList<Student> students;

  Course(String name, Instructor prof) {
    this.name = name;
    this.prof = prof;
    prof.newClass(this);
    students = new MtList<Student>();

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
  IList<Course> courses;

  Student(String name, int id) {
    this.name = name;
    this.id = id;
    courses = new MtList<Course>();
  }

  void enroll(Course c) {
    courses = new ConsList<Course>(c, courses);
    c.addStudent(this);
  }
  
  boolean dejavu(Instructor i) {
    return this.courses.anyCompareMatches((course1, course2) -> {
      return course1.sameProf(course2);
    });
  }

  boolean classmates(Student c) {
    return this.courses.fold((res, course) -> {
      return res || c.courses.fold((res1, course1) -> {
        return res1 || course.equals(course1);
      }, false);
    }, false);
  }
}

class ExamplesRegistrar {
  // fundies, calc
  Student micah;
  // fundies, his
  Student jackson;
  // calc, eng
  Student aidan;
  // eng, his
  Student daniel;
  // none
  Student jacob;

  // fundies, calc
  Instructor razzaq;
  // eng, his
  Instructor smith;
  // none
  Instructor doe;

  Course fundies;
  Course calc;
  Course eng;
  Course his;

  void create() {
    micah = new Student("Micah", 27390);
    jackson = new Student("Jackson", 27140);
    aidan = new Student("Aidan", 27600);
    jacob = new Student("Jacob", 27740);
    daniel = new Student("Daniel", 27978);

    razzaq = new Instructor("Razzaq");
    smith = new Instructor("Smith");
    doe = new Instructor("Doe");

    fundies = new Course("Fundies 2", razzaq);
    calc = new Course("Calculus", razzaq);
    eng = new Course("English", smith);
    his = new Course("History", smith);

    micah.enroll(fundies);
    micah.enroll(calc);

    jackson.enroll(fundies);
    jackson.enroll(his);

    aidan.enroll(calc);
    aidan.enroll(eng);

    daniel.enroll(eng);
    daniel.enroll(his);
  }

  // tests the Course equals method
  boolean testCourseEquals(Tester t) {
    create();
    boolean res = true;
    // tests same course
    res &= t.checkExpect(fundies.equals(fundies), true);
    // tests diffrent courses
    res &= t.checkExpect(his.equals(calc), false);

    return res;
  }

  // tests the sameProf method
  boolean testSameProf(Tester t) {
    create();
    boolean res = true;
    // tests couses with the same prof
    res &= t.checkExpect(fundies.equals(calc), true);
    // tests couses with diffrent profs
    res &= t.checkExpect(his.equals(calc), false);

    return res;
  }

  // tests the Instructor Dejavu method
  boolean testInstructorDejavu(Tester t) {
    create();
    boolean res = true;
    // prof with no courses
    res &= t.checkExpect(doe.dejavu(jackson), false);
    // student with no couses
    res &= t.checkExpect(smith.dejavu(jacob), false);
    // prof that sees a student once
    res &= t.checkExpect(razzaq.dejavu(jackson), false);
    // prof that sees a student twice
    res &= t.checkExpect(razzaq.dejavu(micah), true);
    
    return res;
  }

  // tests the Instructor equals method
  boolean testInstructorEquals(Tester t) {
    create();
    boolean res = true;
    // tests with same prof
    res &= t.checkExpect(razzaq.equals(razzaq), true);
    // tests with 2 diffrent prof
    res &= t.checkExpect(razzaq.equals(smith), false);

    return res;
  }
  
  // tests the student Dejavu method
  boolean testStudentDejavu(Tester t) {
    create();
    boolean res = true;
    // prof with no courses
    res &= t.checkExpect(jackson.dejavu(doe), false);
    // student with no couses
    res &= t.checkExpect(jacob.dejavu(smith), false);
    // prof that sees a student once
    res &= t.checkExpect(jackson.dejavu(razzaq), false);
    // prof that sees a student twice
    res &= t.checkExpect(micah.dejavu(razzaq), true);
    
    return res;
  }
  
  // tests classmates method
  boolean testClassmates(Tester t) {
    create();
    boolean res = true;

    // student with no couses first
    res &= t.checkExpect(jacob.classmates(jackson), false);
    // student with no couses second
    res &= t.checkExpect(jackson.classmates(jacob), false);
    // students that dont share a class
    res &= t.checkExpect(micah.classmates(daniel), false);
    // students that share a class
    res &= t.checkExpect(micah.classmates(jackson), true);
    
    return res;
  }
}

import java.util.function.*;
import tester.Tester;

// represents a point in a list of type T
interface IList<T> {
  <R> R fold(BiFunction<R, T, R> func, R val);

  boolean anyCompareMatches(BiPredicate<T, T> compare);
}

//represents an empty list of type T
class MtList<T> implements IList<T> {
  public <R> R fold(BiFunction<R, T, R> func, R val) {
    return val;
  }

  public boolean anyCompareMatches(BiPredicate<T, T> compare) {
    return false;
  }
}

//represents a point of a list with data of type T
class ConsList<T> implements IList<T> {
  T first;
  IList<T> rest;

  public ConsList(T first, IList<T> rest) {
    this.first = first;
    this.rest = rest;
  }

  public <R> R fold(BiFunction<R, T, R> func, R val) {
    return this.rest.fold(func, func.apply(val, first));
  }

  public boolean anyCompareMatches(BiPredicate<T, T> compare) {
    boolean firstMatch = this.rest.fold((res, element) -> {
      return res || compare.test(this.first, element);
    }, false);
    if (firstMatch) {
      return true;
    }
    return this.rest.anyCompareMatches(compare);
  }
}

// represents a course with a name, proffesor, and has students
class Course {
  String name;
  Instructor prof;
  IList<Student> students;

  // constructor
  Course(String name, Instructor prof) {
    this.name = name;
    this.prof = prof;
    prof.newClass(this);
    students = new MtList<Student>();

  }

  // adds a student to the list of Students
  void addStudent(Student s) {
    students = new ConsList<Student>(s, students);
  }

  // compares this and other course to see if they are the same
  boolean sameCourse(Course other) {
    return this.equals(other);
  }

  // compares the names of the Instuctors of 2 courses to see if they are taught
  // by the same Instructor
  boolean sameProf(Course c) {
    return this.prof.sameInstructor(c.prof);
  }
}

// represents and instuctor who has a name a teahces courses
class Instructor {
  String name;
  IList<Course> courses;

  // constructor
  Instructor(String name) {
    this.name = name;
    courses = new MtList<Course>();
  }

  // adds a course to the proffesors list of courses
  void newClass(Course c) {
    courses = new ConsList<Course>(c, courses);
  }

  // determines whether the given Student is in more than one of this Instructor’s
  // Courses
  boolean dejavu(Student s) {
    return s.dejavu(this);
  }

  // compares this and i instructor to see if they are the same
  boolean sameInstructor(Instructor i) {
    return this.equals(i);
  }
}

// represents a student who has a name, id, and takes courses
class Student {
  String name;
  int id;
  IList<Course> courses;

  // constructor
  Student(String name, int id) {
    this.name = name;
    this.id = id;
    courses = new MtList<Course>();
  }

  // puts a course in a students courses and adds them to the courses' student
  // list
  void enroll(Course c) {
    this.courses = new ConsList<Course>(c, this.courses);
    c.addStudent(this);
  }

  // determines whether the this Student is in more than one of given Instructor’s
  // Courses
  boolean dejavu(Instructor i) {
    return this.courses.anyCompareMatches((course1, course2) -> {
      return course1.sameProf(course2);
    });
  }

  // determines whether the given Student is in any of the same classes as this
  // Student
  boolean classmates(Student c) {
    return this.courses.fold((res, course) -> {
      return res || c.courses.fold((res1, course1) -> {
        return res1 || course.sameCourse(course1);
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

  // gives all objects data
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

  // tests the addStudent method
  boolean testAddStudent(Tester t) {
    micah = new Student("Micah", 27390);
    jackson = new Student("Jackson", 27140);
    razzaq = new Instructor("Razzaq");
    fundies = new Course("Fundies 2", razzaq);

    boolean res = true;
    // tests adding a student to an empty list
    fundies.addStudent(micah);
    res &= t.checkExpect(fundies.students, new ConsList<Student>(micah, new MtList<Student>()));
    // tests adding a student to a list with a student
    fundies.addStudent(jackson);
    res &= t.checkExpect(fundies.students,
        new ConsList<Student>(jackson, new ConsList<Student>(micah, new MtList<Student>())));

    return res;
  }

  // tests the Course equals method
  boolean testCourseEquals(Tester t) {
    create();
    boolean res = true;
    // tests same course
    res &= t.checkExpect(fundies.sameCourse(fundies), true);
    // tests diffrent courses
    res &= t.checkExpect(his.sameCourse(calc), false);

    return res;
  }

  // tests the sameProf method
  boolean testSameProf(Tester t) {
    create();
    boolean res = true;
    // tests courses with the same prof
    res &= t.checkExpect(fundies.sameProf(calc), true);
    // tests courses with different profs
    res &= t.checkExpect(his.sameProf(calc), false);

    return res;
  }

  //tests the newClass method
  boolean testNewClass(Tester t) {
    boolean res = true;
    razzaq = new Instructor("Razzaq");

    // calls method in constructor
    fundies = new Course("Fundies 2", razzaq);
    // tests adding first class to empty list
    res &= t.checkExpect(razzaq.courses, new ConsList<Course>(fundies, new MtList<Course>()));

    // calls method in constructor
    calc = new Course("Calculus", razzaq);
    // tests adding a course to a non empty list
    res &= t.checkExpect(razzaq.courses,
        new ConsList<Course>(calc, new ConsList<Course>(fundies, new MtList<Course>())));

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
    res &= t.checkExpect(razzaq.sameInstructor(razzaq), true);
    // tests with 2 diffrent prof
    res &= t.checkExpect(razzaq.sameInstructor(smith), false);

    return res;
  }
  
  //tests the enroll method
  boolean testEnroll(Tester t) {
    micah = new Student("Micah", 27390);
    razzaq = new Instructor("Razzaq");
    fundies = new Course("Fundies 2", razzaq);
    calc = new Course("Calculus", razzaq);

    boolean res = true;
    // tests adding a Course to an empty list
    micah.enroll(fundies);
    res &= t.checkExpect(micah.courses, new ConsList<Course>(fundies, new MtList<Course>()));
    // tests adding a Course to a list with a Course
    micah.enroll(calc);
    res &= t.checkExpect(micah.courses,
        new ConsList<Course>(calc, new ConsList<Course>(fundies, new MtList<Course>())));

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
  
  // list test
  
  boolean testFold(Tester t) {
    boolean res = true;
    
    IList<Integer> ints = new ConsList<Integer>(1,
        new ConsList<Integer>(2,
        new MtList<Integer>()));
    
    // sum with fold
    
    int sum = ints.fold((a, b) -> a + b, 0);
    
    res &= t.checkExpect(sum, 3);
    
    // print with fold
    
    String print = ints.fold((s, a) -> s + a, "");
    
    res &= t.checkExpect(print, "12");
    
    return res;
  }
  
  boolean testAnyCompareMatches(Tester t) {
    boolean res = true;
    
    IList<Integer> ints = new ConsList<Integer>(1,
        new ConsList<Integer>(2,
        new MtList<Integer>()));
    
    // test duplicates
    
    boolean dupes = ints.anyCompareMatches((a, b) -> a == b);
    
    res &= t.checkExpect(dupes, false);
    
    // test any same sign
    
    boolean signMatches = ints.anyCompareMatches(
        (a, b) -> Integer.signum(a) == Integer.signum(b));
    
    res &= t.checkExpect(signMatches, true);
    
    return res;
  }
}

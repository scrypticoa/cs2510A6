import java.util.function.*;
import tester.Tester;

interface IArith {
  // accepts an IArithVisitor and calls the apply override
  // relevant to this implementation of IArith
  <R> R accept(IArithVisitor<R> visitor);
  
  // helpful for checker tests, strips a specific IArith object of extension classes,
  // and returns with the access level of an IArith
  // This has two uses:
  // - returning with broad IArith access level allows testing the IArithVisitor<R>.apply(IArith)
  // -- like this: IArithVisitor<R>.apply(new Const(2).base());
  // -- rather than this: IArith const2 = new Const(2); IArithVisitor<R>.apply(const2);
  // - stripping the IArith of extension classes fixes check expect issue where
  // -- the actual value would use the non extended class, but the extended class was expected:
  // -- e.g. 
  // --- error:
  // ---- mirror(new Multiplication(1, 2)) -> new Multiplication(2, 1)
  // ---- actual: BinaryFormula(mulfunc, "mul", 2, 1)
  // ---- expected: Multiplication(2, 1)
  // --- solution:
  // ---- mirror(new Multiplication(1, 2)) -> new Multiplication(2, 1).base()
  IArith base();
}

class Const implements IArith {
  double num;

  public Const(double num) {
    this.num = num;
  }
  
  // accepts an IArithVisitor and passes this to its apply override
  // which accepts a Const
  public <R> R accept(IArithVisitor<R> visitor) {
    return visitor.apply(this);
  }
  
  // returns a copy of this const with IArith access level
  public IArith base() {
    return new Const(this.num);
  }
}

class UnaryFormula implements IArith {
  Function<Double, Double> func;
  String name;
  IArith child;

  public UnaryFormula(Function<Double, Double> func, String name, IArith child) {
    this.func = func;
    this.name = name;
    this.child = child;
  }

  // accepts an IArithVisitor and passes this to its apply override
  // which accepts a UnaryFormula
  public <R> R accept(IArithVisitor<R> visitor) {
    return visitor.apply(this);
  }
  
  // passes a to this.func
  public Double apply(Double a) {
    return this.func.apply(a);
  }
  
  // has this.child accept the given IArithVisitor
  public <R> R childAccept(IArithVisitor<R> visitor) {
    return this.child.accept(visitor);
  }
  
  // returns a copy of this UnaryFormula with IArith access level
  // and stripped of any extensions of UnaryFormula
  public IArith base() {
    return new UnaryFormula(this.func, this.name, child.base());
  }
}

class Negation extends UnaryFormula {
  Negation(IArith child) {
    super((a) -> {
      return -a;
    }, "neg", child);
  }
  
  Negation(double child) {
    this(new Const(child));
  }
}

class Square extends UnaryFormula {
  Square(IArith child) {
    super((a) -> {
      return a * a;
    }, "sqr", child);
  }
  
  Square(double child) {
    this(new Const(child));
  }
}


class BinaryFormula implements IArith {
  BiFunction<Double, Double, Double> func;
  String name;
  IArith left;
  IArith right;

  public BinaryFormula(BiFunction<Double, Double, Double> func, String name, IArith left,
      IArith right) {
    this.func = func;
    this.name = name;
    this.left = left;
    this.right = right;
  }

  // accepts an IArithVisitor and passes this to its apply override
  // which accepts a BinaryFormula
  public <R> R accept(IArithVisitor<R> visitor) {
    return visitor.apply(this);
  }
  
  // passes a and b to this.func
  public Double apply(Double a, Double b) {
    return this.func.apply(a, b);
  }
  
  // has this.left accept the given IArithVisitor
  public <R> R leftAccept(IArithVisitor<R> visitor) {
    return this.left.accept(visitor);
  }
  
  // has this.right accept the given IArithVisitor
  public <R> R rightAccept(IArithVisitor<R> visitor) {
    return this.right.accept(visitor);
  }
  
  // returns a copy of this BinaryFormula with IArith access level
  // and stripped of any extensions of BinaryFormula
  public IArith base() {
    return new BinaryFormula(this.func, this.name, left.base(), right.base());
  }
}


class Addition extends BinaryFormula {
  Addition(IArith left, IArith right) {
    super((a, b) -> {
      return a + b;
    }, "plus", left, right);
  }
  
  Addition(double left, double right) {
    this(new Const(left), new Const(right));
  }
  
  Addition(IArith left, double right) {
    this(left, new Const(right));
  }
  
  Addition(double left, IArith right) {
    this(new Const(left), right);
  }
}

class Subtraction extends BinaryFormula {
  Subtraction(IArith left, IArith right) {
    super((a, b) -> {
      return a - b;
    }, "minus", left, right);
  }
  
  Subtraction(double left, double right) {
    this(new Const(left), new Const(right));
  }
  
  Subtraction(IArith left, double right) {
    this(left, new Const(right));
  }
  
  Subtraction(double left, IArith right) {
    this(new Const(left), right);
  }
}

class Multiplication extends BinaryFormula {
  Multiplication(IArith left, IArith right) {
    super((a, b) -> {
      return a * b;
    }, "mul", left, right);
  }
  
  Multiplication(double left, double right) {
    this(new Const(left), new Const(right));
  }
  
  Multiplication(IArith left, double right) {
    this(left, new Const(right));
  }
  
  Multiplication(double left, IArith right) {
    this(new Const(left), right);
  }
}

class Division extends BinaryFormula {
  Division(IArith left, IArith right) {
    super((a, b) -> {
      return a / b;
    }, "div", left, right);
  }
  
  Division(double left, double right) {
    this(new Const(left), new Const(right));
  }
  
  Division(IArith left, double right) {
    this(left, new Const(right));
  }
  
  Division(double left, IArith right) {
    this(new Const(left), right);
  }
}

interface IArithVisitor<R> {
  // asks arith to pass itself to the apply function which accepts its IArith implementation
  R apply(IArith arith);
  
  // returns some value of type R given a Const
  R apply(Const arith);
  
  // returns some value of type R given a UnaryFormula
  R apply(UnaryFormula arith);

  // returns some value of type R given a BinaryFormula
  R apply(BinaryFormula arith);
}

class EvalVisitor implements IArithVisitor<Double> {
  
  // asks arith to pass itself to the apply function of this 
  // visitor which accepts its IArith implementation
  // Then, evaluates the tree of arithmetic
  public Double apply(IArith arith) {
    return arith.accept(this);
  }
  
  // returns the value of the const
  public Double apply(Const arith) {
    return arith.num;
  }
  
  // returns the application of the UnaryFormula on the evaluated child input
  public Double apply(UnaryFormula arith) {
    return arith.apply(arith.childAccept(this));
  }

  // return the application of the BinaryFormula on the evaluated left and right input
  public Double apply(BinaryFormula arith) {
    return arith.func.apply(
        arith.leftAccept(this),
        arith.rightAccept(this));
  }
}

class PrintVisitor implements IArithVisitor<String> {
  
  // asks arith to pass itself to the apply function of this
  // visitor which accepts its IArith implementation
  // Then, prints out the tree of arithmetic
  public String apply(IArith arith) {
    return arith.accept(this);
  }
  
  // returns the const's num, converted to a string
  public String apply(Const arith) {
    return Double.toString(arith.num);
  }
  
  // returns a string in the form:
  // "(arith.name arith.child.accept(this))"
  public String apply(UnaryFormula arith) {
    return 
        "(" + arith.name + " "
        + arith.childAccept(this) + ")";
  }

  // returns a string in the form:
  // "(arith.name arith.left.accept(this) arith.right.accept(this))"
  public String apply(BinaryFormula arith) {
    return 
        "(" + arith.name + " "
        + arith.leftAccept(this) + " "
        + arith.rightAccept(this) + ")";
  }
}

class AllEvenVisitor implements IArithVisitor<Boolean> {
  
  // asks arith to pass itself to the apply function of this
  // visitor which accepts its IArith implementation
  // Then, determines if every const in the tree of arithmetic is even
  public Boolean apply(IArith arith) {
    return arith.accept(this);
  }
  
  // returns true if arith.num  is even
  public Boolean apply(Const arith) {
    return arith.num % 2 == 0;
  }
  
  // returns whether or not every const in the child arithmetic tree is even
  public Boolean apply(UnaryFormula arith) {
    return arith.childAccept(this);
  }

  // returns whether or not every const in the left arithmetic tree is even
  // and every const in the right arithmetic tree is even
  public Boolean apply(BinaryFormula arith) {
    return arith.leftAccept(this) && arith.rightAccept(this);
  }
}

class MirrorVisitor implements IArithVisitor<IArith> {
  
  // asks arith to pass itself to the apply function of this
  // visitor which accepts its IArith implementation
  // Then, flips the left and right child of every BinaryFormula in the arithmetic tree, recursively
  public IArith apply(IArith arith) {
    return arith.accept(this);
  }
  
  // returns a copy of arith
  public IArith apply(Const arith) {
    return new Const(arith.num);
  }
  
  // returns a copy of arith, where every BinaryFormula in the child arithmetic tree is flipped
  public IArith apply(UnaryFormula arith) {
    return new UnaryFormula(
        arith.func,
        arith.name,
        arith.childAccept(this));
  }

  // returns a flipped copy of this BinaryFormula arith, where every BinaryFormula
  // in the left and right arithmetic trees are also flipped
  public IArith apply(BinaryFormula arith) {
    return new BinaryFormula(
        arith.func,
        arith.name,
        arith.rightAccept(this),
        arith.leftAccept(this));
  }
}

class ExamplesVisitors {
  IArith allAdds = new Addition(new Addition(2, 2), new Addition(2, 2));
  IArith pointless = new Multiplication(
      new Multiplication(2, new Subtraction(2, 5)),
      new Addition(new Negation(1), new Division(new Addition(2, 3), 5)));
  IArith large = new Square(
      new Multiplication(new Square(10), new Square(63)));
  
  // visitors
  
  EvalVisitor eval = new EvalVisitor();
  PrintVisitor print = new PrintVisitor();
  AllEvenVisitor allEven = new AllEvenVisitor();
  MirrorVisitor mirror = new MirrorVisitor();
  
  // accept visitor tests
  
  boolean testEvalVisitor(Tester t) {
    boolean res = true;
    
    // simple tests
    
    // - const
    
    res &= t.checkInexact(new Const(2).accept(eval), 2.0, .001);
    
    // - unary
    res &= t.checkInexact(new Negation(2).accept(eval), -2.0, .001);
    res &= t.checkInexact(new Square(2).accept(eval), 4.0, .001);
    
    // - binary
    res &= t.checkInexact(new Addition(1, 2).accept(eval), 3.0, .001);
    res &= t.checkInexact(new Subtraction(1, 2).accept(eval), -1.0, .001);
    res &= t.checkInexact(new Multiplication(1, 2).accept(eval), 2.0, .001);
    res &= t.checkInexact(new Division(1, 2).accept(eval), 1d / 2, .001);
    
    // complicated tests
    res &= t.checkInexact(allAdds.accept(eval), 8.0, .001);
    res &= t.checkInexact(pointless.accept(eval), 0.0, .001);
    res &= t.checkInexact(large.accept(eval), 157529610000.0, .001);
    
    return res;
  }
  
  boolean testPrintVisitor(Tester t) {
    boolean res = true;
    
    // simple tests
    
    // - const
    
    res &= t.checkExpect(new Const(2).accept(print), "2.0");
    
    // - unary
    res &= t.checkExpect(new Negation(2).accept(print), "(neg 2.0)");
    res &= t.checkExpect(new Square(2).accept(print), "(sqr 2.0)");
    
    // - binary
    res &= t.checkExpect(new Addition(1, 2).accept(print), "(plus 1.0 2.0)");
    res &= t.checkExpect(new Subtraction(1, 2).accept(print), "(minus 1.0 2.0)");
    res &= t.checkExpect(new Multiplication(1, 2).accept(print), "(mul 1.0 2.0)");
    res &= t.checkExpect(new Division(1, 2).accept(print), "(div 1.0 2.0)");
    
    // complicated tests
    res &= t.checkExpect(allAdds.accept(print),
        "(plus (plus 2.0 2.0) (plus 2.0 2.0))");
    res &= t.checkExpect(pointless.accept(print),
        "(mul (mul 2.0 (minus 2.0 5.0)) (plus (neg 1.0) (div (plus 2.0 3.0) 5.0)))");
    res &= t.checkExpect(large.accept(print),
        "(sqr (mul (sqr 10.0) (sqr 63.0)))");
        
    return res;
  }
  
  boolean testAllEvenVisitor(Tester t) {
    boolean res = true;
    
    // simple tests
    
    // - const
    
    res &= t.checkExpect(new Const(2).accept(allEven), true);
    res &= t.checkExpect(new Const(1).accept(allEven), false);
    
    // - unary
    res &= t.checkExpect(new Negation(2).accept(allEven), true);
    res &= t.checkExpect(new Negation(1).accept(allEven), false);
    res &= t.checkExpect(new Square(6).accept(allEven), true);
    res &= t.checkExpect(new Square(7).accept(allEven), false);
    
    // - binary
    res &= t.checkExpect(new Addition(1, 2).accept(allEven), false);
    res &= t.checkExpect(new Addition(24, 136).accept(allEven), true);
    res &= t.checkExpect(new Subtraction(17, 24).accept(allEven), false);
    res &= t.checkExpect(new Subtraction(38, 24).accept(allEven), true);
    res &= t.checkExpect(new Multiplication(1, 2).accept(allEven), false);
    res &= t.checkExpect(new Multiplication(50, 100).accept(allEven), true);
    res &= t.checkExpect(new Division(1, 2).accept(allEven), false);
    res &= t.checkExpect(new Division(2, 2).accept(allEven), true);
    
    // complicated tests
    res &= t.checkExpect(allAdds.accept(allEven), true);
    res &= t.checkExpect(pointless.accept(allEven), false);
    res &= t.checkExpect(large.accept(allEven), false);
        
    return res;
  }
  
  boolean testMirrorVisitor(Tester t) {
    boolean res = true;
    
    // simple tests
    
    // - const
    
    res &= t.checkExpect(new Const(2).accept(mirror), new Const(2).base());
    
    // - unary
    res &= t.checkExpect(new Negation(2).accept(mirror), new Negation(2).base());
    res &= t.checkExpect(new Square(2).accept(mirror), new Square(2).base());
    
    // - binary
    res &= t.checkExpect(new Addition(1, 2).accept(mirror), new Addition(2, 1).base());
    res &= t.checkExpect(new Subtraction(1, 2).accept(mirror), new Subtraction(2, 1).base());
    res &= t.checkExpect(new Multiplication(1, 2).accept(mirror), new Multiplication(2, 1).base());
    res &= t.checkExpect(new Division(1, 2).accept(mirror), new Division(2, 1).base());
    
    // complicated tests
    
    IArith pointlessMirror = new Multiplication(
        new Addition(new Division(5, new Addition(3, 2)), new Negation(1)),
        new Multiplication(new Subtraction(5, 2), 2));
    
    IArith largeMirror = new Square(
        new Multiplication(new Square(63), new Square(10)));
    
    res &= t.checkExpect(allAdds.accept(mirror), allAdds.base()); // symmetrical
    res &= t.checkExpect(pointless.accept(mirror), pointlessMirror.base());
    res &= t.checkExpect(large.accept(mirror), largeMirror.base());
    
    return res;
  }
  
  // visitor apply tests
  
  // - generic ariths
  
  boolean testApplyEvalVisitor(Tester t) {
    boolean res = true;
    
    // simple tests
    
    // - const
    
    res &= t.checkInexact(eval.apply(new Const(2).base()), 2.0, .001);
    
    // - unary
    res &= t.checkInexact(eval.apply(new Negation(2).base()), -2.0, .001);
    res &= t.checkInexact(eval.apply(new Square(2).base()), 4.0, .001);
    
    // - binary
    res &= t.checkInexact(eval.apply(new Addition(1, 2).base()), 3.0, .001);
    res &= t.checkInexact(eval.apply(new Subtraction(1, 2).base()), -1.0, .001);
    res &= t.checkInexact(eval.apply(new Multiplication(1, 2).base()), 2.0, .001);
    res &= t.checkInexact(eval.apply(new Division(1, 2).base()), 1d / 2, .001);
    
    // complicated tests
    res &= t.checkInexact(eval.apply(allAdds), 8.0, .001);
    res &= t.checkInexact(eval.apply(pointless), 0.0, .001);
    res &= t.checkInexact(eval.apply(large), 157529610000.0, .001);
    
    return res;
  }
  
  boolean testApplyPrintVisitor(Tester t) {
    boolean res = true;
    
    // simple tests
    
    // - const
    
    res &= t.checkExpect(print.apply(new Const(2).base()), "2.0");
    
    // - unary
    res &= t.checkExpect(print.apply(new Negation(2).base()), "(neg 2.0)");
    res &= t.checkExpect(print.apply(new Square(2).base()), "(sqr 2.0)");
    
    // - binary
    res &= t.checkExpect(print.apply(new Addition(1, 2).base()), "(plus 1.0 2.0)");
    res &= t.checkExpect(print.apply(new Subtraction(1, 2).base()), "(minus 1.0 2.0)");
    res &= t.checkExpect(print.apply(new Multiplication(1, 2).base()), "(mul 1.0 2.0)");
    res &= t.checkExpect(print.apply(new Division(1, 2).base()), "(div 1.0 2.0)");
    
    // complicated tests
    res &= t.checkExpect(print.apply(allAdds),
        "(plus (plus 2.0 2.0) (plus 2.0 2.0))");
    res &= t.checkExpect(print.apply(pointless),
        "(mul (mul 2.0 (minus 2.0 5.0)) (plus (neg 1.0) (div (plus 2.0 3.0) 5.0)))");
    res &= t.checkExpect(print.apply(large),
        "(sqr (mul (sqr 10.0) (sqr 63.0)))");
        
    return res;
  }
  
  boolean testApplyAllEvenVisitor(Tester t) {
    boolean res = true;
    
    // simple tests
    
    // - const
    
    res &= t.checkExpect(allEven.apply(new Const(2).base()), true);
    res &= t.checkExpect(allEven.apply(new Const(1).base()), false);
    
    // - unary
    res &= t.checkExpect(allEven.apply(new Negation(2).base()), true);
    res &= t.checkExpect(allEven.apply(new Negation(1).base()), false);
    res &= t.checkExpect(allEven.apply(new Square(6).base()), true);
    res &= t.checkExpect(allEven.apply(new Square(7).base()), false);
    
    // - binary
    res &= t.checkExpect(allEven.apply(new Addition(1, 2).base()), false);
    res &= t.checkExpect(allEven.apply(new Addition(24, 136).base()), true);
    res &= t.checkExpect(allEven.apply(new Subtraction(17, 24).base()), false);
    res &= t.checkExpect(allEven.apply(new Subtraction(38, 24).base()), true);
    res &= t.checkExpect(allEven.apply(new Multiplication(1, 2).base()), false);
    res &= t.checkExpect(allEven.apply(new Multiplication(50, 100).base()), true);
    res &= t.checkExpect(allEven.apply(new Division(1, 2).base()), false);
    res &= t.checkExpect(allEven.apply(new Division(2, 2).base()), true);
    
    // complicated tests
    res &= t.checkExpect(allEven.apply(allAdds), true);
    res &= t.checkExpect(allEven.apply(pointless), false);
    res &= t.checkExpect(allEven.apply(large), false);
        
    return res;
  }
  
  boolean testApplyMirrorVisitor(Tester t) {
    boolean res = true;
    
    // simple tests
    
    // - const
    
    res &= t.checkExpect(mirror.apply(new Const(2).base()), new Const(2).base());
    
    // - unary
    res &= t.checkExpect(mirror.apply(new Negation(2).base()), new Negation(2).base());
    res &= t.checkExpect(mirror.apply(new Square(2).base()), new Square(2).base());
    
    // - binary
    res &= t.checkExpect(mirror.apply(new Addition(1, 2).base()), new Addition(2, 1).base());
    res &= t.checkExpect(mirror.apply(new Subtraction(1, 2).base()), new Subtraction(2, 1).base());
    res &= t.checkExpect(
        mirror.apply(new Multiplication(1, 2).base()),
        new Multiplication(2, 1).base());
    res &= t.checkExpect(mirror.apply(new Division(1, 2).base()), new Division(2, 1).base());
    
    // complicated tests
    
    IArith pointlessMirror = new Multiplication(
        new Addition(new Division(5, new Addition(3, 2)), new Negation(1)),
        new Multiplication(new Subtraction(5, 2), 2));
    
    IArith largeMirror = new Square(
        new Multiplication(new Square(63), new Square(10)));
    
    res &= t.checkExpect(mirror.apply(allAdds), allAdds.base()); // symmetrical
    res &= t.checkExpect(mirror.apply(pointless), pointlessMirror.base());
    res &= t.checkExpect(mirror.apply(large), largeMirror.base());
    
    return res;
  }
  
  // - explicit ariths
  
  BinaryFormula allAddsExplicit = new Addition(new Addition(2, 2), new Addition(2, 2));
  BinaryFormula pointlessExplicit = new Multiplication(
      new Multiplication(2, new Subtraction(2, 5)),
      new Addition(new Negation(1), new Division(new Addition(2, 3), 5)));
  UnaryFormula largeExplicit = new Square(
      new Multiplication(new Square(10), new Square(63)));
  
  boolean testApplyExplicitEvalVisitor(Tester t) {
    boolean res = true;
    
    // simple tests
    
    // - const
    
    res &= t.checkInexact(eval.apply(new Const(2)), 2.0, .001);
    
    // - unary
    res &= t.checkInexact(eval.apply(new Negation(2)), -2.0, .001);
    res &= t.checkInexact(eval.apply(new Square(2)), 4.0, .001);
    
    // - binary
    res &= t.checkInexact(eval.apply(new Addition(1, 2)), 3.0, .001);
    res &= t.checkInexact(eval.apply(new Subtraction(1, 2)), -1.0, .001);
    res &= t.checkInexact(eval.apply(new Multiplication(1, 2)), 2.0, .001);
    res &= t.checkInexact(eval.apply(new Division(1, 2)), 1d / 2, .001);
    
    // complicated tests
    res &= t.checkInexact(eval.apply(allAddsExplicit), 8.0, .001);
    res &= t.checkInexact(eval.apply(pointlessExplicit), 0.0, .001);
    res &= t.checkInexact(eval.apply(largeExplicit), 157529610000.0, .001);
    
    return res;
  }
  
  boolean testApplyExplicitPrintVisitor(Tester t) {
    boolean res = true;
    
    // simple tests
    
    // - const
    
    res &= t.checkExpect(print.apply(new Const(2)), "2.0");
    
    // - unary
    res &= t.checkExpect(print.apply(new Negation(2)), "(neg 2.0)");
    res &= t.checkExpect(print.apply(new Square(2)), "(sqr 2.0)");
    
    // - binary
    res &= t.checkExpect(print.apply(new Addition(1, 2)), "(plus 1.0 2.0)");
    res &= t.checkExpect(print.apply(new Subtraction(1, 2)), "(minus 1.0 2.0)");
    res &= t.checkExpect(print.apply(new Multiplication(1, 2)), "(mul 1.0 2.0)");
    res &= t.checkExpect(print.apply(new Division(1, 2)), "(div 1.0 2.0)");
    
    // complicated tests
    res &= t.checkExpect(print.apply(allAddsExplicit),
        "(plus (plus 2.0 2.0) (plus 2.0 2.0))");
    res &= t.checkExpect(print.apply(pointlessExplicit),
        "(mul (mul 2.0 (minus 2.0 5.0)) (plus (neg 1.0) (div (plus 2.0 3.0) 5.0)))");
    res &= t.checkExpect(print.apply(largeExplicit),
        "(sqr (mul (sqr 10.0) (sqr 63.0)))");
        
    return res;
  }
  
  boolean testApplyExplicitAllEvenVisitor(Tester t) {
    boolean res = true;
    
    // simple tests
    
    // - const
    
    res &= t.checkExpect(allEven.apply(new Const(2)), true);
    res &= t.checkExpect(allEven.apply(new Const(1)), false);
    
    // - unary
    res &= t.checkExpect(allEven.apply(new Negation(2)), true);
    res &= t.checkExpect(allEven.apply(new Negation(1)), false);
    res &= t.checkExpect(allEven.apply(new Square(6)), true);
    res &= t.checkExpect(allEven.apply(new Square(7)), false);
    
    // - binary
    res &= t.checkExpect(allEven.apply(new Addition(1, 2)), false);
    res &= t.checkExpect(allEven.apply(new Addition(24, 136)), true);
    res &= t.checkExpect(allEven.apply(new Subtraction(17, 24)), false);
    res &= t.checkExpect(allEven.apply(new Subtraction(38, 24)), true);
    res &= t.checkExpect(allEven.apply(new Multiplication(1, 2)), false);
    res &= t.checkExpect(allEven.apply(new Multiplication(50, 100)), true);
    res &= t.checkExpect(allEven.apply(new Division(1, 2)), false);
    res &= t.checkExpect(allEven.apply(new Division(2, 2)), true);
    
    // complicated tests
    res &= t.checkExpect(allEven.apply(allAddsExplicit), true);
    res &= t.checkExpect(allEven.apply(pointlessExplicit), false);
    res &= t.checkExpect(allEven.apply(largeExplicit), false);
        
    return res;
  }
  
  boolean testApplyExplicitMirrorVisitor(Tester t) {
    boolean res = true;
    
    // simple tests
    
    // - const
    
    res &= t.checkExpect(mirror.apply(new Const(2)), new Const(2).base());
    
    // - unary
    res &= t.checkExpect(mirror.apply(new Negation(2)), new Negation(2).base());
    res &= t.checkExpect(mirror.apply(new Square(2)), new Square(2).base());
    
    // - binary
    res &= t.checkExpect(mirror.apply(new Addition(1, 2)), new Addition(2, 1).base());
    res &= t.checkExpect(mirror.apply(new Subtraction(1, 2)), new Subtraction(2, 1).base());
    res &= t.checkExpect(mirror.apply(new Multiplication(1, 2)), new Multiplication(2, 1).base());
    res &= t.checkExpect(mirror.apply(new Division(1, 2)), new Division(2, 1).base());
    
    // complicated tests
    
    IArith pointlessMirror = new Multiplication(
        new Addition(new Division(5, new Addition(3, 2)), new Negation(1)),
        new Multiplication(new Subtraction(5, 2), 2));
    
    IArith largeMirror = new Square(
        new Multiplication(new Square(63), new Square(10)));
    
    res &= t.checkExpect(mirror.apply(allAddsExplicit), allAdds.base()); // symmetrical
    res &= t.checkExpect(mirror.apply(pointlessExplicit), pointlessMirror.base());
    res &= t.checkExpect(mirror.apply(largeExplicit), largeMirror.base());
    
    return res;
  }
}
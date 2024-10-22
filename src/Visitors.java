import java.util.function.*;

interface IArith {
  <R> R accept(IArithVisitor<R> visitor);
}

class Const implements IArith {
  double num;

  public <R> R accept(IArithVisitor<R> visitor) {
    return visitor.apply(this);
  }
}

class UnaryFormula implements IArith {
  Function<Double, Double> func;
  String name;
  IArith child;

  public UnaryFormula(Function <Double, Double> func, String name, IArith child) {
    this.func = func;
    this.name = name;
    this.child = child;
  }

  public <R> R accept(IArithVisitor<R> visitor) {
    return visitor.apply(this);
  }
  
  public <R> R childAccept(IArithVisitor<R> visitor) {
    return this.child.accept(visitor);
  }
}

class Negation extends UnaryFormula {
  Negation(IArith child) {
    super((a) -> {
      return -a;
    }, "neg", child);
  }
}

class Square extends UnaryFormula {
  Square(IArith child) {
    super((a) -> {
      return a * a;
    }, "squ", child);
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

  public <R> R accept(IArithVisitor<R> visitor) {
    return visitor.apply(this);
  }
  
  public <R> R leftAccept(IArithVisitor<R> visitor) {
    return this.left.accept(visitor);
  }
  
  public <R> R rightAccept(IArithVisitor<R> visitor) {
    return this.right.accept(visitor);
  }
}


class Addition extends BinaryFormula {
  Addition(IArith left, IArith right) {
    super((a, b) -> {
      return a + b;
    }, "plus", left, right);
  }
}

class Subtraction extends BinaryFormula {
  Subtraction(IArith left, IArith right) {
    super((a, b) -> {
      return a - b;
    }, "minus", left, right);
  }
}

class Multiplication extends BinaryFormula {
  Multiplication(IArith left, IArith right) {
    super((a, b) -> {
      return a * b;
    }, "mul", left, right);
  }
}

class Division extends BinaryFormula {
  Division(IArith left, IArith right) {
    super((a, b) -> {
      return a / b;
    }, "div", left, right);
  }
}

interface IArithVisitor<R> {
  R apply(Const arith);

  R apply(UnaryFormula arith);

  R apply(BinaryFormula arith);
}

class EvalVisitor implements IArithVisitor<Double> {
  public Double apply(Const arith) {
    return arith.num;
  }
  
  public Double apply(UnaryFormula arith) {
    return arith.func.apply(arith.childAccept(this));
  }

  public Double apply(BinaryFormula arith) {
    return arith.func.apply(
        arith.leftAccept(this),
        arith.rightAccept(this));
  }
}

class PrintVisitor implements IArithVisitor<String> {
  public String apply(Const arith) {
    return Double.toString(arith.num);
  }
  
  public String apply(UnaryFormula arith) {
    return 
        "(" + arith.name + " " +
        arith.childAccept(this) + ")";
  }

  public String apply(BinaryFormula arith) {
    return 
        "(" + arith.name + " " +
        arith.leftAccept(this) + " " +
        arith.rightAccept(this) + ")";
  }
}

class AllEvenVisitor implements IArithVisitor<Boolean> {
  public Boolean apply(Const arith) {
    return arith.num % 2 == 0;
  }
  
  public Boolean apply(UnaryFormula arith) {
    return arith.childAccept(this);
  }

  public Boolean apply(BinaryFormula arith) {
    return arith.leftAccept(this) && arith.rightAccept(this);
  }
}

class MirrorVisitor implements IArithVisitor<IArith> {
  public IArith apply(Const arith) {
    return arith;
  }
  
  public IArith apply(UnaryFormula arith) {
    return arith;
  }

  public IArith apply(BinaryFormula arith) {
    return new BinaryFormula (arith.func, arith.name, arith.right, arith.left);
  }
}


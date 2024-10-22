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
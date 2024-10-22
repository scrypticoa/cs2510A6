import java.util.function.*;

interface IArith {
  <R> R accept(IArithVisitor<R> visitor);
}

class Const implements IArith {
  double num;
  
  public <R>R accept(IArithVisitor<R> visitor) {
    return visitor.apply(this);
  }
}

class UnaryFormula implements IArith {
  Function<Double, Double> func;
  String name;
  IArith child;
  
  public <R>R accept(IArithVisitor<R> visitor) {
    return visitor.apply(this);
  }
}

class BinaryFormula implements IArith {
  BiFunction <Double, Double, Double> func;
  String name;
  IArith left;
  IArith right;
  
  public BinaryFormula(
      BiFunction <Double, Double, Double> func,
      String name, IArith left, IArith right) {
    this.func = func;
    this.name = name;
    this.left = left;
    this.right = right;
  }
  
  public <R>R accept(IArithVisitor<R> visitor) {
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

interface IArithVisitor<R> {
  R apply(Const arith);
  R apply(UnaryFormula arith);
  R apply(BinaryFormula arith);
}
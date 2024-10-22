import java.util.function.*;

interface IArith {
  <R> R accept(IArithVisitor<R> visitor);
}

class Const implements IArith {
  double num;
  
}

class UnaryFormula implements IArith {
  Function<Double, Double> func;
  String name;
  IArith child;
}

class BinaryFormula implements IArith {
  BiFunction <Double, Double, Double> func;
  String name;
  IArith left;
  IArith right;
}

interface IArithVisitor<R> {
  R apply(Const arith);
  R apply(UnaryFormula arith);
  R apply(BinaryFormula arith);
}
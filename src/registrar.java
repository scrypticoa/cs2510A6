import java.util.function.*;

interface IList<T> {
  <R> R fold(BiFunction<R, T, R> func, R val);
}

class MtLoList<T> implements IList<T> {
  public <R> R fold(BiFunction<R, T, R> func, R val) {
    return val;
  }
}

class ConsLoList<T> implements IList<T> {
  T first;
  IList<T> rest;
  
  public ConsLoList(T first, IList<T> rest) {
    this.first = first;
    this.rest = rest;
  }
  
  public <R> R fold(BiFunction<R, T, R> func, R val) {
    return func.apply(val, first);
  }
}


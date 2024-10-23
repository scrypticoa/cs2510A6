interface IList<T> {}

class MtLoList<T> {
  T first;
  IList<T> rest;
  
  public MtLoList(T first, IList<T> rest) {
    this.first = first;
    this.rest = rest;
  }
}

class ConsLoList<T> {
  T first;
  IList<T> rest;
  
  public ConsLoList(T first, IList<T> rest) {
    this.first = first;
    this.rest = rest;
  }
}
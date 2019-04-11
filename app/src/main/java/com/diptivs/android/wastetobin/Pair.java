package com.diptivs.android.wastetobin;


public class Pair<F extends Comparable<F>, S extends Comparable<S>>
    implements Comparable<Pair<F, S>> {
  public F first;
  public S second;

  /** Creates a pair .*/
  public Pair(F first, S second) {
    this.first = first;
    this.second = second;
  }

  @Override
  public int compareTo(Pair<F, S> arg0) {
    int firstCompare = this.first.compareTo(arg0.first);
    int secondCompare = this.second.compareTo(arg0.second);
    return firstCompare == 0 ? secondCompare : firstCompare;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((first == null) ? 0 : first.hashCode());
    result = prime * result + ((second == null) ? 0 : second.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    Pair<? , ?> other = (Pair<? , ?>) obj;
    if (first == null) {
      if (other.first != null) {
        return false;
      }
    } else if (!first.equals(other.first)) {
      return false;
    }
    if (second == null) {
      if (other.second != null) {
        return false;
      }
    } else if (!second.equals(other.second)) {
      return false;
    }
    return true;
  }


  @SuppressWarnings("unchecked")
  public int equalsTo(Object obj) {
    if (obj instanceof Pair) {
      return compareTo((Pair<F, S>) obj);
    } else {
      return -1;
    }
  }
}

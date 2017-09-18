package net.parsonsrun;

import java.io.*;
import java.util.*;

public class Tuple<L, R> implements Serializable {
private static final long serialVersionUID = 1L;

  protected L left;
  protected R right;

  protected Tuple() {
    // Default constructor for serialization
  }

  public Tuple(L inLeft, R inRight) {
    left = inLeft;
    right = inRight;
  }

  public L getLeft() {
    return left;
  }

  public R getRight() {
    return right;
  }
  
  public String toString()
  {
	  return getLeft() + ", " + getRight();
  }
}
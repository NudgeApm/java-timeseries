/*
 * Copyright (c) 2016 Jacob Rachiele
 *
 */

package math.function;

/**
 * A scalar-valued function of one variable.
 * @author Jacob Rachiele
 *
 */
@FunctionalInterface
public interface Function {

  /**
   * Compute and return the value of the function at the given point.
   * @param point the point at which to evaluate the function.
   * @return the value of the function at the given point.
   */
  double at(double point);
}

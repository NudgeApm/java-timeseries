/*
 * Copyright (c) 2016 Jacob Rachiele
 *
 */
package optim;

import math.function.AbstractFunction;

import static java.lang.Math.abs;

/**
 * A line search implementation designed to find a point in the domain that satisfies the strong Wolfe conditions.
 */
final class StrongWolfeLineSearch {

  private static final int MAX_UPDATE_ITERATIONS = 40;
  private static final double DELTA_MAX = 4.0;

  private final AbstractFunction f;
  private final double c1;
  private final double c2;
  private final double f0;
  private final double slope0;
  private final double alphaMax;
  private final double alpha0;
  int m = 0;
  private double alphaT = 0.0;

  /**
   * Use a builder to create a new line search object.
   *
   * @param builder the line search builder containing the necessary data for the new object.
   */
  private StrongWolfeLineSearch(final Builder builder) {
    this.f = builder.f;
    this.c1 = builder.c1;
    this.c2 = builder.c2;
    this.f0 = builder.f0;
    this.slope0 = builder.slope0;
    this.alphaMax = builder.alphaMax;
    this.alpha0 = builder.alpha0;
  }

  /**
   * Get a new builder for the line search.
   *
   * @param f      the function.
   * @param f0     the value of the function at 0.
   * @param slope0 the slope of the function at 0.
   * @return a new builder for the line search.
   */
  static Builder newBuilder(final AbstractFunction f, final double f0, final double slope0) {
    return new Builder(f, f0, slope0);
  }

  /**
   * Perform the line search , returning an element satisfying the strong Wolfe conditions.
   *
   * @return an element satisfying the strong Wolfe conditions.
   */
  final double search() {
    double fNewAlphaT = f0;
    double dNewAlphaT = slope0;
    double newAlphaT = alpha0;

    int i = 1;
    while (i < MAX_UPDATE_ITERATIONS) {
      m++;
      double fAlphaT = fNewAlphaT;
      double dAlphaT = dNewAlphaT;
      fNewAlphaT = f.at(newAlphaT);
      while (abs(fNewAlphaT) == Double.POSITIVE_INFINITY && i < MAX_UPDATE_ITERATIONS) {
        newAlphaT /= 2;
        fAlphaT = fNewAlphaT;
        dAlphaT = dNewAlphaT;
        fNewAlphaT = f.at(newAlphaT);
        i++;
      }
      dNewAlphaT = f.slopeAt(newAlphaT);
      if (fNewAlphaT > f0 + c1 * newAlphaT * slope0 || fNewAlphaT >= fAlphaT && i > 1) {
        return zoom(alphaT, newAlphaT, fAlphaT, fNewAlphaT, dAlphaT, dNewAlphaT);
      }
      // dNewAlphaT = f.slopeAt(newAlphaT);
      if (abs(dNewAlphaT) <= -c2 * slope0) {
        return newAlphaT;
      }
      if (dNewAlphaT >= 0) {
        return zoom(newAlphaT, alphaT, fNewAlphaT, fAlphaT, dNewAlphaT, dAlphaT);
      }
      double oldAlphaT = alphaT;
      alphaT = newAlphaT;
      // Safeguard condition 2.2 since alphaT is increasing.
      newAlphaT = Math.min(alphaT + DELTA_MAX * (alphaT - oldAlphaT), alphaMax);
      i++;
    }
    return newAlphaT;
  }

  private double zoom(double alphaLo, double alphaHi, double fAlphaLo, double fAlphaHi, double dAlphaLo, double dAlphaHi) {
    double alphaJ = 0.0;
    double fAlphaJ;
    double dAlphaJ = 1.0;
    double intervalLength = 0.0;
    double updatedIntervalLength;
    int trials = 0;
    // double mid = 0.0;
    // double fMid = 0.0;

    int k = 1;
    double gradientTolerance = 1E-8;
    while (k < MAX_UPDATE_ITERATIONS && abs(dAlphaJ) > gradientTolerance) {
      m++;
      // mid = 0.80*alphaLo + 0.2*alphaHi;
      // fMid = f.at(mid);
      // alphaJ = QuadraticInterpolation.threePointMinimum(alphaLo, mid, alphaHi, fAlphaLo, fMid, fAlphaHi);
      if (trials == 0) {
        intervalLength = abs(alphaHi - alphaLo);
      }
      updatedIntervalLength = abs(alphaHi - alphaLo);
      if (abs((updatedIntervalLength - intervalLength) / intervalLength) < 0.667 && trials > 2) {
        alphaJ = abs(alphaHi + alphaLo) / 2.0;
        trials = 0;
      } else {
        alphaJ = getTrialValue(alphaLo, alphaHi, fAlphaLo, fAlphaHi, dAlphaLo, dAlphaHi);
        trials++;
      }
      double alphaMin = 5E-3;
      if (alphaJ < alphaMin) {
        return alphaMin;
      }
      fAlphaJ = f.at(alphaJ);
      dAlphaJ = f.slopeAt(alphaJ);
      if (fAlphaJ > f0 + c1 * alphaJ * slope0 || fAlphaJ >= fAlphaLo) {
        alphaHi = alphaJ;
        fAlphaHi = fAlphaJ;
        dAlphaHi = dAlphaJ;
      } else {
        if (abs(dAlphaJ) <= -c2 * slope0) {
          return alphaJ;
        }
        if (dAlphaJ * (alphaHi - alphaLo) >= 0) {
          alphaHi = alphaLo;
          fAlphaHi = fAlphaLo;
          dAlphaHi = dAlphaLo;
        }
        alphaLo = alphaJ;
        fAlphaLo = fAlphaJ;
        dAlphaLo = dAlphaJ;
      }
      k++;
    }
    return alphaJ;
  }

  private double getTrialValue(final double alphaLo, final double alphaHi, double fAlphaLo, double fAlphaHi, double dAlphaLo, double dAlphaHi) {
    final double alphaC;
    final double alphaQ;
    final double alphaS;
    if (fAlphaHi > fAlphaLo) {
      //alphaC = new CubicInterpolation(alphaLo, alphaHi, fAlphaLo, fAlphaHi, dAlphaLo, dAlphaHi).minimum();
      alphaC = new CubicInterpolation(alphaLo, alphaHi, fAlphaLo, fAlphaHi, dAlphaLo, dAlphaHi).minimum();
      alphaQ = new QuadraticInterpolation(alphaLo, alphaHi, fAlphaLo, fAlphaHi, dAlphaLo).minimum();
      return (abs(alphaC - alphaLo) < abs(alphaQ - alphaLo)) ? alphaC : 0.5 * (alphaQ + alphaC);
    } else if (dAlphaLo * dAlphaHi < 0) {
      alphaC = new CubicInterpolation(alphaLo, alphaHi, fAlphaLo, fAlphaHi, dAlphaLo, dAlphaHi).minimum();
      alphaS = QuadraticInterpolation.secantFormulaMinimum(alphaLo, alphaHi, dAlphaLo, dAlphaHi);
      return (abs(alphaC - alphaHi) >= abs(alphaS - alphaHi)) ? alphaC : alphaS;
    } else if (abs(dAlphaHi) <= abs(dAlphaLo)) {
      alphaS = QuadraticInterpolation.secantFormulaMinimum(alphaLo, alphaHi, dAlphaLo, dAlphaHi);
      return alphaS;
    } else {
      return new CubicInterpolation(alphaHi, alphaLo, fAlphaHi, fAlphaLo, dAlphaHi, dAlphaLo).minimum();
    }
  }

  /**
   * A builder for the line search.
   */
  static final class Builder {

    private final AbstractFunction f;
    private final double f0;
    private final double slope0;
    private double c1 = 1E-3;
    private double c2 = 0.5;
    private double alphaMax = 1000.0;
    private double alpha0 = 1.0;

    Builder(AbstractFunction f, double f0, double slope0) {
      this.f = f;
      this.f0 = f0;
      this.slope0 = slope0;
    }

    final Builder c1(double c1) {
      this.c1 = c1;
      return this;
    }

    final Builder c2(double c2) {
      this.c2 = c2;
      return this;
    }

    final Builder alphaMax(double alphaMax) {
      this.alphaMax = alphaMax;
      return this;
    }

    final Builder alpha0(double alpha0) {
      this.alpha0 = alpha0;
      return this;
    }

    public final StrongWolfeLineSearch build() {
      return new StrongWolfeLineSearch(this);
    }
  }
}

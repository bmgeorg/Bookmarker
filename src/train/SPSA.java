package train;

import java.util.Arrays;
import java.util.Random;

/*
 * Based on:
 * An Overview of the Simultaneous Perturbation Method for Efficient Optimization
 * by
 * James C. Spall
 */
public class SPSA implements Trainer {
	final double a = 0, c = 0, A = 0, alpha = 0, gamma = 0;
	final int NUM_ITERATIONS = 100;
	

	@Override
	public double[] train(Model model) {
		double[] theta = Arrays.copyOf(model.getInitialParameters(), model.getInitialParameters().length);
		
		for(int k = 0; k < NUM_ITERATIONS; k++) {
			final double ak = a/Math.pow(k+A, alpha);
			final double ck = c/Math.pow(k, gamma);
			double[] delta = bernoulli(theta.length);
			double[] thetaPlus = transform(theta, delta, new BinaryOperator() {
				@Override
				public double apply(double a, double b) {
					return a + ck*b;
				}
			});
			double[] thetaMinus = transform(theta, delta, new BinaryOperator() {
				@Override
				public double apply(double a, double b) {
					return a - ck*b;
				}
			});
			final double yplus = model.loss(thetaPlus);
			final double yminus = model.loss(thetaMinus);
			double[] ghat = transform(delta, new UnaryOperator() {
				@Override
				public double apply(double a) {
					return (yplus-yminus)/(2*ck*a);
				}
			});
			theta = transform(theta, ghat, new BinaryOperator() {
				@Override
				public double apply(double a, double b) {
					return a - ak*b;
				}
			});
		}
		
		return theta;
	}
	
	//bernoulli +-1 distribution
	public double[] bernoulli(int length) {
		double[] result = new double[length];
		
		Random rand = new Random();
		for(int i = 0; i < length; i++)
			result[i] = rand.nextInt(2) - 1;
		
		return result;
	}
	
	/* Array Manipulations - for performing +-/* on entire arrays */
	private interface UnaryOperator {
		double apply(double a);
	}
	
	private interface BinaryOperator {
		double apply(double a, double b);
	}
	
	private double[] transform(double[] a, UnaryOperator operator) {
		double[] result = new double[a.length];
		
		for(int i = 0; i < a.length; i++)
			result[i] = operator.apply(a[i]);
		
		return result;
	}
	
	private double[] transform(double[] a, double[] b, BinaryOperator operator) {
		double[] result = new double[a.length];
		
		for(int i = 0; i < a.length; i++)
			result[i] = operator.apply(a[i], b[i]);
		
		return result;
	}
}

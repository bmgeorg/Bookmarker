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
	final double a = 3, c = 1, A = 5, alpha = 0.602, gamma = 0.101;
	final int NUM_ITERATIONS = 100;
	

	@Override
	public double[] train(Model model) {
		double[] theta = Arrays.copyOf(model.getInitialParameters(), model.getInitialParameters().length);
		
		int len = theta.length;
		double ak;
		double ck;
		double yplus;
		double yminus;
		double[] delta;
		double[] thetaPlus = new double[len];
		double[] thetaMinus = new double[len];
		double[] ghat = new double[len];
		
		for(int k = 1; k <= NUM_ITERATIONS; k++) {
			ak = a/Math.pow(k+A, alpha);
			ck = c/Math.pow(k, gamma);
			
			delta = bernoulli(len);
			
			for(int i = 0; i < len; i++)
				thetaPlus[i] = theta[i] + ck*delta[i];
			for(int i = 0; i < len; i++)
				thetaPlus[i] = theta[i] - ck*delta[i];
			
			yplus = model.loss(thetaPlus);
			yminus = model.loss(thetaMinus);
			
			for(int i = 0; i < len; i++)
				ghat[i] = (yplus-yminus)/(2*ck*delta[i]);
			
			for(int i = 0; i < len; i++)
				theta[i] = theta[i] + ak*ghat[i];
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
}

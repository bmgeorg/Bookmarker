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
	final int NUM_ITERATIONS = 10000;

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
				thetaMinus[i] = theta[i] - ck*delta[i];

			yplus = model.loss(thetaPlus);
			yminus = model.loss(thetaMinus);

			for(int i = 0; i < len; i++)
				ghat[i] = (yplus-yminus)/(2*ck*delta[i]);

			//print state for testing
			System.out.println("Iteration " + k);
			System.out.println();
			System.out.println("theta:");
			for(int i = 0; i < len; i++) {
				System.out.println(i + ": " + theta[i]);
			}
			System.out.println("Loss: " + model.loss(theta));
			System.out.println("ck: " + ck);
			System.out.println("delta:");
			for(int i = 0; i < len; i++) {
				System.out.println(i + ": " + delta[i]);
			}
			System.out.println("thetaPlus:");
			for(int i = 0; i < len; i++) {
				System.out.println(i + ": " + thetaPlus[i]);
			}
			System.out.println("yplus: " + yplus);
			System.out.println("thetaMinus:");
			for(int i = 0; i < len; i++) {
				System.out.println(i + ": " + thetaMinus[i]);
			}
			System.out.println("yminus: " + yminus);

			System.out.println("ak: " + ak);
			System.out.println("ghat:");
			for(int i = 0; i < len; i++) {
				System.out.println(i + ": " + ghat[i]);
			}
			System.out.println();
			System.out.println();
			//end testing

			for(int i = 0; i < len; i++)
				theta[i] = theta[i] - ak*ghat[i];

		}

		return theta;
	}

	//bernoulli +-1 distribution
	public double[] bernoulli(int length) {
		double[] result = new double[length];

		Random rand = new Random();
		for(int i = 0; i < length; i++)
			result[i] = rand.nextInt(2)*2-1;

		return result;
	}
	public static void main(String args[]) {
		SPSA trainer = new SPSA();
		trainer.train(new Linear());
	}
}

//for testing
class Linear implements Model {

	@Override
	public double[] getInitialParameters() {
		double[] params = new double[2];
		params[0] = 10;
		params[1] = -300;
		return params;
	}

	@Override
	public double loss(double[] parameters) {
		return parameters[0] - parameters[1];
	}

}
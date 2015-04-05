package train;

public interface Model {
	double[] getInitialParameters();
	double loss(double[] parameters);
}
package model;

import java.io.Serializable;

//immutable
public class Tag implements Serializable {
	private static final long serialVersionUID = 8066132246215870812L;
	private String term;
	private double weight;
	public Tag(String term, double weight) {
		assert term != null;
		this.term = term;
		this.weight = weight;
	}
	
	public String getTerm() {
		return term;
	}
	public double getWeight() {
		return weight;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((term == null) ? 0 : term.hashCode());
		long temp;
		temp = Double.doubleToLongBits(weight);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Tag other = (Tag) obj;
		//compare only on term, not weight
		return term.equals(other.term);
	}
}

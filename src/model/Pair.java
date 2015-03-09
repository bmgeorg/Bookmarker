package model;

public class Pair<K, V> {
	private final K left;
	private final V right;

	public static <K, V> Pair<K, V> createPair(K left, V right) {
		return new Pair<K, V>(left, right);
	}

	public Pair(K left, V right) {
		assert(left != null);
		assert(right != null);
		this.left = left;
		this.right = right;
	}

	public K getLeft() {
		return left;
	}

	public V getRight() {
		return right;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((left == null) ? 0 : left.hashCode());
		result = prime * result + ((right == null) ? 0 : right.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj)
			return true;
		if(obj == null)
			return false;
		if(!(obj instanceof Pair))
			return false;
		@SuppressWarnings("unchecked")
		Pair<K, V> other = (Pair<K, V>) obj;
		if(!left.equals(other.left))
			return false;
		if(!right.equals(other.right))
			return false;
		return true;
	}
}
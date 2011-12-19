package jpdftweak.core;

/**
 * A wrapper around an <code>int[]</code> that provides validation and
 * conversion to/from a {@link String}.
 */
public class IntegerList {
	private final int[] value;

	public IntegerList(String string) {
		String[] parts = string.split(",", -1);
		if (parts.length == 0)
			throw new NumberFormatException("Value missing");
		value = new int[parts.length];
		for (int i = 0; i < parts.length; i++) {
			value[i] = Integer.parseInt(parts[i]);
			if (value[i] < 0)
				throw new NumberFormatException("Value may not be negative: " + value[i]);
		}
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < value.length; i++) {
			if (i != 0)
				sb.append(',');
			sb.append(value[i]);
		}
		return sb.toString();
	}

	public int[] getValue() {
		return value;
	}
}

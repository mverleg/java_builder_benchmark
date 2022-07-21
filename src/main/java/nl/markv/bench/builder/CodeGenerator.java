package nl.markv.bench.builder;

public class CodeGenerator {

	static class Type {
		final String typeName;
		final String exampleValue;
		final String annotation;

		public Type(String typeName, String exampleValue, String annotation) {
			this.typeName = typeName;
			this.exampleValue = exampleValue;
			this.annotation = annotation;
		}
	}
	static Type[] TYPES = new Type[]{
			new Type("int", "2", "@Nonnull"),
			new Type("Short", "1", "@Nonnull"),
			new Type("String", "\"hello\"", "@Nonnull"),
			new Type("String", null, "@Nullable"),
			new Type("double", "3.14e0", "@Nonnull"),
			new Type("Long", "Long.MAX_VALUE", "@Nullable"),
			new Type("CharSequence", "null", "@Nullable"),
	};

	public static void main(String[] args) {
		System.out.println("Hello World!");
	}

	void generateDataClass(String className, int fieldCount, int seed) {
		var src = new StringBuilder();
	}

	void generateBuilder() {

	}
}

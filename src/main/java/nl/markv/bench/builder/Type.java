package nl.markv.bench.builder;

import javax.annotation.Nullable;

class Type {
	final String name;
	final String value;
	final @Nullable String annotation;

	static Type[] TYPES = new Type[]{
			new Type("int", "2", null),
			new Type("Short", "1", "@Nonnull"),
			new Type("String", "\"hello\"", "@Nonnull"),
			new Type("String", null, null),
			new Type("double", "3.14e0", null),
			new Type("Long", "Long.MAX_VALUE", "@Nullable"),
			new Type("CharSequence", "null", "@Nullable"),
			new Type("LocalDateTime", "LocalDateTime.of(2022, 7, 22, 19, 0, 9)", "@Nullable"),
			new Type("BigDecimal", "BigDecimal.TEN", "@Nonnull"),
			new Type("int[]", "new int[]{1, 2, 3}", "@Nullable"),
			new Type("List<Float>", "Arrays.asList(1f, 2f, 3f)", "@Nonnull"),
	};


	Type(String name, String value, @Nullable String annotation) {
		this.name = name;
		this.value = value;
		this.annotation = annotation;
	}
}

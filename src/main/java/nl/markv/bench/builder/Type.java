package nl.markv.bench.builder;

import javax.annotation.Nullable;

class Type {

	enum Nullability {
		Nullable,
		Nonnull,
		Unspecified,
		;

		@Override
		public String toString() {
			return switch(this) {
				case Nullable -> "@Nullable ";
				case Nonnull -> "@Nonnull ";
				case Unspecified -> "";
			};
		}
	}

	final String name;
	final String value;
	final Nullability nullability;
	final @Nullable String defaultVal;

	static Type[] TYPES = new Type[]{
			new Type("int", "2", Nullability.Unspecified, "0"),
			new Type("Short", "(short) 1", Nullability.Nonnull, "(short) 0"),
			new Type("String", "\"hello\"", Nullability.Nonnull, "\"\""),
			new Type("String", null, Nullability.Unspecified, null),
			new Type("double", "3.14e0", Nullability.Unspecified, null),
			new Type("Long", "Long.MAX_VALUE", Nullability.Nullable, "null"),
			new Type("CharSequence", "null", Nullability.Nullable, "null"),
			new Type("LocalDateTime", "LocalDateTime.of(2022, 7, 22, 19, 0, 9)", Nullability.Unspecified, null),
			new Type("BigDecimal", "BigDecimal.TEN", Nullability.Nonnull, null),
			new Type("int[]", "new int[]{1, 2, 3}", Nullability.Nullable, "null"),
			new Type("List<Float>", "Arrays.asList(1f, 2f, 3f)", Nullability.Nonnull, "new ArrayList<>()"),
	};

	Type(String name, String value, Nullability nullability, @Nullable String defaultVal) {
		this.name = name;
		this.value = value;
		this.nullability = nullability;
		this.defaultVal = defaultVal;
	}

	boolean isRequired() {
		return this.nullability != Nullability.Nullable && this.defaultVal == null;
	}
}

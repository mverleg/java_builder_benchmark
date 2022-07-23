package nl.markv.bench.builder;

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
	final boolean hasDefault;

	static Type[] TYPES = new Type[]{
			new Type("int", "2", Nullability.Unspecified, false),
			new Type("Short", "1", Nullability.Nonnull, false),
			new Type("String", "\"hello\"", Nullability.Nonnull, false),
			new Type("String", null, Nullability.Unspecified, false),
			new Type("double", "3.14e0", Nullability.Unspecified, false),
			new Type("Long", "Long.MAX_VALUE", Nullability.Nullable, false),
			new Type("CharSequence", "null", Nullability.Nullable, false),
			new Type("LocalDateTime", "LocalDateTime.of(2022, 7, 22, 19, 0, 9)", Nullability.Unspecified, false),
			new Type("BigDecimal", "BigDecimal.TEN", Nullability.Nonnull, false),
			new Type("int[]", "new int[]{1, 2, 3}", Nullability.Nullable, true),
			new Type("List<Float>", "Arrays.asList(1f, 2f, 3f)", Nullability.Nonnull, true),
	};


	Type(String name, String value, Nullability nullability, boolean hasDefault) {
		this.name = name;
		this.value = value;
		this.nullability = nullability;
		this.hasDefault = hasDefault;
	}

	boolean isRequired() {
		return this.nullability != Nullability.Nullable && !this.hasDefault;
	}
}

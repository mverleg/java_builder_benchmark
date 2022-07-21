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

	enum Mode {
		ConstructorOnly,
		HardCodedBuilder,
		ImmutableFlexibleBuilder,
		ImmutableStagedBuilder,
		;

		boolean isInterface() {
			return switch (this) {
				case ConstructorOnly -> false;
				case HardCodedBuilder -> false;
				case ImmutableFlexibleBuilder -> true;
				case ImmutableStagedBuilder -> true;
			};
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
		var gen = new CodeGenerator();
		System.out.println(gen.generateDataClass(Mode.ConstructorOnly, "TestData", 8, 2));
		System.out.println(gen.generateDataClass(Mode.ImmutableStagedBuilder, "TestData", 8, 4));
	}

	CharSequence generateDataClass(Mode mode, String className, int fieldCount, int seed) {
		var src = new StringBuilder()
				.append("public ")
				.append(mode.isInterface() ? "interface " : "final class ")
				.append(className)
				.append(" {\n");
		if (!mode.isInterface()) {
			for (int i = 0; i < fieldCount; i++) {
				var type = TYPES[(seed + i) % TYPES.length];
				src.append("\tprivate final ")
						.append(type.annotation)
						.append(' ')
						.append(type.typeName)
						.append(' ')
						.append(makeName(type.typeName, i))
						.append(";\n");
			}
		}
		for (int i = 0; i < fieldCount; i++) {
			var type = TYPES[(seed + i) % TYPES.length];
			var fieldName = makeName(type.typeName, i);
			src.append("\n\t")
					.append(mode.isInterface() ? "" : "public ")
					.append(type.annotation)
					.append(' ')
					.append(type.typeName)
					.append(" get")
					.append(fieldName)
					.append("()");
			if (mode.isInterface()) {
				src.append(';');
			} else {
				src.append(" {\n\t\treturn this.")
						.append(fieldName)
						.append(";\n\t}\n");
			}
			src.append("\n");
		}
		src.append("}\n");
		return src;
	}

	void generateBuilder() {

	}

	CharSequence makeName(String type, int i) {
		return type.toLowerCase() + "Field" + (i + 1);
	}
}

package nl.markv.bench.builder;

import javax.annotation.Nullable;

public class CodeGenerator {

	static class Type {
		final String typeName;
		final String exampleValue;
		final @Nullable String annotation;

		public Type(String typeName, String exampleValue, @Nullable String annotation) {
			this.typeName = typeName;
			this.exampleValue = exampleValue;
			this.annotation = annotation;
		}

		CharSequence annotatedType() {
			return annotation == null ? typeName : annotation + ' ' + typeName;
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
			new Type("int", "2", null),
			new Type("Short", "1", "@Nonnull"),
			new Type("String", "\"hello\"", "@Nonnull"),
			new Type("String", null, null),
			new Type("double", "3.14e0", null),
			new Type("Long", "Long.MAX_VALUE", "@Nullable"),
			new Type("CharSequence", "null", "@Nullable"),
	};

	public static void main(String[] args) {
		var gen = new CodeGenerator();
		System.out.println(gen.generateDataClass(Mode.ConstructorOnly, "TestData", 8, 2));
		System.out.println(gen.generateDataClass(Mode.ImmutableStagedBuilder, "TestImmutable", 6, 4));
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
						.append(type.annotatedType())
						.append(' ')
						.append(makeFieldName(type.typeName, i))
						.append(";\n");
			}
		}
		for (int i = 0; i < fieldCount; i++) {
			var type = TYPES[(seed + i) % TYPES.length];
			src.append("\n\t")
					.append(mode.isInterface() ? "" : "public ")
					.append(type.annotatedType())
					.append(' ')
					.append(makeGetterName(type.typeName, i))
					.append("()");
			if (mode.isInterface()) {
				src.append(";\n");
			} else {
				src.append(" {\n\t\treturn this.")
						.append(makeFieldName(type.typeName, i))
						.append(";\n\t}\n");
			}
		}
		src.append("}\n");
		return src;
	}

	void generateBuilder() {

	}

	CharSequence makeFieldName(String type, int i) {
		return type.toLowerCase() + "Field" + (i + 1);
	}

	CharSequence makeGetterName(String type, int i) {
		return ("boolean".equals(type) ? "is" : "get") +
				type.substring(0, 1).toUpperCase() + type.substring(1) +
				"Field" + (i + 1);
	}
}

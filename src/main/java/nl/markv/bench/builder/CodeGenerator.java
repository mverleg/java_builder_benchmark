package nl.markv.bench.builder;

import java.util.List;
import java.util.stream.IntStream;

import javax.annotation.Nullable;

public class CodeGenerator {

	static class Type {
		final String name;
		final String value;
		final @Nullable String annotation;

		public Type(String name, String value, @Nullable String annotation) {
			this.name = name;
			this.value = value;
			this.annotation = annotation;
		}
	}

	static class Field {
		final Type type;
		final int index;

		public Field(Type type, int index) {
			this.type = type;
			this.index = index;
		}

		CharSequence fieldName() {
			return type.name + "Field" + (index + 1);
		}

		CharSequence getterName() {
			return ("boolean".equals(type.name) ? "is" : "get") +
					type.name.substring(0, 1).toUpperCase() + type.name.substring(1) +
					"Field" + (index + 1);
		}

		CharSequence annotatedType() {
			return type.annotation == null ? type.name : type.annotation + ' ' + type.name;
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
		System.out.println(gen.generateDataClass(Mode.ImmutableStagedBuilder, "TestImmutable", 6, 4));
		System.out.println(gen.generateDataClass(Mode.ConstructorOnly, "TestData", 8, 2));
	}

	CharSequence generateDataClass(Mode mode, String className, int fieldCount, int seed) {
		var fields = IntStream.range(0, fieldCount)
				.mapToObj(i -> new Field(TYPES[(seed + i) % TYPES.length], i))
				.toList();
		var src = new StringBuilder()
				.append("public ")
				.append(mode.isInterface() ? "interface " : "final class ")
				.append(className)
				.append(" {\n");
		if (!mode.isInterface()) {
			src.append(generateFields(mode, fields));
		}
		if (!mode.isInterface()) {
			src.append(generateConstructor(className, mode, fields));
		}
		src.append(generateGetters(mode, fields));
		src.append("}\n");
		return src;
	}

		private CharSequence generateFields(Mode mode, List<Field> fields) {
		var src = new StringBuilder();
		for (var field : fields) {
			src.append("\tprivate final ")
					.append(field.annotatedType())
					.append(' ')
					.append(field.fieldName())
					.append(";\n");
		}
		return src;
	}

	private CharSequence generateConstructor(String className, Mode mode, List<Field> fields) {
		var src = new StringBuilder()
				.append("\tpublic ")
				.append(className)
				.append("(\n");
		for (var field : fields) {
			src.append("\t\t")
					.append(field.annotatedType())
					.append(' ')
					.append(field.fieldName())
					.append(",\n");
		}
		return src.append("}\n");
	}

	private CharSequence generateGetters(Mode mode, List<Field> fields) {
		var src = new StringBuilder();
		for (var field : fields) {
			src.append("\n\t")
					.append(mode.isInterface() ? "" : "public ")
					.append(field.annotatedType())
					.append(' ')
					.append(field.getterName())
					.append("()");
			if (mode.isInterface()) {
				src.append(";\n");
			} else {
				src.append(" {\n\t\treturn this.")
						.append(field.fieldName())
						.append(";\n\t}\n");
			}
		}
		return src;
	}
}

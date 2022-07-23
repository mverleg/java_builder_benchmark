package nl.markv.bench.builder;

import java.util.List;
import java.util.stream.IntStream;

class ClasGenerator {

	private final Mode mode;

	// var inst = ImmutableStagedBuilder7Impl.builder().int3(1).short4((short)1).string5("").string6("").double7(1d).build();
	//TODO @mark: ^

	ClasGenerator(Mode mode) {
		this.mode = mode;
	}

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

	CharSequence generateDataClass(Clas clas, int fieldCount, int seed) {
		var src = new StringBuilder();
		src.append(generateHeader(mode));
		src.append(generateTypeOpen(mode, clas));
		var fields = IntStream.range(0, fieldCount)
				.mapToObj(i -> new Field(TYPES[(seed + i) % TYPES.length], i))
				.toList();
		if (!mode.isInterface()) {
			src.append(generateFields(mode, fields));
		}
		if (mode.isInterface()) {
			src.append(generateBuilderForward(clas));
		} else {
			src.append(generateConstructor(clas, mode, fields));
		}
		src.append(generateGetters(mode, fields));
		src.append(generateTypeClose());
		return src;
	}

	CharSequence generateHeader(Mode mode) {
		var src = new StringBuilder()
				.append("package bench;\n\n")
				.append("import javax.annotation.Nonnull;\n")
				.append("import javax.annotation.Nullable;\n")
				.append("import java.time.LocalDateTime;\n")
				.append("import java.math.BigDecimal;\n")
				.append("import java.util.List;\n");
		if (mode.isInterface()) {
			src.append("import org.immutables.value.Value;\n");
		}
		return src.append('\n');
	}

	CharSequence generateTypeOpen(Mode mode, Clas clas) {
		var src = new StringBuilder();
		if (mode.isInterface()) {
			src.append("@Value.Immutable\n")
					.append("@Value.Style(");
		}
		if (mode == Mode.ImmutableStagedBuilder) {
			src.append("stagedBuilder = true, ");
		}
		if (mode.isInterface()) {
			src.append("typeImmutable = \"*Impl\", ")
					.append("typeBuilder = \"*Builder\", ")
					.append("passAnnotations = {Nullable.class, Nonnull.class})\n");
		}
		return src.append("public ")
				.append(mode.isInterface() ? "interface " : "final class ")
				.append(clas.name)
				.append(" {\n");
	}

	CharSequence generateTypeClose() {
		return "}\n";
	}

	CharSequence generateFields(Mode mode, List<Field> fields) {
		var src = new StringBuilder();
		for (var field : fields) {
			src.append("\tprivate final ")
					.append(field.annotatedType())
					.append(' ')
					.append(field.fieldName())
					.append(";\n");
		}
		return src.append('\n');
	}

	CharSequence generateBuilderForward(Clas clas) {
		return new StringBuilder("\t")
				.append("\tpublic static ")
				.append(clas.builderName())
				.append(" builder() {\n\t\treturn ")
				.append(clas.implName())
				.append(".builder();\n\t}\n\n");
	}

	CharSequence generateConstructor(Clas clas, Mode mode, List<Field> fields) {
		var src = new StringBuilder("\t")
				.append(mode == Mode.HardCodedBuilder ? "private" : "public")
				.append(' ')
				.append(clas.name)
				.append("(\n\t\t\t");
		boolean isFirst = true;
		for (var field : fields) {
			if (isFirst) {
				isFirst = false;
			} else {
				src.append(",\n\t\t\t");
			}
			src.append(field.annotatedType())
					.append(' ')
					.append(field.fieldName());
		}
		src.append(") {\n");
		for (var field : fields) {
			src.append("\t\tthis.")
					.append(field.fieldName())
					.append(" = ")
					.append(field.fieldName())
					.append(";\n");
		}
		return src.append("\t}\n");
	}

	CharSequence generateGetters(Mode mode, List<Field> fields) {
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

package nl.markv.bench.builder;

import java.util.List;
import java.util.stream.IntStream;

import static nl.markv.bench.builder.Type.TYPES;

class ClasGenerator {

	private final Mode mode;

	ClasGenerator(Mode mode) {
		this.mode = mode;
	}

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
		if (mode == Mode.ImmutableFlexibleBuilder) {
			src.append(generateFlexibleBuilderForward(clas));
		} else if (mode == Mode.ImmutableStagedBuilder) {
			src.append(generateStagedBuilderForward(clas, firstRequiredField(clas, fields)));
		} else {
			src.append(generateConstructor(clas, mode, fields));
		}
		src.append(generateGetters(mode, fields));
		src.append(generateTypeClose());
		return src;
	}

	private Field firstRequiredField(Clas clas, List<Field> fields) {
		for (var field : fields) {
			if (field.type.isRequired()) {
				return field;
			}
		}
		System.err.println("no required fields for " + clas.typeName() + ", falling back to first field");
		return fields.get(0);
	}

	CharSequence generateHeader(Mode mode) {
		var src = new StringBuilder()
				.append("package bench.data;\n\n")
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
					//.append("packageGenerated = \"bench.gen\", ")
					.append("typeBuilder = \"*Builder\", ")
					.append("visibility = Value.Style.ImplementationVisibility.PACKAGE, ")
					.append("passAnnotations = {Nullable.class, Nonnull.class})\n");
		}
		return src.append("public ")
				.append(mode.isInterface() ? "interface " : "final class ")
				.append(clas.typeName())
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

	CharSequence generateFlexibleBuilderForward(Clas clas) {
		return new StringBuilder("\tstatic ")
				.append(clas.implName())
				.append('.')
				.append(clas.builderName())
				.append(" builder() {\n\t\treturn ")
				.append(clas.implName())
				.append(".builder();\n\t}\n");
	}

	CharSequence generateStagedBuilderForward(Clas clas, Field field) {
		return new StringBuilder("\tstatic ")
				.append(clas.implName())
				.append('.')
				.append(field.baseName)
				.append("BuildStage")
				.append(" builder() {\n\t\treturn ")
				.append(clas.implName())
				.append(".builder();\n\t}\n");
	}

	CharSequence generateConstructor(Clas clas, Mode mode, List<Field> fields) {
		var src = new StringBuilder("\t")
				.append(mode == Mode.HardCodedBuilder ? "private" : "public")
				.append(' ')
				.append(clas.typeName())
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
			src.append("\t")
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

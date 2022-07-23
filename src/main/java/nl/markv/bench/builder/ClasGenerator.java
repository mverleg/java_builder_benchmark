package nl.markv.bench.builder;

import java.util.List;
import java.util.stream.IntStream;

import static nl.markv.bench.builder.GeneratorUtil.generateFullConstructor;
import static nl.markv.bench.builder.GeneratorUtil.generateFields;
import static nl.markv.bench.builder.Type.TYPES;

class ClasGenerator {

	private final Mode mode;

	ClasGenerator(Mode mode) {
		this.mode = mode;
	}

	CharSequence generateDataClass(Clas clas, int fieldCount, int seed) {
		var src = new StringBuilder();
		src.append(generateHeader());
		src.append(generateTypeOpen(clas));
		var fields = IntStream.range(0, fieldCount)
				.mapToObj(i -> new Field(TYPES[(seed + i) % TYPES.length], i))
				.toList();
		if (!mode.isInterface()) {
			src.append(generateFields("", fields, true));
			src.append(generateFullConstructor("", clas.typeName(), fields));
		}
		if (mode != Mode.ConstructorOnly) {
			src.append(generateBuilderForward(clas, fields));
		}
		src.append(generateGetters(fields));
		if (mode == Mode.HardCodedBuilder) {
			src.append(new BuilderGenerator("\t").generateBuilderClass(clas, fields));
		}
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

	CharSequence generateHeader() {
		var src = new StringBuilder()
				.append("package bench.data;\n\n")
				.append("import javax.annotation.Nonnull;\n")
				.append("import javax.annotation.Nullable;\n")
				.append("import java.time.LocalDateTime;\n")
				.append("import java.math.BigDecimal;\n")
				.append("import java.util.List;\n")
				.append("import java.util.Arrays;\n");
		if (mode.isInterface()) {
			src.append("import org.immutables.value.Value;\n");
		}
		return src.append('\n');
	}

	CharSequence generateTypeOpen(Clas clas) {
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

	CharSequence generateBuilderForward(Clas clas, List<Field> fields) {
		firstRequiredField(clas, fields);
		assert mode != Mode.ConstructorOnly;
		var builderType = switch (mode) {
			case HardCodedBuilder -> clas.builderName();
			case ImmutableFlexibleBuilder -> clas.implName() + "." + clas.builderName();
			case ImmutableStagedBuilder -> clas.implName() + "." + firstRequiredField(clas, fields).baseName + "BuildStage";
			default -> "NO BUILDER TYPE FOR CONSTRUCTOR-ONLY";
		};
		String createBuilder;
		if (mode.isInterface()) {
			createBuilder = clas.implName() + ".builder()";
		} else {
			createBuilder = "new " + clas.builderName() + "()";
		}
		return new StringBuilder("\tstatic ")
				.append(builderType)
				.append(" builder() {\n\t\treturn ")
				.append(createBuilder)
				.append(";\n\t}\n");
	}

	CharSequence generateGetters(List<Field> fields) {
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

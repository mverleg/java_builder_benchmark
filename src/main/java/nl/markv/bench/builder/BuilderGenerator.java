package nl.markv.bench.builder;

import java.util.List;

import static nl.markv.bench.builder.GeneratorUtil.generateFullConstructor;
import static nl.markv.bench.builder.GeneratorUtil.generateFields;

class BuilderGenerator {

	final String indent;

	BuilderGenerator(String indent) {
		this.indent = indent;
	}

	CharSequence generateBuilderClass(Clas clas, List<Field> fields) {
		var src = new StringBuilder();
		src.append(generateTypeOpen(clas));
		src.append(generateFields(indent, fields, false));
		src.append(generateFullConstructor(indent, clas.builderName(), fields));
		src.append(generateNoArgConstructor(clas, fields));
//		src.append(generateSetters(fields));
//		src.append(generateBuildMethod(clas, fields));
		src.append(generateTypeClose());
		return src;
	}

	CharSequence generateNoArgConstructor(Clas clas, List<Field> fields) {
		var src = new StringBuilder(indent)
				.append('\t')
				.append(clas.builderName())
				.append("() {\n\t\t")
				.append(indent)
				.append("this(");
		var separator = "";
		for (var field : fields) {
			src.append(separator)
					.append("\n\t\t\t")
					.append(indent)
					.append("/*")
					.append(field.fieldName())
					.append("*/ ")
					.append(field.type.value);
			separator = ",";
		}
		return src.append('\n')
				.append(indent)
				.append("\t\t);\n")
				.append(indent)
				.append("\t}\n\n");
	}


	CharSequence generateTypeOpen(Clas clas) {
		return new StringBuilder("\n")
				.append(indent)
				.append("private static class ")
				.append(clas.builderName())
				.append(" {\n");
	}

	CharSequence generateTypeClose() {
		return indent + "}\n";
	}

	CharSequence generateSetters(List<Field> fields) {
		var src = new StringBuilder();
		for (var field : fields) {
			src.append("\t")
					.append("public ")
					.append(field.annotatedType())
					.append(' ')
					.append(field.getterName())
					.append("()")
					.append(" {\n\t\treturn this.")
					.append(field.fieldName())
					.append(";\n\t}\n");
		}
		return src;
	}

	CharSequence generateBuildMethod(Clas clas, List<Field> fields) {
		var src = new StringBuilder("\t")
				.append("public")
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
}

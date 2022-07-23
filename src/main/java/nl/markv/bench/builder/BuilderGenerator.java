package nl.markv.bench.builder;

import java.util.List;

class BuilderGenerator {

	final String indent;

	BuilderGenerator(String indent) {
		this.indent = indent;
	}

	CharSequence generateBuilderClass(Clas clas, List<Field> fields) {
		var src = new StringBuilder();
		src.append(generateTypeOpen(clas));
		src.append(generateConstructor(clas, fields));
//		src.append(generateSetters(fields));
//		src.append(generateBuildMethod(clas, fields));
		src.append(generateTypeClose());
		return src;
	}

	CharSequence generateTypeOpen(Clas clas) {
		return new StringBuilder("\n")
				.append(indent)
				.append("private static class ")
				.append(clas.typeName())
				.append(" {\n");
	}

	CharSequence generateTypeClose() {
		return indent + "}\n";
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

	CharSequence generateConstructor(Clas clas, List<Field> fields) {
		var src = new StringBuilder(indent)
				.append('\t')
				.append(clas.typeName())
				.append("(\n\t\t")
				.append(indent);
		boolean isFirst = true;
		for (var field : fields) {
			if (isFirst) {
				isFirst = false;
			} else {
				src.append(",\n\t\t\t")
						.append(indent);
			}
			src.append(field.annotatedType())
					.append(' ')
					.append(field.fieldName());
		}
		src.append(") {\n");
		for (var field : fields) {
			src.append(indent)
					.append("\t\tthis.")
					.append(field.fieldName())
					.append(" = ")
					.append(field.fieldName())
					.append(";\n");
		}
		return src.append(indent)
				.append("\t}\n");
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

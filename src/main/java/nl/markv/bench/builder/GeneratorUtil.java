package nl.markv.bench.builder;

import java.util.List;

enum GeneratorUtil {
	;

	static CharSequence generateFields(String indent, List<Field> fields, boolean isFinal) {
		var src = new StringBuilder();
		for (var field : fields) {
			src.append(indent)
					.append("\tprivate ")
					.append(isFinal ? "final " : "")
					.append(field.annotatedType())
					.append(' ')
					.append(field.fieldName())
					.append(";\n");
		}
		return src.append('\n');
	}

	static CharSequence generateFullConstructor(String indent, CharSequence className, List<Field> fields) {
		var src = new StringBuilder(indent)
				.append('\t')
				.append(className)
				.append("(\n\t\t\t")
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
				.append("\t}\n\n");
	}

}

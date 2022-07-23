package nl.markv.bench.builder;

import java.util.List;
import java.util.stream.IntStream;

import static nl.markv.bench.builder.Type.TYPES;

enum GeneratorUtil {
	;

	static CharSequence generateFields(String indent, List<Field> fields) {
		var src = new StringBuilder();
		for (var field : fields) {
			src.append(indent)
					.append("\tprivate final ")
					.append(field.annotatedType())
					.append(' ')
					.append(field.fieldName())
					.append(";\n");
		}
		return src.append('\n');
	}

	static CharSequence generateConstructor(String indent, CharSequence className, List<Field> fields) {
		var src = new StringBuilder(indent)
				.append('\t')
				.append(className)
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

}

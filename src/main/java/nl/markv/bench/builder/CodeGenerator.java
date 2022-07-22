package nl.markv.bench.builder;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.nio.file.Paths;
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
			return type.name.toLowerCase() + "Field" + (index + 1);
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

	private final Mode mode;

	public CodeGenerator(Mode mode) {
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
	};

	public static void main(String[] args) {
		int N = 2_000;
		saveGeneratedFiles(new CodeGenerator(Mode.ConstructorOnly), N);
		saveGeneratedFiles(new CodeGenerator(Mode.HardCodedBuilder), N);
		saveGeneratedFiles(new CodeGenerator(Mode.ImmutableFlexibleBuilder), N);
		saveGeneratedFiles(new CodeGenerator(Mode.ImmutableStagedBuilder), N);
	}

	public static void saveGeneratedFiles(CodeGenerator gen, int fileCount) {
		System.out.print(gen.mode.name());
		var dir = Paths.get("/tmp", "generated", gen.mode.name().toLowerCase(), "src", "main", "test");
		dir.toFile().mkdirs();
		for (int seed = 0; seed < fileCount; seed++) {
			if (seed % 1000 == 0) {
				System.out.print('.');
			}
			var fieldCount = 1 + (seed % 99);
			var clsName = gen.mode.name() + seed;
			var txt = gen.generateDataClass(clsName, fieldCount, 2 + seed);
			var pth = Paths.get(dir.toString(), clsName + ".java").toFile();
			try (PrintStream printer = new PrintStream(pth)) {
				printer.println(txt);
			} catch (FileNotFoundException ex) {
				throw new RuntimeException(ex);
			}
		}
		System.out.println(" done");
	}

	CharSequence generateDataClass(String className, int fieldCount, int seed) {
		var src = new StringBuilder();
		src.append(generateHeader(mode));
		src.append(generateTypeOpen(mode, className));
		var fields = IntStream.range(0, fieldCount)
				.mapToObj(i -> new Field(TYPES[(seed + i) % TYPES.length], i))
				.toList();
		if (!mode.isInterface()) {
			src.append(generateFields(mode, fields));
		}
		if (!mode.isInterface()) {
			src.append(generateConstructor(className, mode, fields));
		}
		src.append(generateGetters(mode, fields));
		src.append(generateTypeClose());
		return src;
	}

	CharSequence generateHeader(Mode mode) {
		var src = new StringBuilder()
				.append("package test;\n\n")
				.append("import javax.annotation.Nonnull;\n")
				.append("import javax.annotation.Nullable;\n");
		if (mode.isInterface()) {
			src.append("import org.immutables.value.Value;\n");
		}
		return src.append('\n');
	}

	CharSequence generateTypeOpen(Mode mode, String className) {
		var src = new StringBuilder();
		if (mode.isInterface()) {
			src.append("@Value.Immutable\n");
		}
		if (mode == Mode.ImmutableStagedBuilder) {
			src.append("@Value.Style(stagedBuilder = true)\n");
		}
		return src.append("public ")
				.append(mode.isInterface() ? "interface " : "final class ")
				.append(className)
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

	CharSequence generateConstructor(String className, Mode mode, List<Field> fields) {
		var src = new StringBuilder("\t")
				.append(mode == Mode.HardCodedBuilder ? "private" : "public")
				.append(' ')
				.append(className)
				.append("(");
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

	private static void check(boolean isTrue) {
		if (!isTrue) {
			throw new AssertionError();
		}
	}
}

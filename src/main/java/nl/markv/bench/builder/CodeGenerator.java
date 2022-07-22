package nl.markv.bench.builder;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.IntStream;

import javax.annotation.Nullable;

import static java.lang.System.exit;

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
		final String baseName;

		public Field(Type type, int index) {
			this.type = type;
			this.index = index;
			var name = type.name.replace("[]", "Array").replaceAll("([a-zA-Z]+)<([a-zA-Z]+)>", "$2$1") + (index + 1);
			this.baseName = name.substring(0, 1).toUpperCase() + name.substring(1);
		}

		CharSequence fieldName() {
			return "a" + this.baseName;
		}

		CharSequence getterName() {
			return ("boolean".equals(type.name) ? "is" : "get") + this.baseName;
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
			new Type("LocalDateTime", "LocalDateTime.of(2022, 7, 22, 19, 0, 9)", "@Nullable"),
			new Type("BigDecimal", "BigDecimal.TEN", "@Nonnull"),
			new Type("int[]", "new int[]{1, 2, 3}", "@Nullable"),
			new Type("List<Float>", "Arrays.asList(1f, 2f, 3f)", "@Nonnull"),
	};

	public static void saveGeneratedFiles(CodeGenerator gen, File outPth, int fileCount) {
		System.out.print(gen.mode.name());
		var dir = Paths.get(outPth.getAbsolutePath(), gen.mode.name().toLowerCase(), "src", "main", "java", "bench");
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

	public static void main(String[] args) {
		if (args.length < 2) {
			System.err.println("provide two arguments: 1) output path 2) number of files");
			exit(1);
		}
		var outPth = new File(args[0]);
		int N = 1;
		try {
			N = Integer.parseInt(args[1]);
		} catch (NumberFormatException ex) {
			System.err.println("second argument should be a valid, positive number");
			exit(1);
		}
		System.out.println("generating " + N + " files in '" + outPth + "' for " + Mode.values().length + " generators");
		for (Mode mode : Mode.values()) {
			saveGeneratedFiles(new CodeGenerator(mode), outPth, N);
		}
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

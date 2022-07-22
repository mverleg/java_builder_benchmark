package nl.markv.bench.builder;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.IntStream;

import javax.annotation.Nullable;

import static java.lang.System.exit;

public class MainGenerator {

	static class Clas {
		final String name;

		public Clas(String name) {
			this.name = name;
		}

		CharSequence typeName() {
			return this.name;
		}

		CharSequence implName() {
			return this.name + "Impl";
		}

		CharSequence builderName() {
			return this.name + "Builder";
		}
	}

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

	// var inst = ImmutableStagedBuilder7Impl.builder().int3(1).short4((short)1).string5("").string6("").double7(1d).build();
	//TODO @mark: ^

	public MainGenerator(Mode mode) {
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

	public static void saveGeneratedFiles(MainGenerator gen, File outPth, int fileCount) {
		System.out.print(gen.mode.name());
		var dir = Paths.get(outPth.getAbsolutePath(), gen.mode.name().toLowerCase(), "src", "main", "java", "bench");
		dir.toFile().mkdirs();
		for (int seed = 0; seed < fileCount; seed++) {
			if (seed % 1000 == 0) {
				System.out.print('.');
			}
			var fieldCount = 1 + (seed % 99);
			var clas = new Clas(gen.mode.name() + seed);
			var txt = gen.generateDataClass(clas, fieldCount, 2 + seed);
			var pth = Paths.get(dir.toString(), clas.typeName() + ".java").toFile();
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
			saveGeneratedFiles(new MainGenerator(mode), outPth, N);
		}
	}
}

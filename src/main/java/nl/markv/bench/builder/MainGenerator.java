package nl.markv.bench.builder;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.nio.file.Paths;

import static java.lang.System.exit;

class MainGenerator {

	private final Mode mode;

	// var inst = ImmutableStagedBuilder7Impl.builder().int3(1).short4((short)1).string5("").string6("").double7(1d).build();
	//TODO @mark: ^

	MainGenerator(Mode mode) {
		this.mode = mode;
	}

	static void saveGeneratedFiles(MainGenerator gen, File outPth, int fileCount) {
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

	CharSequence generateDataClass(Clas clas, int fieldCount, int seed) {
		return new ClasGenerator(mode).generateDataClass(clas, fieldCount, seed);
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

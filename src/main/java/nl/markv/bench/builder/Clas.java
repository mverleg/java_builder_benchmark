package nl.markv.bench.builder;

class Clas {
	final String name;

	Clas(String name) {
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

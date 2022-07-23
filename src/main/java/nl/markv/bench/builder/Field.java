package nl.markv.bench.builder;

class Field {
	final Type type;
	final int index;
	final String baseName;

	Field(Type type, int index) {
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

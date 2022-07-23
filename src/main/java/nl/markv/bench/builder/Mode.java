package nl.markv.bench.builder;

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

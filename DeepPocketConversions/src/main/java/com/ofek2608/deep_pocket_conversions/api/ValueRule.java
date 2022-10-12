package com.ofek2608.deep_pocket_conversions.api;

import com.ofek2608.deep_pocket_conversions.impl.ValueRuleParser;

import java.util.List;
import java.util.stream.IntStream;

public abstract sealed class ValueRule {
	public static ValueRule sum(ValueRule ... elements) { return elements.length == 1 ? elements[0] : new RSum(elements); }
	public static ValueRule min(ValueRule ... elements) { return elements.length == 1 ? elements[0] : new RMin(elements); }
	public static ValueRule item(int index) { return new RItem(index); }
	public static ValueRule multiply(ValueRule element, ProcessingNum num) { return num == ProcessingNum.ONE ? element : new RMultiply(element, num); }
	public static ValueRule divide(ValueRule element, ProcessingNum num) { return num == ProcessingNum.ONE ? element : new RDivide(element, num); }

	public static ValueRule constant1() { return item(0); }
	public static ValueRule constant(ProcessingNum num) { return multiply(constant1(), num); }
	public static ValueRule constant0() { return constant(ProcessingNum.ZERO); }

	public static ValueRule parse(IMVCalculationCtx ctx, String code) throws ParsingException { return ValueRuleParser.parse(ctx, code); }

	public abstract ProcessingNum getValue(IMVCalculationCtx ctx);
	public abstract IntStream getDependencies();

	private static final class RSum extends ValueRule {
		private final List<ValueRule> elements;

		private RSum(ValueRule ... elements) {
			this.elements = List.of(elements);
		}

		@Override
		public ProcessingNum getValue(IMVCalculationCtx ctx) {
			return elements.stream()
							.map(element->element.getValue(ctx))
							.reduce(ProcessingNum.ZERO, ProcessingNum::add);
		}

		@Override
		public IntStream getDependencies() {
			return elements.stream().flatMapToInt(ValueRule::getDependencies);
		}
	}

	private static final class RMin extends ValueRule {
		private final List<ValueRule> elements;

		private RMin(ValueRule ... elements) {
			this.elements = List.of(elements);
		}

		@Override
		public ProcessingNum getValue(IMVCalculationCtx ctx) {
			return elements.stream()
							.map(element->element.getValue(ctx))
							.min(ProcessingNum::compareTo)
							.orElse(ProcessingNum.UNDEFINED);
		}

		@Override
		public IntStream getDependencies() {
			return elements.stream().flatMapToInt(ValueRule::getDependencies);
		}
	}

	private static final class RItem extends ValueRule {
		private final int index;

		private RItem(int index) {
			this.index = index;
		}

		@Override
		public ProcessingNum getValue(IMVCalculationCtx ctx) {
			return ctx.hasValue(index) ? ProcessingNum.valueOf(ctx.getValue(index)) : ProcessingNum.UNDEFINED;
		}

		@Override
		public IntStream getDependencies() {
			return IntStream.of(index);
		}
	}

	private static final class RMultiply extends ValueRule {
		private final ValueRule element;
		private final ProcessingNum num;

		private RMultiply(ValueRule element, ProcessingNum num) {
			this.element = element;
			this.num = num;
		}

		@Override
		public ProcessingNum getValue(IMVCalculationCtx ctx) {
			return element.getValue(ctx).multiply(num);
		}

		@Override
		public IntStream getDependencies() {
			return element.getDependencies();
		}
	}

	private static final class RDivide extends ValueRule {
		private final ValueRule element;
		private final ProcessingNum num;

		private RDivide(ValueRule element, ProcessingNum num) {
			this.element = element;
			this.num = num;
		}

		@Override
		public ProcessingNum getValue(IMVCalculationCtx ctx) {
			return element.getValue(ctx).divide(num);
		}

		@Override
		public IntStream getDependencies() {
			return element.getDependencies();
		}
	}
}

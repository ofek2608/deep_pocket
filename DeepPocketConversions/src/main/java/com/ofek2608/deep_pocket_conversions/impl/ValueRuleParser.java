package com.ofek2608.deep_pocket_conversions.impl;

import com.ofek2608.deep_pocket.api.struct.ItemType;
import com.ofek2608.deep_pocket_conversions.DPCUtils;
import com.ofek2608.deep_pocket_conversions.api.IMVCalculationCtx;
import com.ofek2608.deep_pocket_conversions.api.ParsingException;
import com.ofek2608.deep_pocket_conversions.api.ProcessingNum;
import com.ofek2608.deep_pocket_conversions.api.ValueRule;
import org.jetbrains.annotations.Nullable;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public final class ValueRuleParser {

	public static ValueRule parse(IMVCalculationCtx ctx, String code) throws ParsingException {
		return new ValueRuleParser(ctx, code).parseScope(false);
	}

	private ValueRuleParser(IMVCalculationCtx ctx, String code) {
		this.ctx = ctx;
		this.code = code.toCharArray();
	}

	private final IMVCalculationCtx ctx;
	private final char[] code;
	private int index;

	private void skipSpaces() {
		while (index < code.length && code[index] == ' ')
			index++;
	}

	private char next() {
		skipSpaces();
		return index < code.length ? code[index++] : '\0';
	}

	private char peek() {
		skipSpaces();
		return index < code.length ? code[index] : '\0';
	}

	private void skip() {
		skipSpaces();
		if (index < code.length)
			index++;
	}

	private int getIndex() {
		skipSpaces();
		return index;
	}

	private String getStringSince(int start) {
		return new String(code, start, index - start);
	}

	//operator is everything that comes after a number, item name, or variable name
	private boolean isOperator(char c) {
		return c == '+' || c == '*' || c == '/' || c == '|' || c == '{' || c == ')' || c == '\0';
	}

	private String readUntilOperator() {
		skipSpaces();
		int startIndex = getIndex();
		while (!isOperator(peek()))
			next();
		return getStringSince(startIndex);
	}

	private void skipNbt() throws ParsingException {
		Stack<Character> closing = new Stack<>();
		do {
			char c = next();
			switch (c) {
				case '\0' -> throw new ParsingException();
				case '(' -> closing.push(')');
				case '[' -> closing.push(']');
				case ')', ']' -> {
					if (!closing.pop().equals(c))
						throw new ParsingException();
				}
				case '\'', '\"' -> {
					char c2;
					while ((c2 = next()) != c) {
						switch (c2) {
							case '\0' -> throw new ParsingException();
							case '\\' -> next();
						}
					}
				}
			}
		} while (!closing.isEmpty());
	}


	private ValueRule parseScope(boolean withEndingBracket) throws ParsingException {
		List<ValueRule> collectMin = new ArrayList<>();
		List<ValueRule> collectSum = new ArrayList<>();

		boolean collectToMin;
		boolean continueLooping = true;
		while (continueLooping) {
			collectSum.add(parseSingle());
			switch (next()) {
				case '+' -> collectToMin = false;
				case '|' -> collectToMin = true;
				case ')' -> {
					if (!withEndingBracket)
						throw new ParsingException();
					collectToMin = true;
					continueLooping = false;
				}
				case '\0' -> {
					if (withEndingBracket)
						throw new ParsingException();
					collectToMin = true;
					continueLooping = false;
				}
				default -> throw new ParsingException();
			}
			if (collectToMin) {
				collectMin.add(ValueRule.sum(collectSum.toArray(ValueRule[]::new)));
				collectSum.clear();
			}
		}
		return ValueRule.min(collectMin.toArray(ValueRule[]::new));
	}

	private ValueRule parseSingle() throws ParsingException {
		ValueRule element = null;
		ProcessingNum multiply = ProcessingNum.ONE;
		ProcessingNum divide = ProcessingNum.ONE;

		boolean nextDivide = false;
		boolean hasNext = true;
		while (hasNext) {
			ProcessingNum num = tryParseNumber();
			if (num == null) {
				if (nextDivide)
					throw new ParsingException();
				if (element != null)
					throw new ParsingException();
				if (peek() == '(') {
					skip();
					element = parseScope(true);
				} else {
					element = parseItem();
				}
			} else {
				if (nextDivide)
					divide = divide.multiply(num);
				else
					multiply = multiply.multiply(num);
			}

			switch (peek()) {
				case '*' -> {
					nextDivide = false;
					skip();
				}
				case '/' -> {
					nextDivide = true;
					skip();
				}
				default -> hasNext = false;
			}
		}
		if (element == null)
			element = ValueRule.constant1();
		return ValueRule.divide(ValueRule.multiply(element, multiply), divide);
	}

	private ValueRule parseItem() throws ParsingException {
		int start = getIndex();
		readUntilOperator();
		if (peek() == '{')
			skipNbt();
		ItemType type = DPCUtils.parseItemType(getStringSince(start));
		if (type.isEmpty())
			throw new ParsingException();
		int index = ctx.getTypeIndex(type);
		if (index < 0)
			throw new ParsingException();
		return ValueRule.item(index);
	}

	private @Nullable ProcessingNum tryParseNumber() throws ParsingException {
		int startIndex = getIndex();

		char c = peek();
		if (c == '@') {
			next();
			return ProcessingNum.INFINITE;
		}
		if (c == '(') {
			next();
			ProcessingNum result = tryParseNumberScope();
			if (result == null)
				this.index = startIndex;
			return result;
		}
		if (c == '$') {
			next();
			String constName = readUntilOperator();
			if (!ctx.hasConst(constName))
				throw new ParsingException();
			return ProcessingNum.valueOf(ctx.getConst(constName));
		}
		String read = readUntilOperator();
		try {
			BigInteger parsed = new BigInteger(read, 10);
			if (parsed.signum() >= 0)
				return ProcessingNum.valueOf(parsed);
		} catch (Exception ignored) {}
		this.index = startIndex;
		return null;
	}

	private @Nullable ProcessingNum tryParseNumberScope() throws ParsingException {
		ProcessingNum min = ProcessingNum.UNDEFINED;
		ProcessingNum sum = ProcessingNum.ZERO;
		boolean hasNext = true;
		boolean endSum;
		while (hasNext) {
			ProcessingNum add = tryParseNumber();
			if (add == null)
				return null;
			sum = sum.add(add);
			switch (next()) {
				case '+' -> endSum = false;
				case '|' -> endSum = true;
				case ')' -> {
					endSum = true;
					hasNext = false;
				}
				default -> {
					return null;
				}
			}
			if (endSum) {
				if (sum.compareTo(min) < 0)
					min = sum;
				sum = ProcessingNum.ZERO;
			}
		}
		return min;
	}
}

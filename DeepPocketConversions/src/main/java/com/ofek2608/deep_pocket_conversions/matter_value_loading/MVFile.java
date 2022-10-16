package com.ofek2608.deep_pocket_conversions.matter_value_loading;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;

public final class MVFile {
	public final boolean clear;
	public final @UnmodifiableView List<String> blacklist;
	public final @UnmodifiableView Map<String,Long> constants;
	public final @UnmodifiableView List<Map<String,String>> values;

	public MVFile() {
		this.clear = false;
		this.blacklist = Collections.emptyList();
		this.constants = Collections.emptyMap();
		this.values = Collections.emptyList();
	}

	public MVFile(boolean clear, MVFile ... files) {
		// calculate which is the last mv file with clear
		int lastClear = files.length - 1;
		while (lastClear > 0 && !files[lastClear].clear)
			lastClear--;
		// clear
		this.clear = clear;
		// blackList
		this.blacklist = Stream.of(files).skip(lastClear).flatMap(file->file.blacklist.stream()).toList();
		// constants
		this.constants = streamToMap(Stream.of(files).skip(lastClear).flatMap(file->file.constants.entrySet().stream()));
		// values
		this.values = Stream.of(files).skip(lastClear).flatMap(file->file.values.stream()).toList();
	}

	public MVFile(JsonObject json) {
		// clear
		clear = json.get("clear") instanceof JsonPrimitive jsonClear && jsonClear.getAsBoolean();
		// blackList
		blacklist = json.get("blacklist") instanceof JsonArray jsonBlackList ?
						jsonArrayToList(jsonBlackList, JsonElement::getAsString) :
						Collections.emptyList();
		// constants
		constants = json.get("constants") instanceof JsonObject jsonConstants ?
						jsonObjectToMap(jsonConstants, JsonElement::getAsLong) :
						Collections.emptyMap();
		// values
		values = json.get("values") instanceof JsonArray jsonValues ?
						jsonArrayToList(jsonValues,element->jsonObjectToMap((JsonObject)element, JsonElement::getAsString)) :
						json.get("values") instanceof JsonObject jsonValues ?
										List.of(jsonObjectToMap(jsonValues, JsonElement::getAsString)) :
										Collections.emptyList();
	}

	public JsonObject save() {
		JsonObject json = new JsonObject();
		// clear
		json.addProperty("clear", clear);
		// blackList
		JsonArray jsonBlackList = new JsonArray();
		blacklist.forEach(jsonBlackList::add);
		json.add("blacklist", jsonBlackList);
		// constants
		JsonObject jsonConstants = new JsonObject();
		constants.forEach(jsonConstants::addProperty);
		json.add("constants", jsonConstants);
		// values
		if (values.size() == 1) {
			JsonObject jsonValues = new JsonObject();
			values.get(0).forEach(jsonValues::addProperty);
			json.add("values", jsonValues);
		} else if (values.size() > 1) {
			JsonArray jsonValues = new JsonArray();
			for (Map<String, String> valuesSingle : values) {
				JsonObject jsonValuesSingle = new JsonObject();
				valuesSingle.forEach(jsonValuesSingle::addProperty);
				jsonValues.add(jsonValuesSingle);
			}
			json.add("values", jsonValues);
		}

		return json;
	}














	@UnmodifiableView
	private static <T> List<T> jsonArrayToList(JsonArray jsonArray, Function<JsonElement,T> mapping) {
		List<T> result = new ArrayList<>();
		for (JsonElement element : jsonArray) {
			try {
				result.add(mapping.apply(element));
			} catch (Exception ignored) {
			}
		}
		return Collections.unmodifiableList(result);
	}

	@UnmodifiableView
	private static <T> Map<String,T> jsonObjectToMap(JsonObject jsonObject, Function<JsonElement,T> mapping) {
		Map<String,T> result = new HashMap<>();
		for (var entry : jsonObject.entrySet()) {
			try {
				result.put(entry.getKey(), mapping.apply(entry.getValue()));
			} catch (Exception ignored) {
			}
		}
		return Collections.unmodifiableMap(result);
	}

	@UnmodifiableView
	private static <K,V> Map<K,V> streamToMap(Stream<Map.Entry<K,V>> stream) {
		Map<K,V> result = new HashMap<>();
		stream.forEach(entry->result.put(entry.getKey(), entry.getValue()));
		return Collections.unmodifiableMap(result);
	}
}

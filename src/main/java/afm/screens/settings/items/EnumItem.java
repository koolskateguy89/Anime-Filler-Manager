package afm.screens.settings.items;

import java.util.Optional;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;

import lombok.Getter;

import afm.screens.settings.Category;

public class EnumItem<E extends Enum<?>> extends Item {

	@Getter
	private final Class<?> type;

	private final Class<E> clazz;

	@Getter
	ObjectProperty<E> property = new SimpleObjectProperty<>();

	@Getter
	E value;

	public EnumItem(Class<E> clazz, Category category, String name) {
		super(category);
		this.name = name;
		this.clazz = clazz;
		this.type = clazz;

		property.addListener((obs, oldVal, newVal) ->
			value = newVal
		);
	}

	public EnumItem(Class<E> clazz, Category category, String name, String description) {
		this(clazz, category, name);
		this.description = description;
	}

	@Override
	public void setValue(Object o) {
		if (clazz.isInstance(o)) {
			value = clazz.cast(o);
		} else {
			value = null;
		}
		property.setValue(value);
	}

	@Override
	public Optional<ObservableValue<?>> getObservableValue() {
		return Optional.of(property);
	}
	
}

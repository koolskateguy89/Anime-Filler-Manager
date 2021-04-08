package afm.screens.settings.items;

import java.util.Optional;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableValue;

import lombok.Getter;

import org.controlsfx.control.PropertySheet;

public class BooleanItem implements PropertySheet.Item {

	private final Class<?> type = BooleanItem.class;

	@Getter
	String name;

	@Getter
	String description;

	@Getter
	String category;


	BooleanProperty prop = new SimpleBooleanProperty();

	@Getter
	Boolean value;

	public BooleanItem() {

	}

	@Override
	public Class<?> getType() {
		return type;
	}

	@Override
	public void setValue(Object o) {
		if (o instanceof Boolean) {
			value = (Boolean) o;
		} else if (o == null) {
			value = null;
		}
	}

	@Override
	public Optional<ObservableValue<? extends Object>> getObservableValue() {
		return Optional.empty();
	}
}

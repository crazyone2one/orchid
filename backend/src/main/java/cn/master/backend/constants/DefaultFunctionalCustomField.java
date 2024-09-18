package cn.master.backend.constants;

import cn.master.backend.entity.CustomFieldOption;

import java.util.Arrays;
import java.util.List;

/**
 * @author Created by 11's papa on 09/18/2024
 **/
public enum DefaultFunctionalCustomField {
    PRIORITY("functional_priority", CustomFieldType.SELECT,
            Arrays.asList(
                    getNewOption("P0", "P0", 1),
                    getNewOption("P1", "P1", 2),
                    getNewOption("P2", "P2", 3),
                    getNewOption("P3", "P3", 4)
            )
    );

    private final String name;
    private final CustomFieldType type;
    private final List<CustomFieldOption> options;

    DefaultFunctionalCustomField(String name, CustomFieldType type, List<CustomFieldOption> options) {
        this.name = name;
        this.type = type;
        this.options = options;
    }

    public CustomFieldType getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public List<CustomFieldOption> getOptions() {
        return options;
    }

    private static CustomFieldOption getNewOption(String value, String text, Integer pos) {
        CustomFieldOption customFieldOption = new CustomFieldOption();
        customFieldOption.setValue(value);
        customFieldOption.setText(text);
        customFieldOption.setPos(pos);
        customFieldOption.setInternal(true);
        return customFieldOption;
    }
}

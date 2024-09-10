package cn.master.backend.constants;

/**
 * @author Created by 11's papa on 09/06/2024
 **/
public enum TemplateRequiredCustomField {
    BUG_DEGREE("functional_priority");

    private final String name;

    TemplateRequiredCustomField(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}

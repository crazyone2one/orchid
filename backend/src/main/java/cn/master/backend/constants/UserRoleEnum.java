package cn.master.backend.constants;

/**
 * @author Created by 11's papa on 08/08/2024
 **/
public enum UserRoleEnum {
    GLOBAL("global");

    private final String value;

    UserRoleEnum(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return this.value;
    }
}

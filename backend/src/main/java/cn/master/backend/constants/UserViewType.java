package cn.master.backend.constants;

import cn.master.backend.handler.exception.MSException;
import lombok.Getter;

import java.util.List;

/**
 * @author Created by 11's papa on 09/25/2024
 **/
public enum UserViewType implements ValueEnum {

    FUNCTIONAL_CASE("functional-case",
            List.of(InternalUserView.ALL_DATA, InternalUserView.MY_FOLLOW, InternalUserView.MY_CREATE)),
    REVIEW_FUNCTIONAL_CASE("review-functional-case",
            List.of(InternalUserView.ALL_DATA, InternalUserView.MY_REVIEW, InternalUserView.MY_CREATE)),
    API_DEFINITION("api-definition",
            List.of(InternalUserView.ALL_DATA, InternalUserView.MY_FOLLOW, InternalUserView.MY_CREATE)),
    ;

    private final String value;
    @Getter
    private final List<InternalUserView> internalViews;

    UserViewType(String value, List<InternalUserView> internalViews) {
        this.value = value;
        this.internalViews = internalViews;
    }

    @Override
    public Object getValue() {
        return this.value;
    }

    public static UserViewType getByValue(String value) {
        for (UserViewType userViewType : UserViewType.values()) {
            if (userViewType.value.equals(value)) {
                return userViewType;
            }
        }
        throw new MSException("No such view type");
    }
}

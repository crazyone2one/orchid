package cn.master.backend.constants;

import cn.master.backend.payload.dto.system.UserViewDTO;
import cn.master.backend.payload.request.CombineCondition;
import cn.master.backend.payload.request.CombineSearch;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * @author Created by 11's papa on 09/25/2024
 **/
@Getter
public enum InternalUserView {
    ALL_DATA(() -> {
        UserViewDTO userViewDTO = getUserViewDTO("all_data");
        userViewDTO.setConditions(List.of());
        return userViewDTO;
    }),
    MY_FOLLOW(() -> {
        UserViewDTO userViewDTO = getUserViewDTO("my_follow");
        CombineCondition condition = new CombineCondition();
        condition.setName("follower");
        condition.setValue(getCurrentUserArrayValue());
        condition.setOperator(CombineCondition.CombineConditionOperator.IN.name());
        userViewDTO.setConditions(List.of(condition));
        return userViewDTO;
    }),
    MY_CREATE(() -> {
        UserViewDTO userViewDTO = getUserViewDTO("my_create");
        CombineCondition condition = new CombineCondition();
        condition.setName("createUser");
        condition.setValue(getCurrentUserArrayValue());
        condition.setOperator(CombineCondition.CombineConditionOperator.IN.name());
        userViewDTO.setConditions(List.of(condition));
        return userViewDTO;
    }),
    MY_REVIEW(() -> {
        UserViewDTO userViewDTO = getUserViewDTO("my_review");
        CombineCondition condition = new CombineCondition();
        condition.setName("reviewers");
        condition.setValue(getCurrentUserArrayValue());
        condition.setOperator(CombineCondition.CombineConditionOperator.IN.name());
        userViewDTO.setConditions(List.of(condition));
        return userViewDTO;
    });

    private static UserViewDTO getUserViewDTO(String name) {
        UserViewDTO userViewDTO = new UserViewDTO();
        userViewDTO.setSearchMode(CombineSearch.SearchMode.AND.name());
        userViewDTO.setName(name);
        userViewDTO.setId(name);
        return userViewDTO;
    }

    private final UserViewDTO userView;
    public static final String CURRENT_USER = "CURRENT_USER";

    InternalUserView(Supplier<UserViewDTO> initCombineSearchFunc) {
        userView = initCombineSearchFunc.get();
    }

    public static List<String> getCurrentUserArrayValue() {
        List<String> values = new ArrayList<>(0);
        values.add(CURRENT_USER);
        return values;
    }

}

package cn.master.backend.service.impl;

import cn.master.backend.constants.InternalUserView;
import cn.master.backend.constants.UserViewConditionValueType;
import cn.master.backend.constants.UserViewType;
import cn.master.backend.entity.UserView;
import cn.master.backend.entity.UserViewCondition;
import cn.master.backend.handler.exception.MSException;
import cn.master.backend.mapper.UserViewConditionMapper;
import cn.master.backend.mapper.UserViewMapper;
import cn.master.backend.payload.dto.system.UserViewDTO;
import cn.master.backend.payload.dto.system.UserViewListGroupedDTO;
import cn.master.backend.payload.request.CombineCondition;
import cn.master.backend.payload.request.system.UserViewAddRequest;
import cn.master.backend.payload.request.system.UserViewUpdateRequest;
import cn.master.backend.service.UserViewService;
import cn.master.backend.util.EnumValidator;
import cn.master.backend.util.JSON;
import cn.master.backend.util.Translator;
import com.mybatisflex.core.logicdelete.LogicDeleteManager;
import com.mybatisflex.core.query.QueryChain;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

import static cn.master.backend.entity.table.UserViewTableDef.USER_VIEW;
import static cn.master.backend.handler.result.SystemResultCode.USER_VIEW_EXIST;

/**
 * 用户视图 服务层实现。
 *
 * @author 11's papa
 * @since 1.0.0 2024-09-25
 */
@Service
@RequiredArgsConstructor
public class UserViewServiceImpl extends ServiceImpl<UserViewMapper, UserView> implements UserViewService {
    private final UserViewConditionMapper userViewConditionMapper;
    public static final Long POS_STEP = 5000L;

    @Override
    @Transactional(rollbackOn = Exception.class)
    public UserViewDTO add(UserViewAddRequest request, String viewType, String userId) {
        checkAddExist(request, viewType, userId);
        Long nextPos = getNextPos(request.getScopeId(), userId, viewType);
        UserView userView = new UserView();
        BeanUtils.copyProperties(request, userView);
        userView.setViewType(viewType);
        userView.setUserId(userId);
        userView.setPos(nextPos);
        mapper.insert(userView);
        addUserViewConditions(request.getConditions(), userView);
        UserViewDTO userViewDTO = new UserViewDTO();
        BeanUtils.copyProperties(userView, userViewDTO);
        userViewDTO.setScopeId(request.getScopeId());
        return userViewDTO;
    }

    @Override
    @Transactional(rollbackOn = Exception.class)
    public UserViewDTO update(UserViewUpdateRequest request, String viewType, String userId) {
        UserView originUserView = mapper.selectOneById(request.getId());
        // 校验权限，只能修改自己的视图
        checkOwner(userId, originUserView);
        checkUpdateExist(request.getName(), originUserView, userId);
        UserView userView = new UserView();
        BeanUtils.copyProperties(request, userView);
        userView.setViewType(viewType);
        mapper.update(userView);
        if (request.getConditions() != null) {
            // 先删除
            deleteConditionsByViewId(originUserView.getId());
            // 再新增
            addUserViewConditions(request.getConditions(), userView);
        }
        UserViewDTO userViewDTO = new UserViewDTO();
        BeanUtils.copyProperties(userView, userViewDTO);
        userViewDTO.setUserId(userId);
        userViewDTO.setViewType(viewType);
        return userViewDTO;
    }

    @Override
    public List<UserView> list(String scopeId, UserViewType viewType, String userId) {
        UserViewListGroupedDTO userViews = groupedList(scopeId, viewType, userId);
        userViews.getCustomViews().addAll(userViews.getInternalViews());
        return userViews.getCustomViews();
    }

    @Override
    public UserViewListGroupedDTO groupedList(String scopeId, UserViewType viewType, String userId) {
        // 查询系统内置视图
        List<UserView> internalViews = viewType.getInternalViews().stream().map(userViewEnum -> {
            UserViewDTO userViewDTO = userViewEnum.getUserView();
            UserView userView = new UserView();
            BeanUtils.copyProperties(userViewDTO, userView);
            userView.setName(Translator.get("user_view." + userViewDTO.getName()));
            userView.setViewType(viewType.name());
            userView.setScopeId(scopeId);
            userView.setUserId(userId);
            return userView;
        }).toList();
        List<UserView> customUserViews = queryChain().where(UserView::getUserId).eq(userId)
                .eq(UserView::getViewType, viewType.name())
                .eq(UserView::getScopeId, scopeId).list()
                .stream().sorted((a, b) -> Comparator.comparing(UserView::getPos).compare(b, a))
                .toList();
        UserViewListGroupedDTO groupedDTO = new UserViewListGroupedDTO();
        groupedDTO.setInternalViews(internalViews);
        groupedDTO.setCustomViews(customUserViews);
        return groupedDTO;
    }

    @Override
    public UserViewDTO get(String id, UserViewType viewType, String userId) {
        // 先尝试获取系统内置视图
        InternalUserView[] values = InternalUserView.values();
        for (InternalUserView value : values) {
            UserViewDTO userView = value.getUserView();
            UserViewDTO userViewDTO = new UserViewDTO();
            BeanUtils.copyProperties(userView, userViewDTO);
            userViewDTO.setName(Translator.get("user_view." + userView.getName()));
            userViewDTO.setViewType(viewType.name());
            userViewDTO.setUserId(userId);
            // 系统内置视图，前端不展示查询条件
            userViewDTO.setConditions(List.of());
            return userViewDTO;
        }
        UserView userView = mapper.selectOneById(id);
        checkOwner(userId, userView);
        UserViewDTO userViewDTO = new UserViewDTO();
        BeanUtils.copyProperties(userView, userViewDTO);
        List<CombineCondition> conditions = getUserViewConditionsByViewId(id).stream().map(condition -> {
            CombineCondition combineCondition = new CombineCondition();
            BeanUtils.copyProperties(condition, combineCondition);
            Object value = getConditionValueByType(condition.getValueType(), condition.getValue());
            combineCondition.setValue(value);
            return combineCondition;
        }).toList();
        userViewDTO.setConditions(conditions);
        return userViewDTO;
    }

    @Override
    public void delete(String id, String userId) {
        UserView userView = mapper.selectOneById(id);
        checkOwner(userId, userView);
        LogicDeleteManager.execWithoutLogicDelete(() -> mapper.delete(userView));
    }

    private Object getConditionValueByType(String conditionValueTypeStr, String valueStr) {
        UserViewConditionValueType conditionValueType = EnumValidator.validateEnum(UserViewConditionValueType.class, conditionValueTypeStr);
        if (StringUtils.isBlank(valueStr)) {
            return null;
        }
        return switch (conditionValueType) {
            case ARRAY -> JSON.parseObject(valueStr);
            case INT -> Long.valueOf(valueStr);
            case FLOAT -> Double.valueOf(valueStr);
            default -> valueStr;
        };
    }

    private List<UserViewCondition> getUserViewConditionsByViewId(String id) {
        return QueryChain.of(userViewConditionMapper).where(UserViewCondition::getUserViewId).eq(id).list();
    }

    private void deleteConditionsByViewId(String userViewId) {
        LogicDeleteManager.execWithoutLogicDelete(() ->
                userViewConditionMapper.deleteByQuery(QueryChain.of(userViewConditionMapper)
                        .where(UserViewCondition::getUserViewId).eq(userViewId)));
    }

    private void checkUpdateExist(@NotBlank String name, UserView originUserView, String userId) {
        if (StringUtils.isBlank(name)) {
            return;
        }
        boolean exists = queryChain().where(UserView::getName).eq(name)
                .eq(UserView::getUserId, userId)
                .eq(UserView::getViewType, originUserView.getViewType())
                .eq(UserView::getScopeId, originUserView.getScopeId())
                .ne(UserView::getId, originUserView.getId())
                .exists();
        if (exists) {
            throw new MSException(USER_VIEW_EXIST);
        }
    }

    private void checkOwner(String userId, UserView originUserView) {
        if (!StringUtils.equals(userId, originUserView.getUserId())) {
            throw new MSException(Translator.get("check_owner_case"));
        }
    }

    private void addUserViewConditions(@Valid List<CombineCondition> conditions, UserView userView) {
        if (CollectionUtils.isEmpty(conditions)) {
            return;
        }
        List<UserViewCondition> insertConditions = conditions.stream().map(condition -> {
            UserViewCondition userViewCondition = new UserViewCondition();
            BeanUtils.copyProperties(condition, userViewCondition);
            userViewCondition.setOperator(condition.getOperator());
            Object value = condition.getValue();
            String conditionValueType = getConditionValueType(value);
            userViewCondition.setValueType(conditionValueType);
            if (condition.getValue() != null) {
                if (value instanceof List<?>) {
                    userViewCondition.setValue(JSON.toJSONString(value));
                } else {
                    userViewCondition.setValue(condition.getValue().toString());
                }
            }
            userViewCondition.setUserViewId(userView.getId());
            return userViewCondition;
        }).toList();
        userViewConditionMapper.insertBatch(insertConditions);
    }

    private String getConditionValueType(Object value) {
        if (value instanceof List<?>) {
            return UserViewConditionValueType.ARRAY.name();
        } else if (value instanceof Integer || value instanceof Long) {
            return UserViewConditionValueType.INT.name();
        } else if (value instanceof Float || value instanceof Double) {
            return UserViewConditionValueType.FLOAT.name();
        } else {
            return UserViewConditionValueType.STRING.name();
        }
    }

    private Long getNextPos(@NotBlank String scopeId, String userId, String viewType) {
        Long pos = queryChain().select(USER_VIEW.POS).from(USER_VIEW)
                .where(USER_VIEW.SCOPE_ID.eq(scopeId)
                        .and(USER_VIEW.USER_ID.eq(userId))
                        .and(USER_VIEW.VIEW_TYPE.eq(viewType)))
                //.and(USER_VIEW.POS.ge())
                .orderBy(USER_VIEW.POS.desc()).limit(1)
                .oneAs(Long.class);
        return (pos == null ? 0 : pos) + POS_STEP;
    }

    private void checkAddExist(UserViewAddRequest request, String viewType, String userId) {
        boolean exists = queryChain().where(UserView::getName).eq(request.getName()).eq(UserView::getUserId, userId)
                .eq(UserView::getViewType, viewType).eq(UserView::getScopeId, request.getScopeId()).exists();
        if (exists) {
            throw new MSException(USER_VIEW_EXIST);
        }
    }
}

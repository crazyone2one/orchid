package cn.master.backend.service;

import cn.master.backend.constants.UserViewType;
import cn.master.backend.payload.dto.system.UserViewDTO;
import cn.master.backend.payload.dto.system.UserViewListGroupedDTO;
import cn.master.backend.payload.request.system.UserViewAddRequest;
import cn.master.backend.payload.request.system.UserViewUpdateRequest;
import com.mybatisflex.core.service.IService;
import cn.master.backend.entity.UserView;

import java.util.List;

/**
 * 用户视图 服务层。
 *
 * @author 11's papa
 * @since 1.0.0 2024-09-25
 */
public interface UserViewService extends IService<UserView> {
    UserViewDTO add(UserViewAddRequest request, String viewType, String userId);

    UserViewDTO update(UserViewUpdateRequest request, String viewType, String userId);

    List<UserView> list(String scopeId, UserViewType viewType, String userId);

    UserViewListGroupedDTO groupedList(String scopeId, UserViewType viewType, String userId);

    UserViewDTO get(String id, UserViewType viewType, String userId);

    void delete(String id, String userId);
}

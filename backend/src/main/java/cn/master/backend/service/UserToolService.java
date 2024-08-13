package cn.master.backend.service;

import cn.master.backend.entity.User;
import cn.master.backend.payload.dto.TableBatchProcessDTO;
import com.mybatisflex.core.query.QueryChain;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

import static cn.master.backend.entity.table.UserTableDef.USER;

/**
 * @author Created by 11's papa on 08/13/2024
 **/
@Service
public class UserToolService {
    public List<String> getBatchUserIds(TableBatchProcessDTO request) {
        if (request.isSelectAll()) {

            List<User> userList = QueryChain.of(User.class)
                    .where(USER.ID.eq(request.getCondition().getKeyword())
                            .or(USER.NAME.like(request.getCondition().getKeyword())
                                    .or(USER.EMAIL.like(request.getCondition().getKeyword())
                                            .or(USER.PHONE.like(request.getCondition().getKeyword()))))).list();
            List<String> userIdList = userList.stream().map(User::getId).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(request.getExcludeIds())) {
                userIdList.removeAll(request.getExcludeIds());
            }
            return userIdList;
        } else {
            return request.getSelectIds();
        }
    }

    public List<User> selectByIdList(List<String> userIdList) {
        return QueryChain.of(User.class).where(USER.ID.in(userIdList)).list();
    }
}

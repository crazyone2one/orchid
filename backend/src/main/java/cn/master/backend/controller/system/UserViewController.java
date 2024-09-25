package cn.master.backend.controller.system;

import cn.master.backend.constants.UserViewType;
import cn.master.backend.entity.UserView;
import cn.master.backend.payload.dto.system.UserViewDTO;
import cn.master.backend.payload.dto.system.UserViewListGroupedDTO;
import cn.master.backend.payload.request.system.UserViewAddRequest;
import cn.master.backend.payload.request.system.UserViewUpdateRequest;
import cn.master.backend.service.UserViewService;
import cn.master.backend.util.SessionUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author Created by 11's papa on 09/25/2024
 **/
@Tag(name = "视图")
@RestController
@RequestMapping("/user-view")
@RequiredArgsConstructor
public class UserViewController {
    private final UserViewService userViewService;

    @GetMapping("/{viewType}/list")
    @Operation(summary = "视图列表")
    public List<UserView> list(@RequestParam String scopeId, @PathVariable String viewType) {
        UserViewType userViewType = UserViewType.getByValue(viewType);
        return userViewService.list(scopeId, userViewType, SessionUtils.getCurrentUserId());
    }

    @GetMapping("/{viewType}/grouped/list")
    @Operation(summary = "视图列表")
    public UserViewListGroupedDTO groupedList(@RequestParam String scopeId, @PathVariable String viewType) {
        UserViewType userViewType = UserViewType.getByValue(viewType);
        return userViewService.groupedList(scopeId, userViewType, SessionUtils.getCurrentUserId());
    }

    @GetMapping("/{viewType}/get/{id}")
    @Operation(summary = "视图详情")
    public UserViewDTO get(@PathVariable String id, @PathVariable String viewType) {
        UserViewType userViewType = UserViewType.getByValue(viewType);
        return userViewService.get(id, userViewType, SessionUtils.getCurrentUserId());
    }

    @PostMapping("/{viewType}/add")
    @Operation(summary = "新增视图")
    public UserViewDTO add(@Validated @RequestBody UserViewAddRequest request, @PathVariable String viewType) {
        UserViewType userViewType = UserViewType.getByValue(viewType);
        return userViewService.add(request, userViewType.name(), SessionUtils.getCurrentUserId());
    }

    @PostMapping("/{viewType}/update")
    @Operation(summary = "编辑视图")
    public UserViewDTO update(@Validated @RequestBody UserViewUpdateRequest request, @PathVariable String viewType) {
        UserViewType userViewType = UserViewType.getByValue(viewType);
        return userViewService.update(request, userViewType.name(), SessionUtils.getCurrentUserId());
    }

    @GetMapping("/{viewType}/delete/{id}")
    @Operation(summary = "删除视图")
    public void delete(@PathVariable String id, @PathVariable String viewType) {
        userViewService.delete(id, SessionUtils.getCurrentUserId());
    }
}

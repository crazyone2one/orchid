package cn.master.backend.controller.cese;

import cn.master.backend.constants.Logical;
import cn.master.backend.constants.PermissionConstants;
import cn.master.backend.handler.annotation.HasAnyAuthorize;
import cn.master.backend.payload.dto.functional.FunctionalDemandDTO;
import cn.master.backend.payload.request.functional.FunctionalCaseDemandRequest;
import cn.master.backend.payload.request.functional.QueryDemandListRequest;
import cn.master.backend.service.FunctionalCaseDemandService;
import cn.master.backend.util.SessionUtils;
import com.mybatisflex.core.paginate.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Created by 11's papa on 09/13/2024
 **/
@Tag(name = "用例管理-功能用例-关联需求")
@RestController
@RequiredArgsConstructor
@RequestMapping("/functional/case/demand")
public class FunctionalCaseDemandController {
    private final FunctionalCaseDemandService functionalCaseDemandService;

    @PostMapping("/page")
    @Operation(summary = "用例管理-功能用例-关联需求-获取已关联的需求列表")
    @HasAnyAuthorize(value = {PermissionConstants.FUNCTIONAL_CASE_READ, PermissionConstants.FUNCTIONAL_CASE_READ_ADD, PermissionConstants.FUNCTIONAL_CASE_READ_UPDATE, PermissionConstants.FUNCTIONAL_CASE_READ_DELETE}, logical = Logical.OR)
    public Page<FunctionalDemandDTO> listFunctionalCaseDemands(@Validated @RequestBody QueryDemandListRequest request) {
        return functionalCaseDemandService.pageFunctionalCaseDemands(request);
    }
    @PostMapping("/add")
    @Operation(summary = "用例管理-功能用例详情-关联需求-新增/关联需求")
    @HasAnyAuthorize(value = {PermissionConstants.FUNCTIONAL_CASE_READ_ADD, PermissionConstants.FUNCTIONAL_CASE_READ_UPDATE, PermissionConstants.FUNCTIONAL_CASE_READ_DELETE}, logical = Logical.OR)
    public void addDemand(@RequestBody @Validated FunctionalCaseDemandRequest request) {
        functionalCaseDemandService.addDemand(request, SessionUtils.getCurrentUserId());
    }
}

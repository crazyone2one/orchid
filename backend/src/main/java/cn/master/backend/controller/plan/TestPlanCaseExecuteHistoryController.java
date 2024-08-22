package cn.master.backend.controller.plan;

import com.mybatisflex.core.paginate.Page;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.beans.factory.annotation.Autowired;
import cn.master.backend.entity.TestPlanCaseExecuteHistory;
import cn.master.backend.service.TestPlanCaseExecuteHistoryService;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import java.io.Serializable;
import java.util.List;

/**
 * 功能用例执行历史表 控制层。
 *
 * @author 11's papa
 * @since 1.0.0 2024-08-15
 */
@RestController
@Tag(name = "功能用例执行历史表接口")
@RequestMapping("/testPlanCaseExecuteHistory")
public class TestPlanCaseExecuteHistoryController {

    @Autowired
    private TestPlanCaseExecuteHistoryService testPlanCaseExecuteHistoryService;

    /**
     * 添加功能用例执行历史表。
     *
     * @param testPlanCaseExecuteHistory 功能用例执行历史表
     * @return {@code true} 添加成功，{@code false} 添加失败
     */
    @PostMapping("save")
    @Operation(description="保存功能用例执行历史表")
    public boolean save(@RequestBody @Parameter(description="功能用例执行历史表")TestPlanCaseExecuteHistory testPlanCaseExecuteHistory) {
        return testPlanCaseExecuteHistoryService.save(testPlanCaseExecuteHistory);
    }

    /**
     * 根据主键删除功能用例执行历史表。
     *
     * @param id 主键
     * @return {@code true} 删除成功，{@code false} 删除失败
     */
    @DeleteMapping("remove/{id}")
    @Operation(description="根据主键功能用例执行历史表")
    public boolean remove(@PathVariable @Parameter(description="功能用例执行历史表主键")Serializable id) {
        return testPlanCaseExecuteHistoryService.removeById(id);
    }

    /**
     * 根据主键更新功能用例执行历史表。
     *
     * @param testPlanCaseExecuteHistory 功能用例执行历史表
     * @return {@code true} 更新成功，{@code false} 更新失败
     */
    @PutMapping("update")
    @Operation(description="根据主键更新功能用例执行历史表")
    public boolean update(@RequestBody @Parameter(description="功能用例执行历史表主键")TestPlanCaseExecuteHistory testPlanCaseExecuteHistory) {
        return testPlanCaseExecuteHistoryService.updateById(testPlanCaseExecuteHistory);
    }

    /**
     * 查询所有功能用例执行历史表。
     *
     * @return 所有数据
     */
    @GetMapping("list")
    @Operation(description="查询所有功能用例执行历史表")
    public List<TestPlanCaseExecuteHistory> list() {
        return testPlanCaseExecuteHistoryService.list();
    }

    /**
     * 根据功能用例执行历史表主键获取详细信息。
     *
     * @param id 功能用例执行历史表主键
     * @return 功能用例执行历史表详情
     */
    @GetMapping("getInfo/{id}")
    @Operation(description="根据主键获取功能用例执行历史表")
    public TestPlanCaseExecuteHistory getInfo(@PathVariable Serializable id) {
        return testPlanCaseExecuteHistoryService.getById(id);
    }

    /**
     * 分页查询功能用例执行历史表。
     *
     * @param page 分页对象
     * @return 分页对象
     */
    @GetMapping("page")
    @Operation(description="分页查询功能用例执行历史表")
    public Page<TestPlanCaseExecuteHistory> page(@Parameter(description="分页信息")Page<TestPlanCaseExecuteHistory> page) {
        return testPlanCaseExecuteHistoryService.page(page);
    }

}

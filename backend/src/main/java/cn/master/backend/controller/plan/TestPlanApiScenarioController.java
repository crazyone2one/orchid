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
import cn.master.backend.entity.TestPlanApiScenario;
import cn.master.backend.service.TestPlanApiScenarioService;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import java.io.Serializable;
import java.util.List;

/**
 * 测试计划关联场景用例 控制层。
 *
 * @author 11's papa
 * @since 1.0.0 2024-08-15
 */
@RestController
@Tag(name = "测试计划关联场景用例接口")
@RequestMapping("/testPlanApiScenario")
public class TestPlanApiScenarioController {

    @Autowired
    private TestPlanApiScenarioService testPlanApiScenarioService;

    /**
     * 添加测试计划关联场景用例。
     *
     * @param testPlanApiScenario 测试计划关联场景用例
     * @return {@code true} 添加成功，{@code false} 添加失败
     */
    @PostMapping("save")
    @Operation(description="保存测试计划关联场景用例")
    public boolean save(@RequestBody @Parameter(description="测试计划关联场景用例")TestPlanApiScenario testPlanApiScenario) {
        return testPlanApiScenarioService.save(testPlanApiScenario);
    }

    /**
     * 根据主键删除测试计划关联场景用例。
     *
     * @param id 主键
     * @return {@code true} 删除成功，{@code false} 删除失败
     */
    @DeleteMapping("remove/{id}")
    @Operation(description="根据主键测试计划关联场景用例")
    public boolean remove(@PathVariable @Parameter(description="测试计划关联场景用例主键")Serializable id) {
        return testPlanApiScenarioService.removeById(id);
    }

    /**
     * 根据主键更新测试计划关联场景用例。
     *
     * @param testPlanApiScenario 测试计划关联场景用例
     * @return {@code true} 更新成功，{@code false} 更新失败
     */
    @PutMapping("update")
    @Operation(description="根据主键更新测试计划关联场景用例")
    public boolean update(@RequestBody @Parameter(description="测试计划关联场景用例主键")TestPlanApiScenario testPlanApiScenario) {
        return testPlanApiScenarioService.updateById(testPlanApiScenario);
    }

    /**
     * 查询所有测试计划关联场景用例。
     *
     * @return 所有数据
     */
    @GetMapping("list")
    @Operation(description="查询所有测试计划关联场景用例")
    public List<TestPlanApiScenario> list() {
        return testPlanApiScenarioService.list();
    }

    /**
     * 根据测试计划关联场景用例主键获取详细信息。
     *
     * @param id 测试计划关联场景用例主键
     * @return 测试计划关联场景用例详情
     */
    @GetMapping("getInfo/{id}")
    @Operation(description="根据主键获取测试计划关联场景用例")
    public TestPlanApiScenario getInfo(@PathVariable Serializable id) {
        return testPlanApiScenarioService.getById(id);
    }

    /**
     * 分页查询测试计划关联场景用例。
     *
     * @param page 分页对象
     * @return 分页对象
     */
    @GetMapping("page")
    @Operation(description="分页查询测试计划关联场景用例")
    public Page<TestPlanApiScenario> page(@Parameter(description="分页信息")Page<TestPlanApiScenario> page) {
        return testPlanApiScenarioService.page(page);
    }

}

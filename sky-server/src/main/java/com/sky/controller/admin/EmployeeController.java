package com.sky.controller.admin;


import com.sky.constant.JwtClaimsConstant;
import com.sky.dto.EmployeeDTO;
import com.sky.dto.EmployeeLoginDTO;
import com.sky.dto.EmployeePageQueryDTO;
import com.sky.entity.Employee;

import com.sky.properties.JwtProperties;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.EmployeeService;
import com.sky.utils.JwtUtil;
import com.sky.vo.EmployeeLoginVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 员工管理
 */
@RestController
@RequestMapping("/admin/employee")
@Slf4j
@Api(tags = "员工相关接口")
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;
    @Autowired
    private JwtProperties jwtProperties;

    /**
     * 员工登录
     *
     * @param employeeLoginDTO 登录所需的信息（用户名和密码）
     * @return 返回登录成功的信息和JWT令牌
     */
    @PostMapping("/login")
    @ApiOperation("员工登录")
    public Result<EmployeeLoginVO> login(@RequestBody EmployeeLoginDTO employeeLoginDTO) {
        log.info("员工登录：{}", employeeLoginDTO);

        // 通过服务进行员工登录验证
        Employee employee = employeeService.login(employeeLoginDTO);

        // 登录成功后，根据员工ID生成JWT令牌
        Map<String, Object> claims = new HashMap<>();
        claims.put(JwtClaimsConstant.EMP_ID, employee.getId());
        String token = JwtUtil.createJWT(
                jwtProperties.getAdminSecretKey(),
                jwtProperties.getAdminTtl(),
                claims);

        // 构建员工登录成功后的视图对象
        EmployeeLoginVO employeeLoginVO = EmployeeLoginVO.builder()
                .id(employee.getId())
                .userName(employee.getUsername())
                .name(employee.getName())
                .token(token)
                .build();

        // 返回成功的结果，包含员工登录信息
        return Result.success(employeeLoginVO);
    }

    /**
     * 员工退出登录
     *
     * @return 返回退出登录成功的信息
     */
    @ApiOperation("员工退出登录")
    @PostMapping("/logout")
    public Result<String> logout() {
        // 返回成功的结果，表示退出登录成功
        return Result.success();
    }

    /**
     * 新增员工
     */
    @ApiOperation("新增员工")
    @PostMapping
    public Result save(@RequestBody EmployeeDTO employeeDTO){
        log.info("新增员工：{}",employeeDTO);
        employeeService.save(employeeDTO);
        return Result.success();
    }
    @ApiOperation("员工分页查询")
    @GetMapping("/page")
    public Result<PageResult> page(EmployeePageQueryDTO employeePageQueryDTO){
        log.info("员工分页查询：{}",employeePageQueryDTO);
        PageResult pageResult = employeeService.pageQuery(employeePageQueryDTO);
        return Result.success(pageResult);
    }

    @ApiOperation("员工状态修改")
    @PostMapping("/status/{status}")
    public Result StatusChange( @PathVariable Integer status,Long id){
        log.info("员工状态修改：{}",id);
        employeeService.statusChange(id,status);
        return Result.success();
    }
    @ApiOperation("根据id查询员工信息")
    @GetMapping("/{id}")
    public Result<Employee> getById(@PathVariable Long id){
        log.info("根据id查询员工信息：{}",id);
        Employee employee = employeeService.getById(id);
        return Result.success(employee);
    }

    @ApiOperation("编辑员工信息")
    @PutMapping

    public Result update(@RequestBody EmployeeDTO employeeDTO){
        log.info("编辑员工信息：{}",employeeDTO);
        employeeService.update(employeeDTO);
        return Result.success();
    }


}

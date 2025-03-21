package com.example.admin.controller;


import com.example.admin.service.CategoryService;
import com.example.common.common.Result;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author dzk
 * @since 2025-03-20
 */
@RestController
@RequestMapping("/category")
public class CategoryController {

    @Autowired
    CategoryService categoryService;

    @RequestMapping("/getList")
    public Result getList() {
        return Result.success(categoryService.getList());
    }

    @RequestMapping("/add")
    public Result add(@NotEmpty String categoryName,@NotEmpty String categoryCode) {
        return Result.success(categoryService.add(categoryName,categoryCode));
    }

    @RequestMapping("/update")
    public Result update(@NotNull int id, @NotEmpty String categoryName, @NotEmpty String categoryCode) {
        return Result.success(categoryService.update(id,categoryName,categoryCode));
    }
    
    @RequestMapping("/delete")
    public Result delete(@RequestParam int id) {
        if(categoryService.removeById(id)){
            return Result.success("删除成功");
        }
        return Result.error("删除失败");
    }
    @RequestMapping("/sort")
    public Result sort(@RequestBody @NotEmpty List<Map<String, Integer>> sortList) {
        return Result.success(categoryService.updateSort(sortList));
    }
}


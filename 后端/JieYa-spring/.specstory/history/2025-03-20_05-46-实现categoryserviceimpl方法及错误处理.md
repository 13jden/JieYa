<!-- Generated by SpecStory -->

# 实现CategoryServiceImpl方法及错误处理 (2025-03-20 13:46:33)

_**User**_

实现一下这几个方法，错误的话有错误返回

---

_**Assistant**_

# 实现CategoryServiceImpl方法

我将为您实现CategoryServiceImpl类中的几个方法。以下是对这些方法的实现：

```java:jieya-admin/src/main/java/com/example/admin/service/impl/CategoryServiceImpl.java
@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {

    @Override
    public IPage<Category> page(int pageNum, int pageSize) {
        // 创建分页对象
        Page<Category> page = new Page<>(pageNum, pageSize);
        // 使用MyBatis-Plus提供的分页查询方法
        return this.page(page);
    }

    @Override
    public Object update(int id, String categoryName, String categoryCode) {
        try {
            // 先查询是否存在该分类
            Category category = this.getById(id);
            if (category == null) {
                return "分类不存在";
            }
            
            // 检查分类编码是否已存在（排除当前ID）
            LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Category::getCategoryCode, categoryCode)
                        .ne(Category::getId, id);
            if (this.count(queryWrapper) > 0) {
                return "分类编码已存在";
            }
            
            // 更新分类信息
            category.setCategoryName(categoryName);
            category.setCategoryCode(categoryCode);
            
            if (this.updateById(category)) {
                return "更新成功";
            } else {
                return "更新失败";
            }
        } catch (Exception e) {
            return "更新异常: " + e.getMessage();
        }
    }

    @Override
    public Object add(String categoryName, String categoryCode) {
        try {
            // 检查分类编码是否已存在
            LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Category::getCategoryCode, categoryCode);
            if (this.count(queryWrapper) > 0) {
                return "分类编码已存在";
            }
            
            // 创建新分类对象
            Category category = new Category();
            category.setCategoryName(categoryName);
            category.setCategoryCode(categoryCode);
            
            if (this.save(category)) {
                return "添加成功";
            } else {
                return "添加失败";
            }
        } catch (Exception e) {
            return "添加异常: " + e.getMessage();
        }
    }
}
```

## 实现说明

1. **page方法**：
   - 创建了一个MyBatis-Plus的Page对象，传入页码和每页大小
   - 调用ServiceImpl提供的page方法进行分页查询
   - 返回分页结果

2. **update方法**：
   - 首先检查要更新的分类是否存在
   - 检查更新后的分类编码是否与其他分类冲突
   - 更新分类信息并返回相应结果
   - 使用try-catch捕获可能的异常并返回错误信息

3. **add方法**：
   - 检查新分类的编码是否已存在
   - 创建新的分类对象并设置属性
   - 保存到数据库并返回相应结果
   - 使用try-catch捕获可能的异常并返回错误信息

这些实现包含了基本的业务逻辑和错误处理，确保在操作失败时能够返回适当的错误信息。

---



---


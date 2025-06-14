<!-- Generated by SpecStory -->

# Fuzzy Search Implementation for Users and Nodes (2025-04-15 14:25:15)

_**User**_

增加一个node和user的模糊查询方法，分别查user的nickName和node的title和content

---

_**Assistant**_

<think>Let me break down what the user is asking for:

1. They want to add methods for fuzzy searching:
   - Search for users by their nickName
   - Search for nodes (likely a kind of post/note) by their title and content

Looking at the code, I can see that:

1. There's already a method for searching users by nickName in UserController (`searchNickName`), but they might want a more comprehensive method.
2. There's no existing method for searching nodes by title and content.

I need to implement:
1. A method in NodeController for fuzzy searching nodes by title and content
2. The corresponding service method and implementation

Let me first check the existing Node/Note-related classes to understand the structure better:
- NodeController handles HTTP requests for nodes
- NodeService defines the service interface 
- NodeServiceImpl implements the service methods
- There are DTO classes and entities like NoteDto and Node

Let's create the required methods:</think>I'll help you add fuzzy search functionality for both users (by nickName) and nodes (by title an

---

_**User**_

只用中文回答

---

_**Assistant**_

<think>我需要用中文回答用户的请求，并帮助用户增加Node和User的模糊查询方法。具体来说，需要实现按User的nickName以及Node的title和content进行模糊查询的功能。

首先，让我梳理一下需要做的事情：

1. 在NodeController中添加模糊查询方法，用于搜索title和content
2. 在NodeService接口中定义相应的方法
3. 在NodeServiceImpl中实现这个方法
4. 在UserController中，似乎已经有一个根据nickName搜索的方法，但可能需要查看或改进

我先查看现有代码，看看UserController中是否已经有按nickName查询的方法，以及如何实现Node的模糊查询功能。</think>好的，我会帮您增加Node和User的模糊查询方法。

首先，我看到UserController中已经存在一个按照昵称搜索的方法，但我们可以完善Node的模糊查询功能。

我将实现以下功能：
1. 在NodeController中添加一个模糊查询方法，可以根据标题和内容进行搜索
2. 在NodeService接口中定义相关方法
3. 在NodeServiceImpl中实现具体的模糊查询逻辑

让我来实现这些功能：

---

```diff
  
      NoteDto getDetail(String userId,String id);
+     
+     Page<NoteDto> searchByKeyword(String userId, String keyword, int pageNum, int pageSize);
  }
  
```

---

```diff
      }
  
+     @Override
+     public Page<NoteDto> searchByKeyword(String userId, String keyword, int pageNum, int pageSize) {
+         // 创建分页对象
+         Page<Node> page = new Page<>(pageNum, pageSize);
+         LambdaQueryWrapper<Node> queryWrapper = new LambdaQueryWrapper<>();
+         
+         // 添加模糊查询条件，搜索标题和内容
+         queryWrapper.like(Node::getTitle, keyword)
+                    .or()
+                    .like(Node::getContent, keyword);
+                    
+         queryWrapper.orderByDesc(Node::getPostTime);
+         Page<Node> nodePage = nodeMapper.selectPage(page, queryWrapper);
+ 
+         // 准备结果页
+         Page<NoteDto> dtoPage = new Page<>();
+         org.springframework.beans.BeanUtils.copyProperties(nodePage, dtoPage, "records");
+ 
+         List<NoteDto> nodeDtos = new ArrayList<>();
+         // 处理每个笔记转换为DTO并设置附加信息
+         for (Node node : nodePage.getRecords()) {
+             NoteDto nodeDto = CopyTools.copy(node, NoteDto.class);
+ 
+             // 处理封面图片路径
+             if (nodeDto.getCoverImage() != null && !nodeDto.getCoverImage().isEmpty()) {
+                 nodeDto.setCoverImage(hostUrl + "/images/note/" + nodeDto.getCoverImage());
+             }
+ 
+             // 设置作者信息
+             if (node.getUserId() != null) {
+                 User user = userMapper.selectById(node.getUserId());
+                 if (user != null) {
+                     user.setAvatar(hostUrl + "/images/avatar/" + user.getAvatar());
+                     nodeDto.setUser(user);
+                 }
+                 
+                 if(userId.equals(user.getUserId())){
+                     nodeDto.setIsFocus(0);
+                 }else{
+                     if(focusMapper.checkUserFocus(userId,user.getUserId())>0){
+                         nodeDto.setIsFocus(1);
+                     }else{
+                         nodeDto.setIsFocus(-1);
+                     }
+                 }
+             }
+ 
+             nodeDtos.add(nodeDto);
+         }
+ 
+         dtoPage.setRecords(nodeDtos);
+         return dtoPage;
+     }
  }
  
```

---

```diff
      }
  
- 
+     @RequestMapping("/search")
+     public Result searchNotes(@RequestParam String keyword, @RequestParam int pageNum, @RequestParam int pageSize, HttpServletRequest request){
+         String token = request.getHeader("Authorization");
+         String userId = jwtUtil.getUserId(token);
+         return Result.success(nodeService.searchByKeyword(userId, keyword, pageNum, pageSize));
+     }
  }
  
```

---

```diff
          return Result.success(userService.searchByNickName(nickName));
      }
+ 
+     @RequestMapping("/search")
+     public Result<List<User>> searchUsers(@RequestParam String keyword) {
+         return Result.success(userService.searchByNickName(keyword));
+     }
  
      @RequestMapping("/register")
```

---

现在我已经为您添加了Node和User的模糊查询功能。以下是我实现的内容：

1. 在NodeService接口中，我添加了一个新方法 `searchByKeyword`，用于通过关键词搜索笔记。

2. 在NodeServiceImpl实现类中，我实现了这个方法，它可以同时搜索笔记的标题（title）和内容（content）。搜索结果会按照发布时间降序排列，并且每个笔记都包含了作者信息和关注状态。

3. 在NodeController中，我添加了一个新的接口 `/note/search`，可以接收关键词（keyword）、页码（pageNum）和页大小（pageSize）参数，用于前端调用笔记搜索功能。

4. 对于User的搜索，我在UserController中添加了一个更友好的接口 `/user/search`，它可以通过GET请求和请求参数来搜索用户，这样更符合RESTful API的设计风格。原有的按昵称搜索接口 `/user/name/{nick_name}` 也保留了。

这样，前端可以使用以下接口进行模糊搜索：
- 搜索笔记：`/note/search?keyword=关键词&pageNum=1&pageSize=10`
- 搜索用户：`/user/search?keyword=关键词`

两个搜索功能都已经适当地处理了认证和数据转换，确保返回的数据格式与现有API一致。

---

_**User**_

把这里面的处理file地址的逻辑修改，要把http://localhost:8081/files/message/20250415/DMZLR_ik4oBf5GZrYh891b7dbd03e61f021583f841ba0bdd55.jpg改成保留20250415/DMZLR_ik4oBf5GZrYh891b7dbd03e61f021583f841ba0bdd55.jpg

---

_**Assistant**_



---



---


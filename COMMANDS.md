# Pomelo's Shop Mod - 命令列表

## 玩家命令

| 命令 | 说明 | 示例 |
|------|------|------|
| /shop | 打开商店界面 | /shop |
| /shop balance | 查看当前余额 | /shop balance |
| /shop sell <价格> [数量] | 上架手中物品 | /shop sell 100 或 /shop sell 100 64 |
| /shop category create <ID> <名称> | 创建新分类 | /shop category create blocks 方块 |
| /shop category list | 列出所有分类 | /shop category list |

## 管理员命令 (需要权限等级 2)

| 命令 | 说明 | 示例 |
|------|------|------|
| /shop add <玩家> <金额> | 给玩家添加余额 | /shop add Steve 1000 |
| /shop set <玩家> <金额> | 设置玩家余额 | /shop set Steve 5000 |
| /shop reload | 重新加载商店数据 | /shop reload |
| /shop clear | 清空所有商品 | /shop clear |
| /shop category moveup <分类名称> | 上移分类排序 | /shop category moveup 方块 |
| /shop category movedown <分类名称> | 下移分类排序 | /shop category movedown 方块 |
| /shop category setparent <子分类> <父分类> | 设置分类的父分类 | /shop category setparent "子分类名" 父分类名 |
| /shop category setparent <子分类> none | 移除分类的父分类 | /shop category setparent "子分类名" none |
| /shop ban <物品ID> | 禁止指定物品上架 | /shop ban minecraft:bedrock |
| /shop unban <物品ID> | 解除物品禁止 | /shop unban minecraft:bedrock |
| /shop banlist | 查看禁止物品列表 | /shop banlist |
| /shop export <文件名> | 导出商店数据到JSON | /shop shop_backup |
| /shop import <文件名> | 从JSON导入商店数据 | /shop shop_backup |

## 商店界面操作

| 操作 | 说明 |
|------|------|
| 左键商品 | 购买商品（库存为0时无法购买） |
| 右键商品 | 出售1个物品（出售后库存+1） |
| Shift + 右键商品 | 批量出售（出售背包中所有该物品，库存增加对应数量） |
| Ctrl + 右键商品 | 删除自己上架的商品 |
| Shift + 左键商品 | 编辑自己上架的商品（数量、价格、分类、库存） |
| Ctrl + C（悬停商品） | 复制商品信息 |
| Ctrl + V | 粘贴商品到当前分类（添加到最后位置） |

## 快捷键

| 快捷键 | 说明 |
|--------|------|
| 默认: N | 打开商店（可在控制设置中修改） |

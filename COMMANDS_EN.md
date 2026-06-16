# Pomelo's Shop Mod - Command List

## Player Commands

| Command | Description | Example |
|---------|-------------|---------|
| /shop | Open shop interface | /shop |
| /shop balance | Check current balance | /shop balance |
| /shop sell <price> [amount] | List item in hand for sale | /shop sell 100 or /shop sell 100 64 |
| /shop category create <ID> <name> | Create a new category | /shop category create blocks Blocks |
| /shop category list | List all categories | /shop category list |

## Admin Commands (Requires Permission Level 2)

| Command | Description | Example |
|---------|-------------|---------|
| /shop add <player> <amount> | Add money to player's balance | /shop add Steve 1000 |
| /shop set <player> <amount> | Set player's balance | /shop set Steve 5000 |
| /shop reload | Reload shop data | /shop reload |
| /shop clear | Clear all shop items | /shop clear |
| /shop category moveup <category_name> | Move category up in order | /shop category moveup Blocks |
| /shop category movedown <category_name> | Move category down in order | /shop category movedown Blocks |
| /shop category setparent <child> <parent> | Set category's parent category | /shop category setparent "ChildName" ParentName |
| /shop category setparent <child> none | Remove category's parent | /shop category setparent "ChildName" none |
| /shop ban <item_id> | Ban an item from being listed | /shop ban minecraft:bedrock |
| /shop unban <item_id> | Unban a previously banned item | /shop unban minecraft:bedrock |
| /shop banlist | View list of banned items | /shop banlist |
| /shop export <filename> | Export shop data to JSON | /shop export shop_backup |
| /shop import <filename> | Import shop data from JSON | /shop import shop_backup |

## Shop Interface Controls

| Action | Description |
|--------|-------------|
| Left Click Item | Buy item (cannot buy if stock is 0) |
| Right Click Item | Sell 1 item (stock +1 after selling) |
| Shift + Right Click Item | Batch sell (sell all of that item in inventory, (stock +1 after selling) |
| Ctrl + Right Click Item | Delete your own listing |
| Shift + Left Click Item | Edit your own listing (amount, price, category, stock) |
| Ctrl + C (hover on item) | Copy item info |
| Ctrl + V | Paste item to current category (added at the end) |

## Hotkey

| Hotkey | Description |
|--------|-------------|
| Default: N | Open shop (configurable in Controls settings) |
